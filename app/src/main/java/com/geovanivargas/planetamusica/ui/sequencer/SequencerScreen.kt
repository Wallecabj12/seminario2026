package com.geovanivargas.planetamusica.ui.sequencer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.geovanivargas.planetamusica.ui.components.DawButton
import com.geovanivargas.planetamusica.ui.components.DawKnob
import com.geovanivargas.planetamusica.ui.components.DawPanel
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawDarkBackground
import com.geovanivargas.planetamusica.ui.theme.DawLedGreen
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import com.geovanivargas.planetamusica.ui.theme.accentColor

/**
 * Pantalla Secuencia inspirada en el Channel Rack de FL Studio.
 * Cada fila contiene control de mute con LED, nombre del canal, perilla DawKnob
 * y 16 pasos agrupados de a 4 con tonos alternados y cursor de reproducción resaltado.
 */
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
            .background(DawDarkBackground),
    ) {
        if (showTransportBar) {
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

        DawPanel(
            title = "Channel Rack - 16 Steps",
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Cabecera de compás / pasos sincronizada con el scroll horizontal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Espacio reservado para los controles de canal (mute + knob + nombre)
                        Spacer(Modifier.width(180.dp))

                        for (step in 0 until 16) {
                            val beatGroup = step / 4
                            val isFirstOfBeat = step % 4 == 0
                            val isCursorHere = state.isPlaying && state.currentStep == step

                            val headerBg = when {
                                isCursorHere -> DawAccentOrange
                                beatGroup % 2 == 0 -> Color(0xFF191D22)
                                else -> Color(0xFF222830)
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(34.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(headerBg)
                                    .border(
                                        1.dp,
                                        if (isCursorHere) Color.White else DawPanelBorder,
                                        RoundedCornerShape(3.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${step + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = if (isFirstOfBeat || isCursorHere) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCursorHere) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }

                    // Lista de canales del rack con scroll vertical
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(state.rows, key = { it.pad.id }) { row ->
                            ChannelRackRow(
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
}

/**
 * Fila individual del Channel Rack al estilo FL Studio.
 */
@Composable
private fun ChannelRackRow(
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
    val rowShape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(Color(0xFF22272E))
            .border(1.dp, DawPanelBorder, rowShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(vertical = 3.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Controles de canal: Mute LED + Indicador de Categoría + Nombre + DawKnob
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Botón LED de Mute
                DawButton(
                    checked = !pattern.isMuted,
                    onCheckedChange = { onMuteToggle() },
                    text = if (pattern.isMuted) "M" else "ON",
                    ledColor = DawLedGreen,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                )

                Spacer(Modifier.width(4.dp))

                // Tag vertical de color de categoría
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )

                Spacer(Modifier.width(6.dp))

                // Nombre del pad/canal
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 2.dp),
                ) {
                    Text(
                        text = pad.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = pad.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = accent,
                        ),
                        maxLines = 1,
                    )
                }

                // Perilla DawKnob de volumen
                DawKnob(
                    value = pattern.volume,
                    onValueChange = onVolumeChange,
                    size = 30.dp,
                    label = "VOL",
                    activeColor = DawAccentOrange,
                )
            }

            // 16 Pasos rectangulares agrupados de a 4 con colores alternos
            for (stepIndex in 0 until 16) {
                val isStepActive = pattern.steps.getOrElse(stepIndex) { false }
                val isCursorOnThisStep = isPlaying && currentStep == stepIndex
                val beatGroup = stepIndex / 4

                // Tonalidad alterna estilo FL Studio
                val inactiveCellColor = if (beatGroup % 2 == 0) Color(0xFF2B323A) else Color(0xFF38414B)

                ChannelStepCell(
                    isActive = isStepActive,
                    isCursor = isCursorOnThisStep,
                    activeColor = accent,
                    inactiveColor = inactiveCellColor,
                    onClick = { onStepToggle(stepIndex) },
                )
            }
        }
    }
}

/**
 * Celda rectangular de paso para el Channel Rack.
 * Con borde luminoso y punto indicador LED cuando está activa.
 */
@Composable
private fun ChannelStepCell(
    isActive: Boolean,
    isCursor: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellShape = RoundedCornerShape(3.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .sizeIn(minWidth = 34.dp, minHeight = 48.dp)
            .width(34.dp)
            .height(48.dp)
            .clip(cellShape)
            .background(if (isActive) activeColor.copy(alpha = 0.88f) else inactiveColor)
            .border(
                width = if (isCursor) 2.dp else 1.dp,
                color = when {
                    isCursor -> Color.White
                    isActive -> activeColor
                    else -> DawPanelBorder
                },
                shape = cellShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Indicador central LED cuando el paso está encendido
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.9f)),
            )
        } else {
            // Hendidura sutil de botón apagado
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 8.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.Black.copy(alpha = 0.25f)),
            )
        }
    }
}
