package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.*

/**
 * Filtro biquad (LP, HP, BP) con drive (saturación pre-filtro).
 * Puramente Kotlin, sin dependencias Android.
 */
class BiquadFilter {

    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    // Estado del filtro (Direct Form I)
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0

    fun reset() {
        x1 = 0.0; x2 = 0.0; y1 = 0.0; y2 = 0.0
    }

    /**
     * Calcula los coeficientes del filtro biquad.
     * @param type tipo de filtro
     * @param cutoffHz frecuencia de corte en Hz
     * @param q factor Q (resonancia)
     * @param sampleRate tasa de muestreo
     */
    fun setParams(type: FilterType, cutoffHz: Float, q: Float, sampleRate: Int = 44100) {
        val freq = cutoffHz.toDouble().coerceIn(20.0, (sampleRate / 2.0) - 100.0)
        val qVal = q.toDouble().coerceIn(0.5, 20.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val sinW0 = sin(w0)
        val cosW0 = cos(w0)
        val alpha = sinW0 / (2.0 * qVal)

        when (type) {
            FilterType.LOW_PASS -> {
                val b0n = (1.0 - cosW0) / 2.0
                val b1n = 1.0 - cosW0
                val b2n = (1.0 - cosW0) / 2.0
                val a0n = 1.0 + alpha
                val a1n = -2.0 * cosW0
                val a2n = 1.0 - alpha
                b0 = b0n / a0n; b1 = b1n / a0n; b2 = b2n / a0n
                a1 = a1n / a0n; a2 = a2n / a0n
            }
            FilterType.HIGH_PASS -> {
                val b0n = (1.0 + cosW0) / 2.0
                val b1n = -(1.0 + cosW0)
                val b2n = (1.0 + cosW0) / 2.0
                val a0n = 1.0 + alpha
                val a1n = -2.0 * cosW0
                val a2n = 1.0 - alpha
                b0 = b0n / a0n; b1 = b1n / a0n; b2 = b2n / a0n
                a1 = a1n / a0n; a2 = a2n / a0n
            }
            FilterType.BAND_PASS -> {
                val b0n = alpha
                val b1n = 0.0
                val b2n = -alpha
                val a0n = 1.0 + alpha
                val a1n = -2.0 * cosW0
                val a2n = 1.0 - alpha
                b0 = b0n / a0n; b1 = b1n / a0n; b2 = b2n / a0n
                a1 = a1n / a0n; a2 = a2n / a0n
            }
        }
    }

    /**
     * Procesa una muestra.
     */
    fun process(input: Double): Double {
        val output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1; x1 = input
        y2 = y1; y1 = output
        return output
    }

    /**
     * Procesa un bloque de muestras in-place con drive y modulación de cutoff.
     */
    fun processBlock(
        samples: FloatArray,
        type: FilterType,
        baseCutoff: Float,
        resonance: Float,
        drive: Float,
        sampleRate: Int = 44100,
        cutoffModulation: FloatArray? = null,
    ) {
        val q = 0.5 + resonance * 15.0 // map 0..1 → Q 0.5..15.5

        for (i in samples.indices) {
            // Modular cutoff si hay envolvente/LFO
            val modCutoff = if (cutoffModulation != null) {
                (baseCutoff * (1f + cutoffModulation[i])).coerceIn(20f, 20000f)
            } else baseCutoff

            // Recalcular coeficientes si hay modulación (cada muestra es costoso pero preciso)
            if (cutoffModulation != null && (i % 32 == 0 || i == 0)) {
                setParams(type, modCutoff, q.toFloat(), sampleRate)
            } else if (i == 0) {
                setParams(type, modCutoff, q.toFloat(), sampleRate)
            }

            // Drive (saturación pre-filtro)
            var input = samples[i].toDouble()
            if (drive > 0f) {
                input *= (1.0 + drive * 4.0)
                input = tanh(input)
            }

            samples[i] = process(input).toFloat()
        }
    }

    companion object {
        fun tanh(x: Double): Double = kotlin.math.tanh(x)

        /**
         * Genera puntos de la curva de respuesta en frecuencia para la UI.
         */
        fun frequencyResponse(
            type: FilterType,
            cutoffHz: Float,
            resonance: Float,
            sampleRate: Int = 44100,
            numPoints: Int = 128,
        ): FloatArray {
            val filter = BiquadFilter()
            val q = 0.5f + resonance * 15f
            filter.setParams(type, cutoffHz, q, sampleRate)

            val response = FloatArray(numPoints)
            for (i in 0 until numPoints) {
                // Frecuencia logarítmica de 20Hz a 20kHz
                val freq = 20.0 * (20000.0 / 20.0).pow(i.toDouble() / (numPoints - 1))
                val w = 2.0 * PI * freq / sampleRate

                // H(z) = (b0 + b1*z^-1 + b2*z^-2) / (1 + a1*z^-1 + a2*z^-2)
                val cosW = cos(w)
                val sinW = sin(w)
                val cos2W = cos(2 * w)
                val sin2W = sin(2 * w)

                val numReal = filter.b0 + filter.b1 * cosW + filter.b2 * cos2W
                val numImag = -(filter.b1 * sinW + filter.b2 * sin2W)
                val denReal = 1.0 + filter.a1 * cosW + filter.a2 * cos2W
                val denImag = -(filter.a1 * sinW + filter.a2 * sin2W)

                val numMag = sqrt(numReal * numReal + numImag * numImag)
                val denMag = sqrt(denReal * denReal + denImag * denImag)

                val mag = if (denMag > 1e-10) numMag / denMag else 0.0
                // dB normalizado a 0..1 para la UI
                val db = 20.0 * log10(mag.coerceAtLeast(1e-10))
                response[i] = ((db + 40.0) / 40.0).coerceIn(0.0, 1.0).toFloat()
            }
            return response
        }
    }
}
