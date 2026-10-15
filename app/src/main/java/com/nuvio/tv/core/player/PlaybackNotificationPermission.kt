package com.nuvio.tv.core.player

import com.nuvio.tv.livetv.sync.rememberLiveTvNotificationPermissionGate

/** Reuses the Live TV notifications gate (Android 13+ POST_NOTIFICATIONS). */
@androidx.compose.runtime.Composable
fun rememberPlaybackNotificationPermissionGate(onProceed: () -> Unit): () -> Unit =
    rememberLiveTvNotificationPermissionGate(onProceed)
