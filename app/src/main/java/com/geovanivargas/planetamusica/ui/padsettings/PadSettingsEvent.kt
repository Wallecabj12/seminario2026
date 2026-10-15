package com.geovanivargas.planetamusica.ui.padsettings

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

sealed interface PadSettingsEvent {
    data class Open(val pad: Pad) : PadSettingsEvent
    data class NameChanged(val name: String) : PadSettingsEvent
    data class CategoryChanged(val category: PadCategory) : PadSettingsEvent
    data class VolumeChanged(val volume: Float) : PadSettingsEvent
    data class ModeChanged(val isLoop: Boolean) : PadSettingsEvent
    data class RateChanged(val rate: Float) : PadSettingsEvent
    data object ReassignFileClicked : PadSettingsEvent
    data object DeletePadClicked : PadSettingsEvent
    data object SaveClicked : PadSettingsEvent
    data object Dismiss : PadSettingsEvent
}
