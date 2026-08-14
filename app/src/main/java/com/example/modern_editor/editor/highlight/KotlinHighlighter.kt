package com.example.modern_editor.editor.highlight

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import com.example.modern_editor.editor.kotlin.KotlinLexer
import com.example.modern_editor.editor.kotlin.KotlinToken
import com.example.modern_editor.editor.kotlin.TokenType
import com.example.modern_editor.ui.theme.SyntaxColors

/**
 * Applies Kotlin syntax colouring to a [androidx.compose.foundation.text.BasicTextField]
 * backed by a `TextFieldState`.
 *
 * `OutputTransformation` is the supported way to style such a field —
 * `TextFieldBuffer.addStyle` is documented as "only use this function from an
 * OutputTransformation". Styling applied here is purely presentational: it is shown to
 * the user but never becomes part of the state, so it can't leak into a saved file.
 *
 * Because it only adds styles and never edits the buffer, character offsets are
 * unchanged — which is what lets the editor's line-number gutter and current-line
 * highlight keep mapping layout offsets straight onto the state text.
 */
class KotlinHighlighter : OutputTransformation {

    // transformOutput runs every time the text is redrawn — including on cursor moves
    // and scrolls, where the text hasn't changed at all. Re-lexing then would burn a
    // full pass per frame, so the last result is reused until the text actually differs.
    private var cachedText: String? = null
    private var cachedTokens: List<KotlinToken> = emptyList()

    /**
     * The character range currently on screen, kept as snapshot state so that changing
     * it re-runs the transformation — Compose caches the transformed text in a
     * `derivedStateOf`, which tracks the reads made in here.
     *
     * Styling the *whole* file is what makes large files unusable: a 100 KB source
     * produces tens of thousands of spans, and Compose then re-lays-out all of them on
     * every keystroke. Measured on a 4 400-line file that was ~1.4 s and ~96 dropped
     * frames per character. Only the visible slice is ever styled now.
     */
    private var visibleRange by mutableStateOf(0..Int.MAX_VALUE)

    /**
     * Reports which characters are on screen. Snapped outward to [RANGE_QUANTUM] so that
     * ordinary scrolling doesn't invalidate the styling on every pixel.
     */
    fun setVisibleRange(start: Int, end: Int) {
        val from = ((start - VISIBLE_MARGIN) / RANGE_QUANTUM) * RANGE_QUANTUM
        val to = ((end + VISIBLE_MARGIN) / RANGE_QUANTUM + 1) * RANGE_QUANTUM
        val snapped = from.coerceAtLeast(0)..to
        if (snapped != visibleRange) visibleRange = snapped
    }

    override fun TextFieldBuffer.transformOutput() {
        val text = asCharSequence().toString()
        if (text.length > MAX_HIGHLIGHT_LENGTH) return

        val range = visibleRange
        // The whole file is still lexed: a scanner that started at the top of the
        // viewport could begin inside a string or block comment and colour the rest of
        // the file wrongly. Lexing is one cheap pass and is cached; only the styling is
        // limited to what the user can actually see.
        val tokens = tokensFor(text)
        for (token in tokens) {
            if (token.end < range.first) continue
            if (token.start > range.last) break
            val style = styleFor(token.type) ?: continue
            // Defensive clamp: the buffer is the source of these offsets, but a bad
            // range would throw and take the whole field down.
            if (token.start >= 0 && token.end <= text.length && token.start < token.end) {
                addStyle(style, token.start, token.end)
            }
        }
    }

    private fun tokensFor(text: String): List<KotlinToken> {
        if (text == cachedText) return cachedTokens
        val tokens = KotlinLexer.tokenize(text)
        cachedText = text
        cachedTokens = tokens
        return tokens
    }

    private fun styleFor(type: TokenType): SpanStyle? = when (type) {
        TokenType.KEYWORD -> KeywordStyle
        TokenType.STRING -> StringStyle
        TokenType.COMMENT -> CommentStyle
        TokenType.ANNOTATION -> AnnotationStyle
        TokenType.NUMBER -> NumberStyle
        TokenType.FUNCTION_NAME -> FunctionStyle
        // Left unstyled so the field's base colour shows through.
        TokenType.PLAIN -> null
    }

    private companion object {
        /**
         * Final backstop. Viewport limiting handles ordinary large files; past this the
         * lexing pass itself stops being worth it and the editor must stay usable
         * (TC5.7).
         */
        const val MAX_HIGHLIGHT_LENGTH = 2_000_000

        /** Styled padding above and below the viewport, so a flick shows colour. */
        const val VISIBLE_MARGIN = 4_000

        /** Visible range is snapped outward to this, to avoid churn while scrolling. */
        const val RANGE_QUANTUM = 8_000

        val KeywordStyle = SpanStyle(color = SyntaxColors.Keyword)
        val StringStyle = SpanStyle(color = SyntaxColors.StringLiteral)
        val CommentStyle = SpanStyle(color = SyntaxColors.Comment)
        val AnnotationStyle = SpanStyle(color = SyntaxColors.Annotation)
        val NumberStyle = SpanStyle(color = SyntaxColors.Number)
        val FunctionStyle = SpanStyle(color = SyntaxColors.FunctionName)
    }
}
