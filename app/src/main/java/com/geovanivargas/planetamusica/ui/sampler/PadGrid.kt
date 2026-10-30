package com.geovanivargas.planetamusica.ui.sampler

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import com.geovanivargas.planetamusica.ui.theme.accentColor
import kotlinx.coroutines.launch

/**
 * Grid de 16 pads 4x4 estilo MPC.
 * Cuenta con bordes luminosos de alto contraste al tocar, animación de escala,
 * esquinas de 6dp y objetivos táctiles amplios (>= 48dp).
 */
@Composable
fun PadGrid(
    pads: List<Pad>,
    activePadName: String?,
    onPadTap: (Int) -> Unit,
    onPadLongPress: (Int) -> Unit,
    onImportSoundToPad: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(pads, key = { it.id }) { pad ->
            val isActive = pad.hasSound && pad.name == activePadName
            MpcPadItem(
                pad = pad,
                isActive = isActive,
                onTap = { onPadTap(pad.id) },
                onLongPress = { onPadLongPress(pad.id) },
                onPlusClick = { onImportSoundToPad(pad.id) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MpcPadItem(
    pad: Pad,
    isActive: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onPlusClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1.0f) }
    val glowAnim = remember { Animatable(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    val accent = pad.category.accentColor()
    val padShape = RoundedCornerShape(6.dp)

    val targetBgColor = when {
        isActive -> accent.copy(alpha = 0.55f)
        pad.hasSound -> Color(0xFF242A32)
        else -> Color(0xFF1B1F24)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 100),
        label = "mpcPadColor",
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            scale.animateTo(1.05f, tween(60))
            scale.animateTo(1.0f, tween(120))
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .sizeIn(minWidth = 56.dp, minHeight = 56.dp)
            .scale(scale.value)
            .clip(padShape)
            .background(animatedBgColor)
            .border(
                BorderStroke(
                    width = if (isActive || glowAnim.value > 0.05f) 2.5.dp else 1.dp,
                    color = when {
                        isActive -> Color.White
                        glowAnim.value > 0.05f -> accent
                        pad.hasSound -> accent.copy(alpha = 0.5f)
                        else -> DawPanelBorder
                    },
                ),
                shape = padShape,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                        glowAnim.snapTo(1f)
                        scale.animateTo(0.94f, tween(50))
                        scale.animateTo(1.0f, tween(90))
                        glowAnim.animateTo(0f, tween(250))
                    }
                    if (pad.hasSound) onTap() else onPlusClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            )
            .padding(5.dp),
    ) {
        if (pad.hasSound) {
            // Número de pad estilo MPC en la esquina superior izquierda
            Text(
                text = "%02d".format(pad.position + 1),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                ),
                modifier = Modifier.align(Alignment.TopStart),
            )

            // Indicador de color de categoría en la esquina superior derecha
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .align(Alignment.TopEnd),
            )

            // Contenido central: Nombre del sonido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = pad.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = pad.category.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                    ),
                )
            }
        } else {
            // Pad vacío
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    ),
                )
                Text(
                    text = "PAD %02d".format(pad.position + 1),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                    ),
                )
            }
        }
    }
}
