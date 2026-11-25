package com.geedkajannada

import com.geedkajannada.data.Juz
import org.junit.Assert.assertEquals
import org.junit.Test

class JuzTest {
    @Test fun thirtyStartsInOrder() {
        assertEquals(30, Juz.starts.size)
        val sorted = Juz.starts.sortedWith(compareBy({ it.first }, { it.second }))
        assertEquals(sorted, Juz.starts)
    }

    @Test fun juzOfKnownVerses() {
        assertEquals(1, Juz.of(1, 1))
        assertEquals(1, Juz.of(2, 141))
        assertEquals(2, Juz.of(2, 142))
        assertEquals(6, Juz.of(4, 148))
        assertEquals(30, Juz.of(112, 1))
        assertEquals(30, Juz.of(114, 6))
    }
}

class PrayerCurrentTest {
    private fun e(p: com.geedkajannada.data.Prayer, m: Int?) = com.geedkajannada.data.PrayerEntry(p, m, false)
    private val list = listOf(
        e(com.geedkajannada.data.Prayer.SUBAX, 300), e(com.geedkajannada.data.Prayer.DUHUR, 720),
        e(com.geedkajannada.data.Prayer.CASAR, null),
    )

    @Test fun currentIsLatestPassed() {
        assertEquals(com.geedkajannada.data.Prayer.DUHUR, com.geedkajannada.data.PrayerMath.current(list, 800)?.prayer)
    }

    @Test fun beforeFirstWrapsToLast() {
        assertEquals(com.geedkajannada.data.Prayer.DUHUR, com.geedkajannada.data.PrayerMath.current(list, 100)?.prayer)
    }

    @Test fun noneSetIsNull() {
        assertEquals(null, com.geedkajannada.data.PrayerMath.current(listOf(e(com.geedkajannada.data.Prayer.CASAR, null)), 100))
    }
}
