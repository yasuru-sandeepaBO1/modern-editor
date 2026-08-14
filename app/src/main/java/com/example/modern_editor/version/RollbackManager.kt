package com.example.modern_editor.version

import com.example.modern_editor.domain.repository.VersionRepository

class RollbackManager(
    private val repository: VersionRepository
) {
    suspend fun contentAt(fileId: String, versionId: String): String =
        PatchEngine.reconstruct(repository, fileId, versionId)
}
