package com.geovanivargas.planetamusica.ui.sampler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.data.sampler.FileManager
import com.geovanivargas.planetamusica.ui.padsettings.PadSettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplerScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToPresets: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: SamplerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingPadId by remember { mutableIntStateOf(-1) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null && pendingPadId > 0) {
            viewModel.onEvent(SamplerEvent.FileSelected(pendingPadId, uri))
        }
        pendingPadId = -1
    }

    LaunchedEffect(state.requestFilePickerForPadId) {
        state.requestFilePickerForPadId?.let { padId ->
            pendingPadId = padId
            viewModel.onEvent(SamplerEvent.FilePickerConsumed)
            filePicker.launch(arrayOf("audio/mpeg", "audio/wav", "audio/x-wav"))
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SamplerEvent.ClearMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("SoundCraft") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToPresets) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_save),
                                contentDescription = "Presets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = onNavigateToLogin) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_myplaces),
                                contentDescription = "Iniciar sesión",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            PlaybackBar(
                activePadName = state.activePadName,
                isPlaying = state.isPlaying,
                activeLoopsCount = state.activeLoopsCount,
                onStopAll = { viewModel.onEvent(SamplerEvent.StopAll) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategoryChips(
                selected = state.selectedFilter,
                onSelected = { viewModel.onEvent(SamplerEvent.CategoryFilterSelected(it)) },
            )

            LinearProgressIndicator(
                progress = {
                    (state.storageUsedBytes.toFloat() / FileManager.MAX_TOTAL_SIZE_BYTES).coerceIn(0f, 1f)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = if (state.storageUsedBytes > FileManager.MAX_TOTAL_SIZE_BYTES * 0.8)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = state.storageText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(8.dp))

            PadGrid(
                pads = state.filteredPads,
                activePadName = state.activePadName,
                onPadTap = { viewModel.onEvent(SamplerEvent.PadTapped(it)) },
                onPadLongPress = { padId ->
                    viewModel.onEvent(SamplerEvent.PadLongPressed(padId))
                },
                onImportSoundToPad = { padId ->
                    viewModel.onEvent(SamplerEvent.FilePickerRequested(padId))
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    PadSettingsSheet(
        state = state.padSettingsState,
        onEvent = { viewModel.onEvent(SamplerEvent.PadSettingsEventReceived(it)) },
    )
}
