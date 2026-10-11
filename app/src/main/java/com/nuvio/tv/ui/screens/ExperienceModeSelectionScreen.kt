package com.nuvio.tv.ui.screens

import com.nuvio.tv.ui.theme.NuvioTheme

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nuvio.tv.core.device.DeviceFormFactor
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.CardDefaults as M3CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.tv.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.nuvio.tv.data.local.ExperienceModeDataStore
import com.nuvio.tv.data.local.LayoutPreferenceDataStore
import com.nuvio.tv.domain.model.ExperienceMode
import com.nuvio.tv.domain.model.HomeLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ExperienceModeSelectionViewModel @Inject constructor(
    private val experienceModeDataStore: ExperienceModeDataStore,
    private val layoutPreferenceDataStore: LayoutPreferenceDataStore
) : ViewModel() {
    suspend fun choose(mode: ExperienceMode) {
        experienceModeDataStore.setMode(mode)
        if (mode == ExperienceMode.ESSENTIAL) {
            layoutPreferenceDataStore.setLayout(HomeLayout.MODERN)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ExperienceModeSelectionScreen(
    onContinue: (ExperienceMode) -> Unit,
    viewModel: ExperienceModeSelectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val touchPreferred = DeviceFormFactor.prefersTouchGuide(context)
    val isHandheld = !DeviceFormFactor.isTelevision(context)
    val coroutineScope = rememberCoroutineScope()
    val essentialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(touchPreferred) {
        if (!touchPreferred) {
            essentialFocusRequester.requestFocus()
        }
    }

    fun choose(mode: ExperienceMode) {
        coroutineScope.launch {
            viewModel.choose(mode)
            onContinue(mode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isHandheld) NuvioTheme.spacing.lg else 64.dp,
                vertical = NuvioTheme.spacing.xxxl
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.experience_mode_choose_title),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = NuvioTheme.colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(NuvioTheme.spacing.md))
            Text(
                text = stringResource(R.string.experience_mode_choose_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = NuvioTheme.colors.TextSecondary
            )
            Spacer(modifier = Modifier.height(36.dp))
            if (isHandheld) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExperienceModeCard(
                        title = stringResource(R.string.experience_mode_essential),
                        subtitle = stringResource(R.string.experience_mode_essential_card_subtitle),
                        icon = Icons.Default.VideoSettings,
                        onClick = { choose(ExperienceMode.ESSENTIAL) },
                        touchPreferred = touchPreferred,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExperienceModeCard(
                        title = stringResource(R.string.experience_mode_advanced),
                        subtitle = stringResource(R.string.experience_mode_advanced_card_subtitle),
                        icon = Icons.Default.Tune,
                        onClick = { choose(ExperienceMode.ADVANCED) },
                        touchPreferred = touchPreferred,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExperienceModeCard(
                        title = stringResource(R.string.experience_mode_essential),
                        subtitle = stringResource(R.string.experience_mode_essential_card_subtitle),
                        icon = Icons.Default.VideoSettings,
                        onClick = { choose(ExperienceMode.ESSENTIAL) },
                        touchPreferred = touchPreferred,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(essentialFocusRequester)
                    )
                    ExperienceModeCard(
                        title = stringResource(R.string.experience_mode_advanced),
                        subtitle = stringResource(R.string.experience_mode_advanced_card_subtitle),
                        icon = Icons.Default.Tune,
                        onClick = { choose(ExperienceMode.ADVANCED) },
                        touchPreferred = touchPreferred,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    touchPreferred: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(NuvioTheme.radii.md)
    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(NuvioTheme.spacing.xl),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NuvioTheme.colors.TextSecondary
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = NuvioTheme.colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(NuvioTheme.spacing.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NuvioTheme.colors.TextSecondary
            )
        }
    }

    if (touchPreferred) {
        M3Card(
            onClick = onClick,
            modifier = modifier
                .height(210.dp)
                .focusable(),
            shape = shape,
            colors = M3CardDefaults.cardColors(
                containerColor = NuvioTheme.colors.BackgroundCard
            ),
            border = BorderStroke(NuvioTheme.spacing.hairline, NuvioTheme.colors.Border)
        ) {
            cardContent()
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.height(210.dp),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = NuvioTheme.colors.BackgroundCard,
                focusedContainerColor = NuvioTheme.colors.FocusBackground
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(
                    border = BorderStroke(NuvioTheme.spacing.hairline, NuvioTheme.colors.Border),
                    shape = shape
                ),
                focusedBorder = Border(
                    border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs),
                    shape = shape
                )
            ),
            shape = ClickableSurfaceDefaults.shape(shape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
        ) {
            cardContent()
        }
    }
}
