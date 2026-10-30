package com.geovanivargas.planetamusica.ui.sequencer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.ui.components.LevelMeter
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawLcdBackground
import com.geovanivargas.planetamusica.ui.theme.DawLcdText
import com.geovanivargas.planetamusica.ui.theme.DawLedGreen
import com.geovanivargas.planetamusica.ui.theme.DawLedRed
import com.geovanivargas.planetamusica.ui.theme.DawPanelBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder

/**
 * Barra de transporte inspirada en estaciones de trabajo de audio (DAW) de escritorio.
 * Incluye controles Play, Stop y Rec (decorativo), display LCD para BPM y contador de compás.
 */
@Composable
fun TransportBar(
    isPlaying: Boolean,
    bpm: Int,
    currentStep: Int,
    onPlayToggle: () -> Unit,
    onStop: () -> Unit,
    onBpmChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRecActive by remember { mutableStateOf(false) }

    val barShape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(DawPanelBackground)
            .border(1.dp, DawPanelBorder, barShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Controles de transporte: Play, Stop, Rec
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Botón PLAY
                TransportButton(
                    active = isPlaying,
                    activeColor = DawLedGreen,
                    label = "▶",
                    subtext = "PLAY",
                    onClick = onPlayToggle,
                )

                // Botón STOP
                TransportButton(
                    active = !isPlaying,
                    activeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "⏹",
                    subtext = "STOP",
                    onClick = onStop,
                )

                // Botón REC decorativo
                TransportButton(
                    active = isRecActive,
                    activeColor = DawLedRed,
                    label = "●",
                    subtext = "REC",
                    onClick = { isRecActive = !isRecActive },
                )
            }

            // Display LCD: Compás y Beats
            val barNumber = (currentStep / 4) + 1
            val beatNumber = (currentStep % 4) + 1

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // LCD Compás
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DawLcdBackground)
                        .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%02d : %02d".format(barNumber, beatNumber),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DawLcdText,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                            ),
                        )
                        Spacer(Modifier.height(3.dp))
                        // 4 LEDs de beat
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until 4) {
                                val isCurrent = (currentStep % 4) == i
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPlaying && isCurrent) DawAccentOrange
                                            else DawAccentOrange.copy(alpha = 0.2f),
                                        ),
                                )
                            }
                        }
                    }
                }

                // Level meter decorativo master
                LevelMeter(
                    level = if (isPlaying) 0.65f + ((currentStep % 4) * 0.08f) else 0f,
                    height = 36.dp,
                    width = 6.dp,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Display LCD del BPM con botones de ajuste fino (+/-)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(DawLcdBackground)
                .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TEMPO / BPM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                    ),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "%03d".format(bpm),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DawLcdText,
                        fontSize = 15.sp,
                    ),
                )
            }

            // Botones +/- con objetivo táctil >= 48dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DawStepButton(
                    text = "-10",
                    onClick = { onBpmChange((bpm - 10).coerceAtLeast(60)) },
                )
                DawStepButton(
                    text = "-",
                    onClick = { if (bpm > 60) onBpmChange(bpm - 1) },
                )
                DawStepButton(
                    text = "+",
                    onClick = { if (bpm < 200) onBpmChange(bpm + 1) },
                )
                DawStepButton(
                    text = "+10",
                    onClick = { onBpmChange((bpm + 10).coerceAtMost(200)) },
                )
            }
        }
    }
}

@Composable
private fun TransportButton(
    active: Boolean,
    activeColor: Color,
    label: String,
    subtext: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 50.dp, minHeight = 48.dp)
            .clip(shape)
            .background(if (active) Color(0xFF2E353D) else Color(0xFF1E2328))
            .border(
                1.dp,
                if (active) activeColor.copy(alpha = 0.8f) else DawPanelBorder,
                shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = subtext,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun DawStepButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(3.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(Color(0xFF262C33))
            .border(1.dp, DawPanelBorder, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
            ),
        )
    }
}
