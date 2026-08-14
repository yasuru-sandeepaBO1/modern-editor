package com.example.modern_editor.recovery

import android.content.Context

class RecoveryManager(
    private val storage: RecoveryStorage,
    var intervalMs: Long = DEFAULT_INTERVAL_MS
) {
    fun cache(record: RecoveryRecord) {
        if (!record.isDirty) {
            storage.clear()
            return
        }
        storage.write(record)
    }

    fun pendingRecovery(): RecoveryRecord? =
        storage.read()?.takeIf { it.isDirty }

    fun discard() {
        storage.clear()
    }

    fun clear() = discard()

    companion object {
        const val DEFAULT_INTERVAL_MS = 10_000L
    }
}

object RecoveryHolder {
    @Volatile
    var manager: RecoveryManager? = null
        private set

    @Volatile
    var pendingApply: RecoveryRecord? = null
        private set

    fun init(context: Context) {
        if (manager == null) {
            manager = RecoveryManager(FileRecoveryStorage(context.filesDir))
        }
    }

    fun takePendingApply(): RecoveryRecord? {
        val record = pendingApply
        pendingApply = null
        return record
    }

    fun prepareRestore(record: RecoveryRecord) {
        pendingApply = record
        manager?.discard()
    }
}
