package com.example.modern_editor.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.editor.markdown.MarkdownLexer
import com.example.modern_editor.editor.markdown.MdTokenType
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InterFontFamily
import com.example.modern_editor.ui.theme.JetBrainsMonoFamily
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import com.example.modern_editor.ui.theme.SyntaxColors

@Composable
fun MarkdownPreviewOverlay(
    source: String,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderSurface)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text(
                text = "Preview",
                color = PrimaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (source.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing to preview yet.", color = ButtonText, fontSize = 14.sp)
            }
        } else {
            val annotated = remember(source) { markdownPreviewText(source) }
            Text(
                text = annotated,
                color = PrimaryText,
                fontSize = 16.sp,
                fontFamily = InterFontFamily,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            )
        }
    }
}

fun markdownPreviewText(source: String): AnnotatedString {
    val tokens = MarkdownLexer.tokenize(source)
    return buildAnnotatedString {
        append(source)
        tokens.forEach { token ->
            val style = when (token.type) {
                MdTokenType.HEADING -> SpanStyle(
                    color = SyntaxColors.MarkdownHeading,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                MdTokenType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                MdTokenType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                MdTokenType.CODE_SPAN, MdTokenType.CODE_FENCE -> SpanStyle(
                    color = SyntaxColors.StringLiteral,
                    fontFamily = JetBrainsMonoFamily
                )
                MdTokenType.LIST_MARKER -> SpanStyle(fontWeight = FontWeight.Bold, color = SyntaxColors.Keyword)
                MdTokenType.BLOCKQUOTE -> SpanStyle(fontStyle = FontStyle.Italic, color = SyntaxColors.Comment)
                MdTokenType.LINK_TEXT -> SpanStyle(color = SyntaxColors.MarkdownLink)
                MdTokenType.LINK_URL -> SpanStyle(color = SyntaxColors.Comment)
            }
            addStyle(style, token.start, token.end)
        }
    }
}
