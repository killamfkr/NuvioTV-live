package com.nuvio.tv.ui.screens.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.nuvio.tv.LocalHandheldNavigation
import com.nuvio.tv.core.device.DeviceFormFactor

@Composable
fun rememberHandheldPlayerUi(): Boolean {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val portrait = configuration.screenHeightDp > configuration.screenWidthDp
    val handheldNav = LocalHandheldNavigation.current
    return handheldNav ||
        portrait ||
        DeviceFormFactor.useHandheldPlayerUi(
            context = context,
            handheldNavigationActive = handheldNav
        )
}
