package com.nuvio.tv.ui.navigation

import android.util.Log
import androidx.navigation.NavHostController

fun navigateToRootRoute(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String
) {
    if (currentRoute == targetRoute) {
        if (targetRoute == Screen.Home.route) {
            val homeEntry = try {
                navController.getBackStackEntry(Screen.Home.route)
            } catch (_: IllegalArgumentException) {
                return
            }
            val homeViewModel = androidx.lifecycle.ViewModelProvider(homeEntry)[
                com.nuvio.tv.ui.screens.home.HomeViewModel::class.java
            ]
            homeViewModel.requestScrollToTop()
        }
        return
    }
    try {
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: IllegalArgumentException) {
        Log.w("NuvioNavigation", "Route not found in nav graph: $targetRoute", e)
    }
}
