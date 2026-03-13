package com.downnotice.mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.downnotice.mobile.MainViewModel
import com.downnotice.mobile.ui.about.AboutScreen
import com.downnotice.mobile.ui.dashboard.DashboardScreen
import com.downnotice.mobile.ui.detail.FeedDetailScreen
import com.downnotice.mobile.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object DashboardRoute
@Serializable object SettingsRoute
@Serializable object AboutRoute
@Serializable data class FeedDetailRoute(val feedId: String)

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Home, DashboardRoute),
    BottomNavItem("Settings", Icons.Default.Settings, SettingsRoute),
    BottomNavItem("About", Icons.Default.Info, AboutRoute)
)

@Composable
fun DownNoticeNavHost() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom nav only on top-level screens
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hasRoute(item.route::class) == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    viewModel = viewModel,
                    onFeedClick = { feedId ->
                        navController.navigate(FeedDetailRoute(feedId))
                    }
                )
            }
            composable<FeedDetailRoute> { backStackEntry ->
                val route = backStackEntry.arguments?.getString("feedId") ?: return@composable
                FeedDetailScreen(
                    feedId = route,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(viewModel = viewModel)
            }
            composable<AboutRoute> {
                AboutScreen()
            }
        }
    }
}
