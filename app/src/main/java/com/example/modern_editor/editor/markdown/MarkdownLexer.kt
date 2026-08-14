package com.example.modern_editor.editor.markdown

/**
 * Single-pass Markdown scanner.
 *
 * Pure Kotlin with no Android dependency so it unit-tests on the JVM. Markdown's block
 * constructs (headings, list items, blockquotes, fenced code) are line-level, so the
 * outer loop walks lines while carrying the fenced-code state across them; inline
 * constructs (code spans, links, emphasis) are scanned within a line and never cross a
 * line boundary, so one stray `*` or `` ` `` can't bleed into the rest of the file.
 *
 * This is a highlighter's lexer, not a spec-complete CommonMark parser: it aims to be
 * correct and unsurprising for everyday Markdown rather than to handle every nesting
 * corner case.
 */
object MarkdownLexer {

    fun tokenize(text: String): List<MarkdownToken> {
        val tokens = mutableListOf<MarkdownToken>()
        val n = text.length
        var inFence = false

        var i = 0
        while (i < n) {
            var lineEnd = i
            while (lineEnd < n && text[lineEnd] != '\n') lineEnd++
            lexLine(text, i, lineEnd, inFence, tokens).let { toggled ->
                if (toggled) inFence = !inFence
            }
            i = lineEnd + 1
        }
        return tokens
    }

    /** Lexes one line [start, end); returns true if it was a fence delimiter (toggle). */
    private fun lexLine(
        text: String,
        start: Int,
        end: Int,
        inFence: Boolean,
        tokens: MutableList<MarkdownToken>
    ): Boolean {
        val indent = skipSpaces(text, start, end, max = 3)

        if (isFenceDelimiter(text, indent, end)) {
            tokens += MarkdownToken(MdTokenType.CODE_FENCE, start, end)
            return true
        }
        if (inFence) {
            if (end > start) tokens += MarkdownToken(MdTokenType.CODE_FENCE, start, end)
            return false
        }

        if (isHeading(text, indent, end)) {
            tokens += MarkdownToken(MdTokenType.HEADING, start, end)
            return false
        }

        var pos = indent
        // A blockquote marker, then possibly a list marker, then inline content.
        if (pos < end && text[pos] == '>') {
            tokens += MarkdownToken(MdTokenType.BLOCKQUOTE, pos, pos + 1)
            pos = skipSpaces(text, pos + 1, end)
            pos = skipSpaces(text, pos, end)
        }
        pos = consumeListMarker(text, pos, end, tokens)
        lexInline(text, pos, end, tokens)
        return false
    }

    // --- line-level helpers ---

    private fun skipSpaces(text: String, from: Int, end: Int, max: Int = Int.MAX_VALUE): Int {
        var i = from
        var count = 0
        while (i < end && count < max && (text[i] == ' ' || text[i] == '\t')) {
            i++; count++
        }
        return i
    }

    private fun isFenceDelimiter(text: String, from: Int, end: Int): Boolean {
        val c = if (from < end) text[from] else return false
        if (c != '`' && c != '~') return false
        var run = 0
        var i = from
        while (i < end && text[i] == c) { run++; i++ }
        return run >= 3
    }

    private fun isHeading(text: String, from: Int, end: Int): Boolean {
        var i = from
        var hashes = 0
        while (i < end && text[i] == '#') { hashes++; i++ }
        if (hashes !in 1..6) return false
        // A run of #s must be followed by a space (or be the whole line) to be a heading.
        return i >= end || text[i] == ' '
    }

    /** Consumes a leading `- `, `* `, `+ ` or `1. ` marker; returns the position after it. */
    private fun consumeListMarker(
        text: String,
        from: Int,
        end: Int,
        tokens: MutableList<MarkdownToken>
    ): Int {
        if (from >= end) return from
        val c = text[from]
        // Bullet: a single -, * or + that is followed by whitespace (so `*italic*`, whose
        // `*` is followed by a letter, is not mistaken for a bullet).
        if (c == '-' || c == '*' || c == '+') {
            val next = from + 1
            if (next >= end || text[next] == ' ' || text[next] == '\t') {
                tokens += MarkdownToken(MdTokenType.LIST_MARKER, from, from + 1)
                return skipSpaces(text, from + 1, end)
            }
            return from
        }
        // Ordered: digits then `.` or `)` then whitespace/end.
        if (c.isDigit()) {
            var i = from
            while (i < end && text[i].isDigit()) i++
            if (i < end && (text[i] == '.' || text[i] == ')')) {
                val after = i + 1
                if (after >= end || text[after] == ' ' || text[after] == '\t') {
                    tokens += MarkdownToken(MdTokenType.LIST_MARKER, from, after)
                    return skipSpaces(text, after, end)
                }
            }
        }
        return from
    }

    // --- inline scan over [from, end) ---

    private fun lexInline(
        text: String,
        from: Int,
        end: Int,
        tokens: MutableList<MarkdownToken>
    ) {
        var i = from
        while (i < end) {
            when {
                text[i] == '`' -> {
                    val close = matchCodeSpan(text, i, end)
                    if (close > i) {
                        tokens += MarkdownToken(MdTokenType.CODE_SPAN, i, close)
                        i = close
                    } else i++
                }

                text[i] == '[' -> {
                    val link = matchLink(text, i, end)
                    if (link != null) {
                        tokens += MarkdownToken(MdTokenType.LINK_TEXT, i, link.textEnd)
                        tokens += MarkdownToken(MdTokenType.LINK_URL, link.textEnd, link.urlEnd)
                        i = link.urlEnd
                    } else i++
                }

                // Bold before italic: a `**`/`__` pair must not be read as two singles.
                isDoubleDelim(text, i, end) -> {
                    val close = matchEmphasis(text, i + 2, end, text[i], 2)
                    if (close > i) {
                        tokens += MarkdownToken(MdTokenType.BOLD, i, close)
                        i = close
                    } else i++
                }

                text[i] == '*' || text[i] == '_' -> {
                    val close = matchEmphasis(text, i + 1, end, text[i], 1)
                    if (close > i) {
                        tokens += MarkdownToken(MdTokenType.ITALIC, i, close)
                        i = close
                    } else i++
                }

                else -> i++
            }
        }
    }

    /** Length of the backtick run at [openStart]. */
    private fun backtickRun(text: String, openStart: Int, end: Int): Int {
        var i = openStart
        while (i < end && text[i] == '`') i++
        return i - openStart
    }

    /**
     * End offset (exclusive) of a code span opening at [openStart], or [openStart] if it
     * isn't closed on this line. The closing run must match the opening run length.
     */
    private fun matchCodeSpan(text: String, openStart: Int, end: Int): Int {
        val runLen = backtickRun(text, openStart, end)
        var i = openStart + runLen
        while (i < end) {
            if (text[i] == '`') {
                val closeLen = backtickRun(text, i, end)
                if (closeLen == runLen) return i + closeLen
                i += closeLen
            } else i++
        }
        return openStart
    }

    private fun isDoubleDelim(text: String, i: Int, end: Int): Boolean {
        val c = text[i]
        return (c == '*' || c == '_') && i + 1 < end && text[i + 1] == c
    }

    /**
     * End offset (exclusive) of an emphasis span whose closing delimiter is [count]
     * copies of [delim], searched from [contentStart]; or 0 if unclosed on this line.
     * Requires at least one character of content so `**` alone doesn't match.
     */
    private fun matchEmphasis(text: String, contentStart: Int, end: Int, delim: Char, count: Int): Int {
        var i = contentStart
        while (i < end) {
            if (text[i] == delim) {
                var run = 0
                while (i + run < end && text[i + run] == delim) run++
                if (run >= count && i > contentStart) return i + count
                i += run
            } else i++
        }
        return 0
    }

    private class Link(val textEnd: Int, val urlEnd: Int)

    /** Matches `[label](url)` starting at the `[`; null if the whole pattern isn't present. */
    private fun matchLink(text: String, openBracket: Int, end: Int): Link? {
        var i = openBracket + 1
        while (i < end && text[i] != ']') i++
        if (i >= end || text[i] != ']') return null
        val textEnd = i + 1
        if (textEnd >= end || text[textEnd] != '(') return null
        var j = textEnd + 1
        while (j < end && text[j] != ')') j++
        if (j >= end || text[j] != ')') return null
        return Link(textEnd = textEnd, urlEnd = j + 1)
    }
}
