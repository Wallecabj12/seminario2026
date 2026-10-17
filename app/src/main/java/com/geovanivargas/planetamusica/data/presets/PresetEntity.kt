package com.geovanivargas.planetamusica.data.presets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val userId: String?, // null = preset local sin cuenta
    val createdAt: Long = System.currentTimeMillis(),
    val patternData: String? = null,
)
