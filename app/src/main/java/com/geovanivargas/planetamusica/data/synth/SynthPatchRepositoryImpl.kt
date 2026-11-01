package com.geovanivargas.planetamusica.data.synth

import com.geovanivargas.planetamusica.audio.synth.FactoryPatches
import com.geovanivargas.planetamusica.audio.synth.SynthParams
import com.geovanivargas.planetamusica.audio.synth.SynthParamsJson
import com.geovanivargas.planetamusica.domain.synth.SynthPatch
import com.geovanivargas.planetamusica.domain.synth.SynthPatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SynthPatchRepositoryImpl(
    private val dao: SynthPatchDao,
) : SynthPatchRepository {

    override fun getAllPatches(): Flow<List<SynthPatch>> =
        dao.getAllPatches().map { list -> list.map { it.toDomain() } }

    override suspend fun savePatch(patch: SynthPatch): Long {
        val entity = SynthPatchEntity(
            id = patch.id,
            name = patch.name,
            paramsJson = SynthParamsJson.encode(patch.params),
            isFactory = patch.isFactory,
        )
        return dao.insert(entity)
    }

    override suspend fun deletePatch(id: Long) = dao.delete(id)

    override suspend fun renamePatch(id: Long, newName: String) = dao.rename(id, newName)

    suspend fun ensureFactoryPatches() {
        if (dao.factoryCount() > 0) return
        for ((name, params) in FactoryPatches.ALL) {
            savePatch(SynthPatch(name = name, params = params, isFactory = true))
        }
    }

    private fun SynthPatchEntity.toDomain(): SynthPatch {
        val params = SynthParamsJson.decode(paramsJson)
        return SynthPatch(id = id, name = name, params = params, isFactory = isFactory)
    }
}
