package com.geedkajannada.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Dhammaan xogta isticmaalaha waxay ku kaydsan tahay DataStore (qalabka kaliya). */
class SettingsStore(val dataStore: DataStore<Preferences>) {

    private object Keys {
        val selectedDhikr = stringPreferencesKey("selected_dhikr")
        val sound = booleanPreferencesKey("sound_enabled")
        val haptics = booleanPreferencesKey("haptics_enabled")
        val reminderOffset = intPreferencesKey("reminder_offset")
        val quranFontSize = intPreferencesKey("quran_font_size")
        val lastSurah = intPreferencesKey("last_surah")
        val lastVerse = intPreferencesKey("last_verse")
        val bookmarkSurah = intPreferencesKey("bookmark_surah")
        val bookmarkVerse = intPreferencesKey("bookmark_verse")
        fun count(id: String) = intPreferencesKey("count_$id")
        fun goal(id: String) = intPreferencesKey("goal_$id")
        fun rounds(id: String) = intPreferencesKey("rounds_$id")
        fun day(dateKey: String, id: String) = intPreferencesKey("day_${dateKey}_$id")
        fun prayerTime(key: String) = intPreferencesKey("prayer_time_$key")
        fun prayerNotify(key: String) = booleanPreferencesKey("prayer_notify_$key")
    }

    // ---- Dikhri ----
    val selectedDhikrId: Flow<String> =
        dataStore.data.map { it[Keys.selectedDhikr] ?: Dhikrs.all.first().id }

    suspend fun selectDhikr(id: String) {
        dataStore.edit { it[Keys.selectedDhikr] = id }
    }

    fun count(id: String): Flow<Int> = dataStore.data.map { it[Keys.count(id)] ?: 0 }

    fun goal(dhikr: Dhikr): Flow<Int> =
        dataStore.data.map { it[Keys.goal(dhikr.id)] ?: dhikr.defaultGoal }

    fun rounds(id: String): Flow<Int> = dataStore.data.map { it[Keys.rounds(id)] ?: 0 }

    suspend fun setGoal(id: String, goal: Int) {
        dataStore.edit { it[Keys.goal(id)] = goal }
    }

    /** Hal edit oo atomic ah: tirada + wadarta maalinta. Waxay soo celisaa tirada cusub. */
    suspend fun increment(id: String, dateKey: String): Int {
        var result = 0
        dataStore.edit { p ->
            val n = (p[Keys.count(id)] ?: 0) + 1
            p[Keys.count(id)] = n
            val d = Keys.day(dateKey, id)
            p[d] = (p[d] ?: 0) + 1
            result = n
        }
        return result
    }

    suspend fun reset(id: String) {
        dataStore.edit { it[Keys.count(id)] = 0 }
    }

    /** Wareeg cusub: tirada eber, wareegyada +1. */
    suspend fun newRound(id: String) {
        dataStore.edit {
            it[Keys.count(id)] = 0
            it[Keys.rounds(id)] = (it[Keys.rounds(id)] ?: 0) + 1
        }
    }

    // ---- Dejimaha ----
    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.sound] ?: false }
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.haptics] ?: false }
    suspend fun setSound(v: Boolean) {
        dataStore.edit { it[Keys.sound] = v }
    }

    suspend fun setHaptics(v: Boolean) {
        dataStore.edit { it[Keys.haptics] = v }
    }

    // ---- Salaad ----
    val prayerEntries: Flow<List<PrayerEntry>> = dataStore.data.map { p ->
        Prayer.entries.map { pr ->
            val t = p[Keys.prayerTime(pr.key)]
            PrayerEntry(pr, t?.takeIf { it >= 0 }, p[Keys.prayerNotify(pr.key)] ?: false)
        }
    }
    val reminderOffset: Flow<Int> = dataStore.data.map { it[Keys.reminderOffset] ?: 0 }

    suspend fun setPrayerTime(prayer: Prayer, minuteOfDay: Int) {
        dataStore.edit { it[Keys.prayerTime(prayer.key)] = minuteOfDay }
    }

    suspend fun setPrayerNotify(prayer: Prayer, on: Boolean) {
        dataStore.edit { it[Keys.prayerNotify(prayer.key)] = on }
    }

    suspend fun setReminderOffset(minutes: Int) {
        dataStore.edit { it[Keys.reminderOffset] = minutes }
    }

    // ---- Quraan ----
    val quranFontSize: Flow<Int> = dataStore.data.map { it[Keys.quranFontSize] ?: 28 }

    suspend fun setQuranFontSize(sp: Int) {
        dataStore.edit { it[Keys.quranFontSize] = sp.coerceIn(18, 48) }
    }

    /** (suurad, aayad) la akhriyay ugu dambeysay, ama null. */
    val lastRead: Flow<Pair<Int, Int>?> = dataStore.data.map { p ->
        val s = p[Keys.lastSurah]
        if (s == null) null else s to (p[Keys.lastVerse] ?: 1)
    }

    suspend fun setLastRead(surah: Int, verse: Int) {
        dataStore.edit {
            it[Keys.lastSurah] = surah
            it[Keys.lastVerse] = verse
        }
    }

    val bookmark: Flow<Pair<Int, Int>?> = dataStore.data.map { p ->
        val s = p[Keys.bookmarkSurah]
        if (s == null) null else s to (p[Keys.bookmarkVerse] ?: 1)
    }

    suspend fun setBookmark(surah: Int, verse: Int) {
        dataStore.edit {
            it[Keys.bookmarkSurah] = surah
            it[Keys.bookmarkVerse] = verse
        }
    }

    suspend fun clearBookmark() {
        dataStore.edit {
            it.remove(Keys.bookmarkSurah)
            it.remove(Keys.bookmarkVerse)
        }
    }

    // ---- Taariikh ----
    /** Wadarta maalinlaha ah ee dhikri kasta: dateKey -> (dhikrId -> tiro). */
    val dailyTotals: Flow<Map<String, Map<String, Int>>> = dataStore.data.map { p ->
        val out = HashMap<String, MutableMap<String, Int>>()
        for ((k, v) in p.asMap()) {
            val name = k.name
            if (name.startsWith("day_") && v is Int) {
                val rest = name.removePrefix("day_")
                val date = rest.substringBefore('_')
                val id = rest.substringAfter('_')
                out.getOrPut(date) { HashMap() }[id] = v
            }
        }
        out
    }
}
