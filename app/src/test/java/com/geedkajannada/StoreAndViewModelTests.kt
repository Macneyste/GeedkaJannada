package com.geedkajannada

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.SettingsStore
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreTest {
    private lateinit var dir: File
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Before fun setUp() { dir = Files.createTempDirectory("ds").toFile() }
    @After fun tearDown() { scope.cancel(); dir.deleteRecursively() }

    private fun newStore(): SettingsStore =
        SettingsStore(PreferenceDataStoreFactory.create(scope = scope) { File(dir, "s.preferences_pb") })

    @Test fun defaults() = runTest {
        val s = newStore()
        assertTrue(s.soundEnabled.first())
        assertFalse(s.hapticsEnabled.first())
        assertEquals(0, s.count("subxaanallaah").first())
        assertEquals(33, s.goal(Dhikrs.all.first()).first())
    }

    @Test fun incrementIsPersistentAcrossInstances() = runTest {
        val s = newStore()
        assertEquals(1, s.increment("subxaanallaah", "2025-03-10"))
        assertEquals(2, s.increment("subxaanallaah", "2025-03-10"))
        assertEquals(2, s.count("subxaanallaah").first())
        assertEquals(mapOf("2025-03-10" to mapOf("subxaanallaah" to 2)), s.dailyTotals.first())
    }

    @Test fun countsAreIndependentPerDhikr() = runTest {
        val s = newStore()
        s.increment("subxaanallaah", "2025-03-10")
        s.increment("alxamdulillaah", "2025-03-10")
        s.increment("alxamdulillaah", "2025-03-10")
        s.selectDhikr("subxaanallaah")
        assertEquals(1, s.count("subxaanallaah").first())
        assertEquals(2, s.count("alxamdulillaah").first())
    }

    @Test fun resetKeepsDailyTotalsAndNewRoundCountsRounds() = runTest {
        val s = newStore()
        s.increment("allaahu_akbar", "2025-03-10")
        s.reset("allaahu_akbar")
        assertEquals(0, s.count("allaahu_akbar").first())
        assertEquals(1, s.dailyTotals.first()["2025-03-10"]?.get("allaahu_akbar"))
        s.newRound("allaahu_akbar")
        assertEquals(1, s.rounds("allaahu_akbar").first())
    }

    @Test fun autoModeComputesTimesAndManualModeKeepsThem() = runTest {
        val s = newStore()
        val auto = s.prayerEntries.first()
        assertTrue(auto.all { it.minuteOfDay != null })
        s.setPrayerCity("hargeysa")
        assertEquals("hargeysa", s.prayerCity.first().id)
        val hargeysaDhuhr = s.prayerEntries.first().first { it.prayer == Prayer.DUHUR }.minuteOfDay
        s.setPrayerAuto(false)
        assertEquals(hargeysaDhuhr, s.prayerEntries.first().first { it.prayer == Prayer.DUHUR }.minuteOfDay)
        s.setPrayerTime(Prayer.DUHUR, 13 * 60)
        assertEquals(13 * 60, s.prayerEntries.first().first { it.prayer == Prayer.DUHUR }.minuteOfDay)
    }

    @Test fun prayerAndBookmarkRoundTrip() = runTest {
        val s = newStore()
        s.setPrayerAuto(false)
        s.setPrayerTime(Prayer.CASAR, 15 * 60 + 30)
        s.setPrayerNotify(Prayer.CASAR, true)
        val e = s.prayerEntries.first().first { it.prayer == Prayer.CASAR }
        assertEquals(15 * 60 + 30, e.minuteOfDay)
        assertTrue(e.notify)
        s.setBookmark(18, 10)
        assertEquals(18 to 10, s.bookmark.first())
        s.clearBookmark()
        assertEquals(null, s.bookmark.first())
    }
}

class PrayerCheckTests {
    @org.junit.Rule @JvmField val tmp = org.junit.rules.TemporaryFolder()

    @Test fun toggleChecksAndUnchecks() = kotlinx.coroutines.test.runTest {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + kotlinx.coroutines.SupervisorJob())
        val store = SettingsStore(
            androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(scope = scope) {
                java.io.File(tmp.root, "c.preferences_pb")
            },
        )
        store.toggleChecked("2026-01-01", com.geedkajannada.data.Prayer.SUBAX)
        org.junit.Assert.assertEquals(setOf(com.geedkajannada.data.Prayer.SUBAX), store.checkedPrayers("2026-01-01").first())
        org.junit.Assert.assertTrue(store.checkedPrayers("2026-01-02").first().isEmpty())
        store.toggleChecked("2026-01-01", com.geedkajannada.data.Prayer.SUBAX)
        org.junit.Assert.assertTrue(store.checkedPrayers("2026-01-01").first().isEmpty())
        scope.coroutineContext[kotlinx.coroutines.Job]!!.cancel()
    }
}
