package com.geedkajannada

import com.geedkajannada.data.Cities
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerTimesCalc
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesCalcTest {
    private fun hm(h: Int, m: Int) = h * 60 + m

    @Test fun mogadishuMarchTimesAreInPlausibleRanges() {
        val t = PrayerTimesCalc.compute(LocalDate.of(2025, 3, 10), Cities.byId("muqdisho"))
        assertTrue("fajr ${t[Prayer.SUBAX]}", t.getValue(Prayer.SUBAX) in hm(4, 50)..hm(5, 20))
        assertTrue("dhuhr ${t[Prayer.DUHUR]}", t.getValue(Prayer.DUHUR) in hm(12, 5)..hm(12, 15))
        assertTrue("asr ${t[Prayer.CASAR]}", t.getValue(Prayer.CASAR) in hm(15, 15)..hm(15, 40))
        assertTrue("maghrib ${t[Prayer.MAGHRIB]}", t.getValue(Prayer.MAGHRIB) in hm(18, 10)..hm(18, 30))
        assertTrue("isha ${t[Prayer.CISHE]}", t.getValue(Prayer.CISHE) in hm(19, 15)..hm(19, 35))
    }

    @Test fun timesAreOrderedThroughTheDay() {
        for (c in Cities.all) for (day in listOf(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 6, 21))) {
            val t = PrayerTimesCalc.compute(day, c)
            val seq = listOf(Prayer.SUBAX, Prayer.DUHUR, Prayer.CASAR, Prayer.MAGHRIB, Prayer.CISHE).map { t.getValue(it) }
            assertEquals("${c.id} $day $seq", seq.sorted(), seq)
        }
    }

    @Test fun hargeysaIsEarlierThanBoosaasoByLongitude() {
        val d = LocalDate.of(2025, 3, 10)
        val h = PrayerTimesCalc.compute(d, Cities.byId("hargeysa")).getValue(Prayer.DUHUR)
        val b = PrayerTimesCalc.compute(d, Cities.byId("boosaaso")).getValue(Prayer.DUHUR)
        assertTrue(h > b)
    }

    @Test fun unknownCityFallsBackToMogadishu() = assertEquals("muqdisho", Cities.byId("x").id)
}
