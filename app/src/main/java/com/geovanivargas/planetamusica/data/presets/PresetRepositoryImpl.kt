package com.geovanivargas.planetamusica.data.presets

import com.geovanivargas.planetamusica.domain.presets.Preset
import com.geovanivargas.planetamusica.domain.presets.PresetRepository
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresetRepositoryImpl(
    private val dao: PresetDao,
) : PresetRepository {

    override fun getAllPresets(): Flow<List<Preset>> =
        dao.getAllPresetsWithPads().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getPresetById(id: Long): Preset? =
        dao.getPresetWithPadsById(id)?.toDomain()

    override suspend fun savePreset(name: String, userId: String?, pads: List<Pad>, patternData: String?): Long {
        val presetEntity = PresetEntity(
            name = name,
            userId = userId,
            createdAt = System.currentTimeMillis(),
            patternData = patternData,
        )
        val presetId = dao.insertPreset(presetEntity)
        val padEntities = pads.map { pad ->
            PresetPadEntity(
                presetId = presetId,
                padPosition = pad.position,
                name = pad.name,
                filePath = pad.filePath,
                category = pad.category.name,
                volume = pad.volume,
                isLoop = pad.isLoop,
                rate = pad.rate,
            )
        }
        dao.insertPresetPads(padEntities)
        return presetId
    }

    override suspend fun renamePreset(id: Long, newName: String) {
        dao.renamePreset(id, newName)
    }

    override suspend fun deletePreset(id: Long) {
        dao.deletePresetById(id)
    }

    private fun PresetWithPads.toDomain(): Preset {
        val domainPads = pads.sortedBy { it.padPosition }.map { p ->
            Pad(
                id = p.padPosition + 1,
                name = p.name,
                filePath = p.filePath,
                category = runCatching { PadCategory.valueOf(p.category) }.getOrDefault(PadCategory.OTHER),
                position = p.padPosition,
                volume = p.volume,
                isLoop = p.isLoop,
                rate = p.rate,
            )
        }
        return Preset(
            id = preset.id,
            name = preset.name,
            userId = preset.userId,
            createdAt = preset.createdAt,
            pads = domainPads,
            patternData = preset.patternData,
        )
    }
}
