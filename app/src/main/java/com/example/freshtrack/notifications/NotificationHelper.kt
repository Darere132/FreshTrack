package com.example.freshtrack.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.freshtrack.MainActivity
import com.example.freshtrack.R

object NotificationHelper {

    private const val CHANNEL_ID = "freshtrack_expiry_channel"
    private const val CHANNEL_NAME = "Expiry Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for items nearing their expiry date"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        // NotificationChannel existuje až od API 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendExpiryNotification(context: Context, expiredCount: Int, expiringSoonCount: Int) {
        val message = buildMessage(expiredCount, expiringSoonCount)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)   // dočasne, kým nevytvoríš ic_notification
            .setContentTitle("FreshTrack")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // NotificationManagerCompat rieši POST_NOTIFICATIONS permission za nás
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun buildMessage(expiredCount: Int, expiringSoonCount: Int): String {
        return when {
            expiredCount > 0 && expiringSoonCount > 0 ->
                "$expiredCount item(s) expired, $expiringSoonCount item(s) expiring soon."
            expiredCount > 0 ->
                "$expiredCount item(s) in your pantry have expired."
            else ->
                "$expiringSoonCount item(s) in your pantry are expiring soon."
        }
    }
}