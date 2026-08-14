package com.example.modern_editor.editor.highlight

import androidx.compose.ui.text.SpanStyle
import com.example.modern_editor.editor.kotlin.KotlinLexer
import com.example.modern_editor.editor.kotlin.TokenType
import com.example.modern_editor.ui.theme.SyntaxColors

/**
 * Kotlin syntax colouring. Lexes the source and maps each non-plain token to a
 * [StyledSpan]; the caching, viewport limiting and transform loop all live in
 * [SyntaxHighlighter].
 */
class KotlinHighlighter : SyntaxHighlighter() {

    override fun computeSpans(text: String): List<StyledSpan> =
        KotlinLexer.tokenize(text).mapNotNull { token ->
            val style = styleFor(token.type) ?: return@mapNotNull null
            StyledSpan(token.start, token.end, style)
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
        val KeywordStyle = SpanStyle(color = SyntaxColors.Keyword)
        val StringStyle = SpanStyle(color = SyntaxColors.StringLiteral)
        val CommentStyle = SpanStyle(color = SyntaxColors.Comment)
        val AnnotationStyle = SpanStyle(color = SyntaxColors.Annotation)
        val NumberStyle = SpanStyle(color = SyntaxColors.Number)
        val FunctionStyle = SpanStyle(color = SyntaxColors.FunctionName)
    }
}
