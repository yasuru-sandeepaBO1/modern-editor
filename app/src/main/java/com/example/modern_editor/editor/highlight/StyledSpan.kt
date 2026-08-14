package com.example.modern_editor.editor.highlight

import androidx.compose.ui.text.SpanStyle

/**
 * A half-open range [start, end) of the source text and the style to paint it with.
 * The unit a [SyntaxHighlighter] emits, independent of any particular language.
 */
data class StyledSpan(
    val start: Int,
    val end: Int,
    val style: SpanStyle
)
