package com.geedkajannada.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.Dhikr
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.GoalValidation
import com.geedkajannada.data.GoalValidator
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PickerUiState(
    val selected: Dhikr = Dhikrs.all.first(),
    val goal: Int = Dhikrs.all.first().defaultGoal,
    val customInput: String = "",
    val error: CustomGoalError? = null,
)

enum class CustomGoalError { Empty, NotNumber, OutOfRange }

@OptIn(ExperimentalCoroutinesApi::class)
class PickerViewModel(private val store: SettingsStore) : ViewModel() {
    private val input = MutableStateFlow(Pair("", null as CustomGoalError?))

    val uiState: StateFlow<PickerUiState> = combine(
        store.selectedDhikrId.flatMapLatest { id ->
            val d = Dhikrs.byId(id)
            store.goal(d).let { g -> combine(g, store.selectedDhikrId) { goal, _ -> d to goal } }
        },
        input,
    ) { (d, g), (text, err) -> PickerUiState(d, g, text, err) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickerUiState())

    fun select(d: Dhikr) {
        input.value = "" to null
        viewModelScope.launch { store.selectDhikr(d.id) }
    }

    fun setPresetGoal(goal: Int) {
        input.value = "" to null
        viewModelScope.launch { store.setGoal(uiState.value.selected.id, goal) }
    }

    fun onCustomInput(text: String) {
        input.value = text to null
    }

    /** Xaqiiji yoolka gaarka ah (1..9999). Soo celi true haddii la kaydiyay. */
    fun submitCustomGoal(): Boolean {
        val text = input.value.first
        return when (val r = GoalValidator.validate(text)) {
            is GoalValidation.Valid -> {
                viewModelScope.launch { store.setGoal(uiState.value.selected.id, r.goal) }
                input.value = "" to null
                true
            }
            GoalValidation.Empty -> { input.value = text to CustomGoalError.Empty; false }
            GoalValidation.NotNumber -> { input.value = text to CustomGoalError.NotNumber; false }
            GoalValidation.OutOfRange -> { input.value = text to CustomGoalError.OutOfRange; false }
        }
    }
}
