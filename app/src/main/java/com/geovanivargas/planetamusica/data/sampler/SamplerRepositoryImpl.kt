package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import com.geovanivargas.planetamusica.audio.defaults.DefaultSoundGenerator
import com.geovanivargas.planetamusica.audio.defaults.SoundCraftPreferences
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SamplerRepositoryImpl(
    private val dao: PadDao,
    private val context: Context? = null,
) : SamplerRepository {

    override fun getAllPads(): Flow<List<Pad>> =
        dao.getAllPads().map { list -> list.map { it.toDomain() } }

    override suspend fun getPadById(id: Int): Pad? =
        dao.getPadById(id)?.toDomain()

    override suspend fun updatePad(pad: Pad) {
        dao.updatePad(pad.toEntity())
    }

    override suspend fun updateAllPads(pads: List<Pad>) {
        dao.insertAll(pads.map { it.toEntity() })
    }

    override suspend fun initializePads() {
        val count = dao.getPadCount()
        val prefs = context?.let { SoundCraftPreferences(it) }
        val alreadyGenerated = prefs?.areDefaultSoundsGenerated() ?: false

        if (count == 0) {
            val defaultSoundFiles = if (!alreadyGenerated && context != null) {
                DefaultSoundGenerator.generateAll(context).also {
                    prefs?.setDefaultSoundsGenerated()
                }
            } else emptyMap()

            val pads = (0 until 16).map { pos ->
                val spec = DefaultSoundGenerator.DEFAULT_SPECS.find { it.position == pos }
                val file = defaultSoundFiles[pos]
                PadEntity(
                    id = pos + 1,
                    name = spec?.name ?: "Pad ${pos + 1}",
                    filePath = file?.absolutePath,
                    category = (spec?.category ?: PadCategory.OTHER).name,
                    position = pos,
                    volume = 1.0f,
                    isLoop = false,
                    rate = 1.0f,
                )
            }
            dao.insertAll(pads)
        } else if (!alreadyGenerated && context != null) {
            val defaultSoundFiles = DefaultSoundGenerator.generateAll(context)
            prefs?.setDefaultSoundsGenerated()
            for (spec in DefaultSoundGenerator.DEFAULT_SPECS) {
                val current = dao.getPadById(spec.position + 1)
                if (current == null || current.filePath == null) {
                    val file = defaultSoundFiles[spec.position]
                    val updated = (current ?: PadEntity(
                        id = spec.position + 1,
                        name = spec.name,
                        filePath = file?.absolutePath,
                        category = spec.category.name,
                        position = spec.position,
                    )).copy(
                        name = spec.name,
                        filePath = file?.absolutePath,
                        category = spec.category.name,
                    )
                    dao.insertAll(listOf(updated))
                }
            }
        }
    }
}
