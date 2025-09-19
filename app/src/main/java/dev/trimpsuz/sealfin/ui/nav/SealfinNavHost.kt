package dev.trimpsuz.sealfin.ui.nav

import ServerSelectorScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.trimpsuz.sealfin.ui.screens.FavoritesScreen
import dev.trimpsuz.sealfin.ui.screens.HomeScreen
import dev.trimpsuz.sealfin.ui.screens.LibrariesScreen
import dev.trimpsuz.sealfin.ui.screens.LoginScreen
import dev.trimpsuz.sealfin.ui.screens.SettingsScreen
import dev.trimpsuz.sealfin.ui.viewmodel.AuthViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Libraries : BottomNavItem("libraries", Icons.AutoMirrored.Filled.MenuBook, "Libraries")
    object Favorites : BottomNavItem("favorites", Icons.Filled.Favorite, "Favorites")
    object Settings : BottomNavItem("settings", Icons.Filled.Settings, "Settings")
}

@Composable
fun SealfinNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val activeServer by authViewModel.activeServer.collectAsState()
    val servers by authViewModel.servers.collectAsState()

    val startDestination = when {
        servers.isEmpty() -> "login"
        activeServer == null -> "server_selector"
        else -> BottomNavItem.Home.route
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != "login" && currentRoute != "server_selector") {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Libraries,
                        BottomNavItem.Favorites,
                        BottomNavItem.Settings
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(padding)) {
            composable("login") {
                LoginScreen(onLoginSuccess = { navController.navigate(BottomNavItem.Home.route)  { popUpTo("login") { inclusive = true } } })
            }
            composable("server_selector") {
                ServerSelectorScreen(
                    serverViewModel = authViewModel,
                    onServerSelected = { navController.navigate(BottomNavItem.Home.route) { popUpTo("server_selector") { inclusive = true } } }
                )
            }
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Libraries.route) { LibrariesScreen() }
            composable(BottomNavItem.Favorites.route) { FavoritesScreen() }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onServerSelectorRequested = {
                        navController.navigate("server_selector")
                    }
                )
            }

        }
    }
}

