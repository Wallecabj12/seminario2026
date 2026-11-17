package com.geovanivargas.planetamusica.ui.synth

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.audio.synth.*
import com.geovanivargas.planetamusica.data.sampler.FileManager
import com.geovanivargas.planetamusica.data.sampler.SamplerRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SoundCraftDatabase
import com.geovanivargas.planetamusica.data.synth.SynthPatchRepositoryImpl
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.domain.synth.SynthPatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class SynthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoundCraftDatabase.getInstance(application)
    private val patchRepo = SynthPatchRepositoryImpl(db.synthPatchDao())
    private val padRepo = SamplerRepositoryImpl(db.padDao(), application)
    private val fileManager = FileManager(application)
    private val engine: SynthEngineInterface = SynthEngine()

    private val _state = MutableStateFlow(SynthUiState())
    val state: StateFlow<SynthUiState> = _state.asStateFlow()

    private var previewJob: Job? = null
    private var audioTrack: AudioTrack? = null

    init {
        viewModelScope.launch {
            patchRepo.ensureFactoryPatches()
            patchRepo.getAllPatches().collect { patches ->
                _state.update { it.copy(patches = patches) }
            }
        }
        updateVisuals()
        refreshStorage()
    }

    fun onEvent(event: SynthEvent) {
        when (event) {
            // ── Oscillator A ──
            is SynthEvent.OscAShapeChanged -> updateParams { copy(oscA = oscA.copy(shape = event.shape)) }
            is SynthEvent.OscAOctaveChanged -> updateParams { copy(oscA = oscA.copy(octave = event.octave.coerceIn(-2, 2))) }
            is SynthEvent.OscASemitoneChanged -> updateParams { copy(oscA = oscA.copy(semitone = event.semitone.coerceIn(-12, 12))) }
            is SynthEvent.OscADetuneChanged -> updateParams { copy(oscA = oscA.copy(detune = event.detune.coerceIn(-100f, 100f))) }
            is SynthEvent.OscALevelChanged -> updateParams { copy(oscA = oscA.copy(level = event.level.coerceIn(0f, 1f))) }
            is SynthEvent.OscAUnisonChanged -> updateParams { copy(oscA = oscA.copy(unisonVoices = event.voices.coerceIn(1, 5))) }
            is SynthEvent.OscAUnisonDetuneChanged -> updateParams { copy(oscA = oscA.copy(unisonDetune = event.detune.coerceIn(0f, 50f))) }
            // ── Oscillator B ──
            is SynthEvent.OscBShapeChanged -> updateParams { copy(oscB = oscB.copy(shape = event.shape)) }
            is SynthEvent.OscBOctaveChanged -> updateParams { copy(oscB = oscB.copy(octave = event.octave.coerceIn(-2, 2))) }
            is SynthEvent.OscBSemitoneChanged -> updateParams { copy(oscB = oscB.copy(semitone = event.semitone.coerceIn(-12, 12))) }
            is SynthEvent.OscBDetuneChanged -> updateParams { copy(oscB = oscB.copy(detune = event.detune.coerceIn(-100f, 100f))) }
            is SynthEvent.OscBLevelChanged -> updateParams { copy(oscB = oscB.copy(level = event.level.coerceIn(0f, 1f))) }
            is SynthEvent.OscBUnisonChanged -> updateParams { copy(oscB = oscB.copy(unisonVoices = event.voices.coerceIn(1, 5))) }
            is SynthEvent.OscBUnisonDetuneChanged -> updateParams { copy(oscB = oscB.copy(unisonDetune = event.detune.coerceIn(0f, 50f))) }
            // ── Sub ──
            is SynthEvent.SubEnabledChanged -> updateParams { copy(sub = sub.copy(enabled = event.enabled)) }
            is SynthEvent.SubLevelChanged -> updateParams { copy(sub = sub.copy(level = event.level.coerceIn(0f, 1f))) }
            // ── Filter ──
            is SynthEvent.FilterTypeChanged -> updateParams { copy(filter = filter.copy(type = event.type)) }
            is SynthEvent.FilterCutoffChanged -> updateParams { copy(filter = filter.copy(cutoff = event.cutoff.coerceIn(20f, 20000f))) }
            is SynthEvent.FilterResonanceChanged -> updateParams { copy(filter = filter.copy(resonance = event.resonance.coerceIn(0f, 1f))) }
            is SynthEvent.FilterDriveChanged -> updateParams { copy(filter = filter.copy(drive = event.drive.coerceIn(0f, 1f))) }
            // ── Amp Env ──
            is SynthEvent.AmpAttackChanged -> updateParams { copy(ampEnv = ampEnv.copy(attack = event.v.coerceIn(0.001f, 4f))) }
            is SynthEvent.AmpDecayChanged -> updateParams { copy(ampEnv = ampEnv.copy(decay = event.v.coerceIn(0.001f, 4f))) }
            is SynthEvent.AmpSustainChanged -> updateParams { copy(ampEnv = ampEnv.copy(sustain = event.v.coerceIn(0f, 1f))) }
            is SynthEvent.AmpReleaseChanged -> updateParams { copy(ampEnv = ampEnv.copy(release = event.v.coerceIn(0.001f, 4f))) }
            // ── Filter Env ──
            is SynthEvent.FilterEnvAttackChanged -> updateParams { copy(filterEnv = filterEnv.copy(attack = event.v.coerceIn(0.001f, 4f))) }
            is SynthEvent.FilterEnvDecayChanged -> updateParams { copy(filterEnv = filterEnv.copy(decay = event.v.coerceIn(0.001f, 4f))) }
            is SynthEvent.FilterEnvSustainChanged -> updateParams { copy(filterEnv = filterEnv.copy(sustain = event.v.coerceIn(0f, 1f))) }
            is SynthEvent.FilterEnvReleaseChanged -> updateParams { copy(filterEnv = filterEnv.copy(release = event.v.coerceIn(0.001f, 4f))) }
            is SynthEvent.FilterEnvAmountChanged -> updateParams { copy(filterEnvAmount = event.v.coerceIn(0f, 1f)) }
            // ── LFO ──
            is SynthEvent.LfoShapeChanged -> updateParams { copy(lfo = lfo.copy(shape = event.shape)) }
            is SynthEvent.LfoRateChanged -> updateParams { copy(lfo = lfo.copy(rate = event.rate.coerceIn(0.1f, 20f))) }
            is SynthEvent.LfoDepthChanged -> updateParams { copy(lfo = lfo.copy(depth = event.depth.coerceIn(0f, 1f))) }
            is SynthEvent.LfoTargetChanged -> updateParams { copy(lfo = lfo.copy(target = event.target)) }
            // ── FX ──
            is SynthEvent.DistortionEnabledChanged -> updateParams { copy(distortion = distortion.copy(enabled = event.enabled)) }
            is SynthEvent.DistortionMixChanged -> updateParams { copy(distortion = distortion.copy(mix = event.mix.coerceIn(0f, 1f))) }
            is SynthEvent.DelayEnabledChanged -> updateParams { copy(delay = delay.copy(enabled = event.enabled)) }
            is SynthEvent.DelayTimeChanged -> updateParams { copy(delay = delay.copy(time = event.time.coerceIn(0.05f, 1f))) }
            is SynthEvent.DelayFeedbackChanged -> updateParams { copy(delay = delay.copy(feedback = event.feedback.coerceIn(0f, 0.9f))) }
            is SynthEvent.DelayMixChanged -> updateParams { copy(delay = delay.copy(mix = event.mix.coerceIn(0f, 1f))) }
            is SynthEvent.ReverbEnabledChanged -> updateParams { copy(reverb = reverb.copy(enabled = event.enabled)) }
            is SynthEvent.ReverbSizeChanged -> updateParams { copy(reverb = reverb.copy(size = event.size.coerceIn(0f, 1f))) }
            is SynthEvent.ReverbMixChanged -> updateParams { copy(reverb = reverb.copy(mix = event.mix.coerceIn(0f, 1f))) }
            is SynthEvent.MasterLevelChanged -> updateParams { copy(masterLevel = event.level.coerceIn(0f, 1f)) }
            // ── Keyboard ──
            is SynthEvent.NotePressed -> playNote(event.midiNote)
            is SynthEvent.OctaveChanged -> _state.update { it.copy(selectedOctave = event.octave.coerceIn(1, 7)) }
            // ── Patches ──
            SynthEvent.Randomize -> randomize()
            SynthEvent.ResetParams -> {
                _state.update { it.copy(params = SynthParams(), currentPatchName = "Init", currentPatchId = null) }
                updateVisuals()
            }
            SynthEvent.ShowPatchList -> _state.update { it.copy(showPatchListDialog = true) }
            SynthEvent.HidePatchList -> _state.update { it.copy(showPatchListDialog = false) }
            is SynthEvent.LoadPatch -> {
                _state.update { it.copy(
                    params = event.patch.params,
                    currentPatchName = event.patch.name,
                    currentPatchId = event.patch.id,
                    showPatchListDialog = false,
                ) }
                updateVisuals()
            }
            SynthEvent.ShowSavePatchDialog -> _state.update { it.copy(showSavePatchDialog = true) }
            SynthEvent.HideSavePatchDialog -> _state.update { it.copy(showSavePatchDialog = false) }
            is SynthEvent.SavePatch -> savePatch(event.name)
            is SynthEvent.DeletePatch -> viewModelScope.launch { patchRepo.deletePatch(event.id) }
            // ── Export & Soundpad Integration ──
            SynthEvent.ShowExportDialog -> prepareExport()
            SynthEvent.HideExportDialog -> _state.update { it.copy(showExportDialog = false) }
            is SynthEvent.ExportNameChanged -> _state.update { it.copy(exportName = event.name) }
            is SynthEvent.ExportNoteChanged -> _state.update { it.copy(exportNote = event.note.coerceIn(21, 108)) }
            is SynthEvent.ExportDurationChanged -> _state.update { it.copy(exportDurationSec = event.sec.coerceIn(0.2f, 4f)) }
            is SynthEvent.ExportPadSelected -> _state.update { it.copy(selectedExportPad = event.position) }
            is SynthEvent.ConfirmExport -> exportToPad(event.openSoundpadAfter)
            SynthEvent.ShareAudioFile -> shareCurrentAudio()
            SynthEvent.ShareFileConsumed -> _state.update { it.copy(shareFileTrigger = null) }
            SynthEvent.NavigateToPadsConsumed -> _state.update { it.copy(navigateToPadsTrigger = null) }
            // ── General ──
            SynthEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun updateParams(block: SynthParams.() -> SynthParams) {
        _state.update { it.copy(params = it.params.block()) }
        schedulePreviewUpdate()
    }

    private fun schedulePreviewUpdate() {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            delay(150) // debounce
            updateVisuals()
        }
    }

    private fun updateVisuals() {
        viewModelScope.launch(Dispatchers.Default) {
            val p = _state.value.params
            val waveform = (engine as? SynthEngine)?.renderPreview(p, 60, 20) ?: FloatArray(0)
            val filterResp = BiquadFilter.frequencyResponse(p.filter.type, p.filter.cutoff, p.filter.resonance)
            val ampCurve = AdsrGenerator.generateCurve(p.ampEnv)
            val filterCurve = AdsrGenerator.generateCurve(p.filterEnv)
            _state.update { it.copy(
                previewWaveform = waveform,
                filterResponse = filterResp,
                ampEnvCurve = ampCurve,
                filterEnvCurve = filterCurve,
            ) }
        }
    }

    private fun playNote(midiNote: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isRendering = true) }
            val params = _state.value.params
            val samples = withContext(Dispatchers.Default) {
                engine.render(params, midiNote, 1500L)
            }
            playAudioTrack(samples)
            _state.update { it.copy(isRendering = false) }
        }
    }

    private fun playAudioTrack(samples: ShortArray) {
        audioTrack?.stop()
        audioTrack?.release()

        val bufferSize = samples.size * 2
        audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.write(samples, 0, samples.size)
        audioTrack?.play()
    }

    private fun randomize() {
        val shapes = WaveShape.values()
        val filterTypes = FilterType.values()
        val targets = LfoTarget.values()
        val r = Random

        val p = SynthParams(
            oscA = OscParams(
                shape = shapes[r.nextInt(shapes.size)],
                octave = r.nextInt(5) - 2, semitone = r.nextInt(13) - 6,
                detune = r.nextFloat() * 20f - 10f, level = 0.5f + r.nextFloat() * 0.5f,
                unisonVoices = r.nextInt(3) + 1, unisonDetune = r.nextFloat() * 25f,
            ),
            oscB = OscParams(
                shape = shapes[r.nextInt(shapes.size)],
                octave = r.nextInt(3) - 1, level = r.nextFloat() * 0.7f,
                unisonVoices = r.nextInt(2) + 1,
            ),
            sub = SubOscParams(enabled = r.nextBoolean(), level = 0.2f + r.nextFloat() * 0.4f),
            filter = FilterParams(
                type = filterTypes[r.nextInt(filterTypes.size)],
                cutoff = 200f + r.nextFloat() * 8000f,
                resonance = r.nextFloat() * 0.7f, drive = r.nextFloat() * 0.5f,
            ),
            ampEnv = AdsrParams(
                attack = 0.002f + r.nextFloat() * 0.5f, decay = 0.05f + r.nextFloat() * 0.5f,
                sustain = r.nextFloat() * 0.8f, release = 0.05f + r.nextFloat() * 0.8f,
            ),
            filterEnv = AdsrParams(
                attack = 0.002f + r.nextFloat() * 0.3f, decay = 0.05f + r.nextFloat() * 0.6f,
                sustain = r.nextFloat() * 0.5f, release = 0.05f + r.nextFloat() * 0.5f,
            ),
            filterEnvAmount = r.nextFloat() * 0.8f,
            lfo = LfoParams(
                shape = shapes[r.nextInt(shapes.size)],
                rate = 0.1f + r.nextFloat() * 10f, depth = r.nextFloat() * 0.5f,
                target = targets[r.nextInt(targets.size)],
            ),
            distortion = DistortionParams(enabled = r.nextFloat() > 0.6f, mix = r.nextFloat() * 0.4f),
            delay = DelayParams(
                enabled = r.nextFloat() > 0.5f, time = 0.1f + r.nextFloat() * 0.5f,
                feedback = r.nextFloat() * 0.5f, mix = r.nextFloat() * 0.35f,
            ),
            reverb = ReverbParams(
                enabled = r.nextFloat() > 0.4f, size = r.nextFloat() * 0.8f,
                mix = r.nextFloat() * 0.4f,
            ),
            masterLevel = 0.7f + r.nextFloat() * 0.3f,
        )
        _state.update { it.copy(params = p, currentPatchName = "Random", currentPatchId = null) }
        updateVisuals()
    }

    private fun savePatch(name: String) {
        viewModelScope.launch {
            val patch = SynthPatch(name = name, params = _state.value.params)
            val id = patchRepo.savePatch(patch)
            _state.update { it.copy(
                currentPatchName = name, currentPatchId = id,
                showSavePatchDialog = false, message = "Patch '$name' guardado.",
            ) }
        }
    }

    private fun prepareExport() {
        viewModelScope.launch {
            val pads = padRepo.getAllPads().first()
            val freePads = pads.filter { !it.hasSound }.map { it.position }
            val defaultTarget = freePads.firstOrNull() ?: _state.value.selectedExportPad ?: 0
            _state.update { it.copy(
                showExportDialog = true,
                allPads = pads,
                availablePadPositions = freePads,
                selectedExportPad = defaultTarget,
                storageUsedBytes = fileManager.getStorageUsedBytes(),
            ) }
        }
    }

    private fun exportToPad(openSoundpadAfter: Boolean) {
        val s = _state.value
        val targetPos = s.selectedExportPad ?: run {
            _state.update { it.copy(message = "Selecciona un pad de destino.") }
            return
        }
        val name = s.exportName.ifBlank { "Synth Sound" }
        val durationMs = (s.exportDurationSec * 1000).toLong()

        // Validar tamaño estimado
        val estimatedSize = WavWriter.wavFileSizeBytes(durationMs)
        if (estimatedSize > FileManager.MAX_FILE_SIZE_BYTES) {
            _state.update { it.copy(message = "Duración demasiado larga (>10 MB).") }
            return
        }
        if (s.storageUsedBytes + estimatedSize > FileManager.MAX_TOTAL_SIZE_BYTES) {
            _state.update { it.copy(message = "Almacenamiento lleno.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, showExportDialog = false) }

            val samples = withContext(Dispatchers.Default) {
                engine.render(s.params, s.exportNote, durationMs)
            }

            // Guardar WAV en almacenamiento interno
            val ctx = getApplication<Application>()
            val soundsDir = File(ctx.filesDir, FileManager.SOUNDS_DIR).also { it.mkdirs() }
            val fileName = "${System.currentTimeMillis()}_synth_${name.replace(" ", "_")}.wav"
            val file = File(soundsDir, fileName)

            withContext(Dispatchers.IO) {
                WavWriter.writeWavFile(file, samples)
            }

            // Asignar al pad
            val pads = padRepo.getAllPads().first()
            val pad = pads.find { it.position == targetPos }
            if (pad != null) {
                pad.filePath?.let {
                    fileManager.deleteFile(it)
                }
                val updated = pad.copy(
                    name = name,
                    filePath = file.absolutePath,
                    category = PadCategory.SYNTH,
                )
                padRepo.updatePad(updated)
            }

            _state.update { it.copy(
                isExporting = false,
                storageUsedBytes = fileManager.getStorageUsedBytes(),
                message = "Instrumento '$name' asignado al Pad ${targetPos + 1}.",
                navigateToPadsTrigger = if (openSoundpadAfter) targetPos else null,
            ) }
        }
    }

    private fun shareCurrentAudio() {
        val s = _state.value
        val name = s.exportName.ifBlank { s.currentPatchName.ifBlank { "Synth_Sound" } }
        val durationMs = (s.exportDurationSec * 1000).toLong()

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }

            val samples = withContext(Dispatchers.Default) {
                engine.render(s.params, s.exportNote, durationMs)
            }

            val ctx = getApplication<Application>()
            val soundsDir = File(ctx.filesDir, FileManager.SOUNDS_DIR).also { it.mkdirs() }
            val safeName = name.replace(" ", "_")
            val file = File(soundsDir, "${safeName}_${System.currentTimeMillis()}.wav")

            withContext(Dispatchers.IO) {
                WavWriter.writeWavFile(file, samples)
            }

            _state.update { it.copy(
                isExporting = false,
                shareFileTrigger = file,
                storageUsedBytes = fileManager.getStorageUsedBytes(),
                message = "Audio '$safeName.wav' listo para compartir.",
            ) }
        }
    }

    private fun refreshStorage() {
        viewModelScope.launch {
            _state.update { it.copy(storageUsedBytes = fileManager.getStorageUsedBytes()) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioTrack?.stop()
        audioTrack?.release()
    }
}
