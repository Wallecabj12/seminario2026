package com.geovanivargas.planetamusica.domain.presets

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository

class LoadPresetUseCase(
    private val presetRepository: PresetRepository,
    private val samplerRepository: SamplerRepository,
) {
    suspend operator fun invoke(presetId: Long): Preset {
        val preset = presetRepository.getPresetById(presetId)
            ?: throw IllegalArgumentException("No se encontró el preset especificado.")

        val padsToRestore = if (preset.pads.size == 16) {
            preset.pads
        } else {
            (0 until 16).map { pos ->
                preset.pads.find { it.position == pos } ?: Pad(
                    id = pos + 1,
                    name = "Pad ${pos + 1}",
                    position = pos,
                )
            }
        }
        samplerRepository.updateAllPads(padsToRestore)
        return preset
    }
}
