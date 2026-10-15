package com.geovanivargas.planetamusica.ui.sampler

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.data.sampler.FileManager
import com.geovanivargas.planetamusica.data.sampler.SamplerRepositoryImpl
import com.geovanivargas.planetamusica.data.sampler.SoundCraftDatabase
import com.geovanivargas.planetamusica.data.sampler.SoundPoolAudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.AudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UiState ──────────────────────────────────────────────

data class SamplerUiState(
    val pads: List<Pad> = emptyList(),
    val selectedFilter: PadCategory = PadCategory.ALL,
    val activePadName: String? = null,
    val isPlaying: Boolean = false,
    val storageUsedBytes: Long = 0L,
    val message: String? = null,
    val showCategoryDialogForPad: Int? = null,
) {
    val filteredPads: List<Pad>
        get() = if (selectedFilter == PadCategory.ALL) pads
        else pads.filter { it.category == selectedFilter }

    val storageText: String
        get() {
            val usedMb = storageUsedBytes / (1024.0 * 1024.0)
            return "%.1f / 50 MB".format(usedMb)
        }
}

// ── UiEvent ──────────────────────────────────────────────

sealed interface SamplerEvent {
    data class PadTapped(val padId: Int) : SamplerEvent
    data class FileSelected(val padId: Int, val uri: Uri) : SamplerEvent
    data class CategoryFilterSelected(val category: PadCategory) : SamplerEvent
    data class CategoryAssigned(val padId: Int, val category: PadCategory) : SamplerEvent
    data object DismissCategoryDialog : SamplerEvent
    data object StopAll : SamplerEvent
    data object ClearMessage : SamplerEvent
}

// ── ViewModel ────────────────────────────────────────────

class SamplerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoundCraftDatabase.getInstance(application)
    private val fileManager = FileManager(application)
    private val audioPlayer: AudioPlayer = SoundPoolAudioPlayer(application)
    private val repo: SamplerRepository = SamplerRepositoryImpl(db.padDao())

    private val _state = MutableStateFlow(SamplerUiState())
    val state: StateFlow<SamplerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.initializePads()
            _state.update { it.copy(storageUsedBytes = fileManager.getStorageUsedBytes()) }
        }
        viewModelScope.launch {
            repo.getAllPads().collect { pads ->
                // Cargar sonidos en SoundPool para pads con archivo
                pads.filter { it.hasSound }.forEach { pad ->
                    pad.filePath?.let { audioPlayer.loadSound(pad.id, it) }
                }
                _state.update { it.copy(pads = pads) }
            }
        }
    }

    fun onEvent(event: SamplerEvent) {
        when (event) {
            is SamplerEvent.PadTapped -> playPad(event.padId)
            is SamplerEvent.FileSelected -> assignSound(event.padId, event.uri)
            is SamplerEvent.CategoryFilterSelected ->
                _state.update { it.copy(selectedFilter = event.category) }
            is SamplerEvent.CategoryAssigned -> setCategoryForPad(event.padId, event.category)
            SamplerEvent.DismissCategoryDialog ->
                _state.update { it.copy(showCategoryDialogForPad = null) }
            SamplerEvent.StopAll -> {
                audioPlayer.stopAll()
                _state.update { it.copy(activePadName = null, isPlaying = false) }
            }
            SamplerEvent.ClearMessage ->
                _state.update { it.copy(message = null) }
        }
    }

    private fun playPad(padId: Int) {
        val pad = _state.value.pads.find { it.id == padId } ?: return
        if (!pad.hasSound) {
            _state.update { it.copy(message = "Mantén presionado para asignar un sonido.") }
            return
        }
        audioPlayer.play(padId)
        _state.update { it.copy(activePadName = pad.name, isPlaying = true) }
    }

    private fun assignSound(padId: Int, uri: Uri) {
        viewModelScope.launch {
            if (!fileManager.isValidAudioFormat(uri)) {
                _state.update { it.copy(message = "Formato no soportado. Solo .wav y .mp3.") }
                return@launch
            }

            val result = fileManager.copyFileToInternal(uri)
            if (result.isFailure) {
                _state.update { it.copy(message = result.exceptionOrNull()?.message) }
                return@launch
            }

            val filePath = result.getOrThrow()
            val fileName = fileManager.getFileName(uri).substringBeforeLast(".")

            val pad = repo.getPadById(padId) ?: return@launch

            // Borrar archivo anterior si existe
            pad.filePath?.let {
                fileManager.deleteFile(it)
                audioPlayer.unload(padId)
            }

            val updated = pad.copy(name = fileName, filePath = filePath)
            repo.updatePad(updated)
            audioPlayer.loadSound(padId, filePath)

            _state.update {
                it.copy(
                    storageUsedBytes = fileManager.getStorageUsedBytes(),
                    showCategoryDialogForPad = padId,
                )
            }
        }
    }

    private fun setCategoryForPad(padId: Int, category: PadCategory) {
        viewModelScope.launch {
            val pad = repo.getPadById(padId) ?: return@launch
            repo.updatePad(pad.copy(category = category))
            _state.update { it.copy(showCategoryDialogForPad = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
