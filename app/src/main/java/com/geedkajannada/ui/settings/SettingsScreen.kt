package com.geedkajannada.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Gold
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrayer: () -> Unit,
    vm: SettingsViewModel = appViewModel { SettingsViewModel(it.settings) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            ScreenTitle(stringResource(R.string.settings_title))
        }

        GeedkaCard {
            SwitchRow(stringResource(R.string.setting_sound), state.soundOn, vm::setSound)
            SwitchRow(stringResource(R.string.setting_haptics), state.hapticsOn, vm::setHaptics)
        }

        GeedkaCard {
            Text(stringResource(R.string.settings_prayer_section), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_prayer_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Button(
                onClick = onOpenPrayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.TouchMin),
                shape = RoundedCornerShape(Dimens.CardRadiusSmall),
            ) { Text(stringResource(R.string.settings_prayer_open)) }
        }

        Text(
            stringResource(R.string.privacy_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TouchMin)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Emerald,
                uncheckedThumbColor = White,
                uncheckedTrackColor = Line,
                uncheckedBorderColor = Line,
            ),
        )
    }
}
