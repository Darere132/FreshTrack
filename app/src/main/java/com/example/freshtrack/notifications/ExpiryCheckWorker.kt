package com.example.freshtrack.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.freshtrack.data.local.FreshTrackDatabase
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "freshtrack_settings")

class ExpiryCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Načítaj nastavenia priamo z DataStore
            val prefs = context.dataStore.data.first()
            val notificationsEnabled = prefs[booleanPreferencesKey("notifications_enabled")] ?: true
            val daysBeforeExpiry = prefs[intPreferencesKey("days_before_expiry")] ?: 3

            // Ak sú notifikácie vypnuté, nič nerobíme
            if (!notificationsEnabled) return Result.success()

            // 2. Načítaj aktívne položky z DB
            val dao = FreshTrackDatabase.getDatabase(context).itemDao()
            val items = dao.getActiveItemsOnce()

            val now = System.currentTimeMillis()
            val thresholdMillis = daysBeforeExpiry * 24L * 60 * 60 * 1000

            // 3. Roztrieď položky
            val expiredItems = items.filter { it.expirationDate < now }
            val expiringSoonItems = items.filter {
                it.expirationDate >= now && it.expirationDate - now <= thresholdMillis
            }

            // 4. Pošli notifikáciu iba ak je niečo na hlásenie
            if (expiredItems.isNotEmpty() || expiringSoonItems.isNotEmpty()) {
                NotificationHelper.sendExpiryNotification(
                    context = context,
                    expiredCount = expiredItems.size,
                    expiringSoonCount = expiringSoonItems.size
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}