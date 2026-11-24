package com.geedkajannada

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.ui.counter.CounterEvent
import com.geedkajannada.ui.counter.CounterViewModel
import com.geedkajannada.ui.counter.CustomGoalError
import com.geedkajannada.ui.counter.PickerViewModel
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

    @Test fun prayerAndBookmarkRoundTrip() = runTest {
        val s = newStore()
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

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {
    private lateinit var dir: File
    private val dispatcher = StandardTestDispatcher()
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        dir = Files.createTempDirectory("vm").toFile()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
        scope.cancel()
        dir.deleteRecursively()
    }

    private fun store() = SettingsStore(PreferenceDataStoreFactory.create(scope = scope) { File(dir, "s.preferences_pb") })

    private fun TestScope.collectState(vm: CounterViewModel) {
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
    }

    @Test fun tapIncrementsAndDebounces() = runTest(dispatcher) {
        var now = 1000L
        val st = store()
        val vm = CounterViewModel(st, { "2025-03-10" }, { now })
        collectState(vm)
        advanceUntilIdle()
        vm.tap(); advanceUntilIdle()
        now = 1050; vm.tap(); advanceUntilIdle()   // <120ms: diidan
        now = 1200; vm.tap(); advanceUntilIdle()
        assertEquals(2, st.count("subxaanallaah").first())
        assertEquals(2, vm.uiState.value.count)
    }

    @Test fun goalReachedShowsDialogAndDoesNotAutoReset() = runTest(dispatcher) {
        var now = 0L
        val st = store()
        st.setGoal("subxaanallaah", 2)
        val vm = CounterViewModel(st, { "2025-03-10" }, { now })
        collectState(vm)
        advanceUntilIdle()
        vm.tap(); advanceUntilIdle()
        assertFalse(vm.uiState.value.showGoalReached)
        now += 200; vm.tap(); advanceUntilIdle()
        assertTrue(vm.uiState.value.showGoalReached)
        assertEquals(2, vm.uiState.value.count)

        vm.continueAfterGoal(); advanceUntilIdle()
        assertFalse(vm.uiState.value.showGoalReached)
        assertEquals(2, vm.uiState.value.count)

        vm.startNewRound(); advanceUntilIdle()
        assertEquals(0, vm.uiState.value.count)
        assertEquals(1, vm.uiState.value.rounds)
    }

    @Test fun resetRequiresConfirmation() = runTest(dispatcher) {
        val st = store()
        val vm = CounterViewModel(st, { "2025-03-10" }, { 0L })
        collectState(vm)
        advanceUntilIdle()
        vm.tap(); advanceUntilIdle()
        vm.requestReset(); advanceUntilIdle()
        assertTrue(vm.uiState.value.showResetConfirm)
        assertEquals(1, vm.uiState.value.count)
        vm.dismissReset(); advanceUntilIdle()
        assertEquals(1, vm.uiState.value.count)
        vm.requestReset(); vm.confirmReset(); advanceUntilIdle()
        assertEquals(0, vm.uiState.value.count)
    }

    @Test fun eventEmittedOnTap() = runTest(dispatcher) {
        val st = store()
        val vm = CounterViewModel(st, { "2025-03-10" }, { 0L })
        collectState(vm)
        advanceUntilIdle()
        val got = mutableListOf<CounterEvent>()
        val job = backgroundScope.launch(dispatcher) { vm.events.collect { got += it } }
        advanceUntilIdle()
        vm.tap(); advanceUntilIdle()
        job.cancel()
        assertEquals(listOf<CounterEvent>(CounterEvent.Tapped(false)), got)
    }

    @Test fun pickerValidatesCustomGoal() = runTest(dispatcher) {
        val st = store()
        val vm = PickerViewModel(st)
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onCustomInput("0"); assertFalse(vm.submitCustomGoal()); advanceUntilIdle()
        assertEquals(CustomGoalError.OutOfRange, vm.uiState.value.error)
        vm.onCustomInput("abc"); assertFalse(vm.submitCustomGoal()); advanceUntilIdle()
        assertEquals(CustomGoalError.NotNumber, vm.uiState.value.error)
        vm.onCustomInput("250"); assertTrue(vm.submitCustomGoal()); advanceUntilIdle()
        assertEquals(250, vm.uiState.value.goal)
        assertEquals(250, st.goal(Dhikrs.all.first()).first())
    }
}
