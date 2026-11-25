package com.geedkajannada.ui.prayer

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.Cities
import com.geedkajannada.data.PrayerMath
import com.geedkajannada.data.PrayerTimesCalc
import com.geedkajannada.data.ReminderOffsets
import com.geedkajannada.reminder.NotificationHelper
import com.geedkajannada.reminder.ReminderScheduler
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.ErrorRed
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun PrayerScreen(
    vm: PrayerViewModel = run {
        val ctx = LocalContext.current.applicationContext
        appViewModel { c ->
            PrayerViewModel(c.prayers) { ReminderScheduler.rescheduleAll(ctx, c.settings) }
        }
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingPrayer by remember { mutableStateOf<Prayer?>(null) }
    var editing by remember { mutableStateOf<Prayer?>(null) }
    var pickingCity by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        pendingPrayer?.let { vm.onPermissionResult(ok, it) }
        pendingPrayer = null
    }

    var nowSec by remember { mutableIntStateOf(LocalTime.now(PrayerTimesCalc.ZONE).toSecondOfDay()) }
    LaunchedEffect(Unit) {
        while (true) {
            val t = LocalTime.now(PrayerTimesCalc.ZONE)
            nowSec = t.toSecondOfDay()
            delay(1000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
    ) {
        ScreenTitle(stringResource(R.string.prayer_title))

        NextPrayerCard(state.entries, nowSec)

        com.geedkajannada.ui.location.LocationCard()

        if (state.notificationsDenied) {
            GeedkaCard(border = BorderStroke(1.5.dp, ErrorRed)) {
                Text(stringResource(R.string.notif_denied_title), style = MaterialTheme.typography.titleMedium, color = ErrorRed)
                Text(
                    stringResource(R.string.notif_denied_body),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    modifier = Modifier.heightIn(min = Dimens.TouchMin),
                ) { Text(stringResource(R.string.notif_open_settings)) }
            }
        }

        GeedkaCard {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.TouchMin)
                    .toggleable(value = state.auto, role = Role.Switch, onValueChange = vm::setAuto),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.prayer_auto), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.prayer_auto_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.auto,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White, checkedTrackColor = Indigo,
                        uncheckedThumbColor = White, uncheckedTrackColor = Line, uncheckedBorderColor = Line,
                    ),
                )
            }
            if (state.auto) {
                OutlinedButton(
                    onClick = { pickingCity = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(min = Dimens.TouchMin),
                    shape = RoundedCornerShape(Dimens.CardRadiusSmall),
                ) { Text(stringResource(R.string.prayer_city, state.city.nameSo)) }
            }
        }

        GeedkaCard {
            state.entries.forEachIndexed { i, e ->
                PrayerRow(
                    e,
                    auto = state.auto,
                    onEdit = { editing = e.prayer },
                    onToggle = { on ->
                        if (!on) {
                            vm.setNotify(e.prayer, false)
                        } else if (NotificationHelper.hasPermission(context)) {
                            vm.setNotify(e.prayer, true)
                        } else {
                            pendingPrayer = e.prayer
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                )
                if (i < state.entries.lastIndex) androidx.compose.material3.HorizontalDivider(color = Line)
            }
        }

        GeedkaCard {
            Text(stringResource(R.string.prayer_offset_title), style = MaterialTheme.typography.titleMedium)
            val labels = mapOf(0 to R.string.offset_0, 5 to R.string.offset_5, 10 to R.string.offset_10)
            ReminderOffsets.forEach { o ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.TouchMin)
                        .selectable(selected = state.offset == o, role = Role.RadioButton, onClick = { vm.setOffset(o) }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = state.offset == o, onClick = null)
                    Text(stringResource(labels.getValue(o)), modifier = Modifier.padding(start = 12.dp))
                }
            }
            if (!ReminderScheduler.canScheduleExact(context)) {
                Text(
                    stringResource(R.string.exact_alarm_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    if (pickingCity) {
        CityDialog(
            selectedId = state.city.id,
            onDismiss = { pickingCity = false },
            onPick = {
                vm.setCity(it)
                pickingCity = false
            },
        )
    }

    editing?.let { p ->
        val current = state.entries.firstOrNull { it.prayer == p }?.minuteOfDay ?: (12 * 60)
        TimeDialog(
            title = stringResource(R.string.prayer_set_time, stringResource(p.nameRes)),
            initialMinute = current,
            onDismiss = { editing = null },
            onConfirm = { m ->
                vm.setTime(p, m)
                editing = null
            },
        )
    }
}

@Composable
private fun NextPrayerCard(entries: List<PrayerEntry>, nowSec: Int) {
    val next = PrayerMath.next(entries, nowSec / 60, nowSec % 60)
    GeedkaCard(containerColor = Indigo, border = BorderStroke(0.dp, Indigo)) {
        Text(stringResource(R.string.prayer_next), style = MaterialTheme.typography.titleMedium, color = White)
        if (next == null) {
            Text(
                stringResource(R.string.prayer_none_set),
                style = MaterialTheme.typography.bodyLarge,
                color = White,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            val (e, secs) = next
            Text(
                stringResource(e.prayer.nameRes) + " · " + PrayerMath.format(e.minuteOfDay!!),
                style = MaterialTheme.typography.headlineMedium,
                color = White,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.prayer_remaining, PrayerMath.formatRemaining(secs)),
                style = MaterialTheme.typography.bodyLarge,
                color = White,
            )
        }
    }
}

@Composable
private fun PrayerRow(e: PrayerEntry, auto: Boolean, onEdit: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(e.prayer.nameRes), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onEdit,
            enabled = !auto,
            modifier = Modifier.heightIn(min = Dimens.TouchMin),
            shape = RoundedCornerShape(Dimens.CardRadiusSmall),
        ) {
            Text(e.minuteOfDay?.let { PrayerMath.format(it) } ?: stringResource(R.string.prayer_time_unset))
        }
        Switch(
            checked = e.notify && e.minuteOfDay != null,
            onCheckedChange = onToggle,
            enabled = e.minuteOfDay != null,
            modifier = Modifier.padding(start = 12.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = White, checkedTrackColor = Indigo,
                uncheckedThumbColor = White, uncheckedTrackColor = Line, uncheckedBorderColor = Line,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(title: String, initialMinute: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initialMinute / 60, initialMinute % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            Button(onClick = { onConfirm(state.hour * 60 + state.minute) }, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = White,
    )
}

@Composable
private fun CityDialog(selectedId: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prayer_city_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Cities.all.forEach { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = Dimens.TouchMin)
                            .selectable(selected = c.id == selectedId, role = Role.RadioButton, onClick = { onPick(c.id) }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = c.id == selectedId, onClick = null)
                        Text(c.nameSo, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = White,
    )
}
