package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.geovanivargas.planetamusica.domain.sampler.AudioPlayer
import com.geovanivargas.planetamusica.domain.sampler.Pad
import java.util.Collections

class SoundPoolAudioPlayer(context: Context) : AudioPlayer {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIdMap = Collections.synchronizedMap(mutableMapOf<Int, Int>())       // padId -> soundPoolSoundId
    private val streamIdMap = Collections.synchronizedMap(mutableMapOf<Int, Int>())      // padId -> streamId
    private val loadedSounds = Collections.synchronizedSet(mutableSetOf<Int>())          // soundPoolSoundIds ready
    private val activeLoops = Collections.synchronizedSet(mutableSetOf<Int>())            // padIds currently looping
    private val pendingPlayMap = Collections.synchronizedMap(mutableMapOf<Int, Pad>())   // soundId -> Pad pending play

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) {
                loadedSounds.add(soundId)
                pendingPlayMap.remove(soundId)?.let { padToPlay ->
                    play(padToPlay)
                }
            }
        }
    }

    override fun loadSound(padId: Int, filePath: String): Boolean {
        unload(padId)
        val soundId = soundPool.load(filePath, 1)
        return if (soundId != 0) {
            soundIdMap[padId] = soundId
            true
        } else false
    }

    override fun play(pad: Pad) {
        val soundId = soundIdMap[pad.id] ?: return
        if (soundId !in loadedSounds) {
            pendingPlayMap[soundId] = pad
            return
        }

        if (pad.isLoop && activeLoops.contains(pad.id)) {
            stop(pad.id)
            return
        }

        streamIdMap[pad.id]?.let { soundPool.stop(it) }

        val loopCount = if (pad.isLoop) -1 else 0
        val clampedVolume = pad.volume.coerceIn(0f, 1f)
        val clampedRate = pad.rate.coerceIn(0.5f, 2.0f)

        val streamId = soundPool.play(soundId, clampedVolume, clampedVolume, 1, loopCount, clampedRate)
        if (streamId != 0) {
            streamIdMap[pad.id] = streamId
            if (pad.isLoop) {
                activeLoops.add(pad.id)
            }
        }
    }

    override fun stop(padId: Int) {
        streamIdMap.remove(padId)?.let { soundPool.stop(it) }
        activeLoops.remove(padId)
        soundIdMap[padId]?.let { pendingPlayMap.remove(it) }
    }

    override fun stopAll() {
        streamIdMap.values.forEach { soundPool.stop(it) }
        streamIdMap.clear()
        activeLoops.clear()
        pendingPlayMap.clear()
    }

    override fun isLooping(padId: Int): Boolean = padId in activeLoops

    override fun isSoundLoaded(padId: Int): Boolean {
        val soundId = soundIdMap[padId] ?: return false
        return soundId in loadedSounds
    }

    override fun getActiveLoops(): Set<Int> = activeLoops.toSet()

    override fun unload(padId: Int) {
        stop(padId)
        soundIdMap.remove(padId)?.let {
            pendingPlayMap.remove(it)
            soundPool.unload(it)
            loadedSounds.remove(it)
        }
    }

    override fun release() {
        soundPool.release()
        soundIdMap.clear()
        streamIdMap.clear()
        loadedSounds.clear()
        activeLoops.clear()
        pendingPlayMap.clear()
    }
}
