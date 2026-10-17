package com.geovanivargas.planetamusica

import com.geovanivargas.planetamusica.audio.defaults.DefaultSoundGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DefaultSoundGeneratorTest {

    @Test
    fun defaultSoundGenerator_producesValidWavHeaderAndPcm16Mono() {
        val kickSamples = DefaultSoundGenerator.generateSamplesFor("Kick")
        assertTrue("Los samples generados no deben estar vacíos", kickSamples.isNotEmpty())

        val wavBytes = DefaultSoundGenerator.createWavBytes(
            sampleRate = DefaultSoundGenerator.SAMPLE_RATE,
            samples = kickSamples,
        )

        // El archivo debe tener como mínimo los 44 bytes de header
        assertTrue(wavBytes.size >= 44)
        val expectedDataSize = kickSamples.size * 2
        assertEquals(44 + expectedDataSize, wavBytes.size)

        val buffer = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. Chunk RIFF
        val riffTag = String(ByteArray(4).apply { buffer.get(this) })
        assertEquals("RIFF", riffTag)

        val totalDataLen = buffer.int
        assertEquals(expectedDataSize + 36, totalDataLen)

        val waveTag = String(ByteArray(4).apply { buffer.get(this) })
        assertEquals("WAVE", waveTag)

        // 2. Chunk fmt
        val fmtTag = String(ByteArray(4).apply { buffer.get(this) })
        assertEquals("fmt ", fmtTag)

        val subchunk1Size = buffer.int
        assertEquals(16, subchunk1Size) // 16 para PCM

        val audioFormat = buffer.short
        assertEquals(1.toShort(), audioFormat) // 1 = PCM

        val numChannels = buffer.short
        assertEquals(1.toShort(), numChannels) // 1 = Mono

        val sampleRate = buffer.int
        assertEquals(44100, sampleRate) // 44.1 kHz

        val byteRate = buffer.int
        assertEquals(44100 * 2, byteRate) // 88200 B/s

        val blockAlign = buffer.short
        assertEquals(2.toShort(), blockAlign) // 1 canal * 16 bits / 8 = 2 bytes

        val bitsPerSample = buffer.short
        assertEquals(16.toShort(), bitsPerSample) // 16 bits

        // 3. Chunk data
        val dataTag = String(ByteArray(4).apply { buffer.get(this) })
        assertEquals("data", dataTag)

        val dataSize = buffer.int
        assertEquals(expectedDataSize, dataSize)
    }
}
