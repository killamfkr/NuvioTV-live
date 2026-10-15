package com.nuvio.tv.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nuvio.tv.R
import com.nuvio.tv.core.player.BackgroundPlaybackRestrictions
import com.nuvio.tv.data.local.PlayerPreference
import com.nuvio.tv.data.local.PlayerSettings
import com.nuvio.tv.data.local.InternalPlayerEngine
import com.nuvio.tv.ui.theme.NuvioTheme

@Composable
internal fun PlaybackBackgroundRestrictionsSection(settings: PlayerSettings) {
    val internalPlayer = settings.playerPreference == PlayerPreference.INTERNAL
    val exoEngine = settings.internalPlayerEngine != InternalPlayerEngine.MVP_PLAYER
    if (!internalPlayer || !exoEngine) return

    val context = LocalContext.current
    var status by remember { mutableStateOf(BackgroundPlaybackRestrictions.readStatus(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status = BackgroundPlaybackRestrictions.readStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsSectionLabel(text = stringResource(R.string.playback_background_restrictions_label))
    SettingsActionRow(
        title = stringResource(R.string.playback_background_battery),
        subtitle = stringResource(R.string.playback_background_battery_sub),
        value = if (status.batteryUnrestricted) {
            stringResource(R.string.playback_background_battery_ok)
        } else {
            stringResource(R.string.playback_background_battery_action)
        },
        valueColor = if (status.batteryUnrestricted) {
            NuvioTheme.colors.Primary
        } else {
            NuvioTheme.colors.TextSecondary
        },
        onClick = { BackgroundPlaybackRestrictions.openBatterySettings(context) }
    )
    SettingsActionRow(
        title = stringResource(R.string.playback_background_data),
        subtitle = stringResource(R.string.playback_background_data_sub),
        value = if (status.backgroundDataUnrestricted) {
            stringResource(R.string.playback_background_data_ok)
        } else {
            stringResource(R.string.playback_background_data_action)
        },
        valueColor = if (status.backgroundDataUnrestricted) {
            NuvioTheme.colors.Primary
        } else {
            NuvioTheme.colors.TextSecondary
        },
        onClick = { BackgroundPlaybackRestrictions.openBackgroundDataSettings(context) }
    )
}
