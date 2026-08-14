package com.example.modern_editor

import com.example.modern_editor.recovery.FileRecoveryStorage
import com.example.modern_editor.recovery.RecoveryRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class RecoveryStorageTest {
    @Test
    fun `TC16_5 corrupted recovery file is ignored`() {
        val dir = kotlin.io.path.createTempDirectory("recovery").toFile()
        try {
            File(dir, FileRecoveryStorage.FILE_NAME).writeText("not-a-recovery-payload")
            val storage = FileRecoveryStorage(dir)
            assertNull(storage.read())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `TC16_5 truncated recovery file is ignored`() {
        val record = RecoveryRecord(
            fileUri = "content://x",
            fileName = "a.kt",
            fileType = "KOTLIN",
            content = "fun main() {}",
            savedSnapshot = "",
            timestamp = 1L
        )
        val truncated = FileRecoveryStorage.encode(record).dropLast(8)
        assertNull(FileRecoveryStorage.decode(truncated))
    }

    @Test
    fun `TC16_5 valid recovery still round-trips`() {
        val record = RecoveryRecord(
            fileUri = null,
            fileName = "a.txt",
            fileType = "PLAIN_TEXT",
            content = "ok",
            savedSnapshot = "ok",
            timestamp = 2L
        )
        assertEquals(record, FileRecoveryStorage.decode(FileRecoveryStorage.encode(record)))
    }
}
