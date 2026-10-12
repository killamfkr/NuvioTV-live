package com.nuvio.tv.livetv.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nuvio.tv.R

/** Progress notification while IPTV playlists / guides update in the background. */
class LiveTvUpdateNotifications(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.livetv_update_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.livetv_update_notification_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun buildProgress(message: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.livetv_update_notification_title))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()

    fun buildFinished(message: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.livetv_update_notification_title))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    fun cancel() {
        manager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_ID = 8842
        private const val CHANNEL_ID = "livetv_iptv_update"
    }
}
