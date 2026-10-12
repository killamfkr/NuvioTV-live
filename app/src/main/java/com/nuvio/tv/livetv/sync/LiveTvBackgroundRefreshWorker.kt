package com.nuvio.tv.livetv.sync

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.nuvio.tv.R
import com.nuvio.tv.livetv.data.LiveTvRepository
import com.nuvio.tv.livetv.ondemand.OnDemandRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Refreshes IPTV playlists and guides on a schedule, including when the app is not open. */
@HiltWorker
class LiveTvBackgroundRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val liveTvRepository: LiveTvRepository,
    private val onDemandRepository: OnDemandRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val notifier = LiveTvUpdateNotifications(applicationContext)
        val canNotify = LiveTvNotificationPermission.hasPermission(applicationContext)

        suspend fun pushForeground(message: String) {
            if (!canNotify) return
            setForeground(foregroundInfo(notifier.buildProgress(message)))
        }

        try {
            pushForeground(applicationContext.getString(R.string.livetv_update_notification_starting))

            val statusJob = launch {
                liveTvRepository.status.collect { status ->
                    val msg = status.message
                    if (status.loading && !msg.isNullOrBlank()) {
                        pushForeground(msg)
                    }
                }
            }

            liveTvRepository.runScheduledBackgroundRefresh()
            statusJob.cancel()

            pushForeground(applicationContext.getString(R.string.livetv_update_notification_ondemand))
            onDemandRepository.refresh(force = false)

            if (canNotify) {
                val done = applicationContext.getString(R.string.livetv_update_notification_done)
                setForeground(foregroundInfo(notifier.buildFinished(done)))
            }
            notifier.cancel()
            Result.success()
        } catch (e: Exception) {
            notifier.cancel()
            Result.retry()
        }
    }

    private fun foregroundInfo(notification: android.app.Notification): ForegroundInfo {
        val id = LiveTvUpdateNotifications.NOTIFICATION_ID
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(id, notification)
        }
    }
}
