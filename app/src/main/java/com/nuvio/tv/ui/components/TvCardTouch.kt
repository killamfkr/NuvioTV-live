package com.nuvio.tv.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nuvio.tv.core.device.DeviceFormFactor

@Composable
fun prefersTouchCards(): Boolean = DeviceFormFactor.prefersTouchGuide(LocalContext.current)

@Composable
fun Modifier.tvCardTouchClick(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    if (!prefersTouchCards() || !enabled) return this
    return clickable(enabled = enabled, onClick = onClick)
}

fun tvCardRemoteClick(touchPreferred: Boolean, enabled: Boolean = true, onClick: () -> Unit): () -> Unit = {
    if (enabled && !touchPreferred) onClick()
}
