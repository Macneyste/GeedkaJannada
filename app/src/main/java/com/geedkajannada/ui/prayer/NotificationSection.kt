package com.geedkajannada.ui.prayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.Line
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.geedkajannada.R
import com.geedkajannada.data.NotifyMode
import com.geedkajannada.data.ReminderOffsets
import com.geedkajannada.reminder.ReminderScheduler
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.ErrorRed
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.White

/** Ogeysiisyada: switch guud, nooca codka, xasuusin ka hor iyo tijaabo. */
@Composable
fun NotificationSection(
    state: PrayerUiState,
    /** Switch-ka guud: shaashaddu waxay weydiisaa ogolaanshaha haddii loo baahdo. */
    onEnabled: (Boolean) -> Unit,
    onMode: (NotifyMode) -> Unit,
    onOffset: (Int) -> Unit,
    onPickAdhan: () -> Unit,
    onTest: () -> Unit,
) {
    val context = LocalContext.current
    GeedkaCard {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.TouchMin)
                .toggleable(value = state.enabled, role = Role.Switch, onValueChange = onEnabled),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.notif_section), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = state.enabled, onCheckedChange = null)
        }
        if (!state.enabled) return@GeedkaCard

        Chips {
            NotifyMode.entries.forEach { m ->
                val label = when (m) {
                    NotifyMode.SOUND -> R.string.notif_mode_sound
                    NotifyMode.ADHAN -> R.string.notif_mode_adhan
                    NotifyMode.VIBRATE -> R.string.notif_mode_vibrate
                    NotifyMode.SILENT -> R.string.notif_mode_silent
                }
                Chip(stringResource(label), state.mode == m, Modifier.weight(1f)) { onMode(m) }
            }
        }
        if (state.mode == NotifyMode.ADHAN) {
            Text(
                if (state.adhanName != null) stringResource(R.string.notif_adhan_file, state.adhanName)
                else stringResource(R.string.notif_adhan_hint),
                style = MaterialTheme.typography.bodySmall,
                color = BrownMid,
            )
            if (state.adhanFailed) {
                Text(stringResource(R.string.notif_adhan_failed), style = MaterialTheme.typography.bodySmall, color = ErrorRed)
            }
            OutlinedButton(onClick = onPickAdhan, modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.notif_pick_adhan))
            }
        }

        Text(stringResource(R.string.notif_early_title), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        Chips {
            ReminderOffsets.forEach { o ->
                val label = if (o == 0) stringResource(R.string.notif_early_none) else stringResource(R.string.notif_early_short, o)
                Chip(label, state.offset == o, Modifier.weight(1f)) { onOffset(o) }
            }
        }

        Button(onClick = onTest, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = Dimens.TouchMin)) {
            Text(stringResource(R.string.notif_test))
        }

        if (!ReminderScheduler.canScheduleExact(context)) {
            Text(stringResource(R.string.exact_alarm_note), style = MaterialTheme.typography.bodySmall, color = ErrorRed)
            TextButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.open(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                    }
                },
            ) { Text(stringResource(R.string.notif_exact_button)) }
        }
        TextButton(onClick = { context.open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) {
            Text(stringResource(R.string.notif_battery), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Fur bog nidaamka; haddii taleefanku aanu lahayn, waxba ha dhicin. */
private fun Context.open(intent: Intent) {
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

@Composable
private fun Chips(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

@Composable
private fun Chip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    // FilterChip-ka padding-kiisa ayaa qoraalka gooya markay 4 isku saf yihiin.
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier
            .heightIn(min = Dimens.TouchMin)
            .background(if (selected) Indigo else White, shape)
            .border(1.dp, if (selected) Indigo else Line, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) White else BrownInk,
            maxLines = 1,
        )
    }
}
