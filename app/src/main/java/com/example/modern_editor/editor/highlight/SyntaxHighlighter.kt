package com.example.modern_editor.editor.highlight

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Base for language highlighters that colour a
 * [androidx.compose.foundation.text.BasicTextField] backed by a `TextFieldState`.
 *
 * `OutputTransformation` is the supported way to style such a field —
 * `TextFieldBuffer.addStyle` is documented as "only use this function from an
 * OutputTransformation". Styling applied here is purely presentational: it is shown to
 * the user but never becomes part of the state, so it can't leak into a saved file.
 *
 * Because it only adds styles and never edits the buffer, character offsets are
 * unchanged — which is what lets the editor's line-number gutter and current-line
 * highlight keep mapping layout offsets straight onto the state text.
 *
 * Subclasses only supply [computeSpans]; the caching, viewport limiting and per-frame
 * transform loop live here so every language shares the same tuned behaviour.
 */
abstract class SyntaxHighlighter : OutputTransformation {

    /**
     * Spans to paint for [text], ordered by [StyledSpan.start] and non-overlapping.
     * Called at most once per distinct text (results are cached below).
     */
    protected abstract fun computeSpans(text: String): List<StyledSpan>

    // transformOutput runs every time the text is redrawn — including on cursor moves
    // and scrolls, where the text hasn't changed at all. Recomputing then would burn a
    // full pass per frame, so the last result is reused until the text actually differs.
    private var cachedText: String? = null
    private var cachedSpans: List<StyledSpan> = emptyList()

    /**
     * The character range currently on screen, kept as snapshot state so that changing
     * it re-runs the transformation — Compose caches the transformed text in a
     * `derivedStateOf`, which tracks the reads made in here.
     *
     * Styling the *whole* file is what makes large files unusable: a 100 KB source
     * produces tens of thousands of spans, and Compose then re-lays-out all of them on
     * every keystroke. Measured on a 4 400-line file that was ~1.4 s and ~96 dropped
     * frames per character. Only the visible slice is ever styled.
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

    final override fun TextFieldBuffer.transformOutput() {
        val text = asCharSequence().toString()
        if (text.length > MAX_HIGHLIGHT_LENGTH) return

        val range = visibleRange
        // The whole file is still analysed: a scanner that started at the top of the
        // viewport could begin inside a string or fenced block and colour the rest of
        // the file wrongly. That pass is cheap and cached; only the styling is limited
        // to what the user can actually see.
        val spans = spansFor(text)
        for (span in spans) {
            if (span.end < range.first) continue
            if (span.start > range.last) break
            // Defensive clamp: the buffer is the source of these offsets, but a bad
            // range would throw and take the whole field down.
            if (span.start >= 0 && span.end <= text.length && span.start < span.end) {
                addStyle(span.style, span.start, span.end)
            }
        }
    }

    private fun spansFor(text: String): List<StyledSpan> {
        if (text == cachedText) return cachedSpans
        val spans = computeSpans(text)
        cachedText = text
        cachedSpans = spans
        return spans
    }

    protected companion object {
        /**
         * Final backstop. Viewport limiting handles ordinary large files; past this the
         * analysis pass itself stops being worth it and the editor must stay usable.
         */
        const val MAX_HIGHLIGHT_LENGTH = 2_000_000

        /** Styled padding above and below the viewport, so a flick shows colour. */
        const val VISIBLE_MARGIN = 4_000

        /** Visible range is snapped outward to this, to avoid churn while scrolling. */
        const val RANGE_QUANTUM = 8_000
    }
}
