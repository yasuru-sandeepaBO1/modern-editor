package com.example.modern_editor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modern_editor.ui.screens.diffcompare.DiffCompareScreen
import com.example.modern_editor.ui.screens.editor.EditorScreen
import com.example.modern_editor.ui.screens.fileslist.FilesListScreen
import com.example.modern_editor.ui.screens.home.HomeScreen
import com.example.modern_editor.ui.screens.loading.LoadingScreen
import com.example.modern_editor.ui.screens.settings.SettingsScreen
import com.example.modern_editor.ui.screens.versionhistory.VersionHistoryScreen

@Composable
fun EditorNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.Loading.route) {
        composable(Routes.Loading.route) {
            LoadingScreen(
                onLoaded = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Loading.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Home.route) {
            HomeScreen(
                onOpenEditor = { navController.navigate(Routes.Editor.route) },
                onOpenFilesList = { navController.navigate(Routes.FilesList.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }
        composable(Routes.Editor.route) {
            EditorScreen(
                onOpenVersionHistory = { navController.navigate(Routes.VersionHistory.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FilesList.route) {
            FilesListScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.VersionHistory.route) {
            VersionHistoryScreen(
                onOpenDiffCompare = { navController.navigate(Routes.DiffCompare.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DiffCompare.route) {
            DiffCompareScreen(onBack = { navController.popBackStack() })
        }
    }
}
