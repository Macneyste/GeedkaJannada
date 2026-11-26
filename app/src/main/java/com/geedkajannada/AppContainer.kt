package com.geedkajannada

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.geedkajannada.data.LocationProvider
import com.geedkajannada.data.PrayerRepository
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.data.StatsRepository
import com.geedkajannada.feature.tasbeeh.TasbeehRepository
import com.geedkajannada.reminder.NotificationHelper
import com.geedkajannada.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "geedka_settings")

class AppContainer(context: Context) {
    val settings = SettingsStore(context.applicationContext.dataStore)
    val quran = QuranRepository(context.applicationContext)
    val prayers = PrayerRepository(settings)
    val stats = StatsRepository(settings)
    val location = LocationProvider(context.applicationContext)
    val tasbeeh = TasbeehRepository(context.applicationContext.dataStore)
}

class GeedkaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannels(this)
        CoroutineScope(Dispatchers.Default).launch {
            ReminderScheduler.rescheduleAll(this@GeedkaApp, container.settings)
        }
    }
}

val Context.container: AppContainer
    get() = (applicationContext as GeedkaApp).container
