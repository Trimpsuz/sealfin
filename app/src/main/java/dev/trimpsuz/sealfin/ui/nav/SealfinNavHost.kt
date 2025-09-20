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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.trimpsuz.sealfin.ui.screens.FavoritesScreen
import dev.trimpsuz.sealfin.ui.screens.HomeScreen
import dev.trimpsuz.sealfin.ui.screens.LoginScreen
import dev.trimpsuz.sealfin.ui.screens.SettingsScreen
import dev.trimpsuz.sealfin.ui.screens.libraries.LibrariesScreen
import dev.trimpsuz.sealfin.ui.screens.libraries.LibraryDetailsScreen
import dev.trimpsuz.sealfin.ui.viewmodel.AuthViewModel
import dev.trimpsuz.sealfin.utils.navigateOrPopBackStack

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Libraries : BottomNavItem("library", Icons.AutoMirrored.Filled.MenuBook, "Libraries")
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
                            selected = currentRoute?.startsWith(item.route) == true,
                            onClick = {
                                if (item == BottomNavItem.Libraries) {
                                    if (currentRoute?.startsWith(BottomNavItem.Libraries.route) == true &&
                                        currentRoute != BottomNavItem.Libraries.route
                                    ) {
                                        navController.navigateOrPopBackStack(
                                            BottomNavItem.Libraries.route
                                        )
                                    } else {
                                        navController.navigateOrPopBackStack(item.route)
                                    }
                                } else {
                                    navController.navigateOrPopBackStack(item.route)
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
                LoginScreen(onLoginSuccess = { navController.navigate(BottomNavItem.Home.route) { popUpTo("login") { inclusive = true } } })
            }
            composable("server_selector") {
                ServerSelectorScreen(
                    serverViewModel = authViewModel,
                    onServerSelected = { navController.navigate(BottomNavItem.Home.route) { popUpTo("server_selector") { inclusive = true } } }
                )
            }
            composable(BottomNavItem.Home.route) { HomeScreen(
                onLibrarySelected = { libraryId, libraryName ->
                    navController.navigate("library/$libraryId/$libraryName")
                }
            ) }
            composable(BottomNavItem.Libraries.route) {
                LibrariesScreen(
                    onLibrarySelected = { libraryId, libraryName ->
                        navController.navigate("library/$libraryId/$libraryName")
                    }
                )
            }
            composable(
                route = "library/{libraryId}/{libraryName}",
                arguments = listOf(
                    navArgument("libraryId") { type = NavType.StringType },
                    navArgument("libraryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val libraryId = backStackEntry.arguments?.getString("libraryId")!!
                val libraryName = backStackEntry.arguments?.getString("libraryName")!!
                LibraryDetailsScreen(libraryId,
                    libraryName,
                    onBack = { navController.popBackStack()}
                )
            }
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

