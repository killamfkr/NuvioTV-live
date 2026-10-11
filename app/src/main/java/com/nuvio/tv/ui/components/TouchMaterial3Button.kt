package com.nuvio.tv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Touch-first Material3 button. Uses [androidx.compose.material3.Text] with an explicit label
 * color because TV Material [androidx.tv.material3.Text] ignores M3 [ButtonColors.contentColor].
 */
@Composable
fun TouchMaterial3Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    labelColor: Color,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    fontWeight: FontWeight = FontWeight.SemiBold,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            fontWeight = fontWeight,
            color = labelColor,
        )
    }
}
