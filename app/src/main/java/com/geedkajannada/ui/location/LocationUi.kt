package com.geedkajannada.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.reminder.ReminderScheduler
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.ErrorRed

@Composable
fun rememberLocationViewModel(): LocationViewModel {
    val ctx = LocalContext.current.applicationContext
    return appViewModel(key = "location") { c ->
        LocationViewModel(c.settings, c.location) { ReminderScheduler.rescheduleAll(ctx, c.settings) }
    }
}

private fun hasPermission(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/** Marka app-ka la furo: weydii location markii ugu horreysay; haddii horay loo oggolaaday, si aamusnaan ah cusboonaysii goobta. */
@Composable
fun LocationGate(vm: LocationViewModel = rememberLocationViewModel()) {
    val ctx = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), vm::onPermissionResult)

    LaunchedEffect(Unit) {
        if (hasPermission(ctx)) vm.refresh()
    }

    if (state.showPrompt && !hasPermission(ctx)) {
        AlertDialog(
            onDismissRequest = vm::dismissPrompt,
            title = { Text(stringResource(R.string.loc_prompt_title)) },
            text = { Text(stringResource(R.string.loc_prompt_body)) },
            confirmButton = {
                Button(
                    onClick = { launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                    modifier = Modifier.heightIn(min = Dimens.TouchMin),
                ) { Text(stringResource(R.string.loc_allow)) }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissPrompt, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.loc_not_now))
                }
            },
        )
    }
}

/** Kaar ku jira shaashadda Salaad: isticmaal goobtayda / ka tag. */
@Composable
fun LocationCard(vm: LocationViewModel = rememberLocationViewModel()) {
    val ctx = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), vm::onPermissionResult)

    GeedkaCard {
        Text(stringResource(R.string.loc_card_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(if (state.hasLocation) R.string.loc_card_active else R.string.loc_card_hint),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        if (state.failed) {
            Text(stringResource(R.string.loc_failed), style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    if (hasPermission(ctx)) vm.refresh() else launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                enabled = !state.busy,
                modifier = Modifier.heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(if (state.busy) R.string.loc_busy else R.string.loc_use)) }
            if (state.hasLocation) {
                OutlinedButton(onClick = vm::clear, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.loc_clear))
                }
            }
        }
    }
}
