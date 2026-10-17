package com.geovanivargas.planetamusica.ui.presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.app.Application
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    onNavigateBack: () -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: PresetsViewModel = viewModel(
        factory = PresetsViewModel.provideFactory(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.status) {
        when (val status = state.status) {
            is PresetsStatus.Success -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.onEvent(PresetsEvent.ClearStatus)
            }
            is PresetsStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.onEvent(PresetsEvent.ClearStatus)
            }
            else -> Unit
        }
    }

    // Dialog Guardar Nuevo Preset
    if (state.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PresetsEvent.DismissSaveDialog) },
            title = { Text("Guardar preset actual") },
            text = {
                OutlinedTextField(
                    value = state.newPresetName,
                    onValueChange = { viewModel.onEvent(PresetsEvent.PresetNameInputChanged(it)) },
                    label = { Text("Nombre del preset") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(PresetsEvent.ConfirmSavePreset) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PresetsEvent.DismissSaveDialog) }) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Dialog Confirmar Carga de Preset
    state.presetToLoad?.let { preset ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PresetsEvent.DismissLoadDialog) },
            title = { Text("Cargar preset") },
            text = {
                Text("¿Deseas cargar el preset '${preset.name}'? Se reemplazarán los pads actuales.")
            },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(PresetsEvent.ConfirmLoadPreset) }) {
                    Text("Cargar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PresetsEvent.DismissLoadDialog) }) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Dialog Renombrar Preset
    state.presetToRename?.let { _ ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PresetsEvent.DismissRenameDialog) },
            title = { Text("Renombrar preset") },
            text = {
                OutlinedTextField(
                    value = state.renameText,
                    onValueChange = { viewModel.onEvent(PresetsEvent.RenameInputChanged(it)) },
                    label = { Text("Nuevo nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(PresetsEvent.ConfirmRenamePreset) }) {
                    Text("Renombrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PresetsEvent.DismissRenameDialog) }) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Dialog Confirmar Eliminación
    state.presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PresetsEvent.DismissDeleteDialog) },
            title = { Text("Eliminar preset") },
            text = {
                Text("¿Estás seguro de que deseas eliminar el preset '${preset.name}'?")
            },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(PresetsEvent.ConfirmDeletePreset) }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PresetsEvent.DismissDeleteDialog) }) {
                    Text("Cancelar")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Presets") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_btn_revert),
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(PresetsEvent.OpenSaveDialog) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_btn_add),
                                contentDescription = "Guardar preset actual",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (!showTopBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Gestión de Presets",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Button(
                        onClick = { viewModel.onEvent(PresetsEvent.OpenSaveDialog) },
                    ) {
                        Text("+ Guardar actual")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                if (state.status is PresetsStatus.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (state.presets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "No tienes presets guardados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Guarda la configuración actual de los 16 pads presionando el botón '+' en la esquina superior.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onEvent(PresetsEvent.OpenSaveDialog) },
                        modifier = Modifier.height(56.dp),
                    ) {
                        Text("Guardar preset actual")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.presets, key = { it.id }) { preset ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(if (preset.isLocal) "Local" else "Cuenta")
                                        },
                                    )
                                }
                                Text(
                                    text = "${preset.pads.count { it.hasSound }} pads configurados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { viewModel.onEvent(PresetsEvent.RequestRenamePreset(preset)) },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_btn_edit),
                                            contentDescription = "Renombrar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.onEvent(PresetsEvent.RequestDeletePreset(preset)) },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_btn_delete),
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.onEvent(PresetsEvent.RequestLoadPreset(preset)) },
                                    ) {
                                        Text("Cargar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
