package com.nuvio.tv.core.player

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.Player

/**
 * Hands off the active [Player] to [NuvioMediaPlaybackService] so playback can continue in the
 * background with a media notification and lock-screen controls.
 */
object BackgroundPlayback {

    @Volatile
    private var active = false

    fun isActive(): Boolean = active

    fun start(context: Context, player: Player) {
        BackgroundPlaybackHolder.player = player
        val intent = Intent(context, NuvioMediaPlaybackService::class.java).apply {
            action = NuvioMediaPlaybackService.ACTION_ATTACH
        }
        ContextCompat.startForegroundService(context, intent)
        active = true
    }

    fun stop(context: Context) {
        if (!active) return
        markInactive()
        val intent = Intent(context, NuvioMediaPlaybackService::class.java).apply {
            action = NuvioMediaPlaybackService.ACTION_DETACH
        }
        runCatching { context.startService(intent) }
    }

    internal fun markInactive() {
        active = false
        BackgroundPlaybackHolder.player = null
    }
}

internal object BackgroundPlaybackHolder {
    @Volatile
    var player: Player? = null
}
