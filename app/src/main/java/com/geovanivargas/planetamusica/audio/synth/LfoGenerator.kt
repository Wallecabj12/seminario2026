package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.PI
import kotlin.math.sin

/**
 * Generador de LFO. Puramente Kotlin.
 */
object LfoGenerator {

    /**
     * Genera un bloque de modulación LFO.
     * @return FloatArray con valores -1..1 (se multiplica por depth en el punto de uso)
     */
    fun generate(
        shape: WaveShape,
        rateHz: Float,
        numSamples: Int,
        sampleRate: Int = 44100,
        startPhase: Double = 0.0,
    ): FloatArray {
        val output = FloatArray(numSamples)
        val phaseInc = rateHz.toDouble() / sampleRate
        var phase = startPhase

        for (i in 0 until numSamples) {
            var t = phase - phase.toLong().toDouble()
            if (t < 0) t += 1.0

            output[i] = when (shape) {
                WaveShape.SINE -> sin(2.0 * PI * t).toFloat()
                WaveShape.SAW -> (2.0 * t - 1.0).toFloat()
                WaveShape.SQUARE -> if (t < 0.5) 1f else -1f
                WaveShape.TRIANGLE -> (if (t < 0.5) 4.0 * t - 1.0 else 3.0 - 4.0 * t).toFloat()
                WaveShape.NOISE -> (kotlin.random.Random.nextFloat() * 2f - 1f)
            }
            phase += phaseInc
        }

        return output
    }
}
