package com.nuvio.tv.core.player

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * Battery and background-data restrictions that can stop playback when the app is not visible.
 */
object BackgroundPlaybackRestrictions {

    data class Status(
        val batteryUnrestricted: Boolean,
        val backgroundDataUnrestricted: Boolean
    )

    fun readStatus(context: Context): Status {
        val battery = isBatteryUnrestricted(context)
        val data = isBackgroundDataUnrestricted(context)
        return Status(batteryUnrestricted = battery, backgroundDataUnrestricted = data)
    }

    fun isBatteryUnrestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = context.getSystemService<PowerManager>() ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isBackgroundDataUnrestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
        val cm = context.getSystemService<ConnectivityManager>() ?: return true
        return when (cm.restrictBackgroundStatus) {
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED,
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED -> true
            else -> false
        }
    }

    /** System screen to allow unrestricted battery use for Nuvio (or pick Nuvio from the list). */
    fun openBatterySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (runCatching { context.startActivity(direct) }.isSuccess) return
            val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (runCatching { context.startActivity(list) }.isSuccess) return
        }
        openAppDetails(context)
    }

    /** System screen to allow background mobile data / lift data-saver limits for Nuvio. */
    fun openBackgroundDataSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val appData = Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (runCatching { context.startActivity(appData) }.isSuccess) return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val saver = Intent(ACTION_DATA_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (runCatching { context.startActivity(saver) }.isSuccess) return
        }
        val usage = Intent(Settings.ACTION_DATA_USAGE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (runCatching { context.startActivity(usage) }.isSuccess) return
        openAppDetails(context)
    }

    fun openAppDetails(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
