package com.example.modern_editor.data.repository

import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Session-only recent-files index. Component 8 (Room) replaces this backing store
 * without changing [FileRepository] callers — surviving app restart is explicitly that
 * component's job (TC8.2), not this one's.
 */
object InMemoryFileRepository : FileRepository {

    private val _files = MutableStateFlow<List<EditorFile>>(emptyList())
    val files: StateFlow<List<EditorFile>> = _files

    override suspend fun getAll(): List<EditorFile> = _files.value

    override suspend fun getById(id: String): EditorFile? = _files.value.firstOrNull { it.id == id }

    override suspend fun save(file: EditorFile) {
        _files.update { current ->
            (current.filterNot { it.id == file.id } + file).sortedByDescending { it.modifiedAt }
        }
    }

    override suspend fun delete(id: String) {
        _files.update { current -> current.filterNot { it.id == id } }
    }
}
