package com.example.modern_editor.domain.repository

import com.example.modern_editor.domain.model.EditorFile

interface FileRepository {
    suspend fun getAll(): List<EditorFile>
    suspend fun getById(id: String): EditorFile?
    suspend fun save(file: EditorFile)
    suspend fun delete(id: String)
}
