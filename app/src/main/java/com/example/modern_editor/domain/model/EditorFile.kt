package com.example.modern_editor.domain.model

enum class FileType {
    KOTLIN,
    MARKDOWN,
    PLAIN_TEXT
}

data class EditorFile(
    val id: String,
    val name: String,
    val content: String,
    val filePath: String,
    val fileType: FileType,
    val createdAt: Long,
    val modifiedAt: Long
)
