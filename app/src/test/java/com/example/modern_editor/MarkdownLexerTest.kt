package com.example.modern_editor

import com.example.modern_editor.editor.markdown.MarkdownLexer
import com.example.modern_editor.editor.markdown.MarkdownToken
import com.example.modern_editor.editor.markdown.MdTokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers TC6.1–TC6.5, plus the edge cases a hand-written Markdown scanner is most likely
 * to get wrong.
 */
class MarkdownLexerTest {

    private fun lex(src: String): List<Pair<MdTokenType, String>> =
        MarkdownLexer.tokenize(src).map { it.type to src.substring(it.start, it.end) }

    private fun textsOf(src: String, type: MdTokenType): List<String> =
        lex(src).filter { it.first == type }.map { it.second }

    @Test
    fun `TC6_1 heading is recognised`() {
        assertEquals(listOf("# Hello"), textsOf("# Hello", MdTokenType.HEADING))
        assertEquals(listOf("### Deeper"), textsOf("### Deeper", MdTokenType.HEADING))
    }

    @Test
    fun `TC6_2 bold is recognised`() {
        assertEquals(listOf("**Hello**"), textsOf("**Hello**", MdTokenType.BOLD))
        assertEquals(listOf("__Hello__"), textsOf("__Hello__", MdTokenType.BOLD))
    }

    @Test
    fun `TC6_3 italic is recognised`() {
        assertEquals(listOf("*Hello*"), textsOf("*Hello*", MdTokenType.ITALIC))
        assertEquals(listOf("_Hello_"), textsOf("_Hello_", MdTokenType.ITALIC))
    }

    @Test
    fun `TC6_4 list markers are recognised`() {
        // Only the marker is a token, not the item text.
        assertEquals(listOf("-", "-"), textsOf("- Apple\n- Orange", MdTokenType.LIST_MARKER))
        assertEquals(listOf("1.", "2."), textsOf("1. Apple\n2. Orange", MdTokenType.LIST_MARKER))
    }

    @Test
    fun `TC6_5 inline code is recognised`() {
        assertEquals(listOf("`println()`"), textsOf("Call `println()` here", MdTokenType.CODE_SPAN))
    }

    @Test
    fun `bold is one token, not two italics`() {
        assertTrue(textsOf("**Hello**", MdTokenType.ITALIC).isEmpty())
        assertEquals(1, textsOf("**Hello**", MdTokenType.BOLD).size)
    }

    @Test
    fun `code span is literal - no emphasis inside`() {
        assertEquals(listOf("`*x*`"), textsOf("`*x*`", MdTokenType.CODE_SPAN))
        assertTrue(textsOf("`*x*`", MdTokenType.ITALIC).isEmpty())
    }

    @Test
    fun `a bullet star is not italic and a real italic is not a bullet`() {
        // `* item` -> the star is a list marker (followed by a space).
        assertEquals(listOf("*"), textsOf("* item", MdTokenType.LIST_MARKER))
        assertTrue(textsOf("* item", MdTokenType.ITALIC).isEmpty())
        // `*italic*` at line start -> italic, not a bullet.
        assertTrue(textsOf("*italic*", MdTokenType.LIST_MARKER).isEmpty())
        assertEquals(listOf("*italic*"), textsOf("*italic*", MdTokenType.ITALIC))
    }

    @Test
    fun `fenced code block content is not inline-parsed`() {
        val src = "```\n# not a heading\n**not bold**\n```"
        val fences = textsOf(src, MdTokenType.CODE_FENCE)
        assertEquals(listOf("```", "# not a heading", "**not bold**", "```"), fences)
        assertTrue(textsOf(src, MdTokenType.HEADING).isEmpty())
        assertTrue(textsOf(src, MdTokenType.BOLD).isEmpty())
    }

    @Test
    fun `blockquote marker and links`() {
        assertEquals(listOf(">"), textsOf("> quoted", MdTokenType.BLOCKQUOTE))
        assertEquals(listOf("[label]"), textsOf("see [label](http://x)", MdTokenType.LINK_TEXT))
        assertEquals(listOf("(http://x)"), textsOf("see [label](http://x)", MdTokenType.LINK_URL))
    }

    @Test
    fun `unterminated emphasis does not bleed across lines`() {
        val src = "*open\n# heading"
        assertTrue(textsOf(src, MdTokenType.ITALIC).isEmpty())
        // The next line is still a heading, not swallowed by the open `*`.
        assertEquals(listOf("# heading"), textsOf(src, MdTokenType.HEADING))
    }

    @Test
    fun `tokens are ordered and never overlap`() {
        val src = "# Title\n- a **b** `c` [d](e)\n> q\n```\nx\n```"
        val tokens: List<MarkdownToken> = MarkdownLexer.tokenize(src)
        var previousEnd = 0
        for (t in tokens) {
            assertTrue("overlap at $t", t.start >= previousEnd)
            assertTrue("inverted $t", t.end > t.start)
            assertTrue("past end $t", t.end <= src.length)
            previousEnd = t.end
        }
    }
}
