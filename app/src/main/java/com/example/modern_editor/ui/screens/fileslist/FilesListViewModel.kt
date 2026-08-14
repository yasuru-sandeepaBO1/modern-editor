package com.example.modern_editor.ui.screens.fileslist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.modern_editor.EditorApplication
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.fileTypeFromFileName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilesListViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorApplication
    val files = app.fileRepository.observeAll()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    suspend fun rename(file: EditorFile, newName: String) {
        runCatching {
            val uri = Uri.parse(file.filePath)
            val renamedUri = FileStorage.renameDocument(app, uri, newName) ?: uri
            app.fileRepository.delete(file.id)
            app.fileRepository.save(
                file.copy(
                    id = renamedUri.toString(),
                    name = newName,
                    filePath = renamedUri.toString(),
                    fileType = fileTypeFromFileName(newName),
                    modifiedAt = System.currentTimeMillis()
                )
            )
        }.onFailure { _error.value = "Could not rename that file." }
    }

    suspend fun delete(file: EditorFile) {
        runCatching {
            FileStorage.deleteDocument(app, Uri.parse(file.filePath))
            app.fileRepository.delete(file.id)
        }.onFailure { _error.value = "Could not delete that file." }
    }
}
