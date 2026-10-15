package com.geovanivargas.planetamusica.domain.sampler

data class Pad(
    val id: Int = 0,
    val name: String = "",
    val filePath: String? = null,
    val category: PadCategory = PadCategory.OTHER,
    val position: Int = 0,
    val volume: Float = 1.0f,
    val isLoop: Boolean = false,
    val rate: Float = 1.0f,
) {
    val hasSound: Boolean get() = filePath != null
}
