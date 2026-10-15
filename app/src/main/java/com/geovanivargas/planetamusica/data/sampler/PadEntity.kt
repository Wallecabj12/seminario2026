package com.geovanivargas.planetamusica.data.sampler

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pads")
data class PadEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val filePath: String?,
    val category: String,
    val position: Int,
    @ColumnInfo(defaultValue = "1.0") val volume: Float = 1.0f,
    @ColumnInfo(defaultValue = "0") val isLoop: Boolean = false,
    @ColumnInfo(defaultValue = "1.0") val rate: Float = 1.0f,
)
