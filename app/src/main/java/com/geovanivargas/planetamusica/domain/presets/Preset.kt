package com.geovanivargas.planetamusica.domain.presets

import com.geovanivargas.planetamusica.domain.sampler.Pad

data class Preset(
    val id: Long = 0,
    val name: String,
    val userId: String? = null, // null = local
    val createdAt: Long = System.currentTimeMillis(),
    val pads: List<Pad> = emptyList(),
    val patternData: String? = null,
) {
    val isLocal: Boolean get() = userId == null
}
