package com.example.modern_editor.data.repository

import com.example.modern_editor.data.database.DeltaDao
import com.example.modern_editor.data.database.VersionDao
import com.example.modern_editor.data.database.toEntity
import com.example.modern_editor.data.database.toModel
import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.repository.VersionRepository

class RoomVersionRepository(
    private val versionDao: VersionDao,
    private val deltaDao: DeltaDao
) : VersionRepository {

    override suspend fun getVersionsForFile(fileId: String): List<Version> =
        versionDao.getForFile(fileId).map { it.toModel() }

    override suspend fun saveVersion(version: Version) {
        versionDao.upsert(version.toEntity())
    }

    override suspend fun getPatch(versionId: String): Patch? =
        deltaDao.getForVersion(versionId)?.toModel()

    override suspend fun savePatch(patch: Patch) {
        deltaDao.upsert(patch.toEntity())
    }

    override suspend fun deleteForFile(fileId: String) {
        deltaDao.deleteForFile(fileId)
        versionDao.deleteForFile(fileId)
    }
}
