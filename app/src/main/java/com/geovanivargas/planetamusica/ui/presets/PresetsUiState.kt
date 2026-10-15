package com.geovanivargas.planetamusica.ui.presets

import com.geovanivargas.planetamusica.domain.presets.Preset

sealed interface PresetsStatus {
    data object Idle : PresetsStatus
    data object Loading : PresetsStatus
    data class Success(val message: String) : PresetsStatus
    data class Error(val message: String) : PresetsStatus
}

data class PresetsUiState(
    val presets: List<Preset> = emptyList(),
    val status: PresetsStatus = PresetsStatus.Idle,
    val showSaveDialog: Boolean = false,
    val presetToRename: Preset? = null,
    val renameText: String = "",
    val presetToDelete: Preset? = null,
    val presetToLoad: Preset? = null,
    val newPresetName: String = "",
)
