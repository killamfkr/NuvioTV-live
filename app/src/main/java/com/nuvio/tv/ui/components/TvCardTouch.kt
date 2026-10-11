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

/**
 * Touch on TV Material [androidx.tv.material3.Card] often ignores [Modifier.clickable] on the
 * card modifier. Put this on the card's inner content (e.g. the main [Row]) instead.
 */
@Composable
fun Modifier.tvCardContentTouchClick(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    if (!prefersTouchCards() || !enabled) return this
    return clickable(
        enabled = enabled,
        onClick = onClick,
        indication = null,
        interactionSource = null
    )
}

fun tvCardRemoteClick(touchPreferred: Boolean, enabled: Boolean = true, onClick: () -> Unit): () -> Unit = {
    if (enabled && !touchPreferred) onClick()
}
