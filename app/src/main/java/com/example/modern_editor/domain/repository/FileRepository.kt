package com.example.modern_editor.domain.repository

import com.example.modern_editor.domain.model.EditorFile
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun observeAll(): Flow<List<EditorFile>>
    suspend fun getAll(): List<EditorFile>
    suspend fun getById(id: String): EditorFile?
    suspend fun save(file: EditorFile)
    suspend fun delete(id: String)
}
