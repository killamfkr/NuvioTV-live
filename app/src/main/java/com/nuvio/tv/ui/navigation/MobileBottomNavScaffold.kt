package com.nuvio.tv.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.nuvio.tv.DrawerItem
import com.nuvio.tv.livetv.ui.LiveTvFullscreen
import com.nuvio.tv.ui.system.HandheldImmersiveSystemBarsEffect
import com.nuvio.tv.LocalContentFocusRequester
import com.nuvio.tv.LocalOpenSidebar
import com.nuvio.tv.LocalSidebarExpanded
import com.nuvio.tv.ui.navigation.Screen
import com.nuvio.tv.ui.theme.NuvioTheme
import androidx.compose.ui.focus.FocusRequester

@Composable
fun MobileBottomNavScaffold(
    navController: NavHostController,
    startDestination: String,
    currentRoute: String?,
    rootRoutes: Set<String>,
    drawerItems: List<DrawerItem>,
    selectedDrawerRoute: String?,
    hideBuiltInHeaders: Boolean,
    onNavigate: (String) -> Unit,
) {
    val liveTvFullscreen by LiveTvFullscreen.active.collectAsStateWithLifecycle()
    val onVodPlayer = currentRoute?.startsWith("player/") == true ||
        currentRoute?.startsWith("stream/") == true
    val immersive = liveTvFullscreen || onVodPlayer
    HandheldImmersiveSystemBarsEffect(enabled = immersive)
    val showBottomBar = currentRoute in rootRoutes && !immersive
    val contentFocusRequester = remember { FocusRequester() }

    Scaffold(
        containerColor = NuvioTheme.colors.Background,
        contentColor = NuvioTheme.colors.TextPrimary,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NuvioTheme.colors.BackgroundElevated,
                    contentColor = NuvioTheme.colors.TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    drawerItems.forEach { item ->
                        val selected = selectedDrawerRoute == item.route
                        val icon = drawerItemIcon(item)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                onNavigate(item.route)
                                navigateToRootRoute(
                                    navController = navController,
                                    currentRoute = currentRoute,
                                    targetRoute = item.route
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NuvioTheme.colors.Primary,
                                selectedTextColor = NuvioTheme.colors.Primary,
                                unselectedIconColor = NuvioTheme.colors.TextSecondary,
                                unselectedTextColor = NuvioTheme.colors.TextSecondary,
                                indicatorColor = NuvioTheme.colors.Primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = if (immersive) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            }
        ) {
            CompositionLocalProvider(
                LocalSidebarExpanded provides false,
                LocalContentFocusRequester provides contentFocusRequester,
                LocalOpenSidebar provides { /* phones use the bottom bar */ }
            ) {
                NuvioNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    hideBuiltInHeaders = hideBuiltInHeaders
                )
            }
        }
    }
}

private fun drawerItemIcon(item: DrawerItem): ImageVector {
    if (item.icon != null) return item.icon
    return when (item.route) {
        Screen.Home.route -> Icons.Default.Home
        Screen.Discover.route -> Icons.Default.Explore
        Screen.LiveTv.route -> Icons.Default.LiveTv
        Screen.OnDemand.route -> Icons.Default.VideoLibrary
        Screen.Search.route -> Icons.Default.Search
        Screen.Library.route -> Icons.AutoMirrored.Filled.LibraryBooks
        Screen.Settings.route -> Icons.Default.Settings
        else -> Icons.Default.Home
    }
}
