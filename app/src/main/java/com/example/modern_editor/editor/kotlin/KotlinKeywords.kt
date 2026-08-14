package com.example.modern_editor.editor.kotlin

/**
 * Kotlin's keyword sets, split the way the language reference splits them
 * (kotlinlang.org — "Keywords and operators").
 */
object KotlinKeywords {

    /** Reserved everywhere; these can never be identifiers. */
    val HARD = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
        "in", "interface", "is", "null", "object", "package", "return", "super", "this",
        "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while"
    )

    /** Declaration modifiers. Legal as identifiers in theory, vanishingly rare in practice. */
    val MODIFIERS = setOf(
        "abstract", "actual", "annotation", "companion", "const", "crossinline", "data",
        "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
        "lateinit", "noinline", "open", "operator", "out", "override", "private",
        "protected", "public", "reified", "sealed", "suspend", "tailrec", "vararg"
    )

    /**
     * Soft keywords — but only the ones that cannot plausibly appear as a bare
     * identifier.
     *
     * The rest of Kotlin's soft keywords (`get`, `set`, `field`, `value`, `param`,
     * `property`, `receiver`, `delegate`, `file`, `setparam`, `dynamic`) are left out on
     * purpose. They are ordinary variable names in everyday code — `val value = 1` — and
     * telling the two apart needs a parser that knows the surrounding declaration, which
     * a lexer does not have. Highlighting them unconditionally would paint real
     * identifiers as keywords, so they stay plain.
     */
    val SOFT = setOf(
        "by", "catch", "constructor", "finally", "import", "init", "where"
    )

    private val ALL = HARD + MODIFIERS + SOFT

    fun isKeyword(word: String): Boolean = word in ALL
}
