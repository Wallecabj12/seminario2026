package com.geovanivargas.planetamusica.domain.presets

class RenamePresetUseCase(
    private val presetRepository: PresetRepository,
) {
    suspend operator fun invoke(presetId: Long, newName: String) {
        require(newName.isNotBlank()) { "El nombre no puede estar vacío." }
        presetRepository.renamePreset(presetId, newName.trim())
    }
}
