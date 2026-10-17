package com.geovanivargas.planetamusica

import com.geovanivargas.planetamusica.domain.sequencer.CoroutinesSequencerEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SequencerEngineTest {

    @Test
    fun sequencerEngine_advancesStepsAndRespectsBpm() = runTest {
        val engine = CoroutinesSequencerEngine(
            scope = backgroundScope,
            timeProvider = { testScheduler.currentTime },
        )

        // 1. Validar BPM por defecto y clamping (60 a 200)
        assertEquals(120, engine.bpm.value)
        engine.setBpm(160)
        assertEquals(160, engine.bpm.value)

        engine.setBpm(40)
        assertEquals(60, engine.bpm.value) // Clamped min

        engine.setBpm(260)
        assertEquals(200, engine.bpm.value) // Clamped max

        // Configurar 120 BPM: cada paso de semicorchea = 60000 / (120 * 4) = 125 ms
        engine.setBpm(120)

        val recordedSteps = mutableListOf<Int>()
        engine.setOnStepListener { step, _ ->
            recordedSteps.add(step)
        }

        // 2. Iniciar y verificar paso 0
        engine.start()
        assertTrue(engine.isPlaying.value)

        runCurrent()
        assertEquals(0, engine.currentStep.value)
        assertTrue(recordedSteps.contains(0))

        // Avanzar 125ms -> paso 1
        advanceTimeBy(125L)
        runCurrent()
        assertEquals(1, engine.currentStep.value)

        // Avanzar otros 125ms -> paso 2
        advanceTimeBy(125L)
        runCurrent()
        assertEquals(2, engine.currentStep.value)

        // 3. Detener y verificar reseteo a 0
        engine.stop()
        assertFalse(engine.isPlaying.value)
        assertEquals(0, engine.currentStep.value)

        engine.release()
    }
}
