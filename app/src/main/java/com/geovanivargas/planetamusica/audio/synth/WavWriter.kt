package com.geovanivargas.planetamusica.audio.synth

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utilidad para escribir archivos WAV 16-bit PCM Mono 44100 Hz.
 * Reutiliza el formato de DefaultSoundGenerator.
 */
object WavWriter {

    fun createWavBytes(samples: ShortArray, sampleRate: Int = SynthEngine.SAMPLE_RATE): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val audioLen = samples.size * blockAlign
        val totalDataLen = audioLen + 36

        val buffer = ByteBuffer.allocate(44 + audioLen).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())

        // fmt subchunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)                   // Subchunk1Size (PCM)
        buffer.putShort(1)                  // AudioFormat: PCM
        buffer.putShort(numChannels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())

        // data subchunk
        buffer.put("data".toByteArray())
        buffer.putInt(audioLen)

        for (sample in samples) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }

    fun writeWavFile(file: File, samples: ShortArray, sampleRate: Int = SynthEngine.SAMPLE_RATE) {
        val bytes = createWavBytes(samples, sampleRate)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { it.write(bytes) }
    }

    fun wavFileSizeBytes(durationMs: Long, sampleRate: Int = SynthEngine.SAMPLE_RATE): Long {
        val numSamples = ((durationMs / 1000.0) * sampleRate).toLong()
        return 44 + numSamples * 2 // header + 16-bit mono samples
    }
}
