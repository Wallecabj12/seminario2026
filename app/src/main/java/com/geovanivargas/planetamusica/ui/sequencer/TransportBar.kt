package com.geovanivargas.planetamusica.ui.sequencer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.ui.theme.Primary

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Botones grandes Play / Stop
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onPlayToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) Color(0xFF00E676) else Primary,
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            text = if (isPlaying) "⏸ PAUSA" else "▶ PLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text("⏹ STOP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Indicador de Compás / Beat
                Column(horizontalAlignment = Alignment.End) {
                    val bar = (currentStep / 4) + 1
                    val beat = (currentStep % 4) + 1
                    Text(
                        text = "COMPÁS $bar : $beat",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontSize = 13.sp,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    // 4 LEDs de beat
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0 until 4) {
                            val isCurrentBeat = (currentStep / 4) == 0 && (currentStep % 4) == i ||
                                (currentStep >= 4 && (currentStep % 4) == i)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPlaying && isCurrentBeat) Primary else Color.DarkGray,
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Control de BPM: botones +/- y Slider (60 a 200)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "BPM: $bpm",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                    ),
                    modifier = Modifier.width(68.dp),
                )

                FilledIconButton(
                    onClick = { if (bpm > 60) onBpmChange(bpm - 1) },
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { onBpmChange(it.toInt()) },
                    valueRange = 60f..200f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                )

                FilledIconButton(
                    onClick = { if (bpm < 200) onBpmChange(bpm + 1) },
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
