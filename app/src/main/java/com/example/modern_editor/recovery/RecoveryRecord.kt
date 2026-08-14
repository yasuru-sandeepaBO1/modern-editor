package com.example.modern_editor.recovery

data class RecoveryRecord(
    val fileUri: String?,
    val fileName: String,
    val fileType: String,
    val content: String,
    val savedSnapshot: String,
    val timestamp: Long
) {
    val isDirty: Boolean get() = content != savedSnapshot
}
