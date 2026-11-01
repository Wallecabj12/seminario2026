package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generador de osciladores con anti-aliasing PolyBLEP para Saw y Square.
 * Puramente Kotlin, sin dependencias Android.
 */
object Oscillators {

    private const val TWO_PI = 2.0 * PI

    /**
     * Genera un bloque de muestras para un oscilador con parámetros de unison.
     * @param shape forma de onda
     * @param freqHz frecuencia base en Hz
     * @param numSamples número de muestras a generar
     * @param sampleRate tasa de muestreo
     * @param startPhase fase inicial (se actualiza internamente)
     * @param unisonVoices número de voces de unison (1 = sin unison)
     * @param unisonDetuneCents detune entre voces extremas en cents
     * @return FloatArray con las muestras normalizadas -1..1
     */
    fun render(
        shape: WaveShape,
        freqHz: Double,
        numSamples: Int,
        sampleRate: Int = 44100,
        startPhase: Double = 0.0,
        unisonVoices: Int = 1,
        unisonDetuneCents: Float = 10f,
    ): OscResult {
        if (unisonVoices <= 1) {
            return renderSingle(shape, freqHz, numSamples, sampleRate, startPhase)
        }

        // Unison: varias voces con detune distribuido simétricamente
        val voices = unisonVoices.coerceIn(1, 5)
        val output = FloatArray(numSamples)
        var lastPhase = startPhase
        val halfDetune = unisonDetuneCents / 2f

        for (v in 0 until voices) {
            val detuneRatio = if (voices > 1) {
                val t = v.toFloat() / (voices - 1).toFloat() // 0..1
                val cents = -halfDetune + t * unisonDetuneCents
                Math.pow(2.0, cents / 1200.0)
            } else 1.0

            val voiceFreq = freqHz * detuneRatio
            val phaseOffset = if (voices > 1) (v.toDouble() / voices) * TWO_PI else 0.0
            val result = renderSingle(shape, voiceFreq, numSamples, sampleRate, startPhase + phaseOffset)

            for (i in output.indices) {
                output[i] += result.samples[i]
            }
            if (v == voices / 2) lastPhase = result.endPhase
        }

        // Normalizar por número de voces
        val invVoices = 1f / voices
        for (i in output.indices) {
            output[i] *= invVoices
        }

        return OscResult(output, lastPhase)
    }

    private fun renderSingle(
        shape: WaveShape,
        freqHz: Double,
        numSamples: Int,
        sampleRate: Int,
        startPhase: Double,
    ): OscResult {
        val samples = FloatArray(numSamples)
        val phaseInc = freqHz / sampleRate
        var phase = startPhase

        when (shape) {
            WaveShape.SINE -> {
                for (i in 0 until numSamples) {
                    val t = phase - phase.toLong()
                    samples[i] = sin(TWO_PI * t).toFloat()
                    phase += phaseInc
                }
            }
            WaveShape.SAW -> {
                for (i in 0 until numSamples) {
                    var t = phase - phase.toLong()
                    if (t < 0) t += 1.0
                    val naive = (2.0 * t - 1.0).toFloat()
                    val blep = polyBlep(t, phaseInc).toFloat()
                    samples[i] = naive - blep
                    phase += phaseInc
                }
            }
            WaveShape.SQUARE -> {
                for (i in 0 until numSamples) {
                    var t = phase - phase.toLong()
                    if (t < 0) t += 1.0
                    var value = if (t < 0.5) 1.0f else -1.0f
                    value += polyBlep(t, phaseInc).toFloat()
                    val t2 = (t + 0.5) % 1.0
                    value -= polyBlep(t2, phaseInc).toFloat()
                    samples[i] = value
                    phase += phaseInc
                }
            }
            WaveShape.TRIANGLE -> {
                // Integrar un square para obtener triangle con anti-aliasing
                var integratedValue = 0.0f
                for (i in 0 until numSamples) {
                    var t = phase - phase.toLong()
                    if (t < 0) t += 1.0
                    var sq = if (t < 0.5) 1.0f else -1.0f
                    sq += polyBlep(t, phaseInc).toFloat()
                    val t2 = (t + 0.5) % 1.0
                    sq -= polyBlep(t2, phaseInc).toFloat()
                    // Integración con leak para triangle
                    integratedValue = integratedValue * 0.999f + sq * (4.0f * phaseInc.toFloat())
                    samples[i] = integratedValue.coerceIn(-1f, 1f)
                    phase += phaseInc
                }
            }
            WaveShape.NOISE -> {
                val rng = Random(startPhase.toLong())
                for (i in 0 until numSamples) {
                    samples[i] = rng.nextFloat() * 2f - 1f
                    phase += phaseInc
                }
            }
        }

        return OscResult(samples, phase)
    }

    /**
     * PolyBLEP anti-aliasing correction.
     * t = phase in [0, 1), dt = phase increment per sample.
     */
    private fun polyBlep(t: Double, dt: Double): Double {
        // Near rising edge (t ≈ 0)
        if (t < dt) {
            val x = t / dt
            return x + x - x * x - 1.0
        }
        // Near falling edge (t ≈ 1)
        if (t > 1.0 - dt) {
            val x = (t - 1.0) / dt
            return x * x + x + x + 1.0
        }
        return 0.0
    }

    data class OscResult(val samples: FloatArray, val endPhase: Double)
}
