@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.screens.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nuvio.tv.R
import com.nuvio.tv.core.streams.StreamBadgePlacement
import com.nuvio.tv.domain.model.Stream
import com.nuvio.tv.ui.components.SourceChipItem
import com.nuvio.tv.ui.components.SourceChipStatus
import com.nuvio.tv.ui.components.StreamBadgeChips
import com.nuvio.tv.ui.components.TouchMaterial3Button
import com.nuvio.tv.ui.theme.NuvioTheme
import com.nuvio.tv.ui.util.contentTextDirection
import com.nuvio.tv.ui.util.localizeEpisodeTitle

@Composable
internal fun MobileStreamPicker(
    modifier: Modifier = Modifier,
    title: String,
    isEpisode: Boolean,
    season: Int?,
    episode: Int?,
    episodeName: String?,
    year: String?,
    isLoading: Boolean,
    error: String?,
    streams: List<Stream>,
    availableAddons: List<String>,
    sourceChips: List<SourceChipItem>,
    selectedAddonFilter: String?,
    showFileSizeBadges: Boolean,
    showAddonLogo: Boolean,
    badgePlacement: StreamBadgePlacement,
    hasBadgeRules: Boolean,
    onBack: () -> Unit,
    onStreamSelected: (Stream) -> Unit,
    onAddonFilterSelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onExpandStreams: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val orderedAddonNames = remember(availableAddons, sourceChips) {
        buildList {
            addAll(availableAddons)
            sourceChips.forEach { chip -> if (chip.name !in this) add(chip.name) }
        }
    }
    val chipMap = remember(sourceChips) { sourceChips.associateBy { it.name } }
    val lastVisibleIndex by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }
    }
    LaunchedEffect(lastVisibleIndex, streams.size) {
        if (lastVisibleIndex >= streams.size - 20) {
            onExpandStreams()
        }
    }

    val subtitle = remember(isEpisode, season, episode, episodeName, year) {
        when {
            isEpisode && season != null && episode != null -> {
                val base = context.getString(R.string.stream_episode_label, season, episode)
                episodeName?.takeIf { it.isNotBlank() }?.let { "$base · ${it.localizeEpisodeTitle(context)}" }
                    ?: base
            }
            !year.isNullOrBlank() -> year
            else -> null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(NuvioTheme.colors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = NuvioTheme.spacing.md, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = NuvioTheme.colors.TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = NuvioTheme.colors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NuvioTheme.extendedColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.essential_stream_selection_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = NuvioTheme.extendedColors.textSecondary,
            modifier = Modifier.padding(horizontal = NuvioTheme.spacing.md, vertical = NuvioTheme.spacing.xs)
        )

        if (sourceChips.isNotEmpty() || (!isLoading && availableAddons.isNotEmpty())) {
            MobileStreamAddonFilters(
                orderedAddonNames = orderedAddonNames,
                chipMap = chipMap,
                selectedAddon = selectedAddonFilter,
                isStillFetching = sourceChips.any { it.status == SourceChipStatus.LOADING },
                onRefresh = onRefresh,
                onAddonSelected = onAddonFilterSelected
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading && streams.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NuvioTheme.colors.Primary)
                    }
                }
                error != null -> {
                    MobileStreamMessage(
                        message = error,
                        onRetry = onRetry
                    )
                }
                streams.isEmpty() -> {
                    MobileStreamEmpty()
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = NuvioTheme.spacing.md,
                            vertical = NuvioTheme.spacing.sm
                        ),
                        verticalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm)
                    ) {
                        if (!isLoading && streams.isNotEmpty()) {
                            item(key = "stream_count") {
                                Text(
                                    text = stringResource(R.string.plugin_test_found_streams, streams.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NuvioTheme.colors.Primary,
                                    modifier = Modifier.padding(bottom = NuvioTheme.spacing.xs)
                                )
                            }
                        }
                        itemsIndexed(
                            items = streams,
                            key = { index, stream ->
                                val base = stream.stableKey(0)
                                "$base-$index"
                            }
                        ) { _, stream ->
                            MobileStreamLinkCard(
                                stream = stream,
                                showFileSizeBadges = showFileSizeBadges,
                                showAddonLogo = showAddonLogo,
                                badgePlacement = badgePlacement,
                                reserveBadgeSpace = hasBadgeRules && stream.badges.isEmpty(),
                                onClick = { onStreamSelected(stream) }
                            )
                        }
                        item(key = "list_bottom_pad") {
                            Spacer(Modifier.height(NuvioTheme.spacing.xxxl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileStreamAddonFilters(
    orderedAddonNames: List<String>,
    chipMap: Map<String, SourceChipItem>,
    selectedAddon: String?,
    isStillFetching: Boolean,
    onRefresh: () -> Unit,
    onAddonSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = NuvioTheme.spacing.md, vertical = NuvioTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.sm)
    ) {
        item(key = "refresh") {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(48.dp)
            ) {
                if (isStillFetching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = NuvioTheme.colors.Primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.sources_reload),
                        tint = NuvioTheme.colors.TextPrimary
                    )
                }
            }
        }
        item(key = "all") {
            FilterChip(
                selected = selectedAddon == null,
                onClick = { onAddonSelected(null) },
                label = {
                    M3Text(stringResource(R.string.stream_filter_all))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NuvioTheme.colors.Secondary,
                    containerColor = NuvioTheme.colors.BackgroundCard,
                    labelColor = NuvioTheme.colors.TextPrimary,
                    selectedLabelColor = NuvioTheme.colors.OnSecondary
                )
            )
        }
        items(orderedAddonNames, key = { it }) { addon ->
            val status = chipMap[addon]?.status ?: SourceChipStatus.SUCCESS
            val selectable = status == SourceChipStatus.SUCCESS
            FilterChip(
                selected = selectedAddon == addon,
                onClick = { if (selectable) onAddonSelected(addon) },
                enabled = selectable,
                label = {
                    M3Text(
                        text = addon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NuvioTheme.colors.Secondary,
                    containerColor = NuvioTheme.colors.BackgroundCard,
                    labelColor = if (status == SourceChipStatus.ERROR) {
                        NuvioTheme.colors.Error
                    } else {
                        NuvioTheme.colors.TextPrimary
                    },
                    selectedLabelColor = NuvioTheme.colors.OnSecondary
                )
            )
        }
    }
}

@Composable
private fun MobileStreamLinkCard(
    stream: Stream,
    showFileSizeBadges: Boolean,
    showAddonLogo: Boolean,
    badgePlacement: StreamBadgePlacement,
    reserveBadgeSpace: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val unknownStreamLabel = stringResource(R.string.stream_unknown)
    val streamName = remember(stream, unknownStreamLabel) { stream.getDisplayNameOrNull() ?: unknownStreamLabel }
    val streamDescription = remember(stream) { stream.getDisplayDescription() }
    val hasBadges = stream.badges.isNotEmpty() ||
        (showFileSizeBadges && stream.behaviorHints?.videoSize != null) ||
        reserveBadgeSpace
    val shape = RoundedCornerShape(NuvioTheme.radii.md)
    val logoDecodeSize = remember(density) {
        with(density) { NuvioTheme.spacing.xxl.roundToPx() } * 2
    }
    val addonLogoModel = remember(context, stream.addonLogo, logoDecodeSize) {
        stream.addonLogo?.let { logo ->
            ImageRequest.Builder(context)
                .data(logo)
                .size(width = logoDecodeSize, height = logoDecodeSize)
                .memoryCacheKey("${logo}_${logoDecodeSize}x${logoDecodeSize}")
                .crossfade(true)
                .build()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = NuvioTheme.colors.BackgroundCard,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = NuvioTheme.colors.TextPrimary.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NuvioTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(NuvioTheme.spacing.xs)
            ) {
                if (hasBadges && badgePlacement == StreamBadgePlacement.TOP) {
                    if (stream.badges.isNotEmpty() || (showFileSizeBadges && stream.behaviorHints?.videoSize != null)) {
                        StreamBadgeChips(
                            badges = stream.badges,
                            fileSizeBytes = stream.behaviorHints?.videoSize,
                            showFileSizeBadge = showFileSizeBadges
                        )
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                M3Text(
                    text = streamName,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDirection = streamName.contentTextDirection()
                    ),
                    color = NuvioTheme.colors.TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                streamDescription?.takeIf { it.isNotBlank() && it != streamName }?.let { description ->
                    M3Text(
                        text = description,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                            textDirection = description.contentTextDirection()
                        ),
                        color = NuvioTheme.colors.TextPrimary.copy(alpha = 0.82f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (hasBadges && badgePlacement == StreamBadgePlacement.BOTTOM) {
                    if (stream.badges.isNotEmpty() || (showFileSizeBadges && stream.behaviorHints?.videoSize != null)) {
                        StreamBadgeChips(
                            badges = stream.badges,
                            fileSizeBytes = stream.behaviorHints?.videoSize,
                            showFileSizeBadge = showFileSizeBadges,
                            modifier = Modifier.padding(top = NuvioTheme.spacing.xxs)
                        )
                    }
                }
            }
            if (showAddonLogo) {
                Column(horizontalAlignment = Alignment.End) {
                    if (addonLogoModel != null) {
                        AsyncImage(
                            model = addonLogoModel,
                            contentDescription = stream.addonName,
                            modifier = Modifier
                                .size(NuvioTheme.spacing.xxl)
                                .clip(RoundedCornerShape(NuvioTheme.radii.xs)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(NuvioTheme.spacing.xs))
                    M3Text(
                        text = stream.addonName,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                            textDirection = stream.addonName.contentTextDirection()
                        ),
                        color = NuvioTheme.extendedColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileStreamMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NuvioTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = NuvioTheme.colors.Error,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(NuvioTheme.spacing.md))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = NuvioTheme.extendedColors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(NuvioTheme.spacing.lg))
        TouchMaterial3Button(
            text = stringResource(R.string.stream_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            labelColor = NuvioTheme.colors.TextPrimary,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = NuvioTheme.colors.BackgroundCard,
                contentColor = NuvioTheme.colors.TextPrimary
            )
        )
    }
}

@Composable
private fun MobileStreamEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NuvioTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.stream_no_streams),
            style = MaterialTheme.typography.bodyLarge,
            color = NuvioTheme.extendedColors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(NuvioTheme.spacing.md))
        Text(
            text = stringResource(R.string.stream_no_streams_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = NuvioTheme.extendedColors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
