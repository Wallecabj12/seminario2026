package com.geovanivargas.planetamusica.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawPanelBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import com.geovanivargas.planetamusica.ui.theme.DawPanelHeader

/**
 * Contenedor estilo panel de DAW de escritorio.
 * Borde fino de 1dp, esquinas de 4-6dp y barra superior de título.
 */
@Composable
fun DawPanel(
    title: String,
    modifier: Modifier = Modifier,
    headerActions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val panelShape = RoundedCornerShape(6.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(DawPanelBackground)
            .border(1.dp, DawPanelBorder, panelShape),
    ) {
        // Barra superior del panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DawPanelHeader)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Acento decorativo pequeño
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 12.dp)
                    .background(DawAccentOrange, RoundedCornerShape(1.dp)),
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )

            Spacer(Modifier.weight(1f))

            headerActions?.invoke()
        }

        // Línea divisoria de 1dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DawPanelBorder),
        )

        // Contenido del panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            content = content,
        )
    }
}
