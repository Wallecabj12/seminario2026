package com.geovanivargas.planetamusica.domain.sampler

interface AudioPlayer {
    fun loadSound(padId: Int, filePath: String): Boolean
    fun play(padId: Int)
    fun stopAll()
    fun unload(padId: Int)
    fun release()
}
