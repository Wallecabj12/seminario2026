package com.geovanivargas.planetamusica.data.sampler

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SamplerRepositoryImpl(
    private val dao: PadDao,
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
        if (dao.getPadCount() > 0) return
        val pads = (0 until 16).map { pos ->
            PadEntity(
                id = pos + 1,
                name = "Pad ${pos + 1}",
                filePath = null,
                category = PadCategory.OTHER.name,
                position = pos,
            )
        }
        dao.insertAll(pads)
    }
}
