package com.geovanivargas.planetamusica.domain.sequencer

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CoroutinesSequencerEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val timeProvider: () -> Long = { SystemClock.elapsedRealtime() },
) : SequencerEngine {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    override val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    override val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private var patterns: List<StepPattern> = emptyList()
    private var onStepListener: ((step: Int, activePadIds: List<Int>) -> Unit)? = null
    private var loopJob: Job? = null

    override fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(60, 200)
    }

    override fun setPatterns(patterns: List<StepPattern>) {
        this.patterns = patterns
    }

    override fun setOnStepListener(listener: ((step: Int, activePadIds: List<Int>) -> Unit)?) {
        this.onStepListener = listener
    }

    override fun start() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startLoop()
    }

    override fun pause() {
        _isPlaying.value = false
        loopJob?.cancel()
        loopJob = null
    }

    override fun stop() {
        _isPlaying.value = false
        loopJob?.cancel()
        loopJob = null
        _currentStep.value = 0
    }

    override fun release() {
        stop()
        onStepListener = null
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            var step = _currentStep.value
            var nextStepTime = timeProvider()

            while (isActive && _isPlaying.value) {
                // Disparar pads activos para el paso actual
                val activePads = patterns.filter { !it.isMuted && it.steps.getOrElse(step) { false } }
                    .map { it.padId }

                _currentStep.value = step
                onStepListener?.invoke(step, activePads)

                // Calcular intervalo del paso en milisegundos (16th note = 1 beat / 4)
                val stepIntervalMs = (60_000.0 / (_bpm.value * 4.0)).toLong().coerceAtLeast(10L)
                nextStepTime += stepIntervalMs

                val now = timeProvider()
                val delayMs = (nextStepTime - now).coerceAtLeast(1L)

                delay(delayMs)

                // Resincronizar si hubo demora externa excesiva
                if (timeProvider() - nextStepTime > stepIntervalMs * 2) {
                    nextStepTime = timeProvider()
                }

                step = (step + 1) % 16
            }
        }
    }
}
