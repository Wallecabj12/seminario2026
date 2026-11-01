package com.geovanivargas.planetamusica.domain.synth

import kotlinx.coroutines.flow.Flow

interface SynthPatchRepository {
    fun getAllPatches(): Flow<List<SynthPatch>>
    suspend fun savePatch(patch: SynthPatch): Long
    suspend fun deletePatch(id: Long)
    suspend fun renamePatch(id: Long, newName: String)
}
