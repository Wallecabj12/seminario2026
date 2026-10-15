package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.geovanivargas.planetamusica.domain.sampler.AudioPlayer

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

    private val soundIdMap = mutableMapOf<Int, Int>()   // padId → soundPoolSoundId
    private val streamIdMap = mutableMapOf<Int, Int>()   // padId → streamId
    private val loadedSounds = mutableSetOf<Int>()       // soundPoolSoundIds ready

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) loadedSounds.add(soundId)
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

    override fun play(padId: Int) {
        val soundId = soundIdMap[padId] ?: return
        if (soundId !in loadedSounds) return
        val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        if (streamId != 0) streamIdMap[padId] = streamId
    }

    override fun stopAll() {
        streamIdMap.values.forEach { soundPool.stop(it) }
        streamIdMap.clear()
    }

    override fun unload(padId: Int) {
        streamIdMap.remove(padId)?.let { soundPool.stop(it) }
        soundIdMap.remove(padId)?.let {
            soundPool.unload(it)
            loadedSounds.remove(it)
        }
    }

    override fun release() {
        soundPool.release()
        soundIdMap.clear()
        streamIdMap.clear()
        loadedSounds.clear()
    }
}
