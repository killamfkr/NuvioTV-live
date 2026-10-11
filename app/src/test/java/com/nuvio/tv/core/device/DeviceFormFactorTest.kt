package com.nuvio.tv.core.device

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceFormFactorTest {

    @Test
    fun `isTelevision returns true for leanback feature`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns true
        every { context.resources.configuration } returns Configuration()

        assertTrue(DeviceFormFactor.isTelevision(context))
    }

    @Test
    fun `isTelevision returns false for typical phone configuration`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        val configuration = Configuration()
        configuration.uiMode = Configuration.UI_MODE_TYPE_NORMAL
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns false
        every { context.resources.configuration } returns configuration

        assertFalse(DeviceFormFactor.isTelevision(context))
    }

    @Test
    fun `useHandheldStreamUi is true for typical phone`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        val configuration = Configuration()
        configuration.uiMode = Configuration.UI_MODE_TYPE_NORMAL
        configuration.screenWidthDp = 411
        configuration.screenHeightDp = 891
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns false
        every { context.resources.configuration } returns configuration

        assertTrue(DeviceFormFactor.useHandheldStreamUi(context))
    }

    @Test
    fun `useHandheldStreamUi is false for leanback TV`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        val configuration = Configuration()
        configuration.screenWidthDp = 960
        configuration.screenHeightDp = 540
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns true
        every { pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns false
        every { context.resources.configuration } returns configuration

        assertFalse(DeviceFormFactor.useHandheldStreamUi(context))
    }

    @Test
    fun `useHandheldStreamUi is true for portrait touch device misclassified as TV`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        val configuration = Configuration()
        configuration.uiMode = Configuration.UI_MODE_TYPE_TELEVISION
        configuration.screenWidthDp = 411
        configuration.screenHeightDp = 891
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns true
        every { pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true
        every { context.resources.configuration } returns configuration

        assertTrue(DeviceFormFactor.useHandheldStreamUi(context))
    }

    @Test
    fun `useHandheldStreamUi follows handheld navigation flag`() {
        val context = mockk<Context>(relaxed = true)
        val pm = mockk<PackageManager>()
        val configuration = Configuration()
        configuration.screenWidthDp = 960
        configuration.screenHeightDp = 540
        every { context.packageManager } returns pm
        every { pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns true
        every { pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true
        every { context.resources.configuration } returns configuration

        assertTrue(DeviceFormFactor.useHandheldStreamUi(context, handheldNavigationActive = true))
    }
}
