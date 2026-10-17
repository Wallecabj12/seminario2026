package com.geovanivargas.planetamusica.ui.sequencer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.ui.theme.Primary
import com.geovanivargas.planetamusica.ui.theme.accentColor

@Composable
fun SequencerScreen(
    viewModel: SequencerViewModel = viewModel(),
    showTransportBar: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (showTransportBar) {
            // Barra de transporte superior
            TransportBar(
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                currentStep = state.currentStep,
                onPlayToggle = { viewModel.onEvent(SequencerEvent.PlayToggle) },
                onStop = { viewModel.onEvent(SequencerEvent.Stop) },
                onBpmChange = { viewModel.onEvent(SequencerEvent.BpmChanged(it)) },
            )
            Spacer(Modifier.height(8.dp))
        }

        if (state.rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No hay pads con sonido configurados.\nAsigna sonidos en la pestaña Pads.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            // Contenedor scrolleable con cabecera de compás y filas de pads
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            ) {
                // Cabecera con números de pasos (1 al 16) sincronizada con el scroll horizontal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Espacio para la columna del nombre/controles de pad
                    Spacer(Modifier.width(130.dp))

                    for (step in 0 until 16) {
                        val isQuarterBeat = step % 4 == 0
                        val isCursorHere = state.isPlaying && state.currentStep == step

                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(22.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCursorHere -> Primary
                                        isQuarterBeat -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color.Transparent
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${step + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = if (isQuarterBeat || isCursorHere) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCursorHere) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }

                // Lista de filas de los pads con scroll vertical
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(state.rows, key = { it.pad.id }) { row ->
                        SequencerRow(
                            row = row,
                            currentStep = state.currentStep,
                            isPlaying = state.isPlaying,
                            horizontalScrollState = horizontalScrollState,
                            onStepToggle = { step ->
                                viewModel.onEvent(SequencerEvent.StepToggled(row.pad.id, step))
                            },
                            onMuteToggle = {
                                viewModel.onEvent(SequencerEvent.MuteToggled(row.pad.id))
                            },
                            onVolumeChange = { vol ->
                                viewModel.onEvent(SequencerEvent.VolumeChanged(row.pad.id, vol))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SequencerRow(
    row: SequencerPadRow,
    currentStep: Int,
    isPlaying: Boolean,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onStepToggle: (Int) -> Unit,
    onMuteToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pad = row.pad
    val pattern = row.pattern
    val accent = pad.category.accentColor()

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(vertical = 4.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Controles de Pista / Pad
            Row(
                modifier = Modifier
                    .width(130.dp)
                    .padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Indicador de color de categoría
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent),
                )

                Spacer(Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pad.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Slider compacto de volumen
                    Slider(
                        value = pattern.volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Botón Mute
                FilledIconToggleButton(
                    checked = pattern.isMuted,
                    onCheckedChange = { onMuteToggle() },
                    modifier = Modifier.size(26.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        checkedContainerColor = MaterialTheme.colorScheme.error,
                        checkedContentColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            // 16 celdas de pasos
            for (stepIndex in 0 until 16) {
                val isStepActive = pattern.steps.getOrElse(stepIndex) { false }
                val isCursorOnThisStep = isPlaying && currentStep == stepIndex

                StepCell(
                    isActive = isStepActive,
                    isCursor = isCursorOnThisStep,
                    accentColor = accent,
                    onClick = { onStepToggle(stepIndex) },
                )
            }
        }
    }
}

@Composable
private fun StepCell(
    isActive: Boolean,
    isCursor: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellShape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .size(width = 30.dp, height = 36.dp)
            .clip(cellShape)
            .background(
                if (isActive) accentColor else MaterialTheme.colorScheme.surfaceVariant,
            )
            .border(
                border = when {
                    isCursor -> BorderStroke(2.dp, Color.White)
                    isActive -> BorderStroke(1.dp, accentColor)
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                },
                shape = cellShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
            )
        }
    }
}
