package com.nuvio.tv.core.player

import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nuvio.tv.R
import com.nuvio.tv.ui.screens.player.SafeMediaSessionPlayer

/**
 * Foreground playback while Nuvio is in the background: media-style notification and lock-screen
 * transport controls via Media3 [MediaSession].
 */
@OptIn(UnstableApi::class)
class NuvioMediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var playerListener: Player.Listener? = null

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.playback_background_notification_channel)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ATTACH -> attachPlayer()
            ACTION_DETACH -> detachAndStop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

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
        releaseSession()
        val session = MediaSession.Builder(this, SafeMediaSessionPlayer(player)).build()
        mediaSession = session
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                    detachAndStop()
                }
            }
        }
        playerListener = listener
        player.addListener(listener)
    }

    private fun detachAndStop() {
        val player = BackgroundPlaybackHolder.player
        playerListener?.let { listener -> player?.removeListener(listener) }
        playerListener = null
        releaseSession()
        BackgroundPlayback.markInactive()
        stopSelf()
    }

    private fun releaseSession() {
        runCatching { mediaSession?.release() }
        mediaSession = null
    }

    companion object {
        private const val TAG = "NuvioMediaPlayback"
        const val CHANNEL_ID = "nuvio_media_playback"
        const val ACTION_ATTACH = "com.nuvio.tv.action.MEDIA_PLAYBACK_ATTACH"
        const val ACTION_DETACH = "com.nuvio.tv.action.MEDIA_PLAYBACK_DETACH"
    }
}
