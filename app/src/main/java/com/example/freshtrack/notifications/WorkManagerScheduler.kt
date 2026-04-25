package com.example.freshtrack.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val WORK_NAME = "freshtrack_expiry_check"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val delay = calculateDelayMillis(hour, minute)

        val request = OneTimeWorkRequestBuilder<ExpiryCheckWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        // REPLACE — ak používateľ zmení čas v settings, starý plán sa zruší a nahradí novým
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    // Vypočíta koľko milisekúnd zostáva do najbližšieho hour:minute
    // Ak daný čas dnes už prešiel, naplánuje na zajtra
    private fun calculateDelayMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Ak je cieľový čas dnes už v minulosti, posuň na zajtra
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}