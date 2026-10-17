package com.geovanivargas.planetamusica.audio.defaults

import android.content.Context
import com.geovanivargas.planetamusica.domain.sampler.PadCategory
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

data class DefaultSoundSpec(
    val position: Int,
    val name: String,
    val fileName: String,
    val category: PadCategory,
)

object DefaultSoundGenerator {

    const val SAMPLE_RATE = 44100

    val DEFAULT_SPECS = listOf(
        DefaultSoundSpec(0, "Kick", "default_kick.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(1, "Snare", "default_snare.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(2, "HH Cerrado", "default_hh_closed.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(3, "HH Abierto", "default_hh_open.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(4, "Clap", "default_clap.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(5, "Tom", "default_tom.wav", PadCategory.PERCUSSION),
        DefaultSoundSpec(6, "Bass 808", "default_bass.wav", PadCategory.BASS),
        DefaultSoundSpec(7, "Lead Synth", "default_lead.wav", PadCategory.SYNTH),
        DefaultSoundSpec(8, "FX Riser", "default_fx_riser.wav", PadCategory.FX),
        DefaultSoundSpec(9, "Perc", "default_perc.wav", PadCategory.PERCUSSION),
    )

    fun generateAll(context: Context): Map<Int, File> {
        val soundsDir = File(context.filesDir, "sounds").also { it.mkdirs() }
        val generated = mutableMapOf<Int, File>()

        for (spec in DEFAULT_SPECS) {
            val file = File(soundsDir, spec.fileName)
            val samples = generateSamplesFor(spec.name)
            writeWavFile(file, SAMPLE_RATE, samples)
            generated[spec.position] = file
        }

        return generated
    }

    fun generateSamplesFor(soundName: String): ShortArray {
        return when (soundName) {
            "Kick" -> generateKick()
            "Snare" -> generateSnare()
            "HH Cerrado" -> generateClosedHiHat()
            "HH Abierto" -> generateOpenHiHat()
            "Clap" -> generateClap()
            "Tom" -> generateTom()
            "Bass 808" -> generateBass()
            "Lead Synth" -> generateLead()
            "FX Riser" -> generateFxRiser()
            "Perc" -> generatePerc()
            else -> generateKick()
        }
    }

    // 1. Kick: Pitch drop 160Hz -> 45Hz con transitorio y decay exponencial
    private fun generateKick(): ShortArray {
        val duration = 0.35f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 45.0 + 115.0 * exp(-18.0 * t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = exp(-8.0 * t)
            val click = exp(-120.0 * t) * 0.4
            val sampleVal = (sin(phase) + click) * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 31000).toInt().toShort()
        }
        return samples
    }

    // 2. Snare: Cuerpo sinusoidal ~185Hz + ráfaga de ruido blanco
    private fun generateSnare(): ShortArray {
        val duration = 0.25f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        val random = Random(42)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val body = sin(2.0 * PI * 185.0 * t) * exp(-20.0 * t)
            val noise = (random.nextDouble() * 2.0 - 1.0) * exp(-14.0 * t)
            val sampleVal = 0.35 * body + 0.65 * noise
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
        }
        return samples
    }

    // 3. Hi-Hat cerrado: Ruido de alta frecuencia con decay muy rápido (~0.08s)
    private fun generateClosedHiHat(): ShortArray {
        val duration = 0.08f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        val random = Random(123)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noise = random.nextDouble() * 2.0 - 1.0
            val env = exp(-45.0 * t)
            val sampleVal = noise * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
        }
        return samples
    }

    // 4. Hi-Hat abierto: Ruido metálico con decay más largo (~0.35s)
    private fun generateOpenHiHat(): ShortArray {
        val duration = 0.35f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        val random = Random(456)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noise = random.nextDouble() * 2.0 - 1.0
            val env = exp(-9.0 * t)
            val sampleVal = noise * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 25000).toInt().toShort()
        }
        return samples
    }

    // 5. Clap: 3 ráfagas cortas de ruido consecutivas y cola final
    private fun generateClap(): ShortArray {
        val duration = 0.22f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        val random = Random(789)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noise = random.nextDouble() * 2.0 - 1.0
            val burst = when {
                t < 0.012 -> exp(-150.0 * t)
                t < 0.024 -> exp(-150.0 * (t - 0.012))
                t < 0.036 -> exp(-150.0 * (t - 0.024))
                else -> exp(-16.0 * (t - 0.036))
            }
            val sampleVal = noise * burst
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
        return samples
    }

    // 6. Tom: Pitch drop 120Hz -> 65Hz
    private fun generateTom(): ShortArray {
        val duration = 0.30f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 65.0 + 55.0 * exp(-12.0 * t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = exp(-8.0 * t)
            val sampleVal = sin(phase) * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        return samples
    }

    // 7. Bass: Seno grave 55Hz (A1) con ligera saturación armónica
    private fun generateBass(): ShortArray {
        val duration = 0.60f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val fundamental = sin(2.0 * PI * 55.0 * t)
            val harmonic = 0.25 * sin(2.0 * PI * 110.0 * t)
            val env = exp(-3.8 * t)
            val sampleVal = (fundamental + harmonic) * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        return samples
    }

    // 8. Lead: Onda cuadrada suave (~440Hz)
    private fun generateLead(): ShortArray {
        val duration = 0.40f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sine = sin(2.0 * PI * 440.0 * t)
            val square = if (sine >= 0) 0.65 else -0.65
            val attack = (t / 0.015).coerceAtMost(1.0)
            val decay = exp(-5.0 * t)
            val sampleVal = square * attack * decay
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 27000).toInt().toShort()
        }
        return samples
    }

    // 9. FX Riser: Barrido de frecuencia ascendente 120Hz -> 850Hz
    private fun generateFxRiser(): ShortArray {
        val duration = 0.80f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / duration
            val freq = 120.0 + 730.0 * progress * progress
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = progress * (1.0 - progress * 0.2)
            val sampleVal = sin(phase) * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
        return samples
    }

    // 10. Perc: Ping metálico de alta frecuencia (dual sines 800Hz + 1320Hz)
    private fun generatePerc(): ShortArray {
        val duration = 0.15f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val s1 = sin(2.0 * PI * 800.0 * t)
            val s2 = sin(2.0 * PI * 1320.0 * t)
            val env = exp(-26.0 * t)
            val sampleVal = (0.5 * s1 + 0.5 * s2) * env
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
        }
        return samples
    }

    fun createWavBytes(sampleRate: Int = SAMPLE_RATE, samples: ShortArray): ByteArray {
        val totalAudioLen = samples.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2

        val buffer = ByteBuffer.allocate(44 + totalAudioLen).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalDataLen)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // fmt chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size (16 for PCM)
        buffer.putShort(1.toShort()) // AudioFormat (1 = PCM)
        buffer.putShort(1.toShort()) // NumChannels (1 = Mono)
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(2.toShort()) // BlockAlign
        buffer.putShort(16.toShort()) // BitsPerSample

        // data chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(totalAudioLen)

        for (sample in samples) {
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    fun writeWavFile(file: File, sampleRate: Int, samples: ShortArray) {
        val bytes = createWavBytes(sampleRate, samples)
        FileOutputStream(file).use { out ->
            out.write(bytes)
        }
    }
}
