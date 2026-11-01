package com.geovanivargas.planetamusica.ui.main

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.R
import com.geovanivargas.planetamusica.domain.sequencer.StepPattern
import com.geovanivargas.planetamusica.ui.presets.PresetsScreen
import com.geovanivargas.planetamusica.ui.presets.PresetsViewModel
import com.geovanivargas.planetamusica.ui.sampler.SamplerScreen
import com.geovanivargas.planetamusica.ui.sequencer.SequencerEvent
import com.geovanivargas.planetamusica.ui.sequencer.SequencerScreen
import com.geovanivargas.planetamusica.ui.sequencer.SequencerViewModel
import com.geovanivargas.planetamusica.ui.sequencer.TransportBar
import com.geovanivargas.planetamusica.ui.synth.SynthScreen
import com.geovanivargas.planetamusica.ui.theme.DawAccentOrange
import com.geovanivargas.planetamusica.ui.theme.DawDarkBackground
import com.geovanivargas.planetamusica.ui.theme.DawLedGreen
import com.geovanivargas.planetamusica.ui.theme.DawPanelBackground
import com.geovanivargas.planetamusica.ui.theme.DawPanelBorder
import com.geovanivargas.planetamusica.ui.theme.DawPanelHeader

enum class DawTab(val title: String) {
    PADS("Pads"),
    RACK("Rack"),
    SYNTH("Synth"),
    PRESETS("Presets"),
}

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    sequencerViewModel: SequencerViewModel = viewModel(),
    presetsViewModel: PresetsViewModel = viewModel(
        factory = PresetsViewModel.provideFactory(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    var selectedTab by remember { mutableStateOf(DawTab.PADS) }
    val sequencerState by sequencerViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        presetsViewModel.currentPatternProvider = {
            StepPattern.encodeToString(sequencerViewModel.getCurrentPatterns())
        }
        presetsViewModel.onPresetLoaded = { preset ->
            preset.patternData?.let { data ->
                val restored = StepPattern.decodeFromString(data)
                if (restored.isNotEmpty()) {
                    sequencerViewModel.loadPatterns(restored)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DawPanelBackground),
            ) {
                // Barra superior de estado / branding DAW + Login
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DawPanelHeader)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(DawLedGreen),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SOUNDCRAFT DAW",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DawAccentOrange,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontSize = 13.sp,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "STUDIO RACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        ),
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_btn_myplaces),
                            contentDescription = "Sesión de usuario",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Barra de transporte fija arriba
                TransportBar(
                    isPlaying = sequencerState.isPlaying,
                    bpm = sequencerState.bpm,
                    currentStep = sequencerState.currentStep,
                    onPlayToggle = { sequencerViewModel.onEvent(SequencerEvent.PlayToggle) },
                    onStop = { sequencerViewModel.onEvent(SequencerEvent.Stop) },
                    onBpmChange = { sequencerViewModel.onEvent(SequencerEvent.BpmChanged(it)) },
                )

                // Pestañas superiores compactas estilo DAW (Pads | Rack | Synth | Presets)
                DawTopTabBar(
                    tabs = DawTab.values(),
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedTab) {
                DawTab.PADS -> {
                    SamplerScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToPresets = { selectedTab = DawTab.PRESETS },
                        showTopBar = false,
                    )
                }
                DawTab.RACK -> {
                    SequencerScreen(
                        viewModel = sequencerViewModel,
                        showTransportBar = false,
                    )
                }
                DawTab.SYNTH -> {
                    SynthScreen()
                }
                DawTab.PRESETS -> {
                    PresetsScreen(
                        onNavigateBack = { selectedTab = DawTab.PADS },
                        showTopBar = false,
                        viewModel = presetsViewModel,
                    )
                }
            }
        }
    }
}

/**
 * Barra de pestañas superiores compactas estilo DAW de escritorio.
 * Altura fija de 48dp garantizando accesibilidad y respuesta táctil.
 */
@Composable
private fun DawTopTabBar(
    tabs: Array<DawTab>,
    selectedTab: DawTab,
    onTabSelected: (DawTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(DawDarkBackground)
            .border(width = 1.dp, color = DawPanelBorder),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = tab == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(tab) }
                    .background(
                        if (isSelected) DawPanelBackground else Color.Transparent,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Acento luminoso inferior en pestaña activa
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(DawAccentOrange),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Mini LED de estado
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) DawAccentOrange else Color(0xFF4A5562),
                            ),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tab.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DawAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            fontSize = 12.sp,
                        ),
                    )
                }

                // Divisor sutil entre pestañas
                if (index < tabs.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .align(Alignment.CenterEnd)
                            .background(DawPanelBorder),
                    )
                }
            }
        }
    }
}

