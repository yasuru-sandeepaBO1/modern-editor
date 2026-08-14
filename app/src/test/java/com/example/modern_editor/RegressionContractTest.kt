package com.example.modern_editor

import com.example.modern_editor.domain.model.LineChangeType
import com.example.modern_editor.version.DiffEngine
import com.example.modern_editor.version.PatchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM contract checks for Component 16 (TC16.8–TC16.11).
 * Device workflows such as a fresh APK install remain manual.
 */
class RegressionContractTest {
    @Test
    fun `TC16_8 identical text produces no diff changes`() {
        val result = DiffEngine.compare("same\n", "same\n")
        assertFalse(result.hasChanges)
        assertTrue(result.lines.all { it.type == LineChangeType.UNCHANGED })
    }

    @Test
    fun `TC16_9 delta reconstructs later content`() {
        val first = "line one\n"
        val second = "line one\nline two\n"
        val delta = PatchEngine.createDelta(first, second)
        assertEquals(second, PatchEngine.applyDelta(first, delta))
    }

    @Test
    fun `TC16_11 reasonably large buffer diffs without throwing`() {
        val old = (1..2_000).joinToString("\n") { "line $it" }
        val new = old.replace("line 100", "line 100 edited")
        val result = DiffEngine.compare(old, new)
        assertTrue(result.hasChanges)
        assertTrue(result.lines.any { it.type == LineChangeType.ADDED && it.text.contains("edited") })
    }
}
