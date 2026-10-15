package com.nuvio.tv.core.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.nuvio.tv.R
import com.nuvio.tv.ui.screens.player.SafeMediaSessionPlayer

/**
 * Foreground playback while Nuvio is in the background: media notification, lock-screen controls,
 * and a real foreground service so the process is not killed.
 */
@OptIn(UnstableApi::class)
class NuvioMediaPlaybackService : Service() {

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSession? = null
    private var attachedPlayer: Player? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID,
            descriptionAdapter
        )
            .setNotificationListener(notificationListener)
            .setSmallIconResourceId(R.drawable.ic_launcher)
            .setChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
            .setChannelNameResourceId(R.string.playback_background_notification_channel)
            .setChannelDescriptionResourceId(R.string.playback_background_playback_sub)
            .build()
            .apply {
                setUseStopAction(true)
                setUsePlayPauseActions(true)
                setShowPlayButtonIfPlaybackIsSuppressed(true)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ATTACH -> attachPlayer()
            ACTION_DETACH -> detachAndStop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing when the user swipes the app away from recents.
        if (attachedPlayer?.playWhenReady == true) return
        detachAndStop()
    }

    override fun onDestroy() {
        detachAndStop()
        super.onDestroy()
    }

    private fun attachPlayer() {
        val player = BackgroundPlaybackHolder.player
        if (player == null) {
            Log.w(TAG, "No player to attach")
            stopSelf()
            return
        }
        attachedPlayer = player
        promoteToForeground(buildPlaceholderNotification())
        BackgroundPlaybackHolder.completeForegroundHandoff()
        releaseMediaSession()
        val session = MediaSession.Builder(this, SafeMediaSessionPlayer(player)).build()
        mediaSession = session
        playerNotificationManager?.setMediaSessionToken(session.platformToken)
        playerNotificationManager?.setPlayer(player)
    }

    private fun buildPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.playback_background_playback_sub))
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun promoteToForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun detachAndStop() {
        playerNotificationManager?.setPlayer(null)
        releaseMediaSession()
        attachedPlayer = null
        BackgroundPlayback.markInactive()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseMediaSession() {
        runCatching { mediaSession?.release() }
        mediaSession = null
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.playback_background_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.playback_background_playback_sub)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private val descriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.mediaMetadata.title?.takeIf { it.isNotBlank() }
                ?: getString(R.string.app_name)
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return null
            return PendingIntent.getActivity(
                this@NuvioMediaPlaybackService,
                0,
                launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        override fun getCurrentContentText(player: Player): CharSequence? =
            player.mediaMetadata.artist?.takeIf { it.isNotBlank() }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? = null
    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (!BackgroundPlayback.isActive()) return
            val player = attachedPlayer
            val keepForeground = ongoing ||
                (player != null && player.playWhenReady &&
                    player.playbackState != Player.STATE_IDLE &&
                    player.playbackState != Player.STATE_ENDED)
            if (!keepForeground) return
            promoteToForeground(notification)
            BackgroundPlaybackHolder.completeForegroundHandoff()
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            if (dismissedByUser) {
                attachedPlayer?.pause()
            }
            detachAndStop()
        }
    }

    companion object {
        private const val TAG = "NuvioMediaPlayback"
        const val CHANNEL_ID = "nuvio_media_playback"
        private const val NOTIFICATION_ID = 4102
        const val ACTION_ATTACH = "com.nuvio.tv.action.MEDIA_PLAYBACK_ATTACH"
        const val ACTION_DETACH = "com.nuvio.tv.action.MEDIA_PLAYBACK_DETACH"
    }
}
