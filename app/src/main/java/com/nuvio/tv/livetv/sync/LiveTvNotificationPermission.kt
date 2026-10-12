package com.nuvio.tv.livetv.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object LiveTvNotificationPermission {

    fun needsRuntimeRequest(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun hasPermission(context: Context): Boolean {
        if (!needsRuntimeRequest()) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Runs [onProceed] after showing the notifications permission dialog when needed (Android 13+).
 * Auto-update is still enabled if the user denies — updates just won't show a notification.
 */
@Composable
fun rememberLiveTvNotificationPermissionGate(onProceed: () -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingProceed by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (pendingProceed) {
            pendingProceed = false
            onProceed()
        }
    }
    return remember(onProceed, context) {
        {
            if (LiveTvNotificationPermission.needsRuntimeRequest() &&
                !LiveTvNotificationPermission.hasPermission(context)
            ) {
                pendingProceed = true
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onProceed()
            }
        }
    }
}
