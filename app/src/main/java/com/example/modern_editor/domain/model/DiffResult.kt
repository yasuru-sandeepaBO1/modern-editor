package com.example.modern_editor.domain.model

enum class LineChangeType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class DiffLine(
    val type: LineChangeType,
    val text: String,
    val oldLineNumber: Int?,
    val newLineNumber: Int?
)

data class DiffResult(
    val fromVersionId: String,
    val toVersionId: String,
    val lines: List<DiffLine>
) {
    val hasChanges: Boolean get() = lines.any { it.type != LineChangeType.UNCHANGED }
}
