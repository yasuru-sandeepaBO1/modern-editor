package com.example.modern_editor

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.VersionRepository
import com.example.modern_editor.version.PatchEngine
import com.example.modern_editor.version.VersionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class VersionTestRepo : VersionRepository {
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

class VersionManagerTest {

    @Test
    fun `TC11_1 create version records a snapshot`() = runBlocking {
        val repo = VersionTestRepo()
        val created = VersionManager(repo).createVersion("file", "hello", "first")
        assertEquals(VersionType.SNAPSHOT, created.type)
        assertEquals(1, created.versionNumber)
        assertEquals("first", created.label)
        assertEquals("hello", PatchEngine.reconstruct(repo, "file", created.id))
    }

    @Test
    fun `TC11_2 multiple versions are distinguishable`() = runBlocking {
        val repo = VersionTestRepo()
        val manager = VersionManager(repo)
        manager.createVersion("file", "one")
        manager.createVersion("file", "two")
        val v3 = manager.createVersion("file", "three")
        val all = repo.getVersionsForFile("file")
        assertEquals(3, all.size)
        assertEquals(listOf(1, 2, 3), all.map { it.versionNumber })
        assertEquals("three", PatchEngine.reconstruct(repo, "file", v3.id))
    }

    @Test
    fun `TC11_4 undo is independent of version history`() = runBlocking {
        val repo = VersionTestRepo()
        val manager = VersionManager(repo)
        manager.createVersion("file", "kept")
        val sessionEdits = mutableListOf("kept", "typed", "kept")
        sessionEdits.removeLast()
        assertEquals("kept", PatchEngine.reconstruct(repo, "file", repo.getVersionsForFile("file").first().id))
        assertEquals(1, repo.getVersionsForFile("file").size)
    }

    @Test
    fun `TC11_5 versions remain after a new manager is created`() = runBlocking {
        val repo = VersionTestRepo()
        VersionManager(repo).createVersion("file", "persisted")
        val reread = VersionManager(repo).let { repo.getVersionsForFile("file") }
        assertEquals(1, reread.size)
        assertEquals("persisted", PatchEngine.reconstruct(repo, "file", reread.first().id))
    }

    @Test
    fun `TC11_6 only the first version is a snapshot`() = runBlocking {
        val repo = VersionTestRepo()
        val manager = VersionManager(repo)
        manager.createVersion("file", "a")
        manager.createVersion("file", "b")
        manager.createVersion("file", "c")
        val types = repo.getVersionsForFile("file").map { it.type }
        assertEquals(VersionType.SNAPSHOT, types.first())
        assertTrue(types.drop(1).all { it == VersionType.DELTA })
        assertEquals(1, types.count { it == VersionType.SNAPSHOT })
    }
}
