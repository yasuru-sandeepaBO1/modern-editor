package com.example.modern_editor.editor.kotlin

/**
 * Single-pass scanner over Kotlin source.
 *
 * Deliberately pure Kotlin with no Android dependency so it can be unit-tested on the
 * JVM. It only ever reports ranges — it never allocates substrings for the source — so
 * it stays cheap on large files.
 *
 * PLAIN runs are not emitted: everything the lexer doesn't classify is simply left for
 * the field's base text colour, which keeps the span count proportional to the amount of
 * *interesting* code rather than to the file size.
 */
object KotlinLexer {

    fun tokenize(text: String): List<KotlinToken> {
        val tokens = mutableListOf<KotlinToken>()
        var i = 0
        val n = text.length
        // Tracks whether the previous meaningful token was `fun`, so the next identifier
        // can be reported as a declaration name.
        var previousWasFun = false

        while (i < n) {
            val c = text[i]

            when {
                c == '/' && i + 1 < n && text[i + 1] == '/' -> {
                    val start = i
                    while (i < n && text[i] != '\n') i++
                    tokens += KotlinToken(TokenType.COMMENT, start, i)
                }

                c == '/' && i + 1 < n && text[i + 1] == '*' -> {
                    val start = i
                    i = scanBlockComment(text, i)
                    tokens += KotlinToken(TokenType.COMMENT, start, i)
                }

                // Raw strings must be tested before normal strings so `"""` isn't read
                // as an empty string followed by a quote.
                text.startsWith("\"\"\"", i) -> {
                    val start = i
                    i = scanRawString(text, i)
                    tokens += KotlinToken(TokenType.STRING, start, i)
                }

                c == '"' -> {
                    val start = i
                    i = scanString(text, i)
                    tokens += KotlinToken(TokenType.STRING, start, i)
                }

                c == '\'' -> {
                    val start = i
                    i = scanCharLiteral(text, i)
                    tokens += KotlinToken(TokenType.STRING, start, i)
                }

                c == '@' -> {
                    val start = i
                    i = scanAnnotation(text, i)
                    if (i > start + 1) {
                        tokens += KotlinToken(TokenType.ANNOTATION, start, i)
                    }
                }

                c.isDigit() -> {
                    val start = i
                    i = scanNumber(text, i)
                    tokens += KotlinToken(TokenType.NUMBER, start, i)
                }

                c == '`' -> {
                    // Backtick-quoted identifier: `name with spaces`, common in tests.
                    val start = i
                    i++
                    while (i < n && text[i] != '`' && text[i] != '\n') i++
                    if (i < n && text[i] == '`') i++
                    // An identifier, so normally no token — unless it names a function.
                    if (previousWasFun) {
                        tokens += KotlinToken(TokenType.FUNCTION_NAME, start, i)
                    }
                    previousWasFun = false
                }

                isIdentifierStart(c) -> {
                    val start = i
                    i++
                    while (i < n && isIdentifierPart(text[i])) i++
                    val word = text.substring(start, i)
                    when {
                        KotlinKeywords.isKeyword(word) -> {
                            tokens += KotlinToken(TokenType.KEYWORD, start, i)
                            previousWasFun = word == "fun"
                        }

                        previousWasFun -> {
                            tokens += KotlinToken(TokenType.FUNCTION_NAME, start, i)
                            previousWasFun = false
                        }

                        // Plain identifier — no token, base colour applies (TC5.5).
                        else -> previousWasFun = false
                    }
                }

                else -> {
                    // Operators, punctuation and whitespace. Whitespace must not clear
                    // the `fun` flag, otherwise `fun greet()` would never match.
                    if (!c.isWhitespace()) previousWasFun = false
                    i++
                }
            }
        }
        return tokens
    }

    /** Kotlin allows block comments to nest, unlike Java. */
    private fun scanBlockComment(text: String, from: Int): Int {
        var i = from + 2
        var depth = 1
        val n = text.length
        while (i < n && depth > 0) {
            when {
                text.startsWith("/*", i) -> {
                    depth++
                    i += 2
                }

                text.startsWith("*/", i) -> {
                    depth--
                    i += 2
                }

                else -> i++
            }
        }
        return i
    }

    private fun scanRawString(text: String, from: Int): Int {
        var i = from + 3
        val n = text.length
        while (i < n) {
            if (text.startsWith("\"\"\"", i)) return i + 3
            i++
        }
        return n
    }

    /**
     * A normal string stops at the closing quote, or at end of line — Kotlin strings
     * can't span lines, so bailing at the newline keeps one stray quote from colouring
     * the rest of the file.
     */
    private fun scanString(text: String, from: Int): Int {
        var i = from + 1
        val n = text.length
        while (i < n) {
            when (text[i]) {
                '\\' -> i += 2
                '"' -> return i + 1
                '\n' -> return i
                else -> i++
            }
        }
        return n
    }

    private fun scanCharLiteral(text: String, from: Int): Int {
        var i = from + 1
        val n = text.length
        while (i < n) {
            when (text[i]) {
                '\\' -> i += 2
                '\'' -> return i + 1
                '\n' -> return i
                else -> i++
            }
        }
        return n
    }

    /** `@Ann`, `@file:Ann`, `@get:Ann` — the use-site target is part of the token. */
    private fun scanAnnotation(text: String, from: Int): Int {
        var i = from + 1
        val n = text.length
        if (i < n && !isIdentifierStart(text[i])) return from + 1
        while (i < n && isIdentifierPart(text[i])) i++
        if (i < n && text[i] == ':') {
            i++
            while (i < n && isIdentifierPart(text[i])) i++
        }
        return i
    }

    private fun scanNumber(text: String, from: Int): Int {
        var i = from
        val n = text.length
        if (text.startsWith("0x", i) || text.startsWith("0X", i) ||
            text.startsWith("0b", i) || text.startsWith("0B", i)
        ) {
            i += 2
            while (i < n && (text[i].isLetterOrDigit() || text[i] == '_')) i++
            return i
        }
        while (i < n && (text[i].isDigit() || text[i] == '_')) i++
        if (i < n && text[i] == '.' && i + 1 < n && text[i + 1].isDigit()) {
            i++
            while (i < n && (text[i].isDigit() || text[i] == '_')) i++
        }
        if (i < n && (text[i] == 'e' || text[i] == 'E')) {
            val mark = i
            i++
            if (i < n && (text[i] == '+' || text[i] == '-')) i++
            if (i < n && text[i].isDigit()) {
                while (i < n && text[i].isDigit()) i++
            } else {
                i = mark
            }
        }
        // Suffixes: L, f, F, u, U (and combinations such as uL)
        while (i < n && (text[i] == 'L' || text[i] == 'f' || text[i] == 'F' ||
                    text[i] == 'u' || text[i] == 'U')
        ) i++
        return i
    }

    private fun isIdentifierStart(c: Char): Boolean = c.isLetter() || c == '_'

    private fun isIdentifierPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_'
}
