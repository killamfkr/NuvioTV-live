package com.nuvio.tv.core.device

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

object DeviceFormFactor {
    /** True on Android TV, Google TV, Fire TV, and other leanback devices. */
    fun isTelevision(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION) return true
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    /** True when the device reports a touchscreen (phones, tablets, some TV panels). */
    fun supportsTouchscreen(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }

    /** Touch-first guide/player interactions (phone layout or any device with a touchscreen). */
    fun prefersTouchGuide(context: Context): Boolean {
        return !isTelevision(context) || supportsTouchscreen(context)
    }

    /**
     * Full-screen phone/tablet stream picker (stacked list), not the TV side-by-side layout.
     * Uses navigation mode when provided; otherwise infers from form factor and screen shape.
     */
    fun useHandheldStreamUi(
        context: Context,
        handheldNavigationActive: Boolean = false
    ): Boolean {
        if (handheldNavigationActive) return true
        if (!isTelevision(context)) return true
        val config = context.resources.configuration
        if (!supportsTouchscreen(context)) return false
        // Sideloaded / misclassified phones can report leanback + TV uiMode while in portrait.
        val portrait = config.screenHeightDp > config.screenWidthDp
        val compactWidth = config.screenWidthDp < 600
        return portrait || compactWidth
    }
}
