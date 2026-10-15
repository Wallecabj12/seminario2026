package com.geovanivargas.planetamusica.domain.presets

class DeletePresetUseCase(
    private val presetRepository: PresetRepository,
) {
    suspend operator fun invoke(presetId: Long) {
        presetRepository.deletePreset(presetId)
    }
}
