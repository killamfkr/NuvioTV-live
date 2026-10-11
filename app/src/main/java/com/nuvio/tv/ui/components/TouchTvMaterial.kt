@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonBorder
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ButtonScale
import androidx.tv.material3.ButtonShape
import androidx.tv.material3.Card
import androidx.tv.material3.CardBorder
import androidx.tv.material3.CardColors
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CardScale
import androidx.tv.material3.CardShape
import androidx.tv.material3.ClickableSurfaceBorder
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.ClickableSurfaceShape
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonBorder
import androidx.tv.material3.IconButtonColors
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.IconButtonScale
import androidx.tv.material3.IconButtonShape
import androidx.tv.material3.Surface
@Composable
fun touchTvRemoteClick(enabled: Boolean = true, onClick: () -> Unit): () -> Unit {
    return tvCardRemoteClick(prefersTouchCards(), enabled, onClick)
}

@Composable
fun Modifier.touchTvClick(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    return tvCardTouchClick(enabled, onClick)
}

@Composable
fun TouchTvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.colors(),
    shape: ButtonShape = ButtonDefaults.shape(),
    border: ButtonBorder = ButtonDefaults.border(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    scale: ButtonScale = ButtonDefaults.scale(),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = touchTvRemoteClick(enabled, onClick),
        modifier = modifier.touchTvClick(enabled, onClick),
        enabled = enabled,
        colors = colors,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        scale = scale,
        content = content
    )
}

@Composable
fun TouchTvIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.colors(),
    border: IconButtonBorder = IconButtonDefaults.border(),
    shape: IconButtonShape = IconButtonDefaults.shape(),
    scale: IconButtonScale = IconButtonDefaults.scale(),
    content: @Composable BoxScope.() -> Unit
) {
    IconButton(
        onClick = touchTvRemoteClick(enabled, onClick),
        enabled = enabled,
        modifier = modifier.touchTvClick(enabled, onClick),
        colors = colors,
        border = border,
        shape = shape,
        scale = scale,
        content = content
    )
}

@Composable
fun TouchTvSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ClickableSurfaceColors = ClickableSurfaceDefaults.colors(),
    border: ClickableSurfaceBorder = ClickableSurfaceDefaults.border(),
    shape: ClickableSurfaceShape = ClickableSurfaceDefaults.shape(),
    scale: ClickableSurfaceScale = ClickableSurfaceDefaults.scale(),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = touchTvRemoteClick(enabled, onClick),
        modifier = modifier.touchTvClick(enabled, onClick),
        colors = colors,
        border = border,
        shape = shape,
        scale = scale,
        content = content
    )
}

@Composable
fun TouchTvCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: CardShape = CardDefaults.shape(),
    colors: CardColors = CardDefaults.colors(),
    border: CardBorder = CardDefaults.border(),
    scale: CardScale = CardDefaults.scale(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = if (enabled) touchTvRemoteClick(enabled, onClick) else ({ }),
        modifier = modifier.touchTvClick(enabled, onClick),
        shape = shape,
        colors = colors,
        border = border,
        scale = scale
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
