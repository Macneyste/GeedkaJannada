package com.geedkajannada.ui.zakat

import androidx.lifecycle.ViewModel
import com.geedkajannada.data.ZakatCalc
import com.geedkajannada.data.ZakatResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ZakatUiState(
    val cash: String = "",
    val gold: String = "",
    val debts: String = "",
    val nisab: String = "",
    val rate: String = "2.5",
    /** null = gelinta waa khalad. */
    val result: ZakatResult? = null,
    val invalid: Boolean = false,
)

class ZakatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ZakatUiState())
    val uiState: StateFlow<ZakatUiState> = _state

    fun onCash(v: String) = change { copy(cash = v) }
    fun onGold(v: String) = change { copy(gold = v) }
    fun onDebts(v: String) = change { copy(debts = v) }
    fun onNisab(v: String) = change { copy(nisab = v) }
    fun onRate(v: String) = change { copy(rate = v) }

    private fun change(block: ZakatUiState.() -> ZakatUiState) {
        _state.update { recompute(it.block()) }
    }

    companion object {
        fun recompute(s: ZakatUiState): ZakatUiState {
            val cash = ZakatCalc.parseAmount(s.cash)
            val gold = ZakatCalc.parseAmount(s.gold)
            val debts = ZakatCalc.parseAmount(s.debts)
            val nisab = ZakatCalc.parseAmount(s.nisab)
            val rate = ZakatCalc.parseAmount(s.rate)
            if (cash == null || gold == null || debts == null || nisab == null || rate == null || rate > 100) {
                return s.copy(result = null, invalid = true)
            }
            return s.copy(result = ZakatCalc.compute(cash, gold, debts, nisab, rate), invalid = false)
        }
    }
}
