package com.geovanivargas.planetamusica.ui.sequencer

sealed interface SequencerEvent {
    data class StepToggled(val padId: Int, val stepIndex: Int) : SequencerEvent
    data class MuteToggled(val padId: Int) : SequencerEvent
    data class VolumeChanged(val padId: Int, val volume: Float) : SequencerEvent
    data object PlayToggle : SequencerEvent
    data object Stop : SequencerEvent
    data class BpmChanged(val bpm: Int) : SequencerEvent
}
