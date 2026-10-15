package com.geovanivargas.planetamusica.domain.sampler

interface AudioPlayer {
    fun loadSound(padId: Int, filePath: String): Boolean
    fun play(pad: Pad)
    fun stop(padId: Int)
    fun stopAll()
    fun unload(padId: Int)
    fun isLooping(padId: Int): Boolean
    fun getActiveLoops(): Set<Int>
    fun release()
}
