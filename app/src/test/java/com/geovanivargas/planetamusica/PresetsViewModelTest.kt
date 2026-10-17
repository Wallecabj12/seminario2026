package com.geovanivargas.planetamusica

import app.cash.turbine.test
import com.geovanivargas.planetamusica.domain.AuthRepository
import com.geovanivargas.planetamusica.domain.AuthResult
import com.geovanivargas.planetamusica.domain.presets.DeletePresetUseCase
import com.geovanivargas.planetamusica.domain.presets.GetPresetsUseCase
import com.geovanivargas.planetamusica.domain.presets.LoadPresetUseCase
import com.geovanivargas.planetamusica.domain.presets.Preset
import com.geovanivargas.planetamusica.domain.presets.PresetRepository
import com.geovanivargas.planetamusica.domain.presets.RenamePresetUseCase
import com.geovanivargas.planetamusica.domain.presets.SavePresetUseCase
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.SamplerRepository
import com.geovanivargas.planetamusica.ui.presets.PresetsEvent
import com.geovanivargas.planetamusica.ui.presets.PresetsStatus
import com.geovanivargas.planetamusica.ui.presets.PresetsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresetsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakePresetRepository: FakeTestPresetRepository
    private lateinit var fakeSamplerRepository: FakeTestSamplerRepository
    private lateinit var fakeAuthRepository: FakeTestAuthRepository

    private lateinit var viewModel: PresetsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakePresetRepository = FakeTestPresetRepository()
        fakeSamplerRepository = FakeTestSamplerRepository()
        fakeAuthRepository = FakeTestAuthRepository()

        viewModel = PresetsViewModel(
            getPresetsUseCase = GetPresetsUseCase(fakePresetRepository),
            savePresetUseCase = SavePresetUseCase(fakePresetRepository, fakeAuthRepository),
            loadPresetUseCase = LoadPresetUseCase(fakePresetRepository, fakeSamplerRepository),
            deletePresetUseCase = DeletePresetUseCase(fakePresetRepository),
            renamePresetUseCase = RenamePresetUseCase(fakePresetRepository),
            samplerRepository = fakeSamplerRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun savePreset_updatesStateToSuccess() = runTest(testDispatcher) {
        viewModel.state.test {
            // Estado inicial
            val initial = awaitItem()
            assertEquals(PresetsStatus.Idle, initial.status)

            viewModel.onEvent(PresetsEvent.OpenSaveDialog)
            val withDialogOpen = awaitItem()
            assertTrue(withDialogOpen.showSaveDialog)

            viewModel.onEvent(PresetsEvent.PresetNameInputChanged("Preset Rock"))
            val withName = awaitItem()
            assertEquals("Preset Rock", withName.newPresetName)

            viewModel.onEvent(PresetsEvent.ConfirmSavePreset)
            val loading = awaitItem()
            assertEquals(PresetsStatus.Loading, loading.status)

            val success = awaitItem()
            assertTrue(success.status is PresetsStatus.Success)
            assertEquals("Preset 'Preset Rock' guardado correctamente.", (success.status as PresetsStatus.Success).message)

            // Consumir la actualización reactiva de la lista de presets
            val withUpdatedList = awaitItem()
            assertEquals(1, withUpdatedList.presets.size)
            assertEquals("Preset Rock", withUpdatedList.presets.first().name)

            // Verificar persistencia en el repositorio
            assertEquals(1, fakePresetRepository.presetsList.size)
            assertEquals("Preset Rock", fakePresetRepository.presetsList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadPreset_restoresPadsAndUpdatesStateToSuccess() = runTest(testDispatcher) {
        val testPads = (0 until 16).map {
            Pad(id = it + 1, name = "Sonido $it", position = it)
        }
        val savedPreset = Preset(id = 10L, name = "Preset Electro", pads = testPads)
        fakePresetRepository.presetsList.add(savedPreset)

        viewModel.state.test {
            awaitItem() // Initial

            viewModel.onEvent(PresetsEvent.RequestLoadPreset(savedPreset))
            val withDialog = awaitItem()
            assertEquals(savedPreset, withDialog.presetToLoad)

            viewModel.onEvent(PresetsEvent.ConfirmLoadPreset)
            val loading = awaitItem()
            assertEquals(PresetsStatus.Loading, loading.status)

            val success = awaitItem()
            assertTrue(success.status is PresetsStatus.Success)
            assertEquals("Preset 'Preset Electro' cargado.", (success.status as PresetsStatus.Success).message)

            // Verificar que los pads del sampler fueron actualizados
            assertEquals(16, fakeSamplerRepository.padsList.size)
            assertEquals("Sonido 0", fakeSamplerRepository.padsList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

// ── Test Fakes ──────────────────────────────────────────

private class FakeTestPresetRepository : PresetRepository {
    val presetsList = mutableListOf<Preset>()
    private val flow = MutableStateFlow<List<Preset>>(emptyList())

    override fun getAllPresets(): Flow<List<Preset>> = flow.asStateFlow()

    override suspend fun getPresetById(id: Long): Preset? =
        presetsList.find { it.id == id }

    override suspend fun savePreset(name: String, userId: String?, pads: List<Pad>, patternData: String?): Long {
        val id = (presetsList.size + 1).toLong()
        val preset = Preset(id = id, name = name, userId = userId, pads = pads, patternData = patternData)
        presetsList.add(preset)
        flow.value = presetsList.toList()
        return id
    }

    override suspend fun renamePreset(id: Long, newName: String) {
        val index = presetsList.indexOfFirst { it.id == id }
        if (index != -1) {
            presetsList[index] = presetsList[index].copy(name = newName)
            flow.value = presetsList.toList()
        }
    }

    override suspend fun deletePreset(id: Long) {
        presetsList.removeAll { it.id == id }
        flow.value = presetsList.toList()
    }
}

private class FakeTestSamplerRepository : SamplerRepository {
    val padsList = mutableListOf<Pad>()
    private val flow = MutableStateFlow<List<Pad>>(emptyList())

    init {
        val defaultPads = (0 until 16).map { pos ->
            Pad(id = pos + 1, name = "Pad ${pos + 1}", position = pos)
        }
        padsList.addAll(defaultPads)
        flow.value = padsList.toList()
    }

    override fun getAllPads(): Flow<List<Pad>> = flow.asStateFlow()

    override suspend fun getPadById(id: Int): Pad? = padsList.find { it.id == id }

    override suspend fun updatePad(pad: Pad) {
        val index = padsList.indexOfFirst { it.id == pad.id }
        if (index != -1) {
            padsList[index] = pad
            flow.value = padsList.toList()
        }
    }

    override suspend fun updateAllPads(pads: List<Pad>) {
        padsList.clear()
        padsList.addAll(pads)
        flow.value = padsList.toList()
    }

    override suspend fun initializePads() = Unit
}

private class FakeTestAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): AuthResult = AuthResult.Success
    override suspend fun signUp(name: String, email: String, password: String): AuthResult = AuthResult.Success
    override suspend fun recoverPassword(email: String): AuthResult = AuthResult.Success
    override fun getCurrentUserId(): String? = null
}
