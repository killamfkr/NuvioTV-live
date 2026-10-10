@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.screens

import com.nuvio.tv.ui.theme.NuvioTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nuvio.tv.core.device.DeviceFormFactor
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.CardDefaults as M3CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.domain.model.HomeLayout
import com.nuvio.tv.ui.components.ClassicLayoutPreview
import com.nuvio.tv.ui.components.GridLayoutPreview
import com.nuvio.tv.ui.components.ModernLayoutPreview
import com.nuvio.tv.ui.screens.settings.LayoutSettingsEvent
import com.nuvio.tv.ui.screens.settings.LayoutSettingsViewModel
import androidx.compose.ui.res.stringResource
import com.nuvio.tv.R

@Composable
fun LayoutSelectionScreen(
    viewModel: LayoutSettingsViewModel = hiltViewModel(),
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val touchPreferred = DeviceFormFactor.prefersTouchGuide(context)
    val isHandheld = !DeviceFormFactor.isTelevision(context)
    val uiState by viewModel.uiState.collectAsState()
    var selectedLayout by remember { mutableStateOf(HomeLayout.MODERN) }
    val continueFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.selectedLayout) {
        selectedLayout = uiState.selectedLayout
    }

    LaunchedEffect(touchPreferred) {
        if (!touchPreferred) {
            continueFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isHandheld) NuvioTheme.spacing.lg else 80.dp,
                    vertical = NuvioTheme.spacing.xxxl
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = stringResource(R.string.layout_selection_welcome),
                style = MaterialTheme.typography.headlineLarge,
                color = NuvioTheme.colors.TextPrimary
            )

            Spacer(modifier = Modifier.height(NuvioTheme.spacing.sm))

            Text(
                text = stringResource(R.string.layout_selection_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = NuvioTheme.colors.TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Layout cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.xxl, Alignment.CenterHorizontally)
            ) {
                LayoutOptionCard(
                    layout = HomeLayout.MODERN,
                    isSelected = selectedLayout == HomeLayout.MODERN,
                    onSelect = { selectedLayout = HomeLayout.MODERN },
                    touchPreferred = touchPreferred,
                    modifier = Modifier.weight(1f)
                )

                LayoutOptionCard(
                    layout = HomeLayout.GRID,
                    isSelected = selectedLayout == HomeLayout.GRID,
                    onSelect = { selectedLayout = HomeLayout.GRID },
                    touchPreferred = touchPreferred,
                    modifier = Modifier.weight(1f)
                )

                LayoutOptionCard(
                    layout = HomeLayout.CLASSIC,
                    isSelected = selectedLayout == HomeLayout.CLASSIC,
                    onSelect = { selectedLayout = HomeLayout.CLASSIC },
                    touchPreferred = touchPreferred,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(NuvioTheme.spacing.xl))

            // Continue button
            val onContinueClick = {
                viewModel.onEvent(LayoutSettingsEvent.SelectLayout(selectedLayout))
                onContinue()
            }
            if (touchPreferred) {
                androidx.compose.material3.Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NuvioTheme.spacing.xxxl),
                    shape = RoundedCornerShape(NuvioTheme.spacing.xl),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = NuvioTheme.colors.Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.layout_selection_continue),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(NuvioTheme.spacing.xxxl)
                    .focusRequester(continueFocusRequester),
                shape = ButtonDefaults.shape(
                    shape = RoundedCornerShape(NuvioTheme.spacing.xl)
                ),
                colors = ButtonDefaults.colors(
                    containerColor = NuvioTheme.colors.Primary,
                    focusedContainerColor = NuvioTheme.colors.FocusBackground
                ),
                border = ButtonDefaults.border(
                    focusedBorder = Border(
                        border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs),
                        shape = RoundedCornerShape(NuvioTheme.spacing.xl)
                    )
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.layout_selection_continue),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LayoutOptionCard(
    layout: HomeLayout,
    isSelected: Boolean,
    onSelect: () -> Unit,
    touchPreferred: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(NuvioTheme.radii.xl)

    val cardModifier = modifier.onFocusChanged { isFocused = it.isFocused }
    val inner: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NuvioTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    when (layout) {
                        HomeLayout.CLASSIC -> ClassicLayoutPreview(
                            modifier = Modifier.fillMaxSize()
                        )
                        HomeLayout.GRID -> GridLayoutPreview(
                            modifier = Modifier.fillMaxSize()
                        )
                        HomeLayout.MODERN -> ModernLayoutPreview(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NuvioTheme.spacing.lg))

                Text(
                    text = when (layout) {
                        HomeLayout.CLASSIC -> stringResource(R.string.layout_classic)
                        HomeLayout.GRID -> stringResource(R.string.layout_grid)
                        HomeLayout.MODERN -> stringResource(R.string.layout_modern)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected || isFocused) NuvioTheme.colors.TextPrimary else NuvioTheme.colors.TextSecondary
                )

                Spacer(modifier = Modifier.height(NuvioTheme.spacing.xs))

                Text(
                    text = when (layout) {
                        HomeLayout.CLASSIC -> stringResource(R.string.layout_classic_desc)
                        HomeLayout.GRID -> stringResource(R.string.layout_grid_desc)
                        HomeLayout.MODERN -> stringResource(R.string.layout_modern_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = NuvioTheme.colors.TextTertiary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = NuvioTheme.colors.FocusRing,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(NuvioTheme.spacing.sm)
                        .size(NuvioTheme.spacing.xl)
                )
            }
        }
    }

    if (touchPreferred) {
        M3Card(
            onClick = onSelect,
            modifier = cardModifier.focusable(),
            shape = shape,
            colors = M3CardDefaults.cardColors(
                containerColor = NuvioTheme.colors.BackgroundCard
            ),
            border = if (isSelected) {
                BorderStroke(NuvioTheme.spacing.xxs, NuvioTheme.colors.FocusRing)
            } else {
                BorderStroke(NuvioTheme.spacing.hairline, NuvioTheme.colors.Border)
            }
        ) {
            inner()
        }
        return
    }

    Card(
        onClick = onSelect,
        modifier = cardModifier,
        colors = CardDefaults.colors(
            containerColor = NuvioTheme.colors.BackgroundCard,
            focusedContainerColor = NuvioTheme.colors.BackgroundCard
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(
                border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs),
                shape = RoundedCornerShape(NuvioTheme.radii.xl)
            )
        ),
        shape = CardDefaults.shape(shape),
        scale = CardDefaults.scale(focusedScale = 1.03f)
    ) {
        inner()
    }
}
