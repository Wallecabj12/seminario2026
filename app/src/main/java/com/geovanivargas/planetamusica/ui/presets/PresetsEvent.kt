package com.geovanivargas.planetamusica.ui.presets

import com.geovanivargas.planetamusica.domain.presets.Preset

sealed interface PresetsEvent {
    data object OpenSaveDialog : PresetsEvent
    data class PresetNameInputChanged(val name: String) : PresetsEvent
    data object DismissSaveDialog : PresetsEvent
    data object ConfirmSavePreset : PresetsEvent

    data class RequestLoadPreset(val preset: Preset) : PresetsEvent
    data object ConfirmLoadPreset : PresetsEvent
    data object DismissLoadDialog : PresetsEvent

    data class RequestRenamePreset(val preset: Preset) : PresetsEvent
    data class RenameInputChanged(val name: String) : PresetsEvent
    data object ConfirmRenamePreset : PresetsEvent
    data object DismissRenameDialog : PresetsEvent

    data class RequestDeletePreset(val preset: Preset) : PresetsEvent
    data object ConfirmDeletePreset : PresetsEvent
    data object DismissDeleteDialog : PresetsEvent

    data object ClearStatus : PresetsEvent
}
