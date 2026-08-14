package com.example.modern_editor

import com.example.modern_editor.recovery.FileRecoveryStorage
import com.example.modern_editor.recovery.InMemoryRecoveryStorage
import com.example.modern_editor.recovery.RecoveryManager
import com.example.modern_editor.recovery.RecoveryRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryManagerTest {

    private fun dirtyRecord(content: String = "unsaved work") = RecoveryRecord(
        fileUri = "content://files/Main.kt",
        fileName = "Main.kt",
        fileType = "KOTLIN",
        content = content,
        savedSnapshot = "saved",
        timestamp = 1L
    )

    @Test
    fun `TC7_1 recovery data is created for dirty buffer`() {
        val manager = RecoveryManager(InMemoryRecoveryStorage())
        manager.cache(dirtyRecord())
        val pending = manager.pendingRecovery()
        assertNotNull(pending)
        assertEquals("unsaved work", pending!!.content)
        assertEquals("Main.kt", pending.fileName)
    }

    @Test
    fun `TC7_5 no prompt when content matches last save`() {
        val manager = RecoveryManager(InMemoryRecoveryStorage())
        manager.cache(
            RecoveryRecord(
                fileUri = null,
                fileName = "a.txt",
                fileType = "PLAIN_TEXT",
                content = "hello",
                savedSnapshot = "hello",
                timestamp = 1L
            )
        )
        assertNull(manager.pendingRecovery())
    }

    @Test
    fun `TC7_3 restore payload returns previous unsaved content`() {
        val manager = RecoveryManager(InMemoryRecoveryStorage())
        manager.cache(dirtyRecord("previous unsaved"))
        val restored = manager.pendingRecovery()
        assertEquals("previous unsaved", restored?.content)
    }

    @Test
    fun `TC7_4 discard removes recovered content`() {
        val manager = RecoveryManager(InMemoryRecoveryStorage())
        manager.cache(dirtyRecord())
        manager.discard()
        assertNull(manager.pendingRecovery())
    }

    @Test
    fun `TC7_6 stale recovery does not reappear after cleanup`() {
        val manager = RecoveryManager(InMemoryRecoveryStorage())
        manager.cache(dirtyRecord())
        manager.discard()
        manager.cache(
            RecoveryRecord(
                fileUri = null,
                fileName = "a.txt",
                fileType = "PLAIN_TEXT",
                content = "saved",
                savedSnapshot = "saved",
                timestamp = 2L
            )
        )
        assertNull(manager.pendingRecovery())
    }

    @Test
    fun encodeAndDecodeRoundTripIncludingNewlines() {
        val record = RecoveryRecord(
            fileUri = null,
            fileName = "notes.md",
            fileType = "MARKDOWN",
            content = "line1\nline2",
            savedSnapshot = "old\ntext",
            timestamp = 99L
        )
        val encoded = FileRecoveryStorage.encode(record)
        val decoded = FileRecoveryStorage.decode(encoded)
        assertEquals(record, decoded)
    }
}
