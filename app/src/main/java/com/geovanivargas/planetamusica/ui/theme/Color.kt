package com.geovanivargas.planetamusica.ui.theme

import androidx.compose.ui.graphics.Color
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

// DAW Theme - Charcoal & Orange Accent
val DawDarkBackground = Color(0xFF1E2328)
val DawPanelBackground = Color(0xFF242A30)
val DawPanelHeader = Color(0xFF1A1F24)
val DawPanelBorder = Color(0xFF38404A)
val DawSurfaceVariant = Color(0xFF2B3138)
val DawLcdBackground = Color(0xFF121518)
val DawAccentOrange = Color(0xFFFF8A1F)
val DawLcdText = Color(0xFFFF8A1F)
val DawLedGreen = Color(0xFF00E676)
val DawLedRed = Color(0xFFFF3366)

// Material 3 Color Scheme
val Primary = DawAccentOrange
val OnPrimary = Color(0xFF000000)
val PrimaryContainer = Color(0xFF4A2800)
val OnPrimaryContainer = Color(0xFFFFDCBE)

val Secondary = Color(0xFFFFB74D)
val OnSecondary = Color(0xFF422B00)
val SecondaryContainer = Color(0xFF5F3F00)
val OnSecondaryContainer = Color(0xFFFFDDB3)

val Background = DawDarkBackground
val OnBackground = Color(0xFFE2E2E6)
val Surface = DawPanelBackground
val OnSurface = Color(0xFFE2E2E6)
val SurfaceVariant = DawSurfaceVariant
val OnSurfaceVariant = Color(0xFFB0B5BD)

val Error = Color(0xFFFF5252)
val OnError = Color(0xFF000000)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Colores de alto contraste por categoría estilo DAW
val CategoryPercussion = Color(0xFFFF3366) // Rojo vibrante / Rosa punchy
val CategoryBass = Color(0xFF9C27B0)       // Violeta / Púrpura profundo
val CategorySynth = Color(0xFF00E5FF)      // Cyan eléctrico
val CategoryFx = Color(0xFFFFD600)         // Amarillo neón
val CategoryVoices = Color(0xFF00E676)     // Verde brillante
val CategoryOther = Color(0xFF78909C)      // Azul grisáceo neutro

fun PadCategory.accentColor(): Color = when (this) {
    PadCategory.ALL -> Primary
    PadCategory.PERCUSSION -> CategoryPercussion
    PadCategory.BASS -> CategoryBass
    PadCategory.SYNTH -> CategorySynth
    PadCategory.FX -> CategoryFx
    PadCategory.VOICES -> CategoryVoices
    PadCategory.OTHER -> CategoryOther
}
