package com.geovanivargas.planetamusica.domain.presets

import com.geovanivargas.planetamusica.domain.sampler.Pad
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun getAllPresets(): Flow<List<Preset>>
    suspend fun getPresetById(id: Long): Preset?
    suspend fun savePreset(name: String, userId: String?, pads: List<Pad>, patternData: String? = null): Long
    suspend fun renamePreset(id: Long, newName: String)
    suspend fun deletePreset(id: Long)
}
