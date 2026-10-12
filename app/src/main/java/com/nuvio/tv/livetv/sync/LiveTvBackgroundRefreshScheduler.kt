package com.nuvio.tv.livetv.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nuvio.tv.livetv.model.LiveTvSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.nuvio.tv.livetv.data.LiveTvPreferences

/**
 * Keeps a periodic WorkManager job in sync with Live TV settings so IPTV can update on the
 * 12 / 24 hour schedule even when Nuvio is closed.
 */
@Singleton
class LiveTvBackgroundRefreshScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: LiveTvPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            prefs.settings
                .distinctUntilChanged { a, b ->
                    a.autoUpdateSources == b.autoUpdateSources &&
                        a.playlistRefreshHours == b.playlistRefreshHours &&
                        a.epgRefreshHours == b.epgRefreshHours
                }
                .collect { sync(it) }
        }
    }

    fun sync(settings: LiveTvSettings) {
        val wm = WorkManager.getInstance(context)
        if (!settings.autoUpdateSources) {
            wm.cancelUniqueWork(WORK_NAME)
            return
        }
        val intervalHours = minOf(
            settings.playlistRefreshHours.coerceAtLeast(1),
            settings.epgRefreshHours.coerceAtLeast(1)
        ).toLong()
        val request = PeriodicWorkRequestBuilder<LiveTvBackgroundRefreshWorker>(
            intervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        wm.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        private const val WORK_NAME = "livetv_background_refresh"
    }
}
