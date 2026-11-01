package com.geovanivargas.planetamusica

import com.geovanivargas.planetamusica.audio.synth.BiquadFilter
import com.geovanivargas.planetamusica.audio.synth.FilterParams
import com.geovanivargas.planetamusica.audio.synth.FilterType
import com.geovanivargas.planetamusica.audio.synth.SynthEngine
import com.geovanivargas.planetamusica.audio.synth.SynthParams
import com.geovanivargas.planetamusica.audio.synth.WaveShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class SynthEngineTest {

    @Test
    fun render_returnsCorrectDurationAndValidSampleRangeWithoutClipping() {
        val engine = SynthEngine()
        val durationMs = 500L
        val expectedMinSamples = ((durationMs / 1000.0) * SynthEngine.SAMPLE_RATE).toInt()

        val params = SynthParams()
        val samples = engine.render(params, midiNote = 60, durationMs = durationMs)

        // 1. Duración esperada (al menos la duración solicitada en muestras)
        assertTrue(
            "El tamaño de muestras (${samples.size}) debe ser al menos el tiempo solicitado ($expectedMinSamples)",
            samples.size >= expectedMinSamples,
        )

        // 2. Comprobar que no hay muestras vacías y que están dentro del rango 16-bit PCM sin desborde
        var hasNonZero = false
        var maxPeak = 0

        for (sample in samples) {
            val sInt = sample.toInt()
            assertTrue("Muestra no debe exceder 16-bit PCM", sInt in -32768..32767)
            if (kotlin.math.abs(sInt) > maxPeak) {
                maxPeak = kotlin.math.abs(sInt)
            }
            if (sInt != 0) {
                hasNonZero = true
            }
        }

        assertTrue("Debe contener audio renderizado (no silencioso)", hasNonZero)
        assertTrue("El pico no debe superar el límite suave 32767", maxPeak <= 32767)
    }

    @Test
    fun filter_cutoffAttenuatesHighFrequencies() {
        val sampleRate = 44100
        val highFreq = 4000.0 // Frecuencia de prueba alta (4 kHz)
        val numSamples = 1000

        // Generar una onda sinusoidal pura de 4 kHz
        val highFreqSignal = FloatArray(numSamples) { i ->
            sin(2.0 * PI * highFreq * i / sampleRate).toFloat()
        }

        // Señal 1: Filtrada con cutoff bajo (500 Hz) -> debe atenuarse drásticamente
        val samplesLowCutoff = highFreqSignal.clone()
        val filterLow = BiquadFilter()
        filterLow.processBlock(
            samples = samplesLowCutoff,
            type = FilterType.LOW_PASS,
            baseCutoff = 500f,
            resonance = 0.1f,
            drive = 0f,
            sampleRate = sampleRate,
        )

        // Señal 2: Filtrada con cutoff alto (12000 Hz) -> debe pasar casi intacta
        val samplesHighCutoff = highFreqSignal.clone()
        val filterHigh = BiquadFilter()
        filterHigh.processBlock(
            samples = samplesHighCutoff,
            type = FilterType.LOW_PASS,
            baseCutoff = 12000f,
            resonance = 0.1f,
            drive = 0f,
            sampleRate = sampleRate,
        )

        // Calcular RMS de ambas señales filtradas
        val rmsLow = sqrt(samplesLowCutoff.map { it * it }.average())
        val rmsHigh = sqrt(samplesHighCutoff.map { it * it }.average())

        // La señal con corte bajo debe tener sustancialmente menos energía que con corte alto
        assertTrue(
            "El filtro LP con cutoff bajo (500Hz) debe atenuar frecuencias altas más que cutoff alto (12kHz). RMS Low: $rmsLow, RMS High: $rmsHigh",
            rmsLow < rmsHigh * 0.35,
        )
    }
}
