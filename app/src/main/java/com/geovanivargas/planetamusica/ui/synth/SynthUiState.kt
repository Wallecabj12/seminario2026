package com.geovanivargas.planetamusica.ui.synth

import com.geovanivargas.planetamusica.audio.synth.*
import com.geovanivargas.planetamusica.domain.synth.SynthPatch

data class SynthUiState(
    val params: SynthParams = SynthParams(),
    val patches: List<SynthPatch> = emptyList(),
    val currentPatchName: String = "Init",
    val currentPatchId: Long? = null,
    val selectedOctave: Int = 4,
    val previewWaveform: FloatArray = FloatArray(0),
    val filterResponse: FloatArray = FloatArray(0),
    val ampEnvCurve: FloatArray = FloatArray(0),
    val filterEnvCurve: FloatArray = FloatArray(0),
    val isRendering: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null,
    val showExportDialog: Boolean = false,
    val showPatchListDialog: Boolean = false,
    val showSavePatchDialog: Boolean = false,
    val exportName: String = "Synth Sound",
    val exportNote: Int = 60,
    val exportDurationSec: Float = 1.0f,
    val availablePadPositions: List<Int> = emptyList(),
    val selectedExportPad: Int? = null,
    val storageUsedBytes: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SynthUiState) return false
        return params == other.params &&
                patches == other.patches &&
                currentPatchName == other.currentPatchName &&
                currentPatchId == other.currentPatchId &&
                selectedOctave == other.selectedOctave &&
                previewWaveform.contentEquals(other.previewWaveform) &&
                filterResponse.contentEquals(other.filterResponse) &&
                ampEnvCurve.contentEquals(other.ampEnvCurve) &&
                filterEnvCurve.contentEquals(other.filterEnvCurve) &&
                isRendering == other.isRendering &&
                isExporting == other.isExporting &&
                message == other.message &&
                showExportDialog == other.showExportDialog &&
                showPatchListDialog == other.showPatchListDialog &&
                showSavePatchDialog == other.showSavePatchDialog &&
                exportName == other.exportName &&
                exportNote == other.exportNote &&
                exportDurationSec == other.exportDurationSec &&
                availablePadPositions == other.availablePadPositions &&
                selectedExportPad == other.selectedExportPad &&
                storageUsedBytes == other.storageUsedBytes
    }

    override fun hashCode(): Int = params.hashCode() + currentPatchName.hashCode()
}

sealed interface SynthEvent {
    // Oscillators
    data class OscAShapeChanged(val shape: WaveShape) : SynthEvent
    data class OscAOctaveChanged(val octave: Int) : SynthEvent
    data class OscASemitoneChanged(val semitone: Int) : SynthEvent
    data class OscADetuneChanged(val detune: Float) : SynthEvent
    data class OscALevelChanged(val level: Float) : SynthEvent
    data class OscAUnisonChanged(val voices: Int) : SynthEvent
    data class OscAUnisonDetuneChanged(val detune: Float) : SynthEvent
    data class OscBShapeChanged(val shape: WaveShape) : SynthEvent
    data class OscBOctaveChanged(val octave: Int) : SynthEvent
    data class OscBSemitoneChanged(val semitone: Int) : SynthEvent
    data class OscBDetuneChanged(val detune: Float) : SynthEvent
    data class OscBLevelChanged(val level: Float) : SynthEvent
    data class OscBUnisonChanged(val voices: Int) : SynthEvent
    data class OscBUnisonDetuneChanged(val detune: Float) : SynthEvent
    data class SubEnabledChanged(val enabled: Boolean) : SynthEvent
    data class SubLevelChanged(val level: Float) : SynthEvent
    // Filter
    data class FilterTypeChanged(val type: FilterType) : SynthEvent
    data class FilterCutoffChanged(val cutoff: Float) : SynthEvent
    data class FilterResonanceChanged(val resonance: Float) : SynthEvent
    data class FilterDriveChanged(val drive: Float) : SynthEvent
    // Envelopes
    data class AmpAttackChanged(val v: Float) : SynthEvent
    data class AmpDecayChanged(val v: Float) : SynthEvent
    data class AmpSustainChanged(val v: Float) : SynthEvent
    data class AmpReleaseChanged(val v: Float) : SynthEvent
    data class FilterEnvAttackChanged(val v: Float) : SynthEvent
    data class FilterEnvDecayChanged(val v: Float) : SynthEvent
    data class FilterEnvSustainChanged(val v: Float) : SynthEvent
    data class FilterEnvReleaseChanged(val v: Float) : SynthEvent
    data class FilterEnvAmountChanged(val v: Float) : SynthEvent
    // LFO
    data class LfoShapeChanged(val shape: WaveShape) : SynthEvent
    data class LfoRateChanged(val rate: Float) : SynthEvent
    data class LfoDepthChanged(val depth: Float) : SynthEvent
    data class LfoTargetChanged(val target: LfoTarget) : SynthEvent
    // FX
    data class DistortionEnabledChanged(val enabled: Boolean) : SynthEvent
    data class DistortionMixChanged(val mix: Float) : SynthEvent
    data class DelayEnabledChanged(val enabled: Boolean) : SynthEvent
    data class DelayTimeChanged(val time: Float) : SynthEvent
    data class DelayFeedbackChanged(val feedback: Float) : SynthEvent
    data class DelayMixChanged(val mix: Float) : SynthEvent
    data class ReverbEnabledChanged(val enabled: Boolean) : SynthEvent
    data class ReverbSizeChanged(val size: Float) : SynthEvent
    data class ReverbMixChanged(val mix: Float) : SynthEvent
    data class MasterLevelChanged(val level: Float) : SynthEvent
    // Keyboard
    data class NotePressed(val midiNote: Int) : SynthEvent
    data class OctaveChanged(val octave: Int) : SynthEvent
    // Patches
    data object Randomize : SynthEvent
    data object ResetParams : SynthEvent
    data object ShowPatchList : SynthEvent
    data object HidePatchList : SynthEvent
    data class LoadPatch(val patch: SynthPatch) : SynthEvent
    data object ShowSavePatchDialog : SynthEvent
    data object HideSavePatchDialog : SynthEvent
    data class SavePatch(val name: String) : SynthEvent
    data class DeletePatch(val id: Long) : SynthEvent
    // Export
    data object ShowExportDialog : SynthEvent
    data object HideExportDialog : SynthEvent
    data class ExportNameChanged(val name: String) : SynthEvent
    data class ExportNoteChanged(val note: Int) : SynthEvent
    data class ExportDurationChanged(val sec: Float) : SynthEvent
    data class ExportPadSelected(val position: Int) : SynthEvent
    data object ConfirmExport : SynthEvent
    // General
    data object ClearMessage : SynthEvent
}
