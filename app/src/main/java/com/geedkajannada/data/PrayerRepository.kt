package com.geedkajannada.data

import kotlinx.coroutines.flow.Flow

/** Waqtiyada salaadda gacanta ayaa lagu geliyaa. Network iyo location lama isticmaalo. */
class PrayerRepository(private val store: SettingsStore) {
    val entries: Flow<List<PrayerEntry>> = store.prayerEntries
    val offset: Flow<Int> = store.reminderOffset

    suspend fun setTime(prayer: Prayer, minuteOfDay: Int) = store.setPrayerTime(prayer, minuteOfDay)
    suspend fun setNotify(prayer: Prayer, on: Boolean) = store.setPrayerNotify(prayer, on)
    suspend fun setOffset(minutes: Int) = store.setReminderOffset(minutes)
}
