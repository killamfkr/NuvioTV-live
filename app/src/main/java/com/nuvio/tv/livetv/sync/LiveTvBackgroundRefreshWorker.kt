package com.nuvio.tv.livetv.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nuvio.tv.livetv.data.LiveTvRepository
import com.nuvio.tv.livetv.ondemand.OnDemandRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Refreshes IPTV playlists and guides on a schedule, including when the app is not open. */
@HiltWorker
class LiveTvBackgroundRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val liveTvRepository: LiveTvRepository,
    private val onDemandRepository: OnDemandRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            liveTvRepository.runScheduledBackgroundRefresh()
            onDemandRepository.refresh(force = false)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
