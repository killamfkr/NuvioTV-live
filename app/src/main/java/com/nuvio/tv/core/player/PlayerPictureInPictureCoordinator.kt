package com.nuvio.tv.core.player

import android.util.Rational
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        /** Stream is loaded enough for the user to tap PiP (paused is OK). */
        val readyForManualEnter: Boolean = false,
        /** Playing or buffering — used when the user leaves the app (home / recents). */
        val readyForAutoEnter: Boolean = false,
        val enterOnUserLeave: Boolean = false,
        val aspectRatio: Float = 16f / 9f
    )

    @Volatile
    var host: Host? = null

    @Volatile
    var eligibility: Eligibility = Eligibility()

    private val _inPictureInPicture = MutableStateFlow(false)
    val inPictureInPicture: StateFlow<Boolean> = _inPictureInPicture.asStateFlow()

    fun updateInPictureInPicture(inPip: Boolean) {
        _inPictureInPicture.value = inPip
    }

    fun tryEnterPictureInPicture(fromUserLeaveHint: Boolean = false): Boolean {
        val e = eligibility
        if (!e.enabled) return false
        if (fromUserLeaveHint) {
            if (!e.enterOnUserLeave || !e.readyForAutoEnter) return false
        } else if (!e.readyForManualEnter) {
            return false
        }
        return host?.enterPictureInPicture(e.aspectRatio) == true
    }

    fun aspectRatioToRational(aspectRatio: Float): Rational {
        val safe = aspectRatio.coerceIn(0.42f, 2.39f)
        val width = (safe * 100).toInt().coerceAtLeast(1)
        return Rational(width, 100)
    }
}
