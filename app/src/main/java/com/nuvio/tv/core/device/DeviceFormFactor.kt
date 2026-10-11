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
}
