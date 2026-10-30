package com.geovanivargas.planetamusica.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.geovanivargas.planetamusica.ui.theme.DawLcdBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder

/**
 * Barra vertical medidora de nivel estilo DAW (LevelMeter).
 * Visualiza segmentos LED que cambian de verde a naranja/rojo en los niveles altos.
 */
@Composable
fun LevelMeter(
    level: Float,
    modifier: Modifier = Modifier,
    segments: Int = 10,
    width: Dp = 8.dp,
    height: Dp = 44.dp,
) {
    val clampedLevel = level.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(2.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(DawLcdBackground)
            .border(1.dp, DawPanelBorder, shape)
            .padding(1.5.dp),
    ) {
        Column(
            modifier = Modifier.matchParentSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            // Segmentos de arriba a abajo (los de arriba son los más altos/rojos)
            for (i in (segments - 1) downTo 0) {
                val threshold = i.toFloat() / segments
                val isActive = clampedLevel > threshold

                val segmentColor = when {
                    i >= segments * 0.85 -> Color(0xFFFF3366) // Rojo pico
                    i >= segments * 0.65 -> Color(0xFFFF8A1F) // Naranja advertencia
                    else -> Color(0xFF00E676)                // Verde normal
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (isActive) segmentColor else segmentColor.copy(alpha = 0.15f),
                        ),
                )
            }
        }
    }
}
