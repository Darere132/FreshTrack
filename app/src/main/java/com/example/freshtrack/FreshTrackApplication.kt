package com.example.freshtrack

import android.app.Application
import com.example.freshtrack.data.AppContainer
import com.example.freshtrack.data.AppDataContainer
import com.example.freshtrack.notifications.NotificationHelper
import com.example.freshtrack.notifications.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FreshTrackApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        // Vytvor notification channel (bezpečné volať opakovane, Android ho nevytvorí znova)
        NotificationHelper.createNotificationChannel(this)

        // Načítaj nastavenia a naplánuj Worker podľa uložených hodnôt
        CoroutineScope(Dispatchers.IO).launch {
            combine(
                container.settingsDataStore.notificationsEnabled,
                container.settingsDataStore.notificationHour,
                container.settingsDataStore.notificationMinute
            ) { enabled, hour, minute ->
                Triple(enabled, hour, minute)
            }.collect { (enabled, hour, minute) ->
                if (enabled) {
                    WorkManagerScheduler.schedule(this@FreshTrackApplication, hour, minute)
                } else {
                    WorkManagerScheduler.cancel(this@FreshTrackApplication)
                }
            }
        }
    }
}