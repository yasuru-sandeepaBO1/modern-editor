package com.example.modern_editor.version

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.VersionRepository
import java.util.UUID

class VersionManager(
    private val repository: VersionRepository,
    private val snapshotManager: SnapshotManager = SnapshotManager()
) {
    suspend fun createVersion(fileId: String, content: String, label: String? = null): Version {
        val existing = repository.getVersionsForFile(fileId)
        val nextNumber = (existing.maxOfOrNull { it.versionNumber } ?: 0) + 1
        val isFirst = existing.isEmpty()
        val version = Version(
            id = UUID.randomUUID().toString(),
            fileId = fileId,
            type = if (isFirst) VersionType.SNAPSHOT else VersionType.DELTA,
            timestamp = System.currentTimeMillis(),
            label = label,
            versionNumber = nextNumber
        )
        val patchText = if (isFirst) {
            snapshotManager.snapshotContent(content)
        } else {
            val previousId = existing.last().id
            val previousContent = PatchEngine.reconstruct(repository, fileId, previousId)
            PatchEngine.createDelta(previousContent, content)
        }
        repository.saveVersion(version)
        repository.savePatch(
            Patch(
                id = "${version.id}-patch",
                versionId = version.id,
                diff = patchText,
                timestamp = version.timestamp,
                previousVersionId = existing.lastOrNull()?.id
            )
        )
        return version
    }
}
