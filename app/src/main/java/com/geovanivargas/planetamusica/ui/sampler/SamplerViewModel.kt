package com.geovanivargas.planetamusica.ui.sampler

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.audio.synth.FactoryPatches
import com.geovanivargas.planetamusica.audio.synth.SynthEngine
import com.geovanivargas.planetamusica.audio.synth.WavWriter
import com.geovanivargas.planetamusica.data.sampler.FileManager
import com.geovanivargas.planetamusica.data.sampler.SamplerRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SoundCraftDatabase
import com.geovanivargas.planetamusica.data.sampler.SoundPoolAudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.AudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import com.geovanivargas.planetamusica.ui.padsettings.PadSettingsEvent
import com.geovanivargas.planetamusica.ui.padsettings.PadSettingsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ── UiState ──────────────────────────────────────────────

data class SamplerUiState(
    val pads: List<Pad> = emptyList(),
    val selectedFilter: PadCategory = PadCategory.ALL,
    val activePadName: String? = null,
    val isPlaying: Boolean = false,
    val activeLoopsCount: Int = 0,
    val storageUsedBytes: Long = 0L,
    val message: String? = null,
    val padSettingsState: PadSettingsUiState = PadSettingsUiState(),
    val requestFilePickerForPadId: Int? = null,
    val requestNavigateToSynthForPadId: Int? = null,
) {
    val filteredPads: List<Pad>
        get() = if (selectedFilter == PadCategory.ALL) pads
        else pads.filter { it.category == selectedFilter }

    val storageText: String
        get() {
            val usedMb = storageUsedBytes / (1024.0 * 1024.0)
            return "%.1f / 50 MB".format(usedMb)
        }
}

// ── UiEvent ──────────────────────────────────────────────

sealed interface SamplerEvent {
    data class PadTapped(val padId: Int) : SamplerEvent
    data class PadLongPressed(val padId: Int) : SamplerEvent
    data class FilePickerRequested(val padId: Int) : SamplerEvent
    data class FileSelected(val padId: Int, val uri: Uri) : SamplerEvent
    data class CategoryFilterSelected(val category: PadCategory) : SamplerEvent
    data class PadSettingsEventReceived(val event: PadSettingsEvent) : SamplerEvent
    data object FilePickerConsumed : SamplerEvent
    data object NavigateToSynthConsumed : SamplerEvent
    data object StopAll : SamplerEvent
    data object ClearMessage : SamplerEvent
}

// ── ViewModel ────────────────────────────────────────────

class SamplerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoundCraftDatabase.getInstance(application)
    private val fileManager = FileManager(application)
    private val audioPlayer: AudioPlayer = SoundPoolAudioPlayer(application)
    private val repo: SamplerRepository = SamplerRepositoryImpl(db.padDao(), application)
    private var previewAudioTrack: AudioTrack? = null

    private val _state = MutableStateFlow(SamplerUiState())
    val state: StateFlow<SamplerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.initializePads()
            _state.update { it.copy(storageUsedBytes = fileManager.getStorageUsedBytes()) }
        }
        viewModelScope.launch {
            repo.getAllPads().collect { pads ->
                pads.filter { it.hasSound }.forEach { pad ->
                    pad.filePath?.let { audioPlayer.loadSound(pad.id, it) }
                }
                _state.update { it.copy(pads = pads) }
            }
        }
    }

    fun onEvent(event: SamplerEvent) {
        when (event) {
            is SamplerEvent.PadTapped -> playPad(event.padId)
            is SamplerEvent.PadLongPressed -> openPadSettings(event.padId)
            is SamplerEvent.FilePickerRequested ->
                _state.update { it.copy(requestFilePickerForPadId = event.padId) }
            is SamplerEvent.FileSelected -> assignSound(event.padId, event.uri)
            is SamplerEvent.CategoryFilterSelected ->
                _state.update { it.copy(selectedFilter = event.category) }
            is SamplerEvent.PadSettingsEventReceived -> handlePadSettingsEvent(event.event)
            SamplerEvent.FilePickerConsumed ->
                _state.update { it.copy(requestFilePickerForPadId = null) }
            SamplerEvent.NavigateToSynthConsumed ->
                _state.update { it.copy(requestNavigateToSynthForPadId = null) }
            SamplerEvent.StopAll -> {
                audioPlayer.stopAll()
                _state.update {
                    it.copy(
                        activePadName = null,
                        isPlaying = false,
                        activeLoopsCount = 0,
                    )
                }
            }
            SamplerEvent.ClearMessage ->
                _state.update { it.copy(message = null) }
        }
    }

    private fun playPad(padId: Int) {
        val pad = _state.value.pads.find { it.id == padId } ?: return
        if (!pad.hasSound) {
            _state.update { it.copy(message = "Mantén presionado para cargar un sonido o sintetizarlo.") }
            return
        }
        audioPlayer.play(pad)
        val loops = audioPlayer.getActiveLoops().size
        _state.update {
            it.copy(
                activePadName = pad.name,
                isPlaying = true,
                activeLoopsCount = loops,
            )
        }
    }

    private fun openPadSettings(padId: Int) {
        val pad = _state.value.pads.find { it.id == padId } ?: return
        _state.update {
            it.copy(
                padSettingsState = PadSettingsUiState(
                    pad = pad,
                    name = pad.name,
                    category = pad.category,
                    volume = pad.volume,
                    isLoop = pad.isLoop,
                    rate = pad.rate,
                    isVisible = true,
                    isSynthCreatorVisible = false,
                ),
            )
        }
    }

    private fun handlePadSettingsEvent(event: PadSettingsEvent) {
        when (event) {
            is PadSettingsEvent.Open -> openPadSettings(event.pad.id)
            is PadSettingsEvent.NameChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(name = event.name)) }
            is PadSettingsEvent.CategoryChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(category = event.category)) }
            is PadSettingsEvent.VolumeChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(volume = event.volume)) }
            is PadSettingsEvent.ModeChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(isLoop = event.isLoop)) }
            is PadSettingsEvent.RateChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(rate = event.rate)) }
            PadSettingsEvent.ReassignFileClicked -> {
                val padId = _state.value.padSettingsState.pad?.id ?: return
                _state.update {
                    it.copy(
                        requestFilePickerForPadId = padId,
                        padSettingsState = it.padSettingsState.copy(isVisible = false),
                    )
                }
            }
            PadSettingsEvent.DeletePadClicked -> deletePadSound()
            PadSettingsEvent.SaveClicked -> savePadSettings()
            PadSettingsEvent.Dismiss ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(isVisible = false)) }

            // Integración directa Synth
            PadSettingsEvent.OpenSynthCreatorClicked ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(isSynthCreatorVisible = true)) }
            PadSettingsEvent.CloseSynthCreatorClicked ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(isSynthCreatorVisible = false)) }
            is PadSettingsEvent.SynthPresetSelected ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(selectedSynthPreset = event.presetName)) }
            is PadSettingsEvent.SynthNoteChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(selectedMidiNote = event.note)) }
            is PadSettingsEvent.SynthDurationChanged ->
                _state.update { it.copy(padSettingsState = it.padSettingsState.copy(synthDurationSec = event.sec)) }
            PadSettingsEvent.PreviewSynthSound -> previewSynthSound()
            PadSettingsEvent.ApplySynthToPadClicked -> applySynthSoundToPad()
            PadSettingsEvent.OpenFullSynthClicked -> {
                val padId = _state.value.padSettingsState.pad?.id ?: return
                _state.update {
                    it.copy(
                        requestNavigateToSynthForPadId = padId,
                        padSettingsState = it.padSettingsState.copy(isVisible = false, isSynthCreatorVisible = false),
                    )
                }
            }
        }
    }

    private fun getNoteName(note: Int): String {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (note / 12) - 1
        return "${names[note % 12]}$octave"
    }

    private fun previewSynthSound() {
        val s = _state.value.padSettingsState
        val preset = when (s.selectedSynthPreset) {
            "Bass" -> FactoryPatches.BASS
            "Lead" -> FactoryPatches.LEAD
            "Pluck" -> FactoryPatches.PLUCK
            "Pad" -> FactoryPatches.PAD
            "FX" -> FactoryPatches.FX
            else -> FactoryPatches.LEAD
        }
        val durationMs = (s.synthDurationSec * 1000).toLong().coerceAtMost(1500L)

        viewModelScope.launch {
            val engine = SynthEngine()
            val samples = withContext(Dispatchers.Default) {
                engine.render(preset, s.selectedMidiNote, durationMs)
            }

            previewAudioTrack?.stop()
            previewAudioTrack?.release()

            previewAudioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SynthEngine.SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            previewAudioTrack?.write(samples, 0, samples.size)
            previewAudioTrack?.play()
        }
    }

    private fun applySynthSoundToPad() {
        val s = _state.value.padSettingsState
        val pad = s.pad ?: return
        val preset = when (s.selectedSynthPreset) {
            "Bass" -> FactoryPatches.BASS
            "Lead" -> FactoryPatches.LEAD
            "Pluck" -> FactoryPatches.PLUCK
            "Pad" -> FactoryPatches.PAD
            "FX" -> FactoryPatches.FX
            else -> FactoryPatches.LEAD
        }
        val durationMs = (s.synthDurationSec * 1000).toLong()
        val noteName = getNoteName(s.selectedMidiNote)
        val soundName = "Synth ${s.selectedSynthPreset} $noteName"

        viewModelScope.launch {
            val engine = SynthEngine()
            val samples = withContext(Dispatchers.Default) {
                engine.render(preset, s.selectedMidiNote, durationMs)
            }

            val soundsDir = File(getApplication<Application>().filesDir, FileManager.SOUNDS_DIR).also { it.mkdirs() }
            val fileName = "${System.currentTimeMillis()}_synth_${s.selectedSynthPreset}_${s.selectedMidiNote}.wav"
            val file = File(soundsDir, fileName)

            withContext(Dispatchers.IO) {
                WavWriter.writeWavFile(file, samples)
            }

            pad.filePath?.let {
                fileManager.deleteFile(it)
                audioPlayer.unload(pad.id)
            }

            val updated = pad.copy(
                name = soundName,
                filePath = file.absolutePath,
                category = PadCategory.SYNTH,
            )
            repo.updatePad(updated)
            audioPlayer.loadSound(pad.id, file.absolutePath)
            audioPlayer.play(updated)

            _state.update {
                it.copy(
                    storageUsedBytes = fileManager.getStorageUsedBytes(),
                    padSettingsState = it.padSettingsState.copy(isVisible = false, isSynthCreatorVisible = false),
                    message = "Instrumento '$soundName' asignado al Pad ${pad.position + 1}.",
                    activePadName = soundName,
                    isPlaying = true,
                )
            }
        }
    }

    private fun savePadSettings() {
        val s = _state.value.padSettingsState
        val pad = s.pad ?: return
        val updated = pad.copy(
            name = s.name.ifBlank { "Pad ${pad.position + 1}" },
            category = s.category,
            volume = s.volume,
            isLoop = s.isLoop,
            rate = s.rate,
        )
        viewModelScope.launch {
            repo.updatePad(updated)
            _state.update {
                it.copy(
                    padSettingsState = it.padSettingsState.copy(isVisible = false),
                    message = "Ajustes de '${updated.name}' guardados.",
                )
            }
        }
    }

    private fun deletePadSound() {
        val pad = _state.value.padSettingsState.pad ?: return
        viewModelScope.launch {
            pad.filePath?.let {
                fileManager.deleteFile(it)
                audioPlayer.unload(pad.id)
            }
            val resetPad = pad.copy(
                name = "Pad ${pad.position + 1}",
                filePath = null,
                category = PadCategory.OTHER,
                volume = 1.0f,
                isLoop = false,
                rate = 1.0f,
            )
            repo.updatePad(resetPad)
            _state.update {
                it.copy(
                    storageUsedBytes = fileManager.getStorageUsedBytes(),
                    padSettingsState = it.padSettingsState.copy(isVisible = false),
                    message = "Pad ${pad.position + 1} eliminado.",
                    activeLoopsCount = audioPlayer.getActiveLoops().size,
                )
            }
        }
    }

    private fun assignSound(padId: Int, uri: Uri) {
        viewModelScope.launch {
            if (!fileManager.isValidAudioFormat(uri)) {
                _state.update { it.copy(message = "Formato no soportado. Solo .wav y .mp3.") }
                return@launch
            }

            val result = fileManager.copyFileToInternal(uri)
            if (result.isFailure) {
                _state.update { it.copy(message = result.exceptionOrNull()?.message) }
                return@launch
            }

            val filePath = result.getOrThrow()
            val fileName = fileManager.getFileName(uri).substringBeforeLast(".")

            val pad = repo.getPadById(padId) ?: return@launch

            pad.filePath?.let {
                fileManager.deleteFile(it)
                audioPlayer.unload(padId)
            }

            val updated = pad.copy(name = fileName, filePath = filePath)
            repo.updatePad(updated)
            audioPlayer.loadSound(padId, filePath)

            _state.update {
                it.copy(
                    storageUsedBytes = fileManager.getStorageUsedBytes(),
                    message = "Sonido '$fileName' asignado.",
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        previewAudioTrack?.stop()
        previewAudioTrack?.release()
        audioPlayer.release()
    }
}
