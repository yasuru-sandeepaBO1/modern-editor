package com.example.modern_editor

import com.example.modern_editor.editor.autoCloseAfter
import com.example.modern_editor.editor.indentAfterNewline
import com.example.modern_editor.editor.matchingBracket
import com.example.modern_editor.editor.spacesForTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartEditorTest {
    @Test
    fun `TC15 auto-close inserts a matching closer`() {
        val result = autoCloseAfter("{", selected = "", nextChar = null)!!
        assertEquals("}", result.insertAfter)
        assertEquals(0, result.caretOffsetFromInsertStart)
    }

    @Test
    fun `TC15 auto-close wraps a selection`() {
        val result = autoCloseAfter("(", selected = "x + y", nextChar = null)!!
        assertEquals("x + y)", result.insertAfter)
        assertEquals(5, result.caretOffsetFromInsertStart)
    }

    @Test
    fun `TC15 auto-close does not double a closer that is already next`() {
        assertNull(autoCloseAfter("{", selected = "", nextChar = '}'))
    }

    @Test
    fun `TC15 auto-indent copies leading spaces and adds a level after an opener`() {
        assertEquals("        ", indentAfterNewline("    fun f() {", tabSize = 4))
        assertEquals("  ", indentAfterNewline("fun f() {", tabSize = 2))
        assertEquals("", indentAfterNewline("val x = 1", tabSize = 4))
    }

    @Test
    fun `TC15 tab size expands to spaces`() {
        assertEquals("    ", spacesForTab(4))
        assertEquals("  ", spacesForTab(2))
    }

    @Test
    fun `TC15 bracket match finds a nested pair`() {
        val text = "fun f(a: List<Int>) {}"
        val inner = matchingBracket(text, text.indexOf('(') + 1)!!
        assertEquals(text.indexOf('('), inner.openIndex)
        assertEquals(text.indexOf(')'), inner.closeIndex)
    }

    @Test
    fun `TC15 bracket match ignores brackets in strings and empty text`() {
        assertNull(matchingBracket("", 0))
        val text = "val s = \"(not a pair)\""
        assertNull(matchingBracket(text, text.indexOf('(')))
    }
}
