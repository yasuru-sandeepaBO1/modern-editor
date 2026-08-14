package com.example.modern_editor.editor.kotlin

/** The categories the highlighter can colour. */
enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    ANNOTATION,
    NUMBER,

    /** The name in a `fun name(...)` declaration. */
    FUNCTION_NAME,

    /** Identifiers, operators, whitespace — anything drawn in the base text colour. */
    PLAIN
}

/** A half-open range [start, end) of the source text and what it is. */
data class KotlinToken(
    val type: TokenType,
    val start: Int,
    val end: Int
)
