package com.geedkajannada

import com.geedkajannada.data.QiblaMath
import com.geedkajannada.data.ZakatCalc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaZakatTest {
    @Test fun qiblaFromMogadishuIsNorthNorthWest() {
        val b = QiblaMath.bearing(2.0469, 45.3182)
        assertTrue("bearing=$b", b in 343.0..347.0)
    }

    @Test fun qiblaFromLondon() {
        val b = QiblaMath.bearing(51.5074, -0.1278)
        assertTrue("bearing=$b", b in 117.0..120.0)
    }

    @Test fun relativeWrapsAround() {
        assertEquals(10.0, QiblaMath.relative(5.0, 355.0), 1e-9)
        assertEquals(-10.0, QiblaMath.relative(355.0, 5.0), 1e-9)
        assertEquals(0.0, QiblaMath.relative(90.0, 90.0), 1e-9)
    }

    @Test fun zakatDueWhenAboveNisab() {
        val r = ZakatCalc.compute(cash = 10_000.0, gold = 0.0, debts = 2_000.0, nisab = 5_000.0, ratePercent = 2.5)
        assertTrue(r.eligible)
        assertEquals(8_000.0, r.net, 1e-9)
        assertEquals(200.0, r.due, 1e-9)
    }

    @Test fun zakatNotDueBelowNisab() {
        val r = ZakatCalc.compute(1_000.0, 0.0, 0.0, 5_000.0, 2.5)
        assertFalse(r.eligible)
        assertEquals(0.0, r.due, 1e-9)
    }

    @Test fun debtsLargerThanAssetsGiveZero() {
        val r = ZakatCalc.compute(100.0, 0.0, 500.0, 0.0, 2.5)
        assertEquals(0.0, r.net, 1e-9)
        assertFalse(r.eligible)
    }

    @Test fun parseAmountRules() {
        assertEquals(0.0, ZakatCalc.parseAmount("")!!, 0.0)
        assertEquals(12.5, ZakatCalc.parseAmount("12,5")!!, 0.0)
        assertNull(ZakatCalc.parseAmount("abc"))
        assertNull(ZakatCalc.parseAmount("-3"))
    }
}
