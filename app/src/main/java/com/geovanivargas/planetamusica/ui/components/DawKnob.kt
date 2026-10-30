package com.geovanivargas.planetamusica.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawDarkBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import kotlin.math.cos
import kotlin.math.sin

/**
 * Perilla circular estilo DAW de escritorio.
 * Arrastre vertical para ajustar el valor (0f..1f).
 * Muestra el valor en un tooltip flotante durante el arrastre.
 */
@Composable
fun DawKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    size: Dp = 36.dp,
    activeColor: Color = DawAccentOrange,
    trackColor: Color = DawDarkBackground,
    valueFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragAccumulator = value
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val sensitivity = 0.006f
                        val newValue = (dragAccumulator - dragAmount * sensitivity).coerceIn(0f, 1f)
                        dragAccumulator = newValue
                        onValueChange(newValue)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(size)) {
                    val strokeWidth = 3.5.dp.toPx()
                    val diameter = kotlin.math.min(this.size.width, this.size.height) - strokeWidth
                    val arcSize = Size(diameter, diameter)
                    val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    val startAngle = 135f
                    val sweepAngle = 270f

                    // 1. Pista de fondo del arco
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // 2. Arco activo
                    val activeSweep = sweepAngle * value.coerceIn(0f, 1f)
                    if (activeSweep > 0f) {
                        drawArc(
                            color = activeColor,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    // 3. Tapa central circular de la perilla
                    val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)
                    val innerRadius = (diameter / 2f) - strokeWidth * 1.2f

                    drawCircle(
                        color = Color(0xFF262C33),
                        radius = innerRadius,
                        center = centerOffset,
                    )
                    drawCircle(
                        color = DawPanelBorder,
                        radius = innerRadius,
                        center = centerOffset,
                        style = Stroke(width = 1.dp.toPx()),
                    )

                    // 4. Aguja indicadora
                    val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
                    val indicatorInner = innerRadius * 0.35f
                    val indicatorOuter = innerRadius * 0.9f

                    val startPoint = Offset(
                        x = centerOffset.x + (indicatorInner * cos(currentAngleRad)).toFloat(),
                        y = centerOffset.y + (indicatorInner * sin(currentAngleRad)).toFloat(),
                    )
                    val endPoint = Offset(
                        x = centerOffset.x + (indicatorOuter * cos(currentAngleRad)).toFloat(),
                        y = centerOffset.y + (indicatorOuter * sin(currentAngleRad)).toFloat(),
                    )

                    drawLine(
                        color = if (isDragging) activeColor else Color.White,
                        start = startPoint,
                        end = endPoint,
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                // Tooltip flotante mientras se arrastra
                if (isDragging) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-size - 10.dp))
                            .background(Color(0xFF14181B), RoundedCornerShape(4.dp))
                            .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = valueFormatter(value),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DawAccentOrange,
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }
            }

            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
