package com.example.modern_editor

import com.example.modern_editor.editor.offsetForLine
import org.junit.Assert.assertEquals
import org.junit.Test

class GoToLineTest {
    @Test
    fun `TC15 go to line jumps to start of requested line`() {
        val text = "one\ntwo\nthree"
        assertEquals(0, offsetForLine(text, 1))
        assertEquals(4, offsetForLine(text, 2))
        assertEquals(8, offsetForLine(text, 3))
    }

    @Test
    fun `TC15 go to line clamps empty and out of range input`() {
        assertEquals(0, offsetForLine("", 1))
        assertEquals(0, offsetForLine("", 99))
        assertEquals(0, offsetForLine("abc", 0))
        assertEquals(4, offsetForLine("ab\ncd", 99))
    }
}
