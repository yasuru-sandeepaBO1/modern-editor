package com.example.modern_editor

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.VersionRepository
import com.example.modern_editor.version.RollbackManager
import com.example.modern_editor.version.VersionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class RollbackTestRepo : VersionRepository {
    private val versions = mutableListOf<Version>()
    private val patches = mutableMapOf<String, Patch>()

    override suspend fun getVersionsForFile(fileId: String) =
        versions.filter { it.fileId == fileId }.sortedBy { it.versionNumber }

    override suspend fun saveVersion(version: Version) {
        versions.removeAll { it.id == version.id }
        versions += version
    }

    override suspend fun getPatch(versionId: String) = patches[versionId]

    override suspend fun savePatch(patch: Patch) {
        patches[patch.versionId] = patch
    }

    override suspend fun deleteForFile(fileId: String) {
        val ids = versions.filter { it.fileId == fileId }.map { it.id }
        versions.removeAll { it.fileId == fileId }
        ids.forEach { patches.remove(it) }
    }
}

class RollbackManagerTest {

    @Test
    fun `TC12 restore reconstructs exact version content`() = runBlocking {
        val repo = RollbackTestRepo()
        val manager = VersionManager(repo)
        manager.createVersion("f", "one")
        manager.createVersion("f", "one\ntwo")
        val v3 = manager.createVersion("f", "one\ntwo\nthree")
        val v2 = repo.getVersionsForFile("f")[1]
        val rollback = RollbackManager(repo)
        assertEquals("one\ntwo", rollback.contentAt("f", v2.id))
        assertEquals("one\ntwo\nthree", rollback.contentAt("f", v3.id))
        assertEquals("one", rollback.contentAt("f", repo.getVersionsForFile("f").first().id))
    }

    @Test
    fun `TC12 restore middle version ignores later invalid delta`() = runBlocking {
        val repo = RollbackTestRepo()
        val manager = VersionManager(repo)
        manager.createVersion("f", "one")
        val v2 = manager.createVersion("f", "one\ntwo")
        val v3 = manager.createVersion("f", "one\ntwo\nthree")
        val bad = repo.getPatch(v3.id)!!
        repo.savePatch(bad.copy(diff = "this is not a unified diff"))
        assertEquals("one\ntwo", RollbackManager(repo).contentAt("f", v2.id))
    }
}
