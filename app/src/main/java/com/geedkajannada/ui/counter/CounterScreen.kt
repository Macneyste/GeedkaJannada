package com.geedkajannada.ui.counter

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.feedback.SoundPlayer
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Bronze
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Night
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDate

/** Midabka iyo qoraallada dikhri kasta (isla tartanka Dhikrs.all). */
private data class DhikrLook(val color: Color, val meaning: Int, val english: Int)

private val looks = listOf(
    DhikrLook(Indigo, R.string.tas_mean_1, R.string.tas_en_1),
    DhikrLook(Bronze, R.string.tas_mean_2, R.string.tas_en_2),
    DhikrLook(Sun, R.string.tas_mean_3, R.string.tas_en_3),
)

@Composable
fun CounterScreen(
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit,
    vm: CounterViewModel = appViewModel { c ->
        CounterViewModel(c.settings, dateKey = { StatsCalc.dateKey(LocalDate.now()) })
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sound = remember { SoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { sound.release() } }

    LaunchedEffect(vm, state.soundOn, state.hapticsOn) {
        vm.events.collect { e ->
            if (e is CounterEvent.Tapped) {
                if (state.soundOn) if (e.jarFilled) sound.playChime() else sound.playTick()
                if (state.hapticsOn) haptic.performHapticFeedback(
                    if (e.jarFilled) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
                )
            }
        }
    }

    TasbeehContent(
        state = state,
        onTap = vm::tap,
        onNewRound = vm::startNewRound,
        onReset = vm::requestReset,
        onOpenPicker = onOpenPicker,
        onOpenSettings = onOpenSettings,
    )

    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = vm::dismissReset,
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_body)) },
            confirmButton = {
                Button(onClick = vm::confirmReset, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissReset, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = White,
        )
    }
}

@Composable
private fun TasbeehContent(
    state: CounterUiState,
    onTap: () -> Unit,
    onNewRound: () -> Unit,
    onReset: () -> Unit,
    onOpenPicker: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val index = state.index.coerceIn(0, looks.lastIndex)
    val look = looks[index]
    val jar = state.current
    // Midabka gadaasha si tartiib ah ayuu u beddelmaa (0.8s) marka dikhrigu beddelmo; ma jiro iftiin degdeg ah.
    val tint by animateColorAsState(look.color.copy(alpha = 0.10f).compositeOver(CreamPanel), tween(800), label = "tint")

    Column(
        Modifier
            .fillMaxSize()
            .background(tint)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.tas_title),
                style = MaterialTheme.typography.titleLarge,
                color = BrownInk,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(Dimens.TouchMin)) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = Indigo)
            }
        }

        Text(
            stringResource(R.string.tas_ribbon, index + 1, state.jars.size, minOf(jar.count, jar.goal), jar.goal),
            style = MaterialTheme.typography.labelLarge,
            color = Night,
            modifier = Modifier
                .background(Sun, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Crossfade(targetState = index, animationSpec = tween(800), label = "dhikr") { i ->
            val d = state.jars[i].dhikr
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(d.arabic, style = arabicStyle(40), color = BrownInk, textAlign = TextAlign.Center)
                Text(stringResource(d.nameRes), style = MaterialTheme.typography.titleLarge, color = BrownInk)
                Text(stringResource(looks[i].meaning), style = MaterialTheme.typography.bodyMedium, color = BrownMid, textAlign = TextAlign.Center)
                Text(stringResource(looks[i].english), style = MaterialTheme.typography.labelSmall, color = BrownMid, textAlign = TextAlign.Center)
            }
        }

        if (state.complete) {
            CompletionCard(state, onNewRound)
        } else {
            val desc = stringResource(
                R.string.tas_tap_desc,
                stringResource(jar.dhikr.nameRes), jar.count, jar.goal,
            )
            Column(
                Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onTap)
                    .semantics { contentDescription = desc },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BeadsAndWheel(
                    steps = state.total,
                    color = look.color,
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                )
                Text(stringResource(R.string.tas_tap), style = MaterialTheme.typography.bodyMedium, color = BrownMid)
            }
        }

        Row(
            Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clickable(enabled = !state.complete, onClickLabel = stringResource(R.string.tas_tap), onClick = onTap),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.jars.forEachIndexed { i, j ->
                val name = stringResource(j.dhikr.nameRes)
                val jarDesc = stringResource(R.string.tas_jar_desc, name, j.count, j.goal)
                Column(
                    Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) { contentDescription = jarDesc },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    JarArt(j.count, j.goal, looks[i].color, Modifier.fillMaxWidth().height(150.dp))
                    Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                        if (j.full) {
                            Icon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = looks[i].color,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Text(name, style = MaterialTheme.typography.labelSmall, color = BrownInk, maxLines = 1)
                    Text("${minOf(j.count, j.goal)} / ${j.goal}", style = MaterialTheme.typography.labelLarge, color = BrownInk)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing)) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(R.string.reset)) }
            OutlinedButton(
                onClick = onOpenPicker,
                modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(R.string.tas_goals)) }
        }

        if (state.rounds > 0) {
            Text(stringResource(R.string.rounds_done, state.rounds), style = MaterialTheme.typography.bodyMedium, color = BrownMid)
        }
        Text(
            stringResource(R.string.tas_footer),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp),
            color = BrownMid,
        )
    }
}

@Composable
private fun CompletionCard(state: CounterUiState, onNewRound: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Night, RoundedCornerShape(Dimens.HeroRadius))
            .padding(Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.tas_complete, state.total, state.totalGoal),
            style = MaterialTheme.typography.titleMedium,
            color = Sun,
        )
        Text("تَقَبَّلَ ٱللَّهُ مِنَّا وَمِنْكُمْ", style = arabicStyle(30), color = White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tas_complete_latin), style = MaterialTheme.typography.titleSmall, color = White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tas_complete_meaning), style = MaterialTheme.typography.bodyMedium, color = White, textAlign = TextAlign.Center)
        Button(onClick = onNewRound, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
            Text(stringResource(R.string.new_round))
        }
    }
}
