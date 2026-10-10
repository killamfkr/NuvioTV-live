package com.nuvio.tv.livetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.tv.livetv.model.LiveTvSettings
import com.nuvio.tv.ui.theme.NuvioTheme

/** Live TV guide visual presets (Hulu + Live TV matches Hulu's dark guide and green focus). */
enum class LiveGuideAppearance(val key: String, val label: String) {
    CLASSIC("classic", "Classic"),
    HULU("hulu", "Hulu + Live TV");

    companion object {
        fun fromKey(key: String?): LiveGuideAppearance =
            entries.find { it.key == key } ?: HULU
    }
}

internal object HuluGuidePalette {
    val Background = Color(0xFF0B0B0B)
    val Surface = Color(0xFF2A2A2A)
    val SurfaceLive = Color(0xFF383838)
    val SurfacePast = Color(0xFF1F1F1F)
    val Accent = Color(0xFF1CE783)
    val OnAccent = Color(0xFF0B0B0B)
    val TextSecondary = Color(0xFFB3B3B3)
}

internal val LocalGuideAppearance = staticCompositionLocalOf { LiveGuideAppearance.HULU }

@Composable
internal fun currentGuideAppearance(): LiveGuideAppearance = LocalGuideAppearance.current

@Composable
internal fun isHuluGuide(): Boolean = currentGuideAppearance() == LiveGuideAppearance.HULU

@Composable
internal fun guideScreenBackground(): Color =
    if (isHuluGuide()) HuluGuidePalette.Background else NuvioTheme.colors.Background

@Composable
internal fun guideAccent(): Color =
    if (isHuluGuide()) HuluGuidePalette.Accent else NuvioTheme.colors.Secondary

@Composable
internal fun guideAccentOn(): Color =
    if (isHuluGuide()) HuluGuidePalette.OnAccent else NuvioTheme.colors.OnSecondary

@Composable
internal fun guideTimelineLabelColor(): Color =
    if (isHuluGuide()) HuluGuidePalette.TextSecondary else NuvioTheme.colors.TextSecondary

@Composable
internal fun guideCellCornerRadius(): Dp = if (isHuluGuide()) 8.dp else 6.dp

@Composable
internal fun guideProgramHorizontalGap(): Dp = if (isHuluGuide()) 3.dp else 1.5.dp

/** Hulu uses solid green focus cells; classic follows the user's highlight setting. */
internal fun effectiveSolidHighlight(settings: LiveTvSettings, appearance: LiveGuideAppearance): Boolean =
    settings.solidHighlight || appearance == LiveGuideAppearance.HULU

@Composable
internal fun LiveOnAirBadge(modifier: Modifier = Modifier) {
    LiveText(
        text = "LIVE",
        modifier = modifier
            .background(guideAccent(), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = guideAccentOn(),
        size = 10.sp,
        weight = FontWeight.Bold
    )
}
