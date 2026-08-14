package com.example.modern_editor.editor.markdown

/** The Markdown constructs the highlighter can style. */
enum class MdTokenType {
    /** A whole `# Heading` line, levels 1–6. */
    HEADING,

    /** `**bold**` / `__bold__` (including the delimiters). */
    BOLD,

    /** `*italic*` / `_italic_` (including the delimiters). */
    ITALIC,

    /** An inline `` `code` `` span. */
    CODE_SPAN,

    /** A line inside a ``` ``` `` / `~~~` fenced code block (fence lines included). */
    CODE_FENCE,

    /** The bullet or number marker of a list item — only the marker, not its text. */
    LIST_MARKER,

    /** A leading `>` blockquote marker. */
    BLOCKQUOTE,

    /** The `[label]` portion of a `[label](url)` link. */
    LINK_TEXT,

    /** The `(url)` portion of a `[label](url)` link. */
    LINK_URL
}

/** A half-open range [start, end) of the source text and what it is. */
data class MarkdownToken(
    val type: MdTokenType,
    val start: Int,
    val end: Int
)
