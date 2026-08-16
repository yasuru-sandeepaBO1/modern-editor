package com.example.modern_editor.editor.markdown

/** Column alignment for a GFM table cell. */
enum class MdAlign {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Block-level Markdown constructs after markers have been stripped.
 * Preview renders these; the lexer is a separate highlighter and is not used here.
 */
sealed class MdBlock {
    data class Heading(val level: Int, val inlines: List<MdInline>) : MdBlock()
    data class Paragraph(val inlines: List<MdInline>) : MdBlock()
    data class ListBlock(
        val ordered: Boolean,
        val start: Int = 1,
        val items: List<MdListItem>
    ) : MdBlock()
    data class BlockQuote(val children: List<MdBlock>) : MdBlock()
    data class CodeBlock(val language: String?, val code: String) : MdBlock()
    data class Mermaid(val source: String) : MdBlock()
    data class Table(
        val headers: List<List<MdInline>>,
        val aligns: List<MdAlign>,
        val rows: List<List<List<MdInline>>>
    ) : MdBlock()
    data object HorizontalRule : MdBlock()
}

/** One item of an ordered or unordered list. [checked] is non-null for task items. */
data class MdListItem(
    val checked: Boolean?,
    val blocks: List<MdBlock>
)

sealed class MdInline {
    data class Text(val value: String) : MdInline()
    data object HardBreak : MdInline()
    data class Bold(val children: List<MdInline>) : MdInline()
    data class Italic(val children: List<MdInline>) : MdInline()
    data class Strike(val children: List<MdInline>) : MdInline()
    data class Code(val value: String) : MdInline()
    data class Link(val url: String, val children: List<MdInline>) : MdInline()
    data class Image(val alt: String, val url: String, val title: String?) : MdInline()
}

@JvmName("plainTextBlocks")
fun List<MdBlock>.plainText(): String = joinToString("\n") { it.plainText() }

fun MdBlock.plainText(): String = when (this) {
    is MdBlock.Heading -> inlines.plainText()
    is MdBlock.Paragraph -> inlines.plainText()
    is MdBlock.ListBlock -> items.joinToString("\n") { item ->
        item.blocks.plainText()
    }
    is MdBlock.BlockQuote -> children.plainText()
    is MdBlock.CodeBlock -> code
    is MdBlock.Mermaid -> source
    is MdBlock.Table -> {
        val header = headers.joinToString(" | ") { it.plainText() }
        val body = rows.joinToString("\n") { row ->
            row.joinToString(" | ") { it.plainText() }
        }
        if (body.isEmpty()) header else "$header\n$body"
    }
    MdBlock.HorizontalRule -> ""
}

@JvmName("plainTextInlines")
fun List<MdInline>.plainText(): String = joinToString("") { it.plainText() }

fun MdInline.plainText(): String = when (this) {
    is MdInline.Text -> value
    MdInline.HardBreak -> "\n"
    is MdInline.Bold -> children.plainText()
    is MdInline.Italic -> children.plainText()
    is MdInline.Strike -> children.plainText()
    is MdInline.Code -> value
    is MdInline.Link -> children.plainText()
    is MdInline.Image -> alt
}
