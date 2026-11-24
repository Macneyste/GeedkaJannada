package com.geedkajannada.data

/** Mantiqa saafi ah ee tirinta: debounce iyo xaqiijinta yoolka. */
object CounterLogic {
    const val MIN_TAP_INTERVAL_MS = 120L
    const val MIN_GOAL = 1
    const val MAX_GOAL = 9999

    /** Taabasho ka yar 120ms ka dib kii hore waa la diidaa. */
    fun acceptTap(nowMs: Long, lastAcceptedMs: Long?): Boolean =
        lastAcceptedMs == null || nowMs - lastAcceptedMs >= MIN_TAP_INTERVAL_MS

    fun ratio(count: Int, goal: Int): Float =
        if (goal <= 0) 0f else minOf(1f, count.toFloat() / goal)

    /** Yoolka waa la gaaray marka tirada cusub ay si sax ah u le'eg yoolka. */
    fun reachedGoal(newCount: Int, goal: Int): Boolean = newCount == goal
}

sealed interface GoalValidation {
    data class Valid(val goal: Int) : GoalValidation
    data object Empty : GoalValidation
    data object NotNumber : GoalValidation
    data object OutOfRange : GoalValidation
}

object GoalValidator {
    fun validate(input: String): GoalValidation {
        val t = input.trim()
        if (t.isEmpty()) return GoalValidation.Empty
        if (!t.all { it in '0'..'9' }) return GoalValidation.NotNumber
        val n = t.toIntOrNull() ?: return GoalValidation.OutOfRange
        return if (n in CounterLogic.MIN_GOAL..CounterLogic.MAX_GOAL) GoalValidation.Valid(n)
        else GoalValidation.OutOfRange
    }
}
