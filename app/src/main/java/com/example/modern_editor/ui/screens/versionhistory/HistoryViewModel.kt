package com.example.modern_editor.ui.screens.versionhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.modern_editor.EditorApplication
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.fileTypeFromFileName
import com.example.modern_editor.version.RollbackManager
import com.example.modern_editor.version.VersionManager
import com.example.modern_editor.version.VersionSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorApplication
    private val versionsRepo = app.versionRepository
    private val fileRepo = app.fileRepository

    private val _versions = MutableStateFlow<List<Version>>(emptyList())
    val versions: StateFlow<List<Version>> = _versions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun refresh(fileId: String) {
        runCatching {
            _versions.value = versionsRepo.getVersionsForFile(fileId).sortedByDescending { it.versionNumber }
        }.onFailure { _error.value = "Could not load version history." }
    }

    suspend fun saveVersion(fileId: String, content: String, label: String?) {
        runCatching {
            ensureFileRecord(fileId)
            VersionManager(versionsRepo).createVersion(fileId, content, label)
            refresh(fileId)
        }.onFailure { _error.value = "Could not save a version." }
    }

    private suspend fun ensureFileRecord(fileId: String) {
        if (fileRepo.getById(fileId) != null) return
        val now = System.currentTimeMillis()
        val name = VersionSession.fileName.ifBlank { "untitled.txt" }
        fileRepo.save(
            EditorFile(
                id = fileId,
                name = name,
                content = "",
                filePath = fileId,
                fileType = fileTypeFromFileName(name),
                createdAt = now,
                modifiedAt = now
            )
        )
    }

    suspend fun reconstruct(fileId: String, versionId: String): String? =
        runCatching { RollbackManager(versionsRepo).contentAt(fileId, versionId) }
            .onFailure { _error.value = "Could not restore that version." }
            .getOrNull()
}
