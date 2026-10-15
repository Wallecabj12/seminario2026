package com.geovanivargas.planetamusica.ui.padsettings

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

data class PadSettingsUiState(
    val pad: Pad? = null,
    val name: String = "",
    val category: PadCategory = PadCategory.OTHER,
    val volume: Float = 1.0f,
    val isLoop: Boolean = false,
    val rate: Float = 1.0f,
    val isVisible: Boolean = false,
) {
    val volumePercent: Int get() = (volume * 100).toInt()
}
