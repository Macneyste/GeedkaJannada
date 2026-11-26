package com.geedkajannada.data

import kotlinx.coroutines.flow.Flow

/** Waqtiyada salaadda gacanta ayaa lagu geliyaa. Network iyo location lama isticmaalo. */
class PrayerRepository(private val store: SettingsStore) {
    val entries: Flow<List<PrayerEntry>> = store.prayerEntries
    val offset: Flow<Int> = store.reminderOffset
    val auto: Flow<Boolean> = store.prayerAuto
    val city: Flow<City> = store.prayerCity

    val notificationsEnabled: Flow<Boolean> = store.notificationsEnabled
    val notifyMode: Flow<NotifyMode> = store.notifyMode

    suspend fun setNotificationsEnabled(on: Boolean) = store.setNotificationsEnabled(on)
    suspend fun setNotifyMode(mode: NotifyMode) = store.setNotifyMode(mode)

    suspend fun setAuto(on: Boolean) = store.setPrayerAuto(on)
    suspend fun setCity(id: String) = store.setPrayerCity(id)

    suspend fun setTime(prayer: Prayer, minuteOfDay: Int) = store.setPrayerTime(prayer, minuteOfDay)
    suspend fun setNotify(prayer: Prayer, on: Boolean) = store.setPrayerNotify(prayer, on)
    suspend fun setOffset(minutes: Int) = store.setReminderOffset(minutes)
}
