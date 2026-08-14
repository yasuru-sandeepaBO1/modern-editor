package com.example.modern_editor.version

object DiffSession {
    var oldText: String = ""
    var newText: String = ""
    var fromLabel: String = "v1"
    var toLabel: String = "v2"
    var fileName: String = ""
    var fromVersionId: String = "from"
    var toVersionId: String = "to"
    var rollbackFileId: String = ""
    var rollbackVersionId: String = ""
}
