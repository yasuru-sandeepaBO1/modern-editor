package com.example.modern_editor.ui.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object Loading : Routes("loading")
    data object Home : Routes("home")
    data object Editor : Routes("editor?fileUri={fileUri}&fileName={fileName}&fileType={fileType}") {
        const val ARG_FILE_URI = "fileUri"
        const val ARG_FILE_NAME = "fileName"
        const val ARG_FILE_TYPE = "fileType"

        fun withFile(fileUri: String? = null, fileName: String? = null, fileType: String? = null): String {
            val params = buildList {
                fileUri?.let { add("fileUri=${Uri.encode(it)}") }
                fileName?.let { add("fileName=${Uri.encode(it)}") }
                fileType?.let { add("fileType=${Uri.encode(it)}") }
            }
            return if (params.isEmpty()) "editor" else "editor?" + params.joinToString("&")
        }
    }
    data object FilesList : Routes("files_list")
    data object Settings : Routes("settings")
    data object VersionHistory : Routes("version_history")
    data object DiffCompare : Routes("diff_compare")
}
