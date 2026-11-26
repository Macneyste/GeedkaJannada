package com.geedkajannada

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.feature.tasbeeh.TasbeehEvent
import com.geedkajannada.feature.tasbeeh.TasbeehRepository
import com.geedkajannada.feature.tasbeeh.TasbeehSound
import com.geedkajannada.feature.tasbeeh.TasbeehViewModel
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasbeehTest {
    private lateinit var dir: File
    private val dispatcher = StandardTestDispatcher()
    private val scopes = mutableListOf<CoroutineScope>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        dir = Files.createTempDirectory("tasbeeh").toFile()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
        scopes.forEach { it.cancel() }
        dir.deleteRecursively()
    }

    private fun dataStore() = CoroutineScope(Dispatchers.Unconfined + SupervisorJob()).also { scopes += it }.let { scope ->
        PreferenceDataStoreFactory.create(scope = scope) { File(dir, "t.preferences_pb") }
    }

    private suspend fun TasbeehRepository.tapTimes(n: Int) = repeat(n) { tap("2025-03-10") }

    @Test fun thirtyThirdTapMovesToNextDhikr() = runTest {
        val repo = TasbeehRepository(dataStore())
        repo.tapTimes(32)
        assertEquals(0, repo.data.first().index)
        val outcome = repo.tap("2025-03-10")!!
        assertTrue(outcome.jarFilled)
        assertEquals(false, outcome.completed)
        assertEquals(1, repo.data.first().index)
        assertEquals(listOf(33, 0, 0), repo.data.first().counts)
    }

    @Test fun ninetyNinthTapCompletesAndStopsCounting() = runTest {
        val repo = TasbeehRepository(dataStore())
        repo.tapTimes(98)
        assertTrue(repo.tap("2025-03-10")!!.completed)
        assertNull(repo.tap("2025-03-10"))
        val d = repo.data.first()
        assertEquals(listOf(33, 33, 33), d.counts)
        assertEquals(1, d.totalCompleted)
    }

    @Test fun resetClearsJarsButKeepsTotalCompleted() = runTest {
        val repo = TasbeehRepository(dataStore())
        repo.tapTimes(99)
        repo.reset()
        val d = repo.data.first()
        assertEquals(listOf(0, 0, 0), d.counts)
        assertEquals(0, d.index)
        assertEquals(1, d.totalCompleted)
    }

    @Test fun nextKeepsCountsAndSkippedJarIsRevisited() = runTest {
        val repo = TasbeehRepository(dataStore())
        repo.tapTimes(5)
        assertTrue(repo.skipToNext())
        assertEquals(1, repo.data.first().index)
        assertEquals(listOf(5, 0, 0), repo.data.first().counts)
        repo.tapTimes(33) // Alxamdulillaah
        repo.tapTimes(33) // Allaahu Akbar -> ku laabo Subxaanallaah
        assertEquals(0, repo.data.first().index)
    }

    @Test fun stateSurvivesRestart() = runBlocking {
        val first = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
        val file = File(dir, "r.preferences_pb")
        TasbeehRepository(PreferenceDataStoreFactory.create(scope = first) { file }).tapTimes(40)
        first.coroutineContext[Job]!!.cancelAndJoin() // "app-ka xir"

        val second = CoroutineScope(Dispatchers.Unconfined + SupervisorJob()).also { scopes += it }
        val d = TasbeehRepository(PreferenceDataStoreFactory.create(scope = second) { file }).data.first()
        assertEquals(1, d.index)
        assertEquals(listOf(33, 7, 0), d.counts)
    }

    private fun TestScope.observe(vm: TasbeehViewModel): MutableList<TasbeehEvent> {
        val events = mutableListOf<TasbeehEvent>()
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
        backgroundScope.launch(dispatcher) { vm.events.collect { events += it } }
        advanceUntilIdle()
        return events
    }

    private fun sounds(events: List<TasbeehEvent>) = events.filterIsInstance<TasbeehEvent.Sound>().map { it.sound }

    @Test fun mutedViewModelEmitsNoSoundButStillCounts() = runTest(dispatcher) {
        val ds = dataStore()
        val settings = SettingsStore(ds)
        var now = 0L
        val vm = TasbeehViewModel(TasbeehRepository(ds), settings, { "2025-03-10" }, { now })
        val events = observe(vm)

        vm.tap(); advanceUntilIdle()
        assertEquals(listOf(TasbeehSound.TAP, TasbeehSound.BEAD), sounds(events))

        vm.toggleSound(); advanceUntilIdle()
        events.clear()
        now += 200; vm.tap(); advanceUntilIdle()
        assertEquals(emptyList<TasbeehSound>(), sounds(events))
        assertEquals(2, vm.uiState.value.current.count)
    }

    @Test fun debounceAndFilledJarSounds() = runTest(dispatcher) {
        val ds = dataStore()
        val repo = TasbeehRepository(ds)
        repo.tapTimes(32)
        var now = 1000L
        val vm = TasbeehViewModel(repo, SettingsStore(ds), { "2025-03-10" }, { now })
        val events = observe(vm)

        vm.tap(); now += 50; vm.tap(); advanceUntilIdle() // tan labaad waa la diidaa (<120ms)
        assertEquals(listOf(TasbeehSound.CHIME, TasbeehSound.NEXT), sounds(events))
        assertEquals(Dhikrs.all[1].id, vm.uiState.value.current.dhikr.id)
        assertEquals(0, vm.uiState.value.current.count)
    }
}
