package com.geovanivargas.planetamusica.domain.sampler

import kotlinx.coroutines.flow.Flow

interface SamplerRepository {
    fun getAllPads(): Flow<List<Pad>>
    suspend fun getPadById(id: Int): Pad?
    suspend fun updatePad(pad: Pad)
    suspend fun updateAllPads(pads: List<Pad>)
    suspend fun initializePads()
}
