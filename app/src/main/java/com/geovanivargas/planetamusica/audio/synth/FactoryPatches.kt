package com.geovanivargas.planetamusica.audio.synth

/**
 * Patches de fábrica del sintetizador.
 * 5 presets con nombres descriptivos y parámetros musicales.
 */
object FactoryPatches {

    val BASS = SynthParams(
        oscA = OscParams(shape = WaveShape.SAW, octave = -1, level = 0.9f, unisonVoices = 2, unisonDetune = 8f),
        oscB = OscParams(shape = WaveShape.SQUARE, octave = -1, level = 0.3f),
        sub = SubOscParams(enabled = true, level = 0.5f),
        filter = FilterParams(type = FilterType.LOW_PASS, cutoff = 600f, resonance = 0.35f, drive = 0.2f),
        ampEnv = AdsrParams(attack = 0.005f, decay = 0.3f, sustain = 0.6f, release = 0.15f),
        filterEnv = AdsrParams(attack = 0.005f, decay = 0.25f, sustain = 0.0f, release = 0.1f),
        filterEnvAmount = 0.6f,
        lfo = LfoParams(shape = WaveShape.SINE, rate = 0.3f, depth = 0.05f, target = LfoTarget.CUTOFF),
        distortion = DistortionParams(enabled = true, mix = 0.15f),
        masterLevel = 0.85f,
    )

    val LEAD = SynthParams(
        oscA = OscParams(shape = WaveShape.SAW, octave = 0, level = 0.8f, unisonVoices = 3, unisonDetune = 15f),
        oscB = OscParams(shape = WaveShape.SQUARE, octave = 0, semitone = 7, level = 0.3f),
        sub = SubOscParams(enabled = false),
        filter = FilterParams(type = FilterType.LOW_PASS, cutoff = 3000f, resonance = 0.4f, drive = 0.1f),
        ampEnv = AdsrParams(attack = 0.01f, decay = 0.15f, sustain = 0.8f, release = 0.2f),
        filterEnv = AdsrParams(attack = 0.01f, decay = 0.4f, sustain = 0.2f, release = 0.3f),
        filterEnvAmount = 0.5f,
        lfo = LfoParams(shape = WaveShape.SINE, rate = 5f, depth = 0.1f, target = LfoTarget.PITCH),
        delay = DelayParams(enabled = true, time = 0.33f, feedback = 0.35f, mix = 0.2f),
        masterLevel = 0.8f,
    )

    val PLUCK = SynthParams(
        oscA = OscParams(shape = WaveShape.SAW, octave = 0, level = 0.9f, unisonVoices = 1),
        oscB = OscParams(shape = WaveShape.TRIANGLE, octave = 1, level = 0.3f),
        sub = SubOscParams(enabled = false),
        filter = FilterParams(type = FilterType.LOW_PASS, cutoff = 5000f, resonance = 0.5f),
        ampEnv = AdsrParams(attack = 0.002f, decay = 0.15f, sustain = 0.0f, release = 0.1f),
        filterEnv = AdsrParams(attack = 0.001f, decay = 0.12f, sustain = 0.0f, release = 0.08f),
        filterEnvAmount = 0.9f,
        delay = DelayParams(enabled = true, time = 0.2f, feedback = 0.4f, mix = 0.25f),
        reverb = ReverbParams(enabled = true, size = 0.3f, mix = 0.15f),
        masterLevel = 0.85f,
    )

    val PAD = SynthParams(
        oscA = OscParams(shape = WaveShape.SAW, octave = 0, level = 0.5f, unisonVoices = 5, unisonDetune = 25f),
        oscB = OscParams(shape = WaveShape.TRIANGLE, octave = -1, level = 0.4f, unisonVoices = 3, unisonDetune = 20f),
        sub = SubOscParams(enabled = true, level = 0.3f),
        filter = FilterParams(type = FilterType.LOW_PASS, cutoff = 2000f, resonance = 0.2f),
        ampEnv = AdsrParams(attack = 0.8f, decay = 0.5f, sustain = 0.7f, release = 1.0f),
        filterEnv = AdsrParams(attack = 0.5f, decay = 1.0f, sustain = 0.3f, release = 0.8f),
        filterEnvAmount = 0.3f,
        lfo = LfoParams(shape = WaveShape.SINE, rate = 0.5f, depth = 0.15f, target = LfoTarget.CUTOFF),
        reverb = ReverbParams(enabled = true, size = 0.7f, mix = 0.4f),
        masterLevel = 0.75f,
    )

    val FX = SynthParams(
        oscA = OscParams(shape = WaveShape.NOISE, octave = 0, level = 0.6f),
        oscB = OscParams(shape = WaveShape.SINE, octave = 2, level = 0.4f),
        sub = SubOscParams(enabled = false),
        filter = FilterParams(type = FilterType.BAND_PASS, cutoff = 1500f, resonance = 0.7f),
        ampEnv = AdsrParams(attack = 0.3f, decay = 0.8f, sustain = 0.0f, release = 0.5f),
        filterEnv = AdsrParams(attack = 0.1f, decay = 1.0f, sustain = 0.0f, release = 0.3f),
        filterEnvAmount = 0.8f,
        lfo = LfoParams(shape = WaveShape.SAW, rate = 3f, depth = 0.5f, target = LfoTarget.CUTOFF),
        distortion = DistortionParams(enabled = true, mix = 0.2f),
        delay = DelayParams(enabled = true, time = 0.5f, feedback = 0.6f, mix = 0.35f),
        reverb = ReverbParams(enabled = true, size = 0.8f, mix = 0.5f),
        masterLevel = 0.7f,
    )

    val ALL = listOf(
        "Bass" to BASS,
        "Lead" to LEAD,
        "Pluck" to PLUCK,
        "Pad" to PAD,
        "FX" to FX,
    )
}
