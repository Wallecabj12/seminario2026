package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.tanh

/**
 * Cadena de efectos: Distortion → Delay → Reverb (Schroeder simple).
 * Puramente Kotlin.
 */
object Effects {

    /**
     * Aplica distorsión tipo tanh.
     */
    fun applyDistortion(samples: FloatArray, mix: Float) {
        if (mix <= 0f) return
        for (i in samples.indices) {
            val dry = samples[i]
            val wet = tanh((dry * (1f + mix * 8f)).toDouble()).toFloat()
            samples[i] = dry * (1f - mix) + wet * mix
        }
    }

    /**
     * Aplica delay con feedback.
     */
    fun applyDelay(samples: FloatArray, timeSec: Float, feedback: Float, mix: Float, sampleRate: Int = 44100) {
        if (mix <= 0f) return
        val delaySamples = (timeSec * sampleRate).toInt().coerceIn(1, sampleRate * 2)
        val buffer = FloatArray(delaySamples)
        var writePos = 0

        for (i in samples.indices) {
            val dry = samples[i]
            val delayed = buffer[writePos]
            val wet = dry + delayed * feedback
            buffer[writePos] = wet.coerceIn(-1f, 1f)
            writePos = (writePos + 1) % delaySamples
            samples[i] = dry * (1f - mix) + delayed * mix
        }
    }

    /**
     * Reverb simple tipo Schroeder con 4 comb filters + 2 allpass.
     */
    fun applyReverb(samples: FloatArray, size: Float, mix: Float, sampleRate: Int = 44100) {
        if (mix <= 0f) return

        val scaledSize = 0.3f + size * 0.7f // 0.3..1.0

        // 4 parallel comb filters
        val combDelays = intArrayOf(
            (1557 * scaledSize).toInt().coerceAtLeast(1),
            (1617 * scaledSize).toInt().coerceAtLeast(1),
            (1491 * scaledSize).toInt().coerceAtLeast(1),
            (1422 * scaledSize).toInt().coerceAtLeast(1),
        )
        val combFeedback = 0.84f + size * 0.1f

        // 2 series allpass filters
        val allpassDelays = intArrayOf(
            (225 * scaledSize).toInt().coerceAtLeast(1),
            (556 * scaledSize).toInt().coerceAtLeast(1),
        )
        val allpassFeedback = 0.5f

        val combOutputs = Array(4) { FloatArray(samples.size) }

        // Parallel comb filters
        for (c in 0 until 4) {
            val buffer = FloatArray(combDelays[c])
            var pos = 0
            for (i in samples.indices) {
                val delayed = buffer[pos]
                buffer[pos] = samples[i] + delayed * combFeedback
                pos = (pos + 1) % combDelays[c]
                combOutputs[c][i] = delayed
            }
        }

        // Mix comb outputs
        val reverbSignal = FloatArray(samples.size)
        for (i in samples.indices) {
            reverbSignal[i] = (combOutputs[0][i] + combOutputs[1][i] +
                    combOutputs[2][i] + combOutputs[3][i]) * 0.25f
        }

        // Series allpass filters
        for (a in 0 until 2) {
            val buffer = FloatArray(allpassDelays[a])
            var pos = 0
            for (i in reverbSignal.indices) {
                val delayed = buffer[pos]
                val input = reverbSignal[i]
                buffer[pos] = input + delayed * allpassFeedback
                pos = (pos + 1) % allpassDelays[a]
                reverbSignal[i] = delayed - input * allpassFeedback
            }
        }

        // Mix dry/wet
        for (i in samples.indices) {
            samples[i] = samples[i] * (1f - mix) + reverbSignal[i] * mix
        }
    }
}
