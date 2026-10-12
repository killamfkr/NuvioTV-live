package com.nuvio.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.nuvio.tv.livetv.branding.LiveTvBranding
import com.nuvio.tv.ui.theme.NuvioTheme
import com.nuvio.tv.ui.theme.accentBrush
import com.nuvio.tv.ui.theme.brandWordmarkResource

@Composable
fun NuvioLiveWordmark(
    modifier: Modifier = Modifier,
    wordmarkHeight: Dp = 48.dp,
    brandWordmarkRes: Int? = null,
    contentDescription: String = LiveTvBranding.APP_NAME
) {
    val liveBrush = NuvioTheme.palette.accentBrush()
    Row(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        BrandWordmark(
            modifier = Modifier.height(wordmarkHeight),
            contentDescription = null,
            drawableOverride = brandWordmarkRes ?: NuvioTheme.currentTheme.brandWordmarkResource
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Live",
            style = TextStyle(
                fontSize = (wordmarkHeight.value * 0.62f).sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp
            ),
            brush = liveBrush
        )
    }
}
