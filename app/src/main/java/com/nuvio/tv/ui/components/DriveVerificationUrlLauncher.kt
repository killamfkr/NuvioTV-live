package com.nuvio.tv.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CollectDriveVerificationUrlLauncher(openVerificationUrl: SharedFlow<String>) {
    val context = LocalContext.current
    LaunchedEffect(openVerificationUrl) {
        openVerificationUrl.collect { url ->
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}
