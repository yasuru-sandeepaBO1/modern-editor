package com.example.modern_editor.ui.screens.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.modern_editor.EditorApplication
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.fileTypeFromFileName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorApplication
    private val files = app.fileRepository

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    data class LoadedFile(
        val content: String,
        val name: String,
        val uri: Uri,
        val type: FileType,
        val readOnly: Boolean
    )

    fun clearError() {
        _error.value = null
    }

    suspend fun load(uri: Uri): LoadedFile? = runCatching {
        FileStorage.takePersistablePermission(app, uri)
        val content = FileStorage.readText(app, uri)
        val name = FileStorage.displayNameOf(app, uri) ?: (uri.lastPathSegment ?: "untitled.txt")
        val type = fileTypeFromFileName(name)
        val existing = files.getById(uri.toString())
        upsertRecent(uri, name, type, existing?.readOnly ?: false)
        LoadedFile(content, name, uri, type, existing?.readOnly ?: false)
    }.onFailure {
        _error.value = "Could not open that file."
    }.getOrNull()

    suspend fun save(uri: Uri, text: String, name: String, type: FileType, readOnly: Boolean): Boolean = runCatching {
        FileStorage.takePersistablePermission(app, uri)
        FileStorage.writeText(app, uri, text)
        upsertRecent(uri, name, type, readOnly)
        true
    }.onFailure {
        _error.value = "Could not save the file."
    }.getOrDefault(false)

    suspend fun rename(uri: Uri, newName: String): Uri? = runCatching {
        val renamed = FileStorage.renameDocument(app, uri, newName) ?: uri
        val existing = files.getById(uri.toString())
        files.delete(uri.toString())
        upsertRecent(renamed, newName, fileTypeFromFileName(newName), existing?.readOnly ?: false)
        renamed
    }.onFailure {
        _error.value = "Could not rename the file."
    }.getOrNull()

    suspend fun setReadOnly(uri: Uri, readOnly: Boolean) {
        val existing = files.getById(uri.toString()) ?: return
        files.save(existing.copy(readOnly = readOnly, modifiedAt = System.currentTimeMillis()))
    }

    private suspend fun upsertRecent(uri: Uri, name: String, type: FileType, readOnly: Boolean) {
        val now = System.currentTimeMillis()
        val existing = files.getById(uri.toString())
        files.save(
            EditorFile(
                id = uri.toString(),
                name = name,
                content = "",
                filePath = uri.toString(),
                fileType = type,
                createdAt = existing?.createdAt ?: now,
                modifiedAt = now,
                readOnly = readOnly
            )
        )
    }
}
