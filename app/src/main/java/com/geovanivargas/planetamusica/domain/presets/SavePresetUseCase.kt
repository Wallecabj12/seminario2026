package com.geovanivargas.planetamusica.domain.presets

import com.geovanivargas.planetamusica.domain.AuthRepository
import com.geovanivargas.planetamusica.domain.sampler.Pad

class SavePresetUseCase(
    private val presetRepository: PresetRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(name: String, pads: List<Pad>, patternData: String? = null): Long {
        require(name.isNotBlank()) { "El nombre del preset no puede estar vacío." }
        val userId = authRepository.getCurrentUserId()
        return presetRepository.savePreset(name.trim(), userId, pads, patternData)
    }
}
