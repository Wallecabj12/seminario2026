package com.geovanivargas.planetamusica.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geovanivargas.planetamusica.ui.theme.DawLedGreen
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder

/**
 * Botón estilo LED para DAW (encendido / apagado).
 * Cuenta con luz LED indicadora brillante al activarse y atenuada al apagarse.
 * Asegura objetivo táctil mínimo de 48dp.
 */
@Composable
fun DawButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    ledColor: Color = DawLedGreen,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(4.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(if (checked) Color(0xFF2E353D) else Color(0xFF1E2328))
            .border(
                width = 1.dp,
                color = if (checked) ledColor.copy(alpha = 0.6f) else DawPanelBorder,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onCheckedChange(!checked) },
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Lámpara LED
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) ledColor else ledColor.copy(alpha = 0.15f),
                    )
                    .border(
                        0.5.dp,
                        if (checked) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                        CircleShape,
                    ),
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (checked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
