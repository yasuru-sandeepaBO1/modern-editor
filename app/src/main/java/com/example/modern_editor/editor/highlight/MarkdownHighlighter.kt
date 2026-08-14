package com.example.modern_editor.editor.highlight

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.modern_editor.editor.markdown.MarkdownLexer
import com.example.modern_editor.editor.markdown.MdTokenType
import com.example.modern_editor.ui.theme.SyntaxColors

/**
 * Markdown syntax colouring. Emphasis is rendered the way real Markdown editors do it —
 * `**bold**` in an actual bold weight, `*italic*` in italics — rather than as a tint;
 * colour is reserved for structural tokens (headings, code, list markers, quotes, links).
 * The base text stays the editor's normal colour so prose reads naturally.
 */
class MarkdownHighlighter : SyntaxHighlighter() {

    override fun computeSpans(text: String): List<StyledSpan> =
        MarkdownLexer.tokenize(text).map { token ->
            StyledSpan(token.start, token.end, styleFor(token.type))
        }

    private fun styleFor(type: MdTokenType): SpanStyle = when (type) {
        MdTokenType.HEADING -> HeadingStyle
        MdTokenType.BOLD -> BoldStyle
        MdTokenType.ITALIC -> ItalicStyle
        MdTokenType.CODE_SPAN, MdTokenType.CODE_FENCE -> CodeStyle
        MdTokenType.LIST_MARKER -> ListMarkerStyle
        MdTokenType.BLOCKQUOTE -> BlockquoteStyle
        MdTokenType.LINK_TEXT -> LinkTextStyle
        MdTokenType.LINK_URL -> LinkUrlStyle
    }

    private companion object {
        val HeadingStyle = SpanStyle(color = SyntaxColors.MarkdownHeading, fontWeight = FontWeight.Bold)
        val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)
        val ItalicStyle = SpanStyle(fontStyle = FontStyle.Italic)
        val CodeStyle = SpanStyle(color = SyntaxColors.StringLiteral)
        val ListMarkerStyle = SpanStyle(color = SyntaxColors.Keyword, fontWeight = FontWeight.Bold)
        val BlockquoteStyle = SpanStyle(color = SyntaxColors.Comment, fontStyle = FontStyle.Italic)
        val LinkTextStyle = SpanStyle(color = SyntaxColors.MarkdownLink)
        val LinkUrlStyle = SpanStyle(color = SyntaxColors.Comment)
    }
}
