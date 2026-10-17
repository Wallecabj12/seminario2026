package com.geovanivargas.planetamusica.ui.sequencer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.data.sampler.SamplerRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SoundCraftDatabase
import com.geovanivargas.planetamusica.data.sampler.SoundPoolAudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.AudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import com.geovanivargas.planetamusica.domain.sequencer.CoroutinesSequencerEngine
import com.geovanivargas.planetamusica.domain.sequencer.SequencerEngine
import com.geovanivargas.planetamusica.domain.sequencer.StepPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SequencerViewModel(
    application: Application,
    private val repo: SamplerRepository,
    private val audioPlayer: AudioPlayer,
    val engine: SequencerEngine,
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repo = SamplerRepositoryImpl(
            SoundCraftDatabase.getInstance(application).padDao(),
            application,
        ),
        audioPlayer = SoundPoolAudioPlayer(application),
        engine = CoroutinesSequencerEngine(),
    )

    private val _state = MutableStateFlow(SequencerUiState())
    val state: StateFlow<SequencerUiState> = _state.asStateFlow()

    private var loadedPadsMap = mutableMapOf<Int, Pad>()
    private var patternsMap = mutableMapOf<Int, StepPattern>()

    init {
        // Inicializar pads y sonidos por defecto si es necesario
        viewModelScope.launch {
            repo.initializePads()
        }

        // Observar pads y mapear con patrones
        viewModelScope.launch {
            repo.getAllPads().collect { pads ->
                val loadedPads = pads.filter { it.hasSound }
                loadedPadsMap = loadedPads.associateBy { it.id }.toMutableMap()

                // Cargar en SoundPool
                loadedPads.forEach { pad ->
                    pad.filePath?.let { audioPlayer.loadSound(pad.id, it) }
                }

                // Generar o preservar patrones
                if (patternsMap.isEmpty()) {
                    val defaultPatterns = StepPattern.createDefaultPatterns(loadedPads.map { it.id })
                    defaultPatterns.forEach { patternsMap[it.padId] = it }
                } else {
                    loadedPads.forEach { pad ->
                        if (!patternsMap.containsKey(pad.id)) {
                            patternsMap[pad.id] = StepPattern(padId = pad.id)
                        }
                    }
                }

                syncEnginePatterns()
                updateRows()
            }
        }

        // Sincronizar estados del motor
        viewModelScope.launch {
            engine.isPlaying.collect { isPlaying ->
                _state.update { it.copy(isPlaying = isPlaying) }
            }
        }
        viewModelScope.launch {
            engine.currentStep.collect { step ->
                _state.update { it.copy(currentStep = step) }
            }
        }
        viewModelScope.launch {
            engine.bpm.collect { bpm ->
                _state.update { it.copy(bpm = bpm) }
            }
        }

        // Listener de ejecución de cada paso
        engine.setOnStepListener { _, activePadIds ->
            activePadIds.forEach { padId ->
                val pad = loadedPadsMap[padId]
                val pattern = patternsMap[padId]
                if (pad != null && pattern != null && !pattern.isMuted) {
                    val finalVolume = pad.volume * pattern.volume
                    audioPlayer.play(pad.copy(volume = finalVolume))
                }
            }
        }
    }

    fun onEvent(event: SequencerEvent) {
        when (event) {
            is SequencerEvent.StepToggled -> {
                val current = patternsMap[event.padId] ?: StepPattern(padId = event.padId)
                val newSteps = current.steps.clone()
                newSteps[event.stepIndex] = !newSteps[event.stepIndex]
                patternsMap[event.padId] = current.copy(steps = newSteps)
                syncEnginePatterns()
                updateRows()
            }
            is SequencerEvent.MuteToggled -> {
                val current = patternsMap[event.padId] ?: StepPattern(padId = event.padId)
                patternsMap[event.padId] = current.copy(isMuted = !current.isMuted)
                syncEnginePatterns()
                updateRows()
            }
            is SequencerEvent.VolumeChanged -> {
                val current = patternsMap[event.padId] ?: StepPattern(padId = event.padId)
                patternsMap[event.padId] = current.copy(volume = event.volume)
                syncEnginePatterns()
                updateRows()
            }
            SequencerEvent.PlayToggle -> {
                if (_state.value.isPlaying) {
                    engine.pause()
                } else {
                    engine.start()
                }
            }
            SequencerEvent.Stop -> {
                engine.stop()
                audioPlayer.stopAll()
            }
            is SequencerEvent.BpmChanged -> {
                engine.setBpm(event.bpm)
            }
        }
    }

    private fun syncEnginePatterns() {
        engine.setPatterns(patternsMap.values.toList())
    }

    private fun updateRows() {
        val rows = loadedPadsMap.values.sortedBy { it.position }.map { pad ->
            SequencerPadRow(
                pad = pad,
                pattern = patternsMap[pad.id] ?: StepPattern(padId = pad.id),
            )
        }
        _state.update { it.copy(rows = rows) }
    }

    fun getCurrentPatterns(): List<StepPattern> = patternsMap.values.toList()

    fun loadPatterns(newPatterns: List<StepPattern>) {
        patternsMap = newPatterns.associateBy { it.padId }.toMutableMap()
        syncEnginePatterns()
        updateRows()
    }

    override fun onCleared() {
        super.onCleared()
        engine.release()
        audioPlayer.release()
    }
}
