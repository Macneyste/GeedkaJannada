package com.geedkajannada

import com.geedkajannada.data.Hijri
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HijriTest {
    @Test fun knownDate() {
        // 1 Muharram 1446 = 2024-07-07 (Umm al-Qura)
        val h = Hijri.of(LocalDate.of(2024, 7, 7))
        assertEquals(1446, h.year)
        assertEquals(1, h.month)
        assertEquals(1, h.day)
    }

    @Test fun fieldsInRange() {
        val h = Hijri.of(LocalDate.of(2026, 11, 25))
        assertTrue(h.month in 1..12)
        assertTrue(h.day in 1..30)
    }
}
