package com.example.modern_editor.ui.navigation

sealed class Routes(val route: String) {
    data object Loading : Routes("loading")
    data object Home : Routes("home")
    data object Editor : Routes("editor")
    data object FilesList : Routes("files_list")
    data object Settings : Routes("settings")
    data object VersionHistory : Routes("version_history")
    data object DiffCompare : Routes("diff_compare")
}
