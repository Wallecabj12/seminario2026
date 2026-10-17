package com.geovanivargas.planetamusica.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.R
import com.geovanivargas.planetamusica.domain.presets.Preset
import com.geovanivargas.planetamusica.domain.sequencer.StepPattern
import com.geovanivargas.planetamusica.ui.presets.PresetsScreen
import com.geovanivargas.planetamusica.ui.presets.PresetsViewModel
import com.geovanivargas.planetamusica.ui.sampler.SamplerScreen
import com.geovanivargas.planetamusica.ui.sequencer.SequencerEvent
import com.geovanivargas.planetamusica.ui.sequencer.SequencerScreen
import com.geovanivargas.planetamusica.ui.sequencer.SequencerViewModel
import com.geovanivargas.planetamusica.ui.sequencer.TransportBar
import com.geovanivargas.planetamusica.ui.theme.Primary

enum class DawTab(val title: String, val iconRes: Int) {
    PADS("Pads", R.drawable.ic_tab_pads),
    SEQUENCE("Secuencia", R.drawable.ic_tab_sequence),
    PRESETS("Presets", R.drawable.ic_tab_presets),
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                // Cabecera superior compacta: Branding + Login
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SoundCraft DAW",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontSize = 17.sp,
                        ),
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            painter = painterResource(R.drawable.ic_btn_myplaces),
                            contentDescription = "Iniciar sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                DawTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = tab.title,
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Primary,
                            indicatorColor = Primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
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
                DawTab.SEQUENCE -> {
                    SequencerScreen(
                        viewModel = sequencerViewModel,
                        showTransportBar = false,
                    )
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
