package com.nuvio.tv.livetv.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Scheduled IPTV work (playlist / EPG / On Demand import) runs only while Nuvio is in the
 * foreground. Nothing is started after the user leaves the app, including delayed startup jobs.
 */
object LiveTvForegroundGate {
    @Volatile
    var inForeground: Boolean = false

    private val deferred = mutableListOf<() -> Unit>()

    @Volatile
    private var installed = false

    fun install() {
        if (installed) return
        installed = true
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    inForeground = true
                    if (deferred.isEmpty()) return
                    val runs = deferred.toList()
                    deferred.clear()
                    runs.forEach { runCatching(it) }
                }

                override fun onStop(owner: LifecycleOwner) {
                    inForeground = false
                }
            }
        )
    }

    fun canRunScheduledRefresh(): Boolean = inForeground

    /** Run [action] now if the app is open; otherwise run it on the next foreground transition. */
    fun runWhenForeground(action: () -> Unit) {
        if (inForeground) action() else deferred.add(action)
    }
}
