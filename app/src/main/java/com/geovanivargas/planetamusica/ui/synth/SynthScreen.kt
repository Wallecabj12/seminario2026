package com.geovanivargas.planetamusica.ui.synth

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovanivargas.planetamusica.audio.synth.*
import com.geovanivargas.planetamusica.ui.components.DawButton
import com.geovanivargas.planetamusica.ui.components.DawKnob
import com.geovanivargas.planetamusica.ui.components.DawPanel
import com.geovanivargas.planetamusica.ui.theme.*

@Composable
fun SynthScreen(
    modifier: Modifier = Modifier,
    onNavigateToPads: (padPosition: Int?) -> Unit = {},
    viewModel: SynthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SynthEvent.ClearMessage)
        }
    }

    LaunchedEffect(state.shareFileTrigger) {
        state.shareFileTrigger?.let { file ->
            AudioExportHelper.shareAudio(context, file)
            viewModel.onEvent(SynthEvent.ShareFileConsumed)
        }
    }

    LaunchedEffect(state.navigateToPadsTrigger) {
        state.navigateToPadsTrigger?.let { padPos ->
            viewModel.onEvent(SynthEvent.NavigateToPadsConsumed)
            onNavigateToPads(padPos)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // ── Patch header ──
            PatchHeader(state, viewModel::onEvent)

            // ── Waveform display ──
            WaveformDisplay(state.previewWaveform)

            // ── OSC A ──
            OscPanel("OSC A", state.params.oscA,
                onShape = { viewModel.onEvent(SynthEvent.OscAShapeChanged(it)) },
                onOctave = { viewModel.onEvent(SynthEvent.OscAOctaveChanged(it)) },
                onLevel = { viewModel.onEvent(SynthEvent.OscALevelChanged(it)) },
                onDetune = { viewModel.onEvent(SynthEvent.OscADetuneChanged(it)) },
                onUnison = { viewModel.onEvent(SynthEvent.OscAUnisonChanged(it)) },
                onUnisonDetune = { viewModel.onEvent(SynthEvent.OscAUnisonDetuneChanged(it)) },
            )

            // ── OSC B ──
            OscPanel("OSC B", state.params.oscB,
                onShape = { viewModel.onEvent(SynthEvent.OscBShapeChanged(it)) },
                onOctave = { viewModel.onEvent(SynthEvent.OscBOctaveChanged(it)) },
                onLevel = { viewModel.onEvent(SynthEvent.OscBLevelChanged(it)) },
                onDetune = { viewModel.onEvent(SynthEvent.OscBDetuneChanged(it)) },
                onUnison = { viewModel.onEvent(SynthEvent.OscBUnisonChanged(it)) },
                onUnisonDetune = { viewModel.onEvent(SynthEvent.OscBUnisonDetuneChanged(it)) },
            )

            // ── SUB + MASTER ──
            SubMasterPanel(state.params, viewModel::onEvent)

            // ── FILTER ──
            FilterPanel(state.params.filter, state.filterResponse, viewModel::onEvent)

            // ── ENVELOPEs ──
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                EnvPanel("AMP ENV", state.params.ampEnv, state.ampEnvCurve,
                    onA = { viewModel.onEvent(SynthEvent.AmpAttackChanged(it)) },
                    onD = { viewModel.onEvent(SynthEvent.AmpDecayChanged(it)) },
                    onS = { viewModel.onEvent(SynthEvent.AmpSustainChanged(it)) },
                    onR = { viewModel.onEvent(SynthEvent.AmpReleaseChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
                Column(Modifier.weight(1f)) {
                    EnvPanel("FILT ENV", state.params.filterEnv, state.filterEnvCurve,
                        onA = { viewModel.onEvent(SynthEvent.FilterEnvAttackChanged(it)) },
                        onD = { viewModel.onEvent(SynthEvent.FilterEnvDecayChanged(it)) },
                        onS = { viewModel.onEvent(SynthEvent.FilterEnvSustainChanged(it)) },
                        onR = { viewModel.onEvent(SynthEvent.FilterEnvReleaseChanged(it)) },
                    )
                    Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("AMT", style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp))
                        Spacer(Modifier.width(4.dp))
                        DawKnob(value = state.params.filterEnvAmount,
                            onValueChange = { viewModel.onEvent(SynthEvent.FilterEnvAmountChanged(it)) },
                            size = 28.dp)
                    }
                }
            }

            // ── LFO ──
            LfoPanel(state.params.lfo, viewModel::onEvent)

            // ── FX ──
            FxPanel(state.params, viewModel::onEvent)

            // ── Keyboard ──
            SynthKeyboard(state.selectedOctave, viewModel::onEvent)

            // ── Botones de Integración Soundpad y Exportación ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { viewModel.onEvent(SynthEvent.ShowExportDialog) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DawAccentOrange),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("INTEGRAR CON SOUNDPAD", fontWeight = FontWeight.Bold, color = Color.Black)
                }

                OutlinedButton(
                    onClick = { viewModel.onEvent(SynthEvent.ShareAudioFile) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DawAccentOrange),
                ) {
                    Text("Exportar Audio", color = DawAccentOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Snackbar
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        // Loading overlay
        if (state.isExporting) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DawAccentOrange)
                    Spacer(Modifier.height(8.dp))
                    Text("Procesando audio...", color = DawAccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialogs
    if (state.showPatchListDialog) {
        PatchListDialog(state.patches, viewModel::onEvent)
    }
    if (state.showSavePatchDialog) {
        SavePatchDialog(state.currentPatchName, viewModel::onEvent)
    }
    if (state.showExportDialog) {
        ExportDialog(state, viewModel::onEvent)
    }
}

// ── Patch Header ──
@Composable
private fun PatchHeader(state: SynthUiState, onEvent: (SynthEvent) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Patch name LCD
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DawLcdBackground)
                .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
                .clickable { onEvent(SynthEvent.ShowPatchList) }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                state.currentPatchName.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DawAccentOrange, fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                ),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        IconTextButton("LOAD") { onEvent(SynthEvent.ShowPatchList) }
        IconTextButton("SAVE") { onEvent(SynthEvent.ShowSavePatchDialog) }
        IconTextButton("RND") { onEvent(SynthEvent.Randomize) }
        IconTextButton("INIT") { onEvent(SynthEvent.ResetParams) }
    }
}

@Composable
private fun IconTextButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DawSurfaceVariant)
            .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.5.sp,
        ))
    }
}

// ── Waveform ──
@Composable
private fun WaveformDisplay(waveform: FloatArray) {
    DawPanel(title = "WAVEFORM") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(DawLcdBackground)
                .padding(2.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (waveform.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val mid = h / 2
                // Center line
                drawLine(DawPanelBorder, Offset(0f, mid), Offset(w, mid), 0.5f)
                // Waveform
                val step = waveform.size.toFloat() / w.coerceAtLeast(1f)
                val path = Path()
                var started = false
                for (px in 0 until w.toInt()) {
                    val idx = (px * step).toInt().coerceIn(0, waveform.size - 1)
                    val y = mid - waveform[idx] * mid * 0.9f
                    if (!started) { path.moveTo(px.toFloat(), y); started = true }
                    else path.lineTo(px.toFloat(), y)
                }
                drawPath(path, DawAccentOrange, style = Stroke(1.5f, cap = StrokeCap.Round))
            }
        }
    }
}

// ── OSC Panel ──
@Composable
private fun OscPanel(
    title: String,
    osc: OscParams,
    onShape: (WaveShape) -> Unit,
    onOctave: (Int) -> Unit,
    onLevel: (Float) -> Unit,
    onDetune: (Float) -> Unit,
    onUnison: (Int) -> Unit,
    onUnisonDetune: (Float) -> Unit,
) {
    DawPanel(title = title) {
        Column(Modifier.padding(6.dp)) {
            // Shape selector
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WaveShape.values().forEach { shape ->
                    val selected = osc.shape == shape
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) DawAccentOrange.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (selected) DawAccentOrange else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onShape(shape) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(shape.name.take(3), style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) DawAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        ))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DawKnob(osc.level, onLevel, label = "LEVEL", size = 32.dp)
                // Octave buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OCT", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SmallButton("-") { onOctave(osc.octave - 1) }
                        Text("${osc.octave}", style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace, color = DawAccentOrange,
                            fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        SmallButton("+") { onOctave(osc.octave + 1) }
                    }
                }
                DawKnob(((osc.detune + 100f) / 200f), { onDetune(it * 200f - 100f) },
                    label = "DETUNE", size = 32.dp, valueFormatter = { "${((it * 200f) - 100f).toInt()}ct" })
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UNI", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SmallButton("-") { onUnison(osc.unisonVoices - 1) }
                        Text("${osc.unisonVoices}", style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace, color = DawAccentOrange,
                            fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                        SmallButton("+") { onUnison(osc.unisonVoices + 1) }
                    }
                }
                DawKnob(osc.unisonDetune / 50f, { onUnisonDetune(it * 50f) },
                    label = "SPREAD", size = 32.dp, valueFormatter = { "${(it * 50f).toInt()}ct" })
            }
        }
    }
}

@Composable
private fun SmallButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DawSurfaceVariant)
            .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp))
    }
}

// ── Sub + Master ──
@Composable
private fun SubMasterPanel(params: SynthParams, onEvent: (SynthEvent) -> Unit) {
    DawPanel(title = "SUB / MASTER") {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DawButton(checked = params.sub.enabled, onCheckedChange = { onEvent(SynthEvent.SubEnabledChanged(it)) }, text = "SUB")
            DawKnob(params.sub.level, { onEvent(SynthEvent.SubLevelChanged(it)) },
                label = "SUB LVL", size = 32.dp)
            Spacer(Modifier.width(16.dp))
            DawKnob(params.masterLevel, { onEvent(SynthEvent.MasterLevelChanged(it)) },
                label = "MASTER", size = 36.dp, activeColor = DawLedGreen)
        }
    }
}

// ── Filter ──
@Composable
private fun FilterPanel(filter: FilterParams, response: FloatArray, onEvent: (SynthEvent) -> Unit) {
    DawPanel(title = "FILTER") {
        Column(Modifier.padding(6.dp)) {
            // Filter type selector
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterType.values().forEach { type ->
                    val selected = filter.type == type
                    val label = when (type) {
                        FilterType.LOW_PASS -> "LP"
                        FilterType.HIGH_PASS -> "HP"
                        FilterType.BAND_PASS -> "BP"
                    }
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) DawAccentOrange.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (selected) DawAccentOrange else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onEvent(SynthEvent.FilterTypeChanged(type)) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) DawAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        ))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            // Filter response curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DawLcdBackground)
                    .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp)),
            ) {
                Canvas(Modifier.fillMaxSize().padding(2.dp)) {
                    if (response.isEmpty()) return@Canvas
                    val w = size.width; val h = size.height
                    val path = Path()
                    for (i in response.indices) {
                        val x = i.toFloat() / (response.size - 1) * w
                        val y = h - response[i] * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, CategorySynth, style = Stroke(2f, cap = StrokeCap.Round))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DawKnob(
                    value = ((kotlin.math.ln(filter.cutoff / 20f)) / (kotlin.math.ln(20000f / 20f))).coerceIn(0f, 1f),
                    onValueChange = {
                        val freq = 20f * kotlin.math.exp(it * kotlin.math.ln(20000f / 20f))
                        onEvent(SynthEvent.FilterCutoffChanged(freq))
                    },
                    label = "CUTOFF", size = 32.dp,
                    valueFormatter = { v ->
                        val f = 20f * kotlin.math.exp(v * kotlin.math.ln(20000f / 20f))
                        if (f >= 1000) "%.1fk".format(f / 1000) else "%.0f".format(f)
                    },
                    activeColor = CategorySynth,
                )
                DawKnob(filter.resonance, { onEvent(SynthEvent.FilterResonanceChanged(it)) },
                    label = "RESO", size = 32.dp, activeColor = CategorySynth)
                DawKnob(filter.drive, { onEvent(SynthEvent.FilterDriveChanged(it)) },
                    label = "DRIVE", size = 32.dp, activeColor = CategorySynth)
            }
        }
    }
}

// ── ADSR Envelope ──
@Composable
private fun EnvPanel(
    title: String, adsr: AdsrParams, curve: FloatArray,
    onA: (Float) -> Unit, onD: (Float) -> Unit, onS: (Float) -> Unit, onR: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    DawPanel(title = title, modifier = modifier) {
        Column(Modifier.padding(4.dp)) {
            // Curve display
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DawLcdBackground)
                    .border(1.dp, DawPanelBorder, RoundedCornerShape(4.dp)),
            ) {
                Canvas(Modifier.fillMaxSize().padding(2.dp)) {
                    if (curve.isEmpty()) return@Canvas
                    val w = size.width; val h = size.height
                    val path = Path()
                    for (i in curve.indices) {
                        val x = i.toFloat() / (curve.size - 1) * w
                        val y = h - curve[i] * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, DawLedGreen, style = Stroke(1.5f, cap = StrokeCap.Round))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DawKnob(adsr.attack / 4f, { onA(it * 4f) }, label = "A", size = 28.dp,
                    valueFormatter = { "${(it * 4000).toInt()}ms" })
                DawKnob(adsr.decay / 4f, { onD(it * 4f) }, label = "D", size = 28.dp,
                    valueFormatter = { "${(it * 4000).toInt()}ms" })
                DawKnob(adsr.sustain, onS, label = "S", size = 28.dp)
                DawKnob(adsr.release / 4f, { onR(it * 4f) }, label = "R", size = 28.dp,
                    valueFormatter = { "${(it * 4000).toInt()}ms" })
            }
        }
    }
}

// ── LFO ──
@Composable
private fun LfoPanel(lfo: LfoParams, onEvent: (SynthEvent) -> Unit) {
    DawPanel(title = "LFO") {
        Column(Modifier.padding(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WaveShape.values().forEach { shape ->
                    val sel = lfo.shape == shape
                    Box(
                        Modifier.height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (sel) CategoryFx.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (sel) CategoryFx else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onEvent(SynthEvent.LfoShapeChanged(shape)) }
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(shape.name.take(3), style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp, color = if (sel) CategoryFx else MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LfoTarget.values().forEach { target ->
                    val sel = lfo.target == target
                    val label = when (target) { LfoTarget.CUTOFF -> "CUT"; LfoTarget.PITCH -> "PIT"; LfoTarget.VOLUME -> "VOL" }
                    Box(
                        Modifier.height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (sel) CategoryFx.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (sel) CategoryFx else DawPanelBorder, RoundedCornerShape(4.dp))
                            .clickable { onEvent(SynthEvent.LfoTargetChanged(target)) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) CategoryFx else MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DawKnob(lfo.rate / 20f, { onEvent(SynthEvent.LfoRateChanged(it * 20f)) },
                    label = "RATE", size = 32.dp, activeColor = CategoryFx,
                    valueFormatter = { "%.1fHz".format(it * 20f) })
                DawKnob(lfo.depth, { onEvent(SynthEvent.LfoDepthChanged(it)) },
                    label = "DEPTH", size = 32.dp, activeColor = CategoryFx)
            }
        }
    }
}

// ── FX ──
@Composable
private fun FxPanel(params: SynthParams, onEvent: (SynthEvent) -> Unit) {
    DawPanel(title = "EFFECTS") {
        Column(Modifier.padding(6.dp)) {
            // Distortion
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DawButton(checked = params.distortion.enabled, onCheckedChange = { onEvent(SynthEvent.DistortionEnabledChanged(it)) },
                    text = "DIST", ledColor = DawLedRed)
                DawKnob(params.distortion.mix, { onEvent(SynthEvent.DistortionMixChanged(it)) },
                    label = "MIX", size = 28.dp, activeColor = DawLedRed)
            }
            Spacer(Modifier.height(4.dp))
            // Delay
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DawButton(checked = params.delay.enabled, onCheckedChange = { onEvent(SynthEvent.DelayEnabledChanged(it)) }, text = "DELAY")
                DawKnob(params.delay.time, { onEvent(SynthEvent.DelayTimeChanged(it)) },
                    label = "TIME", size = 28.dp, valueFormatter = { "${(it * 1000).toInt()}ms" })
                DawKnob(params.delay.feedback / 0.9f, { onEvent(SynthEvent.DelayFeedbackChanged(it * 0.9f)) },
                    label = "FB", size = 28.dp)
                DawKnob(params.delay.mix, { onEvent(SynthEvent.DelayMixChanged(it)) }, label = "MIX", size = 28.dp)
            }
            Spacer(Modifier.height(4.dp))
            // Reverb
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DawButton(checked = params.reverb.enabled, onCheckedChange = { onEvent(SynthEvent.ReverbEnabledChanged(it)) },
                    text = "VERB", ledColor = DawLedGreen)
                DawKnob(params.reverb.size, { onEvent(SynthEvent.ReverbSizeChanged(it)) },
                    label = "SIZE", size = 28.dp, activeColor = DawLedGreen)
                DawKnob(params.reverb.mix, { onEvent(SynthEvent.ReverbMixChanged(it)) },
                    label = "MIX", size = 28.dp, activeColor = DawLedGreen)
            }
        }
    }
}

// ── Keyboard ──
@Composable
private fun SynthKeyboard(octave: Int, onEvent: (SynthEvent) -> Unit) {
    DawPanel(title = "KEYBOARD") {
        Column(Modifier.padding(6.dp)) {
            // Octave selector
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SmallButton("-") { onEvent(SynthEvent.OctaveChanged(octave - 1)) }
                Text("C$octave", style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace, color = DawAccentOrange,
                    fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                SmallButton("+") { onEvent(SynthEvent.OctaveChanged(octave + 1)) }
            }
            Spacer(Modifier.height(4.dp))
            // Piano keys
            val baseNote = (octave + 1) * 12  // C of selected octave in MIDI
            val whiteNotes = intArrayOf(0, 2, 4, 5, 7, 9, 11, 12) // C D E F G A B C+
            val blackNoteMap = mapOf(0 to 1, 1 to 3, 3 to 6, 4 to 8, 5 to 10) // white index → semitone offset

            Box(Modifier.fillMaxWidth().height(80.dp)) {
                // White keys
                Row(Modifier.fillMaxSize()) {
                    whiteNotes.forEach { semitone ->
                        val note = baseNote + semitone
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(Color(0xFFE8E8E8))
                                .border(1.dp, DawPanelBorder, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .clickable { onEvent(SynthEvent.NotePressed(note)) },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            val noteName = arrayOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")[semitone % 12]
                            Text(noteName, style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp, color = Color.Gray),
                                modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                }
                // Black keys
                Row(Modifier.fillMaxSize()) {
                    whiteNotes.forEachIndexed { wIdx, _ ->
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            blackNoteMap[wIdx]?.let { semitone ->
                                val note = baseNote + semitone
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp)
                                        .width(20.dp)
                                        .fillMaxHeight(0.6f)
                                        .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                                        .background(Color(0xFF1A1A1A))
                                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                                        .clickable { onEvent(SynthEvent.NotePressed(note)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Dialogs ──
@Composable
private fun PatchListDialog(patches: List<com.geovanivargas.planetamusica.domain.synth.SynthPatch>, onEvent: (SynthEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(SynthEvent.HidePatchList) },
        title = { Text("PATCHES", fontWeight = FontWeight.Bold, color = DawAccentOrange) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                patches.forEach { patch ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp)
                            .clickable { onEvent(SynthEvent.LoadPatch(patch)) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (patch.isFactory) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(DawAccentOrange))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(patch.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface))
                        if (!patch.isFactory) {
                            Text("✕", Modifier.clickable { onEvent(SynthEvent.DeletePatch(patch.id)) }.padding(8.dp),
                                color = DawLedRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = DawPanelBorder, thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(SynthEvent.HidePatchList) }) { Text("Cerrar") }
        },
        containerColor = DawPanelBackground,
    )
}

@Composable
private fun SavePatchDialog(currentName: String, onEvent: (SynthEvent) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = { onEvent(SynthEvent.HideSavePatchDialog) },
        title = { Text("GUARDAR PATCH", fontWeight = FontWeight.Bold, color = DawAccentOrange) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Nombre") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DawAccentOrange, cursorColor = DawAccentOrange))
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onEvent(SynthEvent.SavePatch(name)) }) {
                Text("Guardar", color = DawAccentOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(SynthEvent.HideSavePatchDialog) }) { Text("Cancelar") }
        },
        containerColor = DawPanelBackground,
    )
}

@Composable
private fun ExportDialog(
    state: SynthUiState,
    onEvent: (SynthEvent) -> Unit,
) {
    fun midiToName(note: Int): String {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val oct = (note / 12) - 1
        return "${names[note % 12]}$oct"
    }

    AlertDialog(
        onDismissRequest = { onEvent(SynthEvent.HideExportDialog) },
        title = {
            Text(
                text = "ASIGNAR AL SOUNDPAD",
                fontWeight = FontWeight.Bold,
                color = DawAccentOrange,
                fontFamily = FontFamily.Monospace,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.exportName,
                    onValueChange = { onEvent(SynthEvent.ExportNameChanged(it)) },
                    label = { Text("Nombre del instrumento") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DawAccentOrange,
                        cursorColor = DawAccentOrange,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Nota musical
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Nota musical:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${midiToName(state.exportNote)} (MIDI ${state.exportNote})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DawAccentOrange,
                            fontFamily = FontFamily.Monospace,
                        ),
                    )
                }
                Slider(
                    value = state.exportNote.toFloat(),
                    onValueChange = { onEvent(SynthEvent.ExportNoteChanged(it.toInt())) },
                    valueRange = 21f..108f,
                    steps = 87,
                    colors = SliderDefaults.colors(thumbColor = DawAccentOrange, activeTrackColor = DawAccentOrange),
                )

                // Duración
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Duración:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.1f s".format(state.exportDurationSec), style = MaterialTheme.typography.bodySmall, color = DawAccentOrange)
                }
                Slider(
                    value = state.exportDurationSec,
                    onValueChange = { onEvent(SynthEvent.ExportDurationChanged(it)) },
                    valueRange = 0.2f..4f,
                    colors = SliderDefaults.colors(thumbColor = DawAccentOrange, activeTrackColor = DawAccentOrange),
                )

                // Selector de Pad (Matriz completa 1 a 16)
                Text(
                    text = "Selecciona el Pad de destino (1 a 16):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )

                // Renderizamos la cuadrícula 4x4 de pads del soundpad
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            for (col in 0 until 4) {
                                val pos = row * 4 + col
                                val pad = state.allPads.find { it.position == pos }
                                val isSelected = state.selectedExportPad == pos
                                val hasAudio = pad?.hasSound == true
                                val padName = pad?.name?.take(7) ?: "[Libre]"
                                val catColor = pad?.category?.accentColor() ?: DawPanelBorder

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) DawAccentOrange.copy(alpha = 0.35f)
                                            else if (hasAudio) DawSurfaceVariant
                                            else DawDarkBackground
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) DawAccentOrange else if (hasAudio) catColor.copy(alpha = 0.7f) else DawPanelBorder,
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .clickable { onEvent(SynthEvent.ExportPadSelected(pos)) }
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "%02d".format(pos + 1),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) DawAccentOrange else Color.Gray,
                                            ),
                                        )
                                        Text(
                                            text = if (hasAudio) padName else "Libre",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                color = if (isSelected) Color.White else if (hasAudio) Color(0xFFD0D5DD) else Color(0xFF707780),
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val targetPad = state.allPads.find { it.position == state.selectedExportPad }
                if (targetPad != null) {
                    val statusText = if (targetPad.hasSound) {
                        "⚠️ Reemplazará: '${targetPad.name}' (${targetPad.category.label})"
                    } else {
                        "✓ Pad disponible (${targetPad.position + 1})"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (targetPad.hasSound) Color(0xFFFFB74D) else DawLedGreen,
                        ),
                    )
                }

                val usedMb = state.storageUsedBytes / (1024.0 * 1024.0)
                Text(
                    text = "Almacenamiento: %.1f / 50 MB".format(usedMb),
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Button(
                    onClick = { onEvent(SynthEvent.ConfirmExport(openSoundpadAfter = true)) },
                    colors = ButtonDefaults.buttonColors(containerColor = DawAccentOrange),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Asignar y Probar en Soundpad", fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedButton(
                        onClick = { onEvent(SynthEvent.ConfirmExport(openSoundpadAfter = false)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Asignar", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onEvent(SynthEvent.ShareAudioFile) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Compartir", fontSize = 12.sp, color = DawAccentOrange)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(SynthEvent.HideExportDialog) }) {
                Text("Cancelar")
            }
        },
        containerColor = DawPanelBackground,
    )
}
