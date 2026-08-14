package com.example.modern_editor.domain.repository

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version

interface VersionRepository {
    suspend fun getVersionsForFile(fileId: String): List<Version>
    suspend fun saveVersion(version: Version)
    suspend fun getPatch(versionId: String): Patch?
    suspend fun savePatch(patch: Patch)
    suspend fun deleteForFile(fileId: String)
}
