package com.geovanivargas.planetamusica.ui.sequencer

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sequencer.StepPattern

data class SequencerPadRow(
    val pad: Pad,
    val pattern: StepPattern,
)

data class SequencerUiState(
    val rows: List<SequencerPadRow> = emptyList(),
    val isPlaying: Boolean = false,
    val currentStep: Int = 0,
    val bpm: Int = 120,
)
