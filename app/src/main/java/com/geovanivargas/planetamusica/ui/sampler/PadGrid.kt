package com.geovanivargas.planetamusica.ui.sampler

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.ui.theme.accentColor
import kotlinx.coroutines.launch

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
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pads, key = { it.id }) { pad ->
            val isActive = pad.hasSound && pad.name == activePadName
            PadItem(
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
private fun PadItem(
    pad: Pad,
    isActive: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onPlusClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1.0f) }

    val accent = pad.category.accentColor()
    val targetBgColor = when {
        isActive -> accent
        pad.hasSound -> accent.copy(alpha = 0.32f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 120),
        label = "padColor",
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            scale.animateTo(1.08f, tween(80))
            scale.animateTo(1.0f, tween(140))
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = if (pad.hasSound) {
            BorderStroke(if (isActive) 2.dp else 1.dp, accent)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        },
        colors = CardDefaults.cardColors(
            containerColor = animatedBgColor,
            contentColor = if (isActive) Color.Black else Color.White,
        ),
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale.value)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                        scale.animateTo(0.92f, tween(60))
                        scale.animateTo(1.0f, tween(100))
                    }
                    onTap()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (pad.hasSound) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = pad.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = pad.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = (if (isActive) Color.Black else Color.White).copy(alpha = 0.8f),
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Text(
                        text = "Pad ${pad.position + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                        ),
                    )
                }
            }
        }
    }
}
