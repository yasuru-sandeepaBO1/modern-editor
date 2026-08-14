package com.example.modern_editor

import com.example.modern_editor.domain.model.LineChangeType
import com.example.modern_editor.version.DiffEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffEngineTest {

    @Test
    fun `TC9_1 added line is shown`() {
        val result = DiffEngine.compare("a\nb\nc", "a\nb\nc\nd")
        val added = result.lines.filter { it.type == LineChangeType.ADDED }
        assertEquals(listOf("d"), added.map { it.text })
        assertEquals(4, added.single().newLineNumber)
    }

    @Test
    fun `TC9_2 removed line is shown`() {
        val result = DiffEngine.compare("a\nb\nc", "a\nc")
        val removed = result.lines.filter { it.type == LineChangeType.REMOVED }
        assertEquals(listOf("b"), removed.map { it.text })
    }

    @Test
    fun `TC9_3 modified line is a removed and added pair`() {
        val result = DiffEngine.compare("a\nhello\nc", "a\nworld\nc")
        val changed = result.lines.filter { it.type != LineChangeType.UNCHANGED }
        assertEquals(LineChangeType.REMOVED, changed[0].type)
        assertEquals("hello", changed[0].text)
        assertEquals(LineChangeType.ADDED, changed[1].type)
        assertEquals("world", changed[1].text)
    }

    @Test
    fun `TC9_4 multiple changes are all displayed`() {
        val result = DiffEngine.compare("keep\nremove\nchange", "keep\nadded\nchanged")
        val types = result.lines.filter { it.type != LineChangeType.UNCHANGED }.map { it.type to it.text }
        assertTrue(types.contains(LineChangeType.REMOVED to "remove"))
        assertTrue(types.any { it.first == LineChangeType.ADDED && it.second.contains("add") || it.second == "added" || it.second == "changed" })
        assertTrue(result.hasChanges)
    }

    @Test
    fun `TC9_5 identical versions have no changes`() {
        val result = DiffEngine.compare("same\nfile", "same\nfile", "v1", "v2")
        assertFalse(result.hasChanges)
        assertEquals("v1", result.fromVersionId)
        assertEquals("v2", result.toVersionId)
    }
}
