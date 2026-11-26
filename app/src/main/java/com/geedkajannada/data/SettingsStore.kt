package com.geedkajannada.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Dhammaan xogta isticmaalaha waxay ku kaydsan tahay DataStore (qalabka kaliya). */
class SettingsStore(val dataStore: DataStore<Preferences>) {

    private object Keys {
        val selectedDhikr = stringPreferencesKey("selected_dhikr")
        val sound = booleanPreferencesKey("sound_enabled")
        val haptics = booleanPreferencesKey("haptics_enabled")
        val reminderOffset = intPreferencesKey("reminder_offset")
        val notifEnabled = booleanPreferencesKey("notif_enabled")
        val notifMode = stringPreferencesKey("notif_mode")
        val prayerAuto = booleanPreferencesKey("prayer_auto")
        val prayerCity = stringPreferencesKey("prayer_city")
        val quranFontSize = intPreferencesKey("quran_font_size")
        val readerDark = booleanPreferencesKey("reader_dark")
        val locLat = doublePreferencesKey("loc_lat")
        val locLng = doublePreferencesKey("loc_lng")
        val locationAsked = booleanPreferencesKey("location_asked")
        val lastSurah = intPreferencesKey("last_surah")
        val lastVerse = intPreferencesKey("last_verse")
        val bookmarkSurah = intPreferencesKey("bookmark_surah")
        val bookmarkVerse = intPreferencesKey("bookmark_verse")
        fun count(id: String) = intPreferencesKey("count_$id")
        fun goal(id: String) = intPreferencesKey("goal_$id")
        fun rounds(id: String) = intPreferencesKey("rounds_$id")
        fun day(dateKey: String, id: String) = intPreferencesKey("day_${dateKey}_$id")
        fun checked(dateKey: String, prayer: String) = booleanPreferencesKey("check_${dateKey}_$prayer")
        fun adhkar(date: String, period: String, id: String) = booleanPreferencesKey("adhkar_${date}_${period}_$id")
        fun adhkarCount(date: String, period: String, id: String) = intPreferencesKey("adhkarc_${date}_${period}_$id")
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
    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.sound] ?: true }
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.haptics] ?: false }
    suspend fun setSound(v: Boolean) {
        dataStore.edit { it[Keys.sound] = v }
    }

    suspend fun setHaptics(v: Boolean) {
        dataStore.edit { it[Keys.haptics] = v }
    }

    // ---- Salaad ----
    /** Goobta la isticmaalayo: location-ka la kaydiyay (haddii la oggolaaday), ama magaalada la doortay. */
    private fun effectiveCity(p: Preferences): City {
        val lat = p[Keys.locLat]
        val lng = p[Keys.locLng]
        return if (lat != null && lng != null) City("gps", "Goobtaada", lat, lng) else Cities.byId(p[Keys.prayerCity])
    }

    private fun zoneOffsetHours(date: LocalDate): Double =
        PrayerTimesCalc.ZONE.rules.getOffset(date.atTime(12, 0).atZone(PrayerTimesCalc.ZONE).toInstant()).totalSeconds / 3600.0

    private fun entriesFrom(p: Preferences, date: LocalDate): List<PrayerEntry> {
        val auto = p[Keys.prayerAuto] ?: true
        val calc = if (auto) PrayerTimesCalc.compute(date, effectiveCity(p), zoneOffsetHours(date)) else null
        return Prayer.entries.map { pr ->
            val minute = calc?.get(pr) ?: p[Keys.prayerTime(pr.key)]?.takeIf { it >= 0 }
            PrayerEntry(pr, minute, p[Keys.prayerNotify(pr.key)] ?: true)
        }
    }

    /** Waqtiyada maanta (si toos ah loo xisaabiyay, ama gacanta loo geliyay). */
    val prayerEntries: Flow<List<PrayerEntry>> = dataStore.data.map { entriesFrom(it, LocalDate.now(PrayerTimesCalc.ZONE)) }

    /** Waqtiyada taariikh gaar ah (loogu isticmaalo dejinta alarm-ka berrito). */
    suspend fun prayerEntriesFor(date: LocalDate): List<PrayerEntry> = entriesFrom(dataStore.data.first(), date)

    val prayerAuto: Flow<Boolean> = dataStore.data.map { it[Keys.prayerAuto] ?: true }
    val prayerCity: Flow<City> = dataStore.data.map { effectiveCity(it) }

    /** Location-ka la kaydiyay (lat, lng) ama null. Waa maxalli; server looma dirayo. */
    val savedLocation: Flow<Pair<Double, Double>?> = dataStore.data.map { p ->
        val lat = p[Keys.locLat]
        val lng = p[Keys.locLng]
        if (lat != null && lng != null) lat to lng else null
    }

    suspend fun setLocation(lat: Double, lng: Double) {
        dataStore.edit {
            it[Keys.locLat] = lat
            it[Keys.locLng] = lng
            it[Keys.prayerAuto] = true
        }
    }

    suspend fun clearLocation() {
        dataStore.edit {
            it.remove(Keys.locLat)
            it.remove(Keys.locLng)
        }
    }

    val locationAsked: Flow<Boolean> = dataStore.data.map { it[Keys.locationAsked] ?: false }

    suspend fun setLocationAsked() {
        dataStore.edit { it[Keys.locationAsked] = true }
    }

    suspend fun setPrayerCity(id: String) {
        dataStore.edit {
            it[Keys.prayerCity] = id
            // Magaalo gacanta la doortay ayaa ka sarreysa location-ka la kaydiyay.
            it.remove(Keys.locLat)
            it.remove(Keys.locLng)
        }
    }

    /** Auto ON: waqtiyada la xisaabiyo. Auto OFF: waqtiyada hadda la muujinayo ayaa gacanta loo koobiyeeyaa si loo bedelo. */
    suspend fun setPrayerAuto(auto: Boolean) {
        dataStore.edit { p ->
            if (!auto && (p[Keys.prayerAuto] ?: true)) {
                for (e in entriesFrom(p, LocalDate.now(PrayerTimesCalc.ZONE))) {
                    e.minuteOfDay?.let { p[Keys.prayerTime(e.prayer.key)] = it }
                }
            }
            p[Keys.prayerAuto] = auto
        }
    }

    val reminderOffset: Flow<Int> = dataStore.data.map { it[Keys.reminderOffset] ?: 0 }

    /** Switch-ka guud. Isticmaalayaasha hore: ON haddii salaad uun horay loo shiday. */
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { p ->
        p[Keys.notifEnabled] ?: Prayer.entries.any { p[Keys.prayerNotify(it.key)] == true }
    }

    suspend fun setNotificationsEnabled(on: Boolean) {
        dataStore.edit { it[Keys.notifEnabled] = on }
    }

    val notifyMode: Flow<NotifyMode> = dataStore.data.map { NotifyMode.byKey(it[Keys.notifMode]) }

    suspend fun setNotifyMode(mode: NotifyMode) {
        dataStore.edit { it[Keys.notifMode] = mode.key }
    }

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

    val readerDark: Flow<Boolean> = dataStore.data.map { it[Keys.readerDark] ?: false }

    suspend fun setReaderDark(on: Boolean) {
        dataStore.edit { it[Keys.readerDark] = on }
    }

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

    // ---- Adkaar ----
    /** Adkaarta la dhammeeyay (id-yo) ee taariikh iyo waqti gaar ah. */
    fun adhkarDone(date: String, period: AdhkarPeriod): Flow<Set<String>> = dataStore.data.map { p ->
        val prefix = "adhkar_${date}_${period.key}_"
        p.asMap().entries.filter { it.key.name.startsWith(prefix) && it.value == true }
            .map { it.key.name.removePrefix(prefix) }.toSet()
    }

    suspend fun setAdhkarDone(date: String, period: AdhkarPeriod, id: String, done: Boolean) {
        dataStore.edit { p ->
            val k = Keys.adhkar(date, period.key, id)
            if (done) p[k] = true else {
                p.remove(k)
                p.remove(Keys.adhkarCount(date, period.key, id))
            }
        }
    }

    /** Tirada adkaar kasta ee maanta la sameeyay (id -> tiro). */
    fun adhkarCounts(date: String, period: AdhkarPeriod): Flow<Map<String, Int>> = dataStore.data.map { p ->
        val prefix = "adhkarc_${date}_${period.key}_"
        p.asMap().entries.filter { it.key.name.startsWith(prefix) && it.value is Int }
            .associate { it.key.name.removePrefix(prefix) to it.value as Int }
    }

    /** Hal tiro ku dar; marka la gaaro [target] si toos ah ayaa loo calaamadeeyaa dhammaaday. */
    suspend fun incrementAdhkar(date: String, period: AdhkarPeriod, id: String, target: Int) {
        dataStore.edit { p ->
            val ck = Keys.adhkarCount(date, period.key, id)
            val n = minOf(target, (p[ck] ?: 0) + 1)
            p[ck] = n
            if (n >= target) p[Keys.adhkar(date, period.key, id)] = true
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

    /** Salaadaha maanta la calaamadeeyay (taabasho ku jira shaashadda Hoyga). */
    fun checkedPrayers(dateKey: String): Flow<Set<Prayer>> = dataStore.data.map { p ->
        Prayer.entries.filter { p[Keys.checked(dateKey, it.key)] == true }.toSet()
    }

    suspend fun toggleChecked(dateKey: String, prayer: Prayer) {
        dataStore.edit {
            val k = Keys.checked(dateKey, prayer.key)
            if (it[k] == true) it.remove(k) else it[k] = true
        }
    }
}
