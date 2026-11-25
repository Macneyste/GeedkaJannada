package com.geedkajannada.ui.zakat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geedkajannada.R
import com.geedkajannada.ui.BackHeader
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import java.util.Locale

@Composable
fun ZakatScreen(onBack: () -> Unit, vm: ZakatViewModel = viewModel()) {
    val s by vm.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        BackHeader(stringResource(R.string.zakat_title), onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Field(R.string.zakat_cash, s.cash, vm::onCash)
            Field(R.string.zakat_gold, s.gold, vm::onGold)
            Field(R.string.zakat_debts, s.debts, vm::onDebts)
            Field(R.string.zakat_nisab, s.nisab, vm::onNisab)
            Field(R.string.zakat_rate, s.rate, vm::onRate)
            if (s.invalid) {
                Text(stringResource(R.string.zakat_invalid), color = MaterialTheme.colorScheme.error)
            }
            s.result?.let { r ->
                GeedkaCard {
                    Text(stringResource(R.string.zakat_net, fmt(r.net)), style = MaterialTheme.typography.bodyLarge)
                    if (r.eligible) {
                        Text(stringResource(R.string.zakat_due, fmt(r.due)), style = MaterialTheme.typography.titleLarge, color = Indigo)
                    } else {
                        Text(stringResource(R.string.zakat_below), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text(stringResource(R.string.zakat_disclaimer), style = MaterialTheme.typography.bodyMedium, color = BrownMid)
        }
    }
}

private fun fmt(v: Double) = String.format(Locale.US, "%,.2f", v)

@Composable
private fun Field(label: Int, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
