package com.geovanivargas.planetamusica.data.synth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "synth_patches")
data class SynthPatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val paramsJson: String,
    val isFactory: Boolean = false,
)
