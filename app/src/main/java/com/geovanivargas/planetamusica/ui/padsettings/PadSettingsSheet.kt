package com.geovanivargas.planetamusica.ui.padsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PadSettingsSheet(
    state: PadSettingsUiState,
    onEvent: (PadSettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible || state.pad == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(PadSettingsEvent.Dismiss) },
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Ajustes de ${state.pad.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            // Renombrar
            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(PadSettingsEvent.NameChanged(it)) },
                label = { Text("Nombre del pad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // Categoría
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PadCategory.assignable.forEach { cat ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick = { onEvent(PadSettingsEvent.CategoryChanged(cat)) },
                        label = { Text(cat.label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Volumen (0% - 100%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Volumen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.volumePercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = state.volume,
                onValueChange = { onEvent(PadSettingsEvent.VolumeChanged(it)) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Modo: One-shot / Loop
            Text(
                text = "Modo de reproducción",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !state.isLoop,
                    onClick = { onEvent(PadSettingsEvent.ModeChanged(false)) },
                    label = { Text("One-shot") },
                )
                FilterChip(
                    selected = state.isLoop,
                    onClick = { onEvent(PadSettingsEvent.ModeChanged(true)) },
                    label = { Text("Loop") },
                )
            }

            Spacer(Modifier.height(16.dp))

            // Velocidad (0.5x - 2.0x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Velocidad de reproducción",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%.1fx".format(state.rate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = state.rate,
                onValueChange = { onEvent(PadSettingsEvent.RateChanged(it)) },
                valueRange = 0.5f..2.0f,
                steps = 14,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // Acciones secundarias: Reasignar / Eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onEvent(PadSettingsEvent.ReassignFileClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reasignar")
                }
                if (state.pad.hasSound) {
                    OutlinedButton(
                        onClick = { onEvent(PadSettingsEvent.DeletePadClicked) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Eliminar")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Guardar cambios
            Button(
                onClick = { onEvent(PadSettingsEvent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
