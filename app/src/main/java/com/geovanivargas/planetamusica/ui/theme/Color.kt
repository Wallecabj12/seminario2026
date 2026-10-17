package com.geovanivargas.planetamusica.ui.theme

import androidx.compose.ui.graphics.Color
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

// Primary – cyan/teal for "sound wave" feel
val Primary = Color(0xFF00E5FF)
val OnPrimary = Color(0xFF003640)
val PrimaryContainer = Color(0xFF004D5E)
val OnPrimaryContainer = Color(0xFF97F0FF)

// Secondary – warm orange accent
val Secondary = Color(0xFFFFB74D)
val OnSecondary = Color(0xFF422B00)
val SecondaryContainer = Color(0xFF5F3F00)
val OnSecondaryContainer = Color(0xFFFFDDB3)

// Background / Surface
val Background = Color(0xFF0E1114)
val OnBackground = Color(0xFFE2E2E6)
val Surface = Color(0xFF1A1C20)
val OnSurface = Color(0xFFE2E2E6)
val SurfaceVariant = Color(0xFF262930)
val OnSurfaceVariant = Color(0xFFC4C6CF)

// Error
val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
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