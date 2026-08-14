package com.example.modern_editor.recovery

import java.io.File

interface RecoveryStorage {
    fun write(record: RecoveryRecord)
    fun read(): RecoveryRecord?
    fun clear()
}

class FileRecoveryStorage(private val directory: File) : RecoveryStorage {

    private val file: File get() = File(directory, FILE_NAME)

    override fun write(record: RecoveryRecord) {
        directory.mkdirs()
        file.writeText(encode(record), Charsets.UTF_8)
    }

    override fun read(): RecoveryRecord? {
        if (!file.exists()) return null
        return runCatching { decode(file.readText(Charsets.UTF_8)) }.getOrNull()
    }

    override fun clear() {
        if (file.exists()) file.delete()
    }

    companion object {
        const val FILE_NAME = "crash_recovery.txt"
        private const val HEADER = "RECOVERY_V1"

        fun encode(record: RecoveryRecord): String = buildString {
            appendLine(HEADER)
            appendLine(record.fileUri.orEmpty())
            appendLine(record.fileName)
            appendLine(record.fileType)
            appendLine(record.timestamp.toString())
            appendLine(record.savedSnapshot.length.toString())
            append(record.savedSnapshot)
            append('\n')
            appendLine(record.content.length.toString())
            append(record.content)
        }

        fun decode(raw: String): RecoveryRecord? {
            val headerEnd = raw.indexOf('\n')
            if (headerEnd < 0) return null
            if (raw.substring(0, headerEnd).trimEnd('\r') != HEADER) return null
            var cursor = headerEnd + 1

            fun nextLine(): String? {
                val end = raw.indexOf('\n', cursor)
                if (end < 0) {
                    val line = raw.substring(cursor)
                    cursor = raw.length
                    return line.trimEnd('\r')
                }
                val line = raw.substring(cursor, end).trimEnd('\r')
                cursor = end + 1
                return line
            }

            fun nextBlock(length: Int): String? {
                if (cursor + length > raw.length) return null
                val block = raw.substring(cursor, cursor + length)
                cursor += length
                if (cursor < raw.length && raw[cursor] == '\n') cursor++
                else if (cursor < raw.length && raw[cursor] == '\r') {
                    cursor++
                    if (cursor < raw.length && raw[cursor] == '\n') cursor++
                }
                return block
            }

            val uriLine = nextLine() ?: return null
            val fileName = nextLine() ?: return null
            val fileType = nextLine() ?: return null
            val timestamp = nextLine()?.toLongOrNull() ?: return null
            val savedLen = nextLine()?.toIntOrNull() ?: return null
            val savedSnapshot = nextBlock(savedLen) ?: return null
            val contentLen = nextLine()?.toIntOrNull() ?: return null
            val content = nextBlock(contentLen) ?: return null
            return RecoveryRecord(
                fileUri = uriLine.ifEmpty { null },
                fileName = fileName,
                fileType = fileType,
                content = content,
                savedSnapshot = savedSnapshot,
                timestamp = timestamp
            )
        }
    }
}

class InMemoryRecoveryStorage : RecoveryStorage {
    private var record: RecoveryRecord? = null

    override fun write(record: RecoveryRecord) {
        this.record = record
    }

    override fun read(): RecoveryRecord? = record

    override fun clear() {
        record = null
    }
}
