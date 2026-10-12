package com.nuvio.tv.core.player

import android.util.Rational

/**
 * Activity ↔ player bridge for handheld picture-in-picture. The player screen updates
 * [eligibility]; [MainActivity] reads it from [onUserLeaveHint].
 */
object PlayerPictureInPictureCoordinator {

    fun interface Host {
        fun enterPictureInPicture(aspectRatio: Float): Boolean
    }

    data class Eligibility(
        val enabled: Boolean = false,
        val activePlayback: Boolean = false,
        val enterOnUserLeave: Boolean = false,
        val aspectRatio: Float = 16f / 9f
    )

    @Volatile
    var host: Host? = null

    @Volatile
    var eligibility: Eligibility = Eligibility()

    @Volatile
    var isInPictureInPicture: Boolean = false

    fun tryEnterPictureInPicture(fromUserLeaveHint: Boolean = false): Boolean {
        val e = eligibility
        if (!e.enabled || !e.activePlayback) return false
        if (fromUserLeaveHint && !e.enterOnUserLeave) return false
        return host?.enterPictureInPicture(e.aspectRatio) == true
    }

    fun aspectRatioToRational(aspectRatio: Float): Rational {
        val safe = aspectRatio.coerceIn(0.42f, 2.39f)
        val width = (safe * 100).toInt().coerceAtLeast(1)
        return Rational(width, 100)
    }
}
