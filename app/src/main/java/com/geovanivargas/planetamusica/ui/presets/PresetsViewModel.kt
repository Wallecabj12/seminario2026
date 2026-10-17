package com.geovanivargas.planetamusica.ui.presets

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.data.FakeAuthRepository
import com.geovanivargas.planetamusica.data.presets.PresetRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SamplerRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SoundCraftDatabase
import com.geovanivargas.planetamusica.domain.presets.DeletePresetUseCase
import com.geovanivargas.planetamusica.domain.presets.GetPresetsUseCase
import com.geovanivargas.planetamusica.domain.presets.LoadPresetUseCase
import com.geovanivargas.planetamusica.domain.presets.Preset
import com.geovanivargas.planetamusica.domain.presets.RenamePresetUseCase
import com.geovanivargas.planetamusica.domain.presets.SavePresetUseCase
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PresetsViewModel(
    private val getPresetsUseCase: GetPresetsUseCase,
    private val savePresetUseCase: SavePresetUseCase,
    private val loadPresetUseCase: LoadPresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
    private val renamePresetUseCase: RenamePresetUseCase,
    private val samplerRepository: SamplerRepository,
) : ViewModel() {

    constructor(application: Application) : this(
        getPresetsUseCase = GetPresetsUseCase(
            PresetRepositoryImpl(SoundCraftDatabase.getInstance(application).presetDao()),
        ),
        savePresetUseCase = SavePresetUseCase(
            PresetRepositoryImpl(SoundCraftDatabase.getInstance(application).presetDao()),
            FakeAuthRepository(),
        ),
        loadPresetUseCase = LoadPresetUseCase(
            PresetRepositoryImpl(SoundCraftDatabase.getInstance(application).presetDao()),
            SamplerRepositoryImpl(SoundCraftDatabase.getInstance(application).padDao(), application),
        ),
        deletePresetUseCase = DeletePresetUseCase(
            PresetRepositoryImpl(SoundCraftDatabase.getInstance(application).presetDao()),
        ),
        renamePresetUseCase = RenamePresetUseCase(
            PresetRepositoryImpl(SoundCraftDatabase.getInstance(application).presetDao()),
        ),
        samplerRepository = SamplerRepositoryImpl(SoundCraftDatabase.getInstance(application).padDao(), application),
    )

    companion object {
        fun provideFactory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PresetsViewModel(application) as T
                }
            }
    }

    private val _state = MutableStateFlow(PresetsUiState())
    val state: StateFlow<PresetsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getPresetsUseCase().collect { presets ->
                _state.update { it.copy(presets = presets) }
            }
        }
    }

    fun onEvent(event: PresetsEvent) {
        when (event) {
            PresetsEvent.OpenSaveDialog ->
                _state.update { it.copy(showSaveDialog = true, newPresetName = "") }

            is PresetsEvent.PresetNameInputChanged ->
                _state.update { it.copy(newPresetName = event.name) }

            PresetsEvent.DismissSaveDialog ->
                _state.update { it.copy(showSaveDialog = false, newPresetName = "") }

            PresetsEvent.ConfirmSavePreset -> saveCurrentPreset()

            is PresetsEvent.RequestLoadPreset ->
                _state.update { it.copy(presetToLoad = event.preset) }

            PresetsEvent.ConfirmLoadPreset -> loadSelectedPreset()

            PresetsEvent.DismissLoadDialog ->
                _state.update { it.copy(presetToLoad = null) }

            is PresetsEvent.RequestRenamePreset ->
                _state.update { it.copy(presetToRename = event.preset, renameText = event.preset.name) }

            is PresetsEvent.RenameInputChanged ->
                _state.update { it.copy(renameText = event.name) }

            PresetsEvent.ConfirmRenamePreset -> renameSelectedPreset()

            PresetsEvent.DismissRenameDialog ->
                _state.update { it.copy(presetToRename = null, renameText = "") }

            is PresetsEvent.RequestDeletePreset ->
                _state.update { it.copy(presetToDelete = event.preset) }

            PresetsEvent.ConfirmDeletePreset -> deleteSelectedPreset()

            PresetsEvent.DismissDeleteDialog ->
                _state.update { it.copy(presetToDelete = null) }

            PresetsEvent.ClearStatus ->
                _state.update { it.copy(status = PresetsStatus.Idle) }
        }
    }

    var currentPatternProvider: (() -> String?)? = null
    var onPresetLoaded: ((Preset) -> Unit)? = null

    private fun saveCurrentPreset() {
        val name = _state.value.newPresetName.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(status = PresetsStatus.Error("Ingresa un nombre para el preset.")) }
            return
        }
        _state.update { it.copy(status = PresetsStatus.Loading, showSaveDialog = false) }
        viewModelScope.launch {
            try {
                val currentPads = samplerRepository.getAllPads().first()
                val patternData = currentPatternProvider?.invoke()
                savePresetUseCase(name, currentPads, patternData)
                _state.update {
                    it.copy(
                        status = PresetsStatus.Success("Preset '$name' guardado correctamente."),
                        newPresetName = "",
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(status = PresetsStatus.Error(e.message ?: "Error al guardar el preset."))
                }
            }
        }
    }

    private fun loadSelectedPreset() {
        val preset = _state.value.presetToLoad ?: return
        _state.update { it.copy(status = PresetsStatus.Loading, presetToLoad = null) }
        viewModelScope.launch {
            try {
                val loaded = loadPresetUseCase(preset.id)
                onPresetLoaded?.invoke(loaded)
                _state.update {
                    it.copy(status = PresetsStatus.Success("Preset '${preset.name}' cargado."))
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(status = PresetsStatus.Error(e.message ?: "Error al cargar el preset."))
                }
            }
        }
    }

    private fun renameSelectedPreset() {
        val preset = _state.value.presetToRename ?: return
        val newName = _state.value.renameText.trim()
        if (newName.isEmpty()) {
            _state.update { it.copy(status = PresetsStatus.Error("El nombre no puede estar vacío.")) }
            return
        }
        _state.update { it.copy(status = PresetsStatus.Loading, presetToRename = null) }
        viewModelScope.launch {
            try {
                renamePresetUseCase(preset.id, newName)
                _state.update {
                    it.copy(status = PresetsStatus.Success("Preset renombrado a '$newName'."))
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(status = PresetsStatus.Error(e.message ?: "Error al renombrar el preset."))
                }
            }
        }
    }

    private fun deleteSelectedPreset() {
        val preset = _state.value.presetToDelete ?: return
        _state.update { it.copy(status = PresetsStatus.Loading, presetToDelete = null) }
        viewModelScope.launch {
            try {
                deletePresetUseCase(preset.id)
                _state.update {
                    it.copy(status = PresetsStatus.Success("Preset '${preset.name}' eliminado."))
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(status = PresetsStatus.Error(e.message ?: "Error al eliminar el preset."))
                }
            }
        }
    }
}
