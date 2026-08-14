package com.example.modern_editor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Syntax highlighting colours — the default IntelliJ/Android Studio "Darcula" scheme,
 * i.e. what Kotlin looks like in Android Studio itself.
 *
 * These are scoped to the **code area only**. Every piece of app chrome still comes from
 * the locked 8-colour palette in [Color.kt] / docs/design_system.md; syntax highlighting
 * is the one place that needs hues distinct enough to tell token kinds apart, which the
 * chrome palette (eight near-identical dark blue-greys plus white) cannot express.
 */
object SyntaxColors {
    /** Keywords and modifiers — `class`, `fun`, `val`, `private`. */
    val Keyword = Color(0xFFCC7832)

    /** String, raw string and character literals. */
    val StringLiteral = Color(0xFF6A8759)

    /** Line and block comments. */
    val Comment = Color(0xFF808080)

    /** Annotations, including any use-site target: `@Override`, `@file:JvmName`. */
    val Annotation = Color(0xFFBBB529)

    /** Numeric literals. */
    val Number = Color(0xFF6897BB)

    /** The name in a `fun name(...)` declaration. */
    val FunctionName = Color(0xFFFFC66D)

    /** Base colour for code that isn't otherwise classified (identifiers, operators). */
    val CodeText = Color(0xFFA9B7C6)

    // --- Markdown ---
    // Structural Markdown tokens; emphasis (bold/italic) is expressed with font weight
    // and style rather than colour, so it needs no entry here.

    /** Markdown heading lines (`# Title`), rendered bold. */
    val MarkdownHeading = Color(0xFF6897BB)

    /** Markdown link text `[label]`. Reuses the classic Darcula link blue. */
    val MarkdownLink = Color(0xFF287BDE)
}
