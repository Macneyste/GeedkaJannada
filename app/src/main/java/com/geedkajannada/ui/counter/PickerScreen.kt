package com.geedkajannada.ui.counter

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.arabicStyle

@Composable
fun PickerScreen(
    onBack: () -> Unit,
    vm: PickerViewModel = appViewModel { PickerViewModel(it.settings) },
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
            ScreenTitle(stringResource(R.string.picker_title))
        }

        Dhikrs.all.forEach { d ->
            val selected = d.id == state.selected.id
            GeedkaCard(
                modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = { vm.select(d) }),
                border = if (selected) BorderStroke(2.dp, Emerald) else BorderStroke(1.dp, Line),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(d.arabic, style = arabicStyle(28), color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(d.nameRes), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.picker_default_goal, d.defaultGoal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.selected),
                            tint = Emerald,
                        )
                    }
                }
            }
        }

        GeedkaCard {
            Text(stringResource(R.string.picker_goal_section), style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                listOf(33, 100).forEach { g ->
                    val active = state.goal == g
                    val shape = RoundedCornerShape(Dimens.CardRadiusSmall)
                    if (active) {
                        Button(
                            onClick = { vm.setPresetGoal(g) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = Dimens.TouchMin),
                            shape = shape,
                        ) { Text(g.toString()) }
                    } else {
                        OutlinedButton(
                            onClick = { vm.setPresetGoal(g) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = Dimens.TouchMin),
                            shape = shape,
                        ) { Text(g.toString()) }
                    }
                }
            }
            OutlinedTextField(
                value = state.customInput,
                onValueChange = vm::onCustomInput,
                label = { Text(stringResource(R.string.custom_goal)) },
                singleLine = true,
                isError = state.error != null,
                supportingText = {
                    state.error?.let {
                        Text(
                            stringResource(
                                when (it) {
                                    CustomGoalError.Empty -> R.string.custom_goal_empty
                                    CustomGoalError.NotNumber -> R.string.custom_goal_not_number
                                    CustomGoalError.OutOfRange -> R.string.custom_goal_range
                                },
                            ),
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(Dimens.CardRadiusSmall),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            Button(
                onClick = { vm.submitCustomGoal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.TouchMin),
                shape = RoundedCornerShape(Dimens.CardRadiusSmall),
            ) { Text(stringResource(R.string.custom_goal_apply), textAlign = TextAlign.Center) }
            Text(
                stringResource(R.string.goal_current, state.goal),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
