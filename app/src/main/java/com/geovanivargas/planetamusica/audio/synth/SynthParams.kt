package com.geovanivargas.planetamusica.audio.synth

/** Forma de onda del oscilador */
enum class WaveShape { SINE, SAW, SQUARE, TRIANGLE, NOISE }

/** Tipo de filtro biquad */
enum class FilterType { LOW_PASS, HIGH_PASS, BAND_PASS }

/** Destino del LFO */
enum class LfoTarget { CUTOFF, PITCH, VOLUME }

data class OscParams(
    val shape: WaveShape = WaveShape.SAW,
    val octave: Int = 0,
    val semitone: Int = 0,
    val detune: Float = 0f,
    val level: Float = 0.8f,
    val unisonVoices: Int = 1,
    val unisonDetune: Float = 10f,
    val unisonSpread: Float = 0.5f,
)

data class SubOscParams(
    val enabled: Boolean = false,
    val level: Float = 0.4f,
)

data class FilterParams(
    val type: FilterType = FilterType.LOW_PASS,
    val cutoff: Float = 8000f,
    val resonance: Float = 0.5f,
    val drive: Float = 0f,
)

data class AdsrParams(
    val attack: Float = 0.01f,
    val decay: Float = 0.2f,
    val sustain: Float = 0.7f,
    val release: Float = 0.3f,
)

data class LfoParams(
    val shape: WaveShape = WaveShape.SINE,
    val rate: Float = 2f,
    val depth: Float = 0.3f,
    val target: LfoTarget = LfoTarget.CUTOFF,
)

data class DistortionParams(
    val enabled: Boolean = false,
    val mix: Float = 0.3f,
)

data class DelayParams(
    val enabled: Boolean = false,
    val time: Float = 0.3f,
    val feedback: Float = 0.4f,
    val mix: Float = 0.3f,
)

data class ReverbParams(
    val enabled: Boolean = false,
    val size: Float = 0.5f,
    val mix: Float = 0.25f,
)

data class SynthParams(
    val oscA: OscParams = OscParams(shape = WaveShape.SAW),
    val oscB: OscParams = OscParams(shape = WaveShape.SQUARE, level = 0f),
    val sub: SubOscParams = SubOscParams(),
    val filter: FilterParams = FilterParams(),
    val ampEnv: AdsrParams = AdsrParams(),
    val filterEnv: AdsrParams = AdsrParams(attack = 0.005f, decay = 0.3f, sustain = 0.0f, release = 0.2f),
    val filterEnvAmount: Float = 0.3f,
    val lfo: LfoParams = LfoParams(),
    val distortion: DistortionParams = DistortionParams(),
    val delay: DelayParams = DelayParams(),
    val reverb: ReverbParams = ReverbParams(),
    val masterLevel: Float = 0.8f,
)
