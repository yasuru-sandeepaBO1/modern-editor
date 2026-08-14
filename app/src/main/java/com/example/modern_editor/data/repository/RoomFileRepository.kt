package com.example.modern_editor.data.repository

import com.example.modern_editor.data.database.FileDao
import com.example.modern_editor.data.database.toEntity
import com.example.modern_editor.data.database.toModel
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.repository.FileRepository
import com.example.modern_editor.domain.repository.VersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFileRepository(
    private val fileDao: FileDao,
    private val versionRepository: VersionRepository
) : FileRepository {

    override fun observeAll(): Flow<List<EditorFile>> =
        fileDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override suspend fun getAll(): List<EditorFile> = fileDao.getAll().map { it.toModel() }

    override suspend fun getById(id: String): EditorFile? = fileDao.getById(id)?.toModel()

    override suspend fun save(file: EditorFile) {
        fileDao.upsert(file.toEntity())
    }

    override suspend fun delete(id: String) {
        versionRepository.deleteForFile(id)
        fileDao.deleteById(id)
    }
}
