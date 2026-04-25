package com.example.freshtrack.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.freshtrack.FreshTrackApplication
import com.example.freshtrack.data.local.FreshTrackDatabase
import kotlinx.coroutines.flow.first

class ExpiryCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Získaj nastavenia cez AppContainer — žiadny nový DataStore delegate
            val dataStore = (context.applicationContext as FreshTrackApplication)
                .container.settingsDataStore

            val notificationsEnabled = dataStore.notificationsEnabled.first()
            if (!notificationsEnabled) return Result.success()

            val daysBeforeExpiry = dataStore.daysBeforeExpiry.first()
            val hour = dataStore.notificationHour.first()
            val minute = dataStore.notificationMinute.first()

            // Načítaj aktívne položky z DB
            val dao = FreshTrackDatabase.getDatabase(context).itemDao()
            val items = dao.getActiveItemsOnce()

            val now = System.currentTimeMillis()
            val thresholdMillis = daysBeforeExpiry * 24L * 60 * 60 * 1000

            val expiredItems = items.filter { it.expirationDate < now }
            val expiringSoonItems = items.filter {
                it.expirationDate >= now && it.expirationDate - now <= thresholdMillis
            }

            if (expiredItems.isNotEmpty() || expiringSoonItems.isNotEmpty()) {
                NotificationHelper.sendExpiryNotification(
                    context = context,
                    expiredCount = expiredItems.size,
                    expiringSoonCount = expiringSoonItems.size
                )
            }

            // Naplánuj seba na ďalší deň
            WorkManagerScheduler.schedule(context, hour, minute)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}