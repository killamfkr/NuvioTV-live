package com.nuvio.tv.livetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.tv.livetv.model.ChannelGroup
import com.nuvio.tv.livetv.model.EpgProgram
import com.nuvio.tv.livetv.model.LiveChannel
import com.nuvio.tv.ui.theme.NuvioTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTvMobileScreen(
    onOpenSettings: () -> Unit,
    onFindInNuvio: () -> Unit = {},
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    var fullscreen by remember { mutableStateOf(false) }
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val user by viewModel.userState.collectAsStateWithLifecycle()
    val programs by viewModel.programs.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val playback by viewModel.playbackState.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()

    var pinGroup by remember { mutableStateOf<ChannelGroup?>(null) }
    var pinWrong by remember { mutableStateOf(false) }

    PauseLiveTvInBackground(viewModel.playback)
    DisposableEffect(Unit) {
        viewModel.playback.attach()
        onDispose { viewModel.playback.detach() }
    }
    LaunchedEffect(fullscreen) { LiveTvFullscreen.active.value = fullscreen }
    DisposableEffect(Unit) { onDispose { LiveTvFullscreen.active.value = false } }

    val fullscreenRequests by LiveTvFullscreen.requests.collectAsStateWithLifecycle()
    LaunchedEffect(fullscreenRequests) {
        if (!LiveTvFullscreen.pending) return@LaunchedEffect
        LiveTvFullscreen.pending = false
        fullscreen = true
    }

    LaunchedEffect(now) {
        viewModel.ensureGuideRange(now - 3_600_000L, now + 6 * 3_600_000L)
    }

    fun openChannel(channel: LiveChannel) {
        viewModel.preview(channel)
        fullscreen = true
    }

    fun selectGroup(group: ChannelGroup) {
        if (group.id in user.lockedGroups && viewModel.parentalEnabled()) {
            pinGroup = group
            pinWrong = false
            return
        }
        viewModel.selectGroup(group.id)
    }

    if (fullscreen) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            LivePlayerSurface(
                player = viewModel.playback.player,
                useSurfaceView = true,
                modifier = Modifier.fillMaxSize(),
                onAttached = { viewModel.playback.onSurfaceAttached() },
                aspectMode = aspectModeOf(settings.aspectMode)
            )
            LiveTvPlayerScreen(
                onBack = { fullscreen = false },
                onFindInNuvio = { fullscreen = false; onFindInNuvio() },
                onBackToGroups = { fullscreen = false },
                embedded = true,
                viewModel = viewModel
            )
        }
        return
    }

    pinGroup?.let { g ->
        TextInputDialog(
            title = if (pinWrong) "Wrong PIN, try again" else "\"${g.title}\" is locked. Enter your PIN",
            initial = "",
            hint = "PIN",
            confirmLabel = "Unlock",
            numeric = true,
            onDismiss = { pinGroup = null },
            onConfirm = { pin ->
                if (viewModel.unlockGroup(g, pin)) {
                    pinGroup = null
                    viewModel.selectGroup(g.id)
                } else {
                    pinWrong = true
                }
            }
        )
    }

    Scaffold(
        containerColor = NuvioTheme.colors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Live TV") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Live TV settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NuvioTheme.colors.Background,
                    titleContentColor = NuvioTheme.colors.TextPrimary,
                    actionIconContentColor = NuvioTheme.colors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!ui.hasSources) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Add a playlist in Live TV settings to get started.",
                            color = NuvioTheme.colors.TextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Open settings",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenSettings)
                                .background(NuvioTheme.colors.Primary.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            color = NuvioTheme.colors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                return@Scaffold
            }

            if (status.loading && ui.channels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NuvioTheme.colors.Primary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            status.message ?: "Loading channels…",
                            color = NuvioTheme.colors.TextSecondary
                        )
                    }
                }
                return@Scaffold
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ui.groups.forEach { group ->
                    FilterChip(
                        selected = ui.selectedGroupId == group.id,
                        onClick = { selectGroup(group) },
                        label = { Text(group.title, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NuvioTheme.colors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = NuvioTheme.colors.Primary
                        )
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(ui.channels, key = { _, ch -> ch.key }) { _, channel ->
                    MobileChannelRow(
                        channel = channel,
                        program = currentProgram(programs[channel.key], now),
                        isFavorite = channel.key in user.favorites,
                        isPlaying = playback.channelKey == channel.key,
                        showNumber = settings.showChannelNumbers,
                        showLogo = settings.showChannelLogos,
                        use24h = settings.use24HourClock,
                        onClick = { openChannel(channel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileChannelRow(
    channel: LiveChannel,
    program: EpgProgram?,
    isFavorite: Boolean,
    isPlaying: Boolean,
    showNumber: Boolean,
    showLogo: Boolean,
    use24h: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (isPlaying) {
        NuvioTheme.colors.Primary.copy(alpha = 0.12f)
    } else {
        NuvioTheme.colors.BackgroundElevated
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showNumber && channel.number > 0) {
            Text(
                text = channel.number.toString(),
                modifier = Modifier.width(36.dp),
                color = NuvioTheme.colors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        if (showLogo) {
            ChannelLogo(channel.logo, 44.dp)
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    color = NuvioTheme.colors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isFavorite) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = NuvioTheme.colors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            val subtitle = program?.title?.takeIf { it.isNotBlank() } ?: "No guide data"
            Text(
                text = subtitle,
                color = NuvioTheme.colors.TextSecondary,
                fontSize = 13.sp,
                maxLines = 2
            )
            program?.let {
                Text(
                    text = formatProgramTime(it, use24h),
                    color = NuvioTheme.colors.TextSecondary.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun currentProgram(programs: List<EpgProgram>?, nowMs: Long): EpgProgram? =
    programs?.firstOrNull { nowMs >= it.startMs && nowMs < it.stopMs }

private fun formatProgramTime(program: EpgProgram, use24h: Boolean): String {
    val pattern = if (use24h) "HH:mm" else "h:mm a"
    val fmt = SimpleDateFormat(pattern, Locale.getDefault())
    val start = fmt.format(Date(program.startMs))
    val end = fmt.format(Date(program.stopMs))
    return "$start – $end"
}
