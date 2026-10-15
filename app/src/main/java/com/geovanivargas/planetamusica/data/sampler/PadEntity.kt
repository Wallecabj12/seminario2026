package com.geovanivargas.planetamusica.data.sampler

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pads")
data class PadEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val filePath: String?,
    val category: String,
    val position: Int,
)
