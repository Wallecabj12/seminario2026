package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.exp

/**
 * Envolvente ADSR. Puramente Kotlin.
 * Genera un bloque de valores de envolvente [0..1] para cada muestra.
 */
object AdsrGenerator {

    /**
     * Genera la envolvente ADSR completa para una nota con duración y release.
     * @param params parámetros ADSR
     * @param totalSamples número total de muestras (incluyendo release)
     * @param noteOnSamples muestras durante las que la nota está activa (antes del release)
     * @param sampleRate tasa de muestreo
     */
    fun generate(
        params: AdsrParams,
        totalSamples: Int,
        noteOnSamples: Int,
        sampleRate: Int = 44100,
    ): FloatArray {
        val env = FloatArray(totalSamples)
        val attackSamples = (params.attack * sampleRate).toInt().coerceAtLeast(1)
        val decaySamples = (params.decay * sampleRate).toInt().coerceAtLeast(1)
        val releaseSamples = (params.release * sampleRate).toInt().coerceAtLeast(1)
        val sustain = params.sustain.coerceIn(0f, 1f)

        var level = 0f
        var releaseStartLevel = sustain

        for (i in 0 until totalSamples) {
            if (i < noteOnSamples) {
                // Note ON phase
                when {
                    i < attackSamples -> {
                        // Attack: lineal de 0 a 1
                        level = i.toFloat() / attackSamples
                    }
                    i < attackSamples + decaySamples -> {
                        // Decay: exponencial de 1 a sustain
                        val decayPos = (i - attackSamples).toFloat() / decaySamples
                        level = 1f + (sustain - 1f) * (1f - exp(-3f * decayPos))
                    }
                    else -> {
                        // Sustain
                        level = sustain
                    }
                }
                releaseStartLevel = level
            } else {
                // Release phase
                val releasePos = (i - noteOnSamples).toFloat() / releaseSamples
                if (releasePos < 1f) {
                    level = releaseStartLevel * (1f - releasePos) * exp(-3f * releasePos)
                } else {
                    level = 0f
                }
            }
            env[i] = level.coerceIn(0f, 1f)
        }

        return env
    }

    /**
     * Genera puntos de la curva ADSR para dibujar en la UI.
     */
    fun generateCurve(params: AdsrParams, numPoints: Int = 128): FloatArray {
        val totalDuration = params.attack + params.decay + 0.3f + params.release
        val sampleRate = (numPoints / totalDuration).toInt().coerceAtLeast(100)
        val totalSamples = numPoints
        val noteOnSamples = ((params.attack + params.decay + 0.3f) / totalDuration * numPoints).toInt()
        return generate(params, totalSamples, noteOnSamples, sampleRate = (numPoints / totalDuration).toInt().coerceAtLeast(100))
    }
}
