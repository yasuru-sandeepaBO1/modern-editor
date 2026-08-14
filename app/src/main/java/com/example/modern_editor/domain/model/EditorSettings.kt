package com.example.modern_editor.domain.model

data class EditorSettings(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val wordWrap: Boolean = true,
    val lineNumbers: Boolean = true,
    val highlightCurrentLine: Boolean = true,
    val theme: AppTheme = AppTheme.DARK,
    val syntaxHighlighting: Boolean = true,
    val autoSaveIntervalMs: Long = 10_000L,
    val readOnlyByDefault: Boolean = false
)

enum class AppTheme { DARK, LIGHT, SYSTEM }
