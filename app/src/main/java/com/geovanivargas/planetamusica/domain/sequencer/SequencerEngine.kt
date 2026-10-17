package com.geovanivargas.planetamusica.domain.sequencer

import kotlinx.coroutines.flow.StateFlow

interface SequencerEngine {
    val isPlaying: StateFlow<Boolean>
    val currentStep: StateFlow<Int>
    val bpm: StateFlow<Int>

    fun setBpm(newBpm: Int)
    fun setPatterns(patterns: List<StepPattern>)
    fun setOnStepListener(listener: ((step: Int, activePadIds: List<Int>) -> Unit)?)
    fun start()
    fun pause()
    fun stop()
    fun release()
}
