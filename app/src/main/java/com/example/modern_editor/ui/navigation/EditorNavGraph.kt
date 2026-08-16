package com.example.modern_editor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.modern_editor.recovery.RecoveryHolder
import com.example.modern_editor.version.VersionSession
import com.example.modern_editor.ui.components.RecoveryDialog
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
            val pendingRecovery = remember { RecoveryHolder.manager?.pendingRecovery() }
            var showRecovery by remember { mutableStateOf(pendingRecovery != null) }
            HomeScreen(
                onOpenFile = { uri -> navController.navigate(Routes.Editor.withFile(fileUri = uri)) },
                onCreateFile = { name, type ->
                    navController.navigate(Routes.Editor.withFile(fileName = name, fileType = type))
                },
                onOpenFilesList = { navController.navigate(Routes.FilesList.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
            if (showRecovery && pendingRecovery != null) {
                RecoveryDialog(
                    fileName = pendingRecovery.fileName,
                    onRestore = {
                        showRecovery = false
                        RecoveryHolder.prepareRestore(pendingRecovery)
                        navController.navigate(
                            Routes.Editor.withFile(
                                fileUri = pendingRecovery.fileUri,
                                fileName = pendingRecovery.fileName,
                                fileType = pendingRecovery.fileType
                            )
                        )
                    },
                    onDiscard = {
                        RecoveryHolder.manager?.discard()
                        showRecovery = false
                    }
                )
            }
        }
        composable(
            route = Routes.Editor.route,
            arguments = listOf(
                navArgument(Routes.Editor.ARG_FILE_URI) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.Editor.ARG_FILE_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.Editor.ARG_FILE_TYPE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            EditorScreen(
                initialFileUri = backStackEntry.arguments?.getString(Routes.Editor.ARG_FILE_URI),
                initialFileName = backStackEntry.arguments?.getString(Routes.Editor.ARG_FILE_NAME),
                initialFileType = backStackEntry.arguments?.getString(Routes.Editor.ARG_FILE_TYPE),
                onOpenVersionHistory = {
                    navController.navigate(
                        Routes.VersionHistory.withFile(VersionSession.fileId.ifBlank { "unknown" })
                    )
                },
                onOpenSettings = { navController.navigate(Routes.Settings.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FilesList.route) {
            FilesListScreen(
                onOpenFile = { uri -> navController.navigate(Routes.Editor.withFile(fileUri = uri)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.VersionHistory.route,
            arguments = listOf(
                navArgument(Routes.VersionHistory.ARG_FILE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            VersionHistoryScreen(
                fileId = backStackEntry.arguments?.getString(Routes.VersionHistory.ARG_FILE_ID),
                onOpenDiffCompare = { navController.navigate(Routes.DiffCompare.route) },
                onBack = { navController.popBackStack() },
                onRestored = { navController.popBackStack(Routes.Editor.route, inclusive = false) }
            )
        }
        composable(Routes.DiffCompare.route) {
            DiffCompareScreen(
                onBack = { navController.popBackStack() },
                onRestored = { navController.popBackStack(Routes.Editor.route, inclusive = false) }
            )
        }
    }
}
