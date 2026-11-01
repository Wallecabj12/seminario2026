package com.geovanivargas.planetamusica.audio.synth

import org.json.JSONObject

/**
 * Serialización manual de SynthParams a/desde JSON usando org.json (disponible en Android SDK).
 */
object SynthParamsJson {

    fun encode(p: SynthParams): String {
        val root = JSONObject()
        root.put("oscA", encodeOsc(p.oscA))
        root.put("oscB", encodeOsc(p.oscB))
        root.put("sub", JSONObject().put("enabled", p.sub.enabled).put("level", p.sub.level.toDouble()))
        root.put("filter", JSONObject()
            .put("type", p.filter.type.name)
            .put("cutoff", p.filter.cutoff.toDouble())
            .put("resonance", p.filter.resonance.toDouble())
            .put("drive", p.filter.drive.toDouble()))
        root.put("ampEnv", encodeAdsr(p.ampEnv))
        root.put("filterEnv", encodeAdsr(p.filterEnv))
        root.put("filterEnvAmount", p.filterEnvAmount.toDouble())
        root.put("lfo", JSONObject()
            .put("shape", p.lfo.shape.name)
            .put("rate", p.lfo.rate.toDouble())
            .put("depth", p.lfo.depth.toDouble())
            .put("target", p.lfo.target.name))
        root.put("distortion", JSONObject().put("enabled", p.distortion.enabled).put("mix", p.distortion.mix.toDouble()))
        root.put("delay", JSONObject()
            .put("enabled", p.delay.enabled)
            .put("time", p.delay.time.toDouble())
            .put("feedback", p.delay.feedback.toDouble())
            .put("mix", p.delay.mix.toDouble()))
        root.put("reverb", JSONObject()
            .put("enabled", p.reverb.enabled)
            .put("size", p.reverb.size.toDouble())
            .put("mix", p.reverb.mix.toDouble()))
        root.put("masterLevel", p.masterLevel.toDouble())
        return root.toString()
    }

    fun decode(json: String): SynthParams {
        return try {
            val r = JSONObject(json)
            SynthParams(
                oscA = decodeOsc(r.optJSONObject("oscA")),
                oscB = decodeOsc(r.optJSONObject("oscB"), OscParams(shape = WaveShape.SQUARE, level = 0f)),
                sub = r.optJSONObject("sub")?.let {
                    SubOscParams(it.optBoolean("enabled", false), it.optDouble("level", 0.4).toFloat())
                } ?: SubOscParams(),
                filter = r.optJSONObject("filter")?.let {
                    FilterParams(
                        type = try { FilterType.valueOf(it.optString("type", "LOW_PASS")) } catch (_: Exception) { FilterType.LOW_PASS },
                        cutoff = it.optDouble("cutoff", 8000.0).toFloat(),
                        resonance = it.optDouble("resonance", 0.5).toFloat(),
                        drive = it.optDouble("drive", 0.0).toFloat(),
                    )
                } ?: FilterParams(),
                ampEnv = decodeAdsr(r.optJSONObject("ampEnv")),
                filterEnv = decodeAdsr(r.optJSONObject("filterEnv"), AdsrParams(0.005f, 0.3f, 0f, 0.2f)),
                filterEnvAmount = r.optDouble("filterEnvAmount", 0.3).toFloat(),
                lfo = r.optJSONObject("lfo")?.let {
                    LfoParams(
                        shape = try { WaveShape.valueOf(it.optString("shape", "SINE")) } catch (_: Exception) { WaveShape.SINE },
                        rate = it.optDouble("rate", 2.0).toFloat(),
                        depth = it.optDouble("depth", 0.3).toFloat(),
                        target = try { LfoTarget.valueOf(it.optString("target", "CUTOFF")) } catch (_: Exception) { LfoTarget.CUTOFF },
                    )
                } ?: LfoParams(),
                distortion = r.optJSONObject("distortion")?.let {
                    DistortionParams(it.optBoolean("enabled", false), it.optDouble("mix", 0.3).toFloat())
                } ?: DistortionParams(),
                delay = r.optJSONObject("delay")?.let {
                    DelayParams(
                        it.optBoolean("enabled", false),
                        it.optDouble("time", 0.3).toFloat(),
                        it.optDouble("feedback", 0.4).toFloat(),
                        it.optDouble("mix", 0.3).toFloat(),
                    )
                } ?: DelayParams(),
                reverb = r.optJSONObject("reverb")?.let {
                    ReverbParams(it.optBoolean("enabled", false), it.optDouble("size", 0.5).toFloat(), it.optDouble("mix", 0.25).toFloat())
                } ?: ReverbParams(),
                masterLevel = r.optDouble("masterLevel", 0.8).toFloat(),
            )
        } catch (_: Exception) {
            SynthParams()
        }
    }

    private fun encodeOsc(o: OscParams): JSONObject = JSONObject()
        .put("shape", o.shape.name)
        .put("octave", o.octave)
        .put("semitone", o.semitone)
        .put("detune", o.detune.toDouble())
        .put("level", o.level.toDouble())
        .put("unisonVoices", o.unisonVoices)
        .put("unisonDetune", o.unisonDetune.toDouble())
        .put("unisonSpread", o.unisonSpread.toDouble())

    private fun decodeOsc(j: JSONObject?, default: OscParams = OscParams()): OscParams {
        j ?: return default
        return OscParams(
            shape = try { WaveShape.valueOf(j.optString("shape", default.shape.name)) } catch (_: Exception) { default.shape },
            octave = j.optInt("octave", default.octave),
            semitone = j.optInt("semitone", default.semitone),
            detune = j.optDouble("detune", default.detune.toDouble()).toFloat(),
            level = j.optDouble("level", default.level.toDouble()).toFloat(),
            unisonVoices = j.optInt("unisonVoices", default.unisonVoices),
            unisonDetune = j.optDouble("unisonDetune", default.unisonDetune.toDouble()).toFloat(),
            unisonSpread = j.optDouble("unisonSpread", default.unisonSpread.toDouble()).toFloat(),
        )
    }

    private fun encodeAdsr(a: AdsrParams): JSONObject = JSONObject()
        .put("attack", a.attack.toDouble())
        .put("decay", a.decay.toDouble())
        .put("sustain", a.sustain.toDouble())
        .put("release", a.release.toDouble())

    private fun decodeAdsr(j: JSONObject?, default: AdsrParams = AdsrParams()): AdsrParams {
        j ?: return default
        return AdsrParams(
            attack = j.optDouble("attack", default.attack.toDouble()).toFloat(),
            decay = j.optDouble("decay", default.decay.toDouble()).toFloat(),
            sustain = j.optDouble("sustain", default.sustain.toDouble()).toFloat(),
            release = j.optDouble("release", default.release.toDouble()).toFloat(),
        )
    }
}
