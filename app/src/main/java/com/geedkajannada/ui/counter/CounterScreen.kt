package com.geedkajannada.ui.counter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.feedback.SoundPlayer
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDate

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
                if (state.soundOn) if (e.goalReached) sound.playChime() else sound.playTick()
                if (state.hapticsOn) haptic.performHapticFeedback(
                    if (e.goalReached) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
                )
            }
        }
    }

    CounterContent(
        state = state,
        onTap = vm::tap,
        onOpenSettings = onOpenSettings,
        onOpenPicker = onOpenPicker,
        onReset = vm::requestReset,
    )

    if (state.showGoalReached) {
        AlertDialog(
            onDismissRequest = vm::continueAfterGoal,
            title = { Text(stringResource(com.geedkajannada.R.string.goal_reached_title)) },
            text = {
                Text(
                    stringResource(
                        com.geedkajannada.R.string.goal_reached_body,
                        state.goal,
                        stringResource(state.dhikr.nameRes),
                    ),
                )
            },
            confirmButton = {
                Button(onClick = vm::startNewRound, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(com.geedkajannada.R.string.new_round))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::continueAfterGoal, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(com.geedkajannada.R.string.keep_going))
                }
            },
            containerColor = com.geedkajannada.ui.theme.White,
        )
    }

    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = vm::dismissReset,
            title = { Text(stringResource(com.geedkajannada.R.string.reset_title)) },
            text = { Text(stringResource(com.geedkajannada.R.string.reset_body)) },
            confirmButton = {
                Button(onClick = vm::confirmReset, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(com.geedkajannada.R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissReset, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(com.geedkajannada.R.string.cancel))
                }
            },
            containerColor = com.geedkajannada.ui.theme.White,
        )
    }
}

@Composable
private fun CounterContent(
    state: CounterUiState,
    onTap: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit,
    onReset: () -> Unit,
) {
    val remaining = (state.goal - state.count).coerceAtLeast(0)
    val message = when {
        state.count == 0 -> stringResource(com.geedkajannada.R.string.goal_message_start)
        state.count >= state.goal -> stringResource(com.geedkajannada.R.string.goal_message_done)
        else -> stringResource(com.geedkajannada.R.string.goal_message_progress, remaining)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(com.geedkajannada.R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text(stringResource(com.geedkajannada.R.string.settings))
            }
        }

        GeedkaCard {
            Text(
                state.dhikr.arabic,
                style = arabicStyle(34),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(state.dhikr.nameRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = onOpenPicker,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(com.geedkajannada.R.string.change_dhikr)) }
        }

        TreeCard(state, onTap)

        Text(
            state.count.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(com.geedkajannada.R.string.tap_tree_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        GeedkaCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(com.geedkajannada.R.string.goal_label),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(com.geedkajannada.R.string.goal_progress, state.count, state.goal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { state.ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = IndigoLight,
                trackColor = Line,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            Spacer(Modifier.height(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            if (state.rounds > 0) {
                Text(
                    stringResource(com.geedkajannada.R.string.rounds_done, state.rounds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing)) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.TouchMin),
                shape = RoundedCornerShape(Dimens.CardRadiusSmall),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) { Text(stringResource(com.geedkajannada.R.string.reset)) }
            Button(
                onClick = onOpenPicker,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.TouchMin),
                shape = RoundedCornerShape(Dimens.CardRadiusSmall),
                colors = ButtonDefaults.buttonColors(),
            ) { Text(stringResource(com.geedkajannada.R.string.new_dhikr)) }
        }
    }
}

@Composable
private fun TreeCard(state: CounterUiState, onTap: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "tapScale")
    val desc = stringResource(com.geedkajannada.R.string.tree_desc, state.count)
    Box(
        Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .background(Sand, RoundedCornerShape(Dimens.TreeCardRadius))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onTap() },
                )
            }
            .semantics {
                contentDescription = desc
                role = Role.Button
                onClick { onTap(); true }
            },
    ) {
        TreeCanvas(state.ratio, Modifier.fillMaxSize().padding(8.dp))
    }
}
