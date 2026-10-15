package com.geovanivargas.planetamusica.domain.presets

import kotlinx.coroutines.flow.Flow

class GetPresetsUseCase(
    private val presetRepository: PresetRepository,
) {
    operator fun invoke(): Flow<List<Preset>> = presetRepository.getAllPresets()
}
