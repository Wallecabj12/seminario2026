package com.geovanivargas.planetamusica

import com.geovanivargas.planetamusica.domain.sampler.Pad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioSettingsValidationTest {

    @Test
    fun volume_clampingAndPercentage_validation() {
        // Validación de rango 0.0f - 1.0f
        val normalVolume = 0.8f.coerceIn(0f, 1f)
        assertEquals(0.8f, normalVolume, 0.001f)

        val negativeVolume = (-0.5f).coerceIn(0f, 1f)
        assertEquals(0.0f, negativeVolume, 0.001f)

        val excessiveVolume = 1.75f.coerceIn(0f, 1f)
        assertEquals(1.0f, excessiveVolume, 0.001f)

        // Verificación de conversión a porcentaje (0% - 100%)
        val percent = (normalVolume * 100).toInt()
        assertEquals(80, percent)
    }

    @Test
    fun playbackRate_clamping_validation() {
        // Rango de velocidad permitido según SoundPool: 0.5x a 2.0x
        val normalRate = 1.25f.coerceIn(0.5f, 2.0f)
        assertEquals(1.25f, normalRate, 0.001f)

        val tooSlowRate = 0.2f.coerceIn(0.5f, 2.0f)
        assertEquals(0.5f, tooSlowRate, 0.001f)

        val tooFastRate = 3.5f.coerceIn(0.5f, 2.0f)
        assertEquals(2.0f, tooFastRate, 0.001f)
    }

    @Test
    fun pad_defaultSettings_integrity() {
        val pad = Pad(id = 1, name = "Kick", position = 0)
        assertEquals(1.0f, pad.volume, 0.001f)
        assertFalse(pad.isLoop)
        assertEquals(1.0f, pad.rate, 0.001f)
        assertFalse(pad.hasSound)

        val configuredPad = pad.copy(
            filePath = "/data/sounds/kick.wav",
            volume = 0.75f,
            isLoop = true,
            rate = 1.5f,
        )
        assertTrue(configuredPad.hasSound)
        assertTrue(configuredPad.isLoop)
        assertEquals(0.75f, configuredPad.volume, 0.001f)
        assertEquals(1.5f, configuredPad.rate, 0.001f)
    }
}
