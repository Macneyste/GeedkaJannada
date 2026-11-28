package com.geedkajannada.ui.prayer

import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.reminder.PrayerAlert
import com.geedkajannada.reminder.AdhanAudio
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import com.geedkajannada.reminder.NotificationHelper
import com.geedkajannada.reminder.ReminderScheduler
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.ErrorRed
import com.geedkajannada.ui.theme.White
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun PrayerScreen(
    vm: PrayerViewModel = run {
        val ctx = LocalContext.current.applicationContext
        appViewModel { c ->
            PrayerViewModel(
                c.prayers,
                reschedule = { ReminderScheduler.rescheduleAll(ctx, c.settings) },
                importAdhan = { uri -> AdhanAudio.import(ctx, uri, c.settings) },
                testAlert = { prayer -> PrayerAlert.fire(ctx, prayer, 0) },
            )
        }
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Prayer?>(null) }
    var pickingCity by remember { mutableStateOf(false) }
    var setupOpen by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        vm.onPermissionResult(ok)
    }
    val adhanPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.onAdhanPicked(uri)
    }

    var nowSec by remember { mutableIntStateOf(LocalTime.now(PrayerTimesCalc.ZONE).toSecondOfDay()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowSec = LocalTime.now(PrayerTimesCalc.ZONE).toSecondOfDay()
            delay(1000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(stringResource(R.string.prayer_title))
        NextPrayerCard(state.entries, nowSec)

        // Waqtiyada: magac, waqti, iyo gambaleel (ogeysiiska salaaddaas)
        GeedkaCard {
            state.entries.forEach { e ->
                PrayerRow(
                    e,
                    auto = state.auto,
                    bell = state.enabled && e.notify,
                    onEdit = { editing = e.prayer },
                    onBell = {
                        if (state.enabled) {
                            vm.setNotify(e.prayer, !e.notify)
                        } else if (NotificationHelper.hasPermission(context)) {
                            vm.setNotify(e.prayer, true)
                            vm.setEnabled(true)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
            }
        }

        if (state.notificationsDenied || (state.enabled && !NotificationHelper.hasPermission(context))) {
            GeedkaCard(border = BorderStroke(1.5.dp, ErrorRed)) {
                Text(stringResource(R.string.notif_denied_body), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp).heightIn(min = Dimens.TouchMin),
                ) { Text(stringResource(R.string.notif_open_settings)) }
            }
        }

        NotificationSection(
            state = state,
            onEnabled = { on ->
                if (!on || NotificationHelper.hasPermission(context)) {
                    vm.setEnabled(on)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onMode = vm::setMode,
            onOffset = vm::setOffset,
            onPickAdhan = { adhanPicker.launch(arrayOf("audio/*")) },
            onTest = {
                val next = PrayerMath.next(state.entries, nowSec / 60, nowSec % 60)?.first?.prayer ?: Prayer.SUBAX
                vm.test(next)
            },
        )

        // Dejinta naadir ah waa la laabay si boggu nadiif u ahaado
        GeedkaCard {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.TouchMin)
                    .clickable { setupOpen = !setupOpen },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.prayer_setup), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stringResource(if (setupOpen) R.string.prayer_setup_close else R.string.prayer_setup_open), color = Indigo)
            }
            if (setupOpen) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.TouchMin)
                        .toggleable(value = state.auto, role = Role.Switch, onValueChange = vm::setAuto),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.prayer_auto_short), modifier = Modifier.weight(1f))
                    Switch(checked = state.auto, onCheckedChange = null)
                }
                if (state.auto) {
                    OutlinedButton(
                        onClick = { pickingCity = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = Dimens.TouchMin),
                    ) { Text(stringResource(R.string.prayer_city, state.city.nameSo)) }
                }
                com.geedkajannada.ui.location.LocationCard()
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
private fun PrayerRow(e: PrayerEntry, auto: Boolean, bell: Boolean, onEdit: () -> Unit, onBell: () -> Unit) {
    val name = stringResource(e.prayer.nameRes)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        // Waqtiga gacanta ayaa lagu beddeli karaa marka xisaabta tooska ah damsan tahay.
        TextButton(onClick = onEdit, enabled = !auto, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
            Text(
                e.minuteOfDay?.let { PrayerMath.format(it) } ?: stringResource(R.string.prayer_time_unset),
                style = MaterialTheme.typography.titleLarge,
                color = BrownInk,
            )
        }
        IconButton(onClick = onBell, modifier = Modifier.size(Dimens.TouchMin)) {
            Icon(
                painterResource(if (bell) R.drawable.ic_bell else R.drawable.ic_bell_off),
                contentDescription = stringResource(if (bell) R.string.prayer_bell_on else R.string.prayer_bell_off, name),
                tint = if (bell) Indigo else BrownMid,
            )
        }
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
