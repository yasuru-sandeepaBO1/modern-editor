package com.example.modern_editor.domain.model

enum class FileType {
    KOTLIN,
    MARKDOWN,
    PLAIN_TEXT
}

val FileType.extension: String
    get() = when (this) {
        FileType.KOTLIN -> ".kt"
        FileType.MARKDOWN -> ".md"
        FileType.PLAIN_TEXT -> ".txt"
    }

fun fileTypeFromFileName(name: String): FileType = when {
    name.endsWith(".kt") -> FileType.KOTLIN
    name.endsWith(".md") -> FileType.MARKDOWN
    else -> FileType.PLAIN_TEXT
}

data class EditorFile(
    val id: String,
    val name: String,
    val content: String,
    val filePath: String,
    val fileType: FileType,
    val createdAt: Long,
    val modifiedAt: Long,
    val readOnly: Boolean = false
)

fun relativeTimeLabel(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diffSeconds = (now - millis) / 1000
    return when {
        diffSeconds < 60 -> "just now"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        diffSeconds < 604800 -> "${diffSeconds / 86400}d ago"
        else -> "${diffSeconds / 604800}w ago"
    }
}
