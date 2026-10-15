package com.geovanivargas.planetamusica.data.presets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "preset_pads",
    foreignKeys = [
        ForeignKey(
            entity = PresetEntity::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("presetId")],
)
data class PresetPadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val padPosition: Int,
    val name: String,
    val filePath: String?,
    val category: String,
    val volume: Float = 1.0f,
    val isLoop: Boolean = false,
    val rate: Float = 1.0f,
)
