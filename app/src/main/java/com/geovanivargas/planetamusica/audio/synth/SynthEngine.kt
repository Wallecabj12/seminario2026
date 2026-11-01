package com.geovanivargas.planetamusica.audio.synth

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Motor de síntesis offline. Puramente Kotlin, sin dependencias Android.
 * Renderiza un bloque PCM 16-bit mono 44100 Hz.
 */
interface SynthEngineInterface {
    fun render(params: SynthParams, midiNote: Int, durationMs: Long): ShortArray
}

class SynthEngine : SynthEngineInterface {

    companion object {
        const val SAMPLE_RATE = 44100
        private const val MAX_AMPLITUDE = 32767

        /** Convierte nota MIDI a frecuencia Hz */
        fun midiToFreq(note: Int): Double = 440.0 * 2.0.pow((note - 69.0) / 12.0)
    }

    override fun render(params: SynthParams, midiNote: Int, durationMs: Long): ShortArray {
        val totalSamples = ((durationMs / 1000.0) * SAMPLE_RATE).toInt()
        if (totalSamples <= 0) return ShortArray(0)

        // Duración de nota activa vs release
        val releaseSamples = (params.ampEnv.release * SAMPLE_RATE).toInt()
        val noteOnSamples = (totalSamples - releaseSamples).coerceAtLeast(totalSamples / 2)
        val totalWithRelease = noteOnSamples + releaseSamples

        // 1. Calcular frecuencia base
        val baseFreq = midiToFreq(midiNote)

        // 2. Oscilador A
        val freqA = baseFreq * 2.0.pow(params.oscA.octave.toDouble()) *
                2.0.pow(params.oscA.semitone / 12.0) *
                2.0.pow(params.oscA.detune / 1200.0)
        val oscA = if (params.oscA.level > 0f) {
            Oscillators.render(
                params.oscA.shape, freqA, totalWithRelease, SAMPLE_RATE,
                unisonVoices = params.oscA.unisonVoices,
                unisonDetuneCents = params.oscA.unisonDetune,
            ).samples
        } else FloatArray(totalWithRelease)

        // 3. Oscilador B
        val freqB = baseFreq * 2.0.pow(params.oscB.octave.toDouble()) *
                2.0.pow(params.oscB.semitone / 12.0) *
                2.0.pow(params.oscB.detune / 1200.0)
        val oscB = if (params.oscB.level > 0f) {
            Oscillators.render(
                params.oscB.shape, freqB, totalWithRelease, SAMPLE_RATE,
                unisonVoices = params.oscB.unisonVoices,
                unisonDetuneCents = params.oscB.unisonDetune,
            ).samples
        } else FloatArray(totalWithRelease)

        // 4. Sub oscilador (seno, -1 octava)
        val subOsc = if (params.sub.enabled && params.sub.level > 0f) {
            Oscillators.render(
                WaveShape.SINE, baseFreq / 2.0, totalWithRelease, SAMPLE_RATE,
            ).samples
        } else FloatArray(totalWithRelease)

        // 5. Mezclar osciladores
        val mixed = FloatArray(totalWithRelease)
        for (i in mixed.indices) {
            mixed[i] = oscA[i] * params.oscA.level +
                    oscB[i] * params.oscB.level +
                    (if (params.sub.enabled) subOsc[i] * params.sub.level else 0f)
        }

        // 6. LFO
        val lfo = LfoGenerator.generate(
            params.lfo.shape, params.lfo.rate, totalWithRelease, SAMPLE_RATE,
        )

        // 7. Aplicar LFO a pitch (modular frecuencia requeriría re-renderizar, simplifiquemos con FM)
        if (params.lfo.target == LfoTarget.PITCH && params.lfo.depth > 0f) {
            // Aplicar vibrato simple modulando amplitud como fase FM
            // Simplificación: modular la amplitud ya mezclada con un tono sutil
            for (i in mixed.indices) {
                val pitchMod = 1f + lfo[i] * params.lfo.depth * 0.05f
                mixed[i] *= pitchMod
            }
        }

        // 8. Envolvente de filtro
        val filterEnv = AdsrGenerator.generate(
            params.filterEnv, totalWithRelease, noteOnSamples, SAMPLE_RATE,
        )

        // Modulación de cutoff = filter env + LFO (si target = CUTOFF)
        val cutoffMod = FloatArray(totalWithRelease)
        for (i in cutoffMod.indices) {
            var mod = filterEnv[i] * params.filterEnvAmount * 4f // escala la envolvente
            if (params.lfo.target == LfoTarget.CUTOFF && params.lfo.depth > 0f) {
                mod += lfo[i] * params.lfo.depth
            }
            cutoffMod[i] = mod
        }

        // 9. Filtro biquad
        val filter = BiquadFilter()
        filter.processBlock(
            mixed, params.filter.type, params.filter.cutoff,
            params.filter.resonance, params.filter.drive,
            SAMPLE_RATE, cutoffMod,
        )

        // 10. Envolvente de amplitud
        val ampEnv = AdsrGenerator.generate(
            params.ampEnv, totalWithRelease, noteOnSamples, SAMPLE_RATE,
        )

        for (i in mixed.indices) {
            mixed[i] *= ampEnv[i]
        }

        // 11. LFO volumen (tremolo)
        if (params.lfo.target == LfoTarget.VOLUME && params.lfo.depth > 0f) {
            for (i in mixed.indices) {
                val volMod = 1f - params.lfo.depth * 0.5f * (1f - lfo[i])
                mixed[i] *= volMod.coerceIn(0f, 1f)
            }
        }

        // 12. Efectos en cadena
        if (params.distortion.enabled) {
            Effects.applyDistortion(mixed, params.distortion.mix)
        }
        if (params.delay.enabled) {
            Effects.applyDelay(mixed, params.delay.time, params.delay.feedback, params.delay.mix, SAMPLE_RATE)
        }
        if (params.reverb.enabled) {
            Effects.applyReverb(mixed, params.reverb.size, params.reverb.mix, SAMPLE_RATE)
        }

        // 13. Master level
        for (i in mixed.indices) {
            mixed[i] *= params.masterLevel
        }

        // 14. Limitador suave (soft clipper) para evitar clipping
        softLimit(mixed)

        // 15. Convertir a PCM 16-bit
        val output = ShortArray(totalWithRelease)
        for (i in output.indices) {
            output[i] = (mixed[i] * MAX_AMPLITUDE).toInt().coerceIn(-MAX_AMPLITUDE, MAX_AMPLITUDE).toShort()
        }

        return output
    }

    /**
     * Renderiza una vista previa corta (solo los primeros ms) para la UI waveform.
     */
    fun renderPreview(params: SynthParams, midiNote: Int, durationMs: Long = 20): FloatArray {
        val totalSamples = ((durationMs / 1000.0) * SAMPLE_RATE).toInt()
        if (totalSamples <= 0) return FloatArray(0)

        val baseFreq = midiToFreq(midiNote)
        val freqA = baseFreq * 2.0.pow(params.oscA.octave.toDouble()) *
                2.0.pow(params.oscA.semitone / 12.0) *
                2.0.pow(params.oscA.detune / 1200.0)
        val oscA = if (params.oscA.level > 0f) {
            Oscillators.render(params.oscA.shape, freqA, totalSamples, SAMPLE_RATE,
                unisonVoices = params.oscA.unisonVoices, unisonDetuneCents = params.oscA.unisonDetune).samples
        } else FloatArray(totalSamples)

        val freqB = baseFreq * 2.0.pow(params.oscB.octave.toDouble()) *
                2.0.pow(params.oscB.semitone / 12.0) *
                2.0.pow(params.oscB.detune / 1200.0)
        val oscB = if (params.oscB.level > 0f) {
            Oscillators.render(params.oscB.shape, freqB, totalSamples, SAMPLE_RATE,
                unisonVoices = params.oscB.unisonVoices, unisonDetuneCents = params.oscB.unisonDetune).samples
        } else FloatArray(totalSamples)

        val mixed = FloatArray(totalSamples)
        for (i in mixed.indices) {
            mixed[i] = oscA[i] * params.oscA.level + oscB[i] * params.oscB.level
        }

        val filter = BiquadFilter()
        filter.processBlock(mixed, params.filter.type, params.filter.cutoff, params.filter.resonance, params.filter.drive, SAMPLE_RATE)

        return mixed
    }

    private fun softLimit(samples: FloatArray) {
        // Buscar el pico
        var peak = 0f
        for (s in samples) {
            val a = abs(s)
            if (a > peak) peak = a
        }

        if (peak > 0.95f) {
            // Soft clip con tanh
            val gain = 0.95f / peak
            for (i in samples.indices) {
                samples[i] = tanh(samples[i] * 1.5 * gain).toFloat() * 0.95f
            }
        }
    }
}
