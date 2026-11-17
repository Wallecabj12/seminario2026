package com.geovanivargas.planetamusica.ui.padsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import com.geovanivargas.planetamusica.ui.components.DawPanel
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawLcdBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import com.geovanivargas.planetamusica.ui.theme.DawSurfaceVariant

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
                text = "Ajustes de ${state.pad.name} (Pad ${state.pad.position + 1})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
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

            // ── Acciones de Sonido e Integración Synth ──
            Text(
                text = "Asignación de sonido",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        if (state.isSynthCreatorVisible) {
                            onEvent(PadSettingsEvent.CloseSynthCreatorClicked)
                        } else {
                            onEvent(PadSettingsEvent.OpenSynthCreatorClicked)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isSynthCreatorVisible) DawSurfaceVariant else DawAccentOrange,
                        contentColor = if (state.isSynthCreatorVisible) DawAccentOrange else Color.Black,
                    ),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = if (state.isSynthCreatorVisible) "Ocultar Synth" else "Sintetizar sonido",
                        fontWeight = FontWeight.Bold,
                    )
                }

                OutlinedButton(
                    onClick = { onEvent(PadSettingsEvent.ReassignFileClicked) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("Importar")
                }

                if (state.pad.hasSound) {
                    OutlinedButton(
                        onClick = { onEvent(PadSettingsEvent.DeletePadClicked) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text("Eliminar")
                    }
                }
            }

            // Panel desplegable de creación rápida con Synth
            if (state.isSynthCreatorVisible) {
                Spacer(Modifier.height(12.dp))
                QuickSynthCreatorSection(state = state, onEvent = onEvent)
            }

            Spacer(Modifier.height(20.dp))

            // Guardar cambios generales
            Button(
                onClick = { onEvent(PadSettingsEvent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text("Guardar ajustes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickSynthCreatorSection(
    state: PadSettingsUiState,
    onEvent: (PadSettingsEvent) -> Unit,
) {
    val presets = listOf("Bass", "Lead", "Pluck", "Pad", "FX")
    val quickNotes = listOf(
        36 to "C2", 40 to "E2", 43 to "G2", 45 to "A2",
        48 to "C3", 51 to "D#3", 55 to "G3", 58 to "A#3",
        60 to "C4", 64 to "E4", 67 to "G4", 72 to "C5",
    )

    fun midiToName(note: Int): String {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val oct = (note / 12) - 1
        return "${names[note % 12]}$oct"
    }

    DawPanel(title = "SINTETIZAR INSTRUMENTO PARA ESTE PAD") {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Presets
            Text("Instrumento de síntesis:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { preset ->
                    val isSelected = state.selectedSynthPreset == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) DawAccentOrange.copy(alpha = 0.25f) else DawSurfaceVariant)
                            .border(1.dp, if (isSelected) DawAccentOrange else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onEvent(PadSettingsEvent.SynthPresetSelected(preset)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = preset.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DawAccentOrange else MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }

            // Nota Musical
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Nota musical:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${midiToName(state.selectedMidiNote)} (MIDI ${state.selectedMidiNote})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DawAccentOrange,
                    ),
                )
            }

            // Chips de notas rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                quickNotes.forEach { (note, label) ->
                    val isSel = state.selectedMidiNote == note
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) DawAccentOrange else DawLcdBackground)
                            .border(1.dp, if (isSel) DawAccentOrange else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onEvent(PadSettingsEvent.SynthNoteChanged(note)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else DawAccentOrange,
                            ),
                        )
                    }
                }
            }

            // Slider nota
            Slider(
                value = state.selectedMidiNote.toFloat(),
                onValueChange = { onEvent(PadSettingsEvent.SynthNoteChanged(it.toInt())) },
                valueRange = 24f..84f,
                steps = 60,
                colors = SliderDefaults.colors(thumbColor = DawAccentOrange, activeTrackColor = DawAccentOrange),
            )

            // Duración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Duración:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f seg".format(state.synthDurationSec), style = MaterialTheme.typography.labelSmall, color = DawAccentOrange)
            }
            Slider(
                value = state.synthDurationSec,
                onValueChange = { onEvent(PadSettingsEvent.SynthDurationChanged(it)) },
                valueRange = 0.2f..3.0f,
                colors = SliderDefaults.colors(thumbColor = DawAccentOrange, activeTrackColor = DawAccentOrange),
            )

            // Botones de acción del Synth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onEvent(PadSettingsEvent.PreviewSynthSound) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Preescuchar")
                }

                Button(
                    onClick = { onEvent(PadSettingsEvent.ApplySynthToPadClicked) },
                    colors = ButtonDefaults.buttonColors(containerColor = DawAccentOrange),
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Asignar a este Pad", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            TextButton(
                onClick = { onEvent(PadSettingsEvent.OpenFullSynthClicked) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Diseñar en Synth completo ↗", color = DawAccentOrange, fontSize = 12.sp)
            }
        }
    }
}
