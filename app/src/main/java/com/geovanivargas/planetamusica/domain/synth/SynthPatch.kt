package com.geovanivargas.planetamusica.domain.synth

import com.geovanivargas.planetamusica.audio.synth.SynthParams

data class SynthPatch(
    val id: Long = 0L,
    val name: String,
    val params: SynthParams,
    val isFactory: Boolean = false,
)
