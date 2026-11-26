package com.geedkajannada.ui.prayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.geedkajannada.R
import com.geedkajannada.data.NotifyMode
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.ReminderOffsets
import com.geedkajannada.reminder.ReminderScheduler
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.ErrorRed
import com.geedkajannada.ui.theme.Line

/** Qaybta "Ogeysiisyada": switch guud, salaad kasta, dhawaaqa, ogeysiis hore iyo ogolaanshaha nidaamka. */
@Composable
fun NotificationSection(
    state: PrayerUiState,
    /** Switch-ka guud: shaashaddu waxay weydiisaa ogolaanshaha haddii loo baahdo. */
    onEnabled: (Boolean) -> Unit,
    onPrayer: (Prayer, Boolean) -> Unit,
    onMode: (NotifyMode) -> Unit,
    onOffset: (Int) -> Unit,
) {
    val context = LocalContext.current
    GeedkaCard {
        SwitchRow(stringResource(R.string.notif_section), state.enabled, onEnabled, title = true)
        if (!state.enabled) {
            Text(stringResource(R.string.notif_off_hint), style = MaterialTheme.typography.bodyMedium)
            return@GeedkaCard
        }
        HorizontalDivider(color = Line)
        state.entries.forEach { e ->
            SwitchRow(stringResource(e.prayer.nameRes), e.notify, { onPrayer(e.prayer, it) })
        }

        Heading(R.string.notif_mode_title)
        NotifyMode.entries.forEach { m ->
            val label = when (m) {
                NotifyMode.SOUND -> R.string.notif_mode_sound
                NotifyMode.VIBRATE -> R.string.notif_mode_vibrate
                NotifyMode.SILENT -> R.string.notif_mode_silent
            }
            RadioRow(stringResource(label), state.mode == m) { onMode(m) }
        }

        Heading(R.string.notif_early_title)
        ReminderOffsets.forEach { o ->
            val label = if (o == 0) stringResource(R.string.notif_early_none) else stringResource(R.string.notif_early_minutes, o)
            RadioRow(label, state.offset == o) { onOffset(o) }
        }

        if (!ReminderScheduler.canScheduleExact(context)) {
            Column(Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.exact_alarm_note), style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
                SystemButton(R.string.notif_exact_button) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.open(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                    }
                }
            }
        }
        SystemButton(R.string.notif_battery) { context.open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
    }
}

/** Fur bog nidaamka; haddii taleefanku aanu lahayn, waxba ha dhicin. */
private fun Context.open(intent: Intent) {
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

@Composable
private fun Heading(res: Int) {
    Text(stringResource(res), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun SystemButton(label: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .heightIn(min = Dimens.TouchMin),
        border = BorderStroke(1.dp, Line),
    ) { Text(stringResource(label)) }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit, title: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TouchMin)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = if (title) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TouchMin)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}
