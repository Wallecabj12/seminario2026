package com.geovanivargas.planetamusica.data.presets

import androidx.room.Embedded
import androidx.room.Relation

data class PresetWithPads(
    @Embedded val preset: PresetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "presetId",
    )
    val pads: List<PresetPadEntity>,
)
