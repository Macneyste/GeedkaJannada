package com.geedkajannada

import com.geedkajannada.data.CounterLogic
import com.geedkajannada.data.GoalValidation
import com.geedkajannada.data.GoalValidator
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerMath
import com.geedkajannada.data.QuranParser
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.reminder.ReminderPlan
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterLogicTest {
    @Test fun firstTapAccepted() = assertTrue(CounterLogic.acceptTap(1000, null))
    @Test fun tapUnder120msRejected() = assertFalse(CounterLogic.acceptTap(1119, 1000))
    @Test fun tapAt120msAccepted() = assertTrue(CounterLogic.acceptTap(1120, 1000))
    @Test fun ratioIsCapped() {
        assertEquals(1f, CounterLogic.ratio(50, 33), 0f)
        assertEquals(0.5f, CounterLogic.ratio(5, 10), 0f)
        assertEquals(0f, CounterLogic.ratio(5, 0), 0f)
    }
    @Test fun goalReachedOnlyOnExactMatch() {
        assertTrue(CounterLogic.reachedGoal(33, 33))
        assertFalse(CounterLogic.reachedGoal(34, 33))
        assertFalse(CounterLogic.reachedGoal(32, 33))
    }
}

class GoalValidatorTest {
    @Test fun valid() = assertEquals(GoalValidation.Valid(100), GoalValidator.validate(" 100 "))
    @Test fun bounds() {
        assertEquals(GoalValidation.Valid(1), GoalValidator.validate("1"))
        assertEquals(GoalValidation.Valid(9999), GoalValidator.validate("9999"))
    }
    @Test fun rejectsZeroAndTooBig() {
        assertEquals(GoalValidation.OutOfRange, GoalValidator.validate("0"))
        assertEquals(GoalValidation.OutOfRange, GoalValidator.validate("10000"))
        assertEquals(GoalValidation.OutOfRange, GoalValidator.validate("99999999999999"))
    }
    @Test fun rejectsNonDigits() {
        assertEquals(GoalValidation.NotNumber, GoalValidator.validate("12a"))
        assertEquals(GoalValidation.NotNumber, GoalValidator.validate("-5"))
        assertEquals(GoalValidation.NotNumber, GoalValidator.validate("3.5"))
    }
    @Test fun rejectsEmpty() = assertEquals(GoalValidation.Empty, GoalValidator.validate("  "))
}

class StatsCalcTest {
    private val today = LocalDate.of(2025, 3, 10)
    @Test fun streakCountsConsecutiveDays() {
        val daily = mapOf(
            "2025-03-10" to mapOf("a" to 5),
            "2025-03-09" to mapOf("a" to 1, "b" to 2),
            "2025-03-08" to mapOf("b" to 1),
            "2025-03-06" to mapOf("a" to 9),
        )
        val s = StatsCalc.summarize(daily, today)
        assertEquals(3, s.streak)
        assertEquals(5, s.today)
        assertEquals(5 + 3 + 1 + 9, s.week)
        assertEquals(7, s.last7.size)
        assertEquals(today, s.last7.last().date)
    }
    @Test fun streakContinuesFromYesterdayIfTodayEmpty() {
        val s = StatsCalc.summarize(mapOf("2025-03-09" to mapOf("a" to 1)), today)
        assertEquals(1, s.streak)
    }
    @Test fun noDataMeansZero() {
        val s = StatsCalc.summarize(emptyMap(), today)
        assertEquals(0, s.streak)
        assertEquals(0, s.week)
    }
    @Test fun perDhikrTotals() {
        val s = StatsCalc.summarize(
            mapOf("2025-03-10" to mapOf("a" to 2), "2025-01-01" to mapOf("a" to 3)), today,
        )
        assertEquals(5, s.perDhikr["a"])
    }
}

class ChallengesTest {
    @Test fun bestStreakAndMilestones() {
        val daily = (0..6).associate { "2025-02-" + "%02d".format(10 + it) to mapOf("a" to 1) } +
            mapOf("2025-03-10" to mapOf("a" to 1))
        val s = StatsCalc.summarize(daily, LocalDate.of(2025, 3, 10))
        assertEquals(1, s.streak)
        assertEquals(7, s.bestStreak)
        assertEquals(listOf(3, 7), com.geedkajannada.data.Challenges.achieved(s.bestStreak))
        assertEquals(14, com.geedkajannada.data.Challenges.next(s.bestStreak))
        assertNull(com.geedkajannada.data.Challenges.next(100))
    }
}

class PrayerMathTest {
    private fun e(p: Prayer, h: Int, m: Int = 0) = PrayerEntry(p, h * 60 + m, false)
    private val all = listOf(e(Prayer.SUBAX, 5), e(Prayer.DUHUR, 12), e(Prayer.CISHE, 20))

    @Test fun nextToday() {
        val (entry, secs) = PrayerMath.next(all, 10 * 60)!!
        assertEquals(Prayer.DUHUR, entry.prayer)
        assertEquals(2 * 3600, secs)
    }
    @Test fun wrapsToTomorrow() {
        val (entry, secs) = PrayerMath.next(all, 21 * 60)!!
        assertEquals(Prayer.SUBAX, entry.prayer)
        assertEquals(8 * 3600, secs)
    }
    @Test fun noneSet() = assertNull(PrayerMath.next(listOf(PrayerEntry(Prayer.SUBAX, null, false)), 0))
    @Test fun formats() {
        assertEquals("05:07", PrayerMath.format(5 * 60 + 7))
        assertEquals("1:02:03", PrayerMath.formatRemaining(3723))
        assertEquals("02:03", PrayerMath.formatRemaining(123))
    }
}

class ReminderPlanTest {
    private val zone = ZoneId.of("Africa/Mogadishu")
    private val day = java.time.LocalDate.of(2025, 3, 10)
    private fun entries(notify: Boolean = true) =
        listOf(PrayerEntry(Prayer.SUBAX, 5 * 60, notify), PrayerEntry(Prayer.DUHUR, 12 * 60, notify))
    private val days = (0L until 10L).map { day.plusDays(it) to entries() }
    private fun at(h: Int, m: Int) = ZonedDateTime.of(2025, 3, 10, h, m, 0, 0, zone)

    @Test fun pastPrayersAreSkippedAndOnlySevenDaysPlanned() {
        val plan = ReminderPlan.plan(days, 0, at(8, 0))
        assertEquals(13, plan.size) // 7 x 2, ka yar Subax-kii maanta ee dhaafay
        assertEquals(at(12, 0).toInstant().toEpochMilli(), plan.first().atMillis)
        assertEquals(plan.size, plan.map { it.requestCode }.toSet().size)
        assertTrue(plan.all { it.requestCode in ReminderPlan.REQUEST_CODES })
    }

    @Test fun earlyAlarmIsAddedBeforeThePrayer() {
        val plan = ReminderPlan.plan(days.take(1), 15, at(8, 0))
        assertEquals(listOf(0, 15), plan.map { it.earlyMinutes })
        assertEquals(at(11, 45).toInstant().toEpochMilli(), plan[1].atMillis)
    }

    @Test fun switchedOffPrayersAreNotPlanned() {
        assertTrue(ReminderPlan.plan(listOf(day to entries(notify = false)), 5, at(0, 0)).isEmpty())
    }

    @Test fun staleRules() {
        val prayer = at(12, 0).toInstant().toEpochMilli()
        assertFalse(ReminderPlan.isStale(prayer + 60_000, prayer, prayer, 0))
        assertTrue(ReminderPlan.isStale(prayer + ReminderPlan.STALE_AFTER_MS + 1, prayer, prayer, 0))
        assertTrue(ReminderPlan.isStale(prayer, prayer - 5 * 60_000, prayer, 5))
    }
}

class QuranParserTest {
    private val json = """[
      {"id":1,"so":"Al-Faatixa","ar":"الفاتحة","type":"makki","n":2,"v":["a","b"]},
      {"id":2,"so":"Al-Baqara","ar":"البقرة","type":"madani","n":1,"v":["c"]}]"""

    @Test fun parses() {
        val list = QuranParser.parse(json)
        assertEquals(2, list.size)
        assertTrue(list[0].surah.isMakki)
        assertFalse(list[1].surah.isMakki)
        assertEquals(listOf("a", "b"), list[0].verses)
    }
    @Test fun filtersByNameNumberAndArabic() {
        val s = QuranParser.parse(json).map { it.surah }
        assertEquals(1, QuranParser.filter(s, "faatix").size)
        assertEquals(1, QuranParser.filter(s, "al baqara".replace(" ", "-")).size)
        assertEquals(1, QuranParser.filter(s, "2").size)
        assertEquals(1, QuranParser.filter(s, "البقرة").size)
        assertEquals(2, QuranParser.filter(s, "").size)
        assertEquals(0, QuranParser.filter(s, "zzz").size)
    }
}
