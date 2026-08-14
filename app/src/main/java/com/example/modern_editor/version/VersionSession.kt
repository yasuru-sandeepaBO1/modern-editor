package com.example.modern_editor.version

import kotlinx.coroutines.flow.MutableStateFlow

object VersionSession {
    var fileId: String = ""
    var fileName: String = ""
    var currentContent: String = ""
    val pendingRollbackContent = MutableStateFlow<String?>(null)
}
