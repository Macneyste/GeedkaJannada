package com.geedkajannada.feature.tasbeeh

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.feature.tasbeeh.components.BeadString
import com.geedkajannada.feature.tasbeeh.components.CompletionCard
import com.geedkajannada.feature.tasbeeh.components.DhikrHeader
import com.geedkajannada.feature.tasbeeh.components.JarRow
import com.geedkajannada.feature.tasbeeh.sound.TasbeehSoundPlayer
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.JarGold
import com.geedkajannada.ui.theme.White
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TasbeehScreen(
    vm: TasbeehViewModel = appViewModel { c ->
        TasbeehViewModel(c.tasbeeh, c.settings, dateKey = { StatsCalc.dateKey(LocalDate.now()) })
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val player = remember { TasbeehSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { player.release() } }
    val settings = (context.applicationContext as com.geedkajannada.GeedkaApp).container.settings
    val hapticsOn by settings.hapticsEnabled.collectAsStateWithLifecycle(false)

    LaunchedEffect(vm, hapticsOn) {
        vm.events.collect { e ->
            when (e) {
                is TasbeehEvent.Sound -> launch {
                    delay(e.delayMs)
                    player.play(e.sound)
                }
                is TasbeehEvent.Haptic -> if (hapticsOn) {
                    haptic.performHapticFeedback(
                        if (e.strong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
                    )
                }
                TasbeehEvent.Complete -> Unit // kaarka dhammaystirka wuxuu ka yimaadaa uiState.complete
            }
        }
    }

    TasbeehContent(
        state = state,
        onTap = vm::tap,
        onNext = vm::next,
        onReset = vm::requestReset,
        onRestart = vm::restartAfterComplete,
        onToggleSound = vm::toggleSound,
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
    state: TasbeehUiState,
    onTap: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onRestart: () -> Unit,
    onToggleSound: () -> Unit,
) {
    val jar = state.current
    val color = Color(jar.dhikr.color)
    // Midabka gadaasha si tartiib ah ayuu u beddelmaa; flashing ma jiro.
    val tint by animateColorAsState(color.copy(alpha = 0.10f).compositeOver(CreamPanel), tween(800), label = "tint")

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
            IconButton(onClick = onToggleSound, modifier = Modifier.size(Dimens.TouchMin)) {
                Icon(
                    painterResource(if (state.soundOn) R.drawable.ic_sound_on else R.drawable.ic_sound_off),
                    contentDescription = stringResource(if (state.soundOn) R.string.tas_mute else R.string.tas_unmute),
                    tint = Indigo,
                )
            }
        }

        Text(
            stringResource(R.string.tas_ribbon, state.index + 1, state.jars.size, jar.count, jar.goal),
            style = MaterialTheme.typography.labelLarge,
            color = BrownInk,
            modifier = Modifier
                .background(JarGold, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        DhikrHeader(jar.dhikr)

        if (state.complete) {
            CompletionCard(state.total, state.totalGoal, state.totalCompleted, onRestart)
        } else {
            BeadString(state.total, color, Modifier.fillMaxWidth().widthIn(max = 480.dp).height(160.dp))
        }

        JarRow(state.jars, Modifier.widthIn(max = 480.dp))

        if (!state.complete) {
            val desc = stringResource(R.string.tas_tap_desc, stringResource(jar.dhikr.nameRes), jar.count, jar.goal)
            Button(
                onClick = onTap,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .semantics { contentDescription = desc },
                shape = RoundedCornerShape(Dimens.HeroRadius),
            ) { Text(stringResource(R.string.tas_tap), style = MaterialTheme.typography.titleLarge) }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing)) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.reset))
                }
                OutlinedButton(onClick = onNext, modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.tas_next))
                }
            }
        }

        Text(
            stringResource(R.string.tas_footer),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp),
            color = BrownMid,
        )
    }
}
