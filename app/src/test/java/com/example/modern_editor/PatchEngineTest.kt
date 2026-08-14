package com.example.modern_editor

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.VersionRepository
import com.example.modern_editor.version.PatchEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class PatchTestRepo : VersionRepository {
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

class PatchEngineTest {

    private suspend fun PatchTestRepo.put(version: Version, contentOrDelta: String) {
        saveVersion(version)
        savePatch(Patch(version.id + "-p", version.id, contentOrDelta, version.timestamp))
    }

    @Test
    fun `TC10_1 initial snapshot is stored`() = runBlocking {
        val repo = PatchTestRepo()
        val text = "hello\nworld"
        repo.put(Version("v1", "f", VersionType.SNAPSHOT, 1L, versionNumber = 1), text)
        assertEquals(text, PatchEngine.reconstruct(repo, "f", "v1"))
    }

    @Test
    fun `TC10_2 small change is a delta`() = runBlocking {
        val v1 = "line one\nline two"
        val v2 = "line one\nline two changed"
        val delta = PatchEngine.createDelta(v1, v2)
        assertTrue("delta should be smaller than a full copy", delta.length < v2.length + 40)
        assertEquals(v2, PatchEngine.applyDelta(v1, delta))
    }

    @Test
    fun `TC10_3 multiple line changes are represented`() {
        val previous = "a\nb\nc"
        val current = "a\nB\nc\nd"
        val delta = PatchEngine.createDelta(previous, current)
        assertEquals(current, PatchEngine.applyDelta(previous, delta))
    }

    @Test
    fun `TC10_4 reconstruct middle version`() = runBlocking {
        val repo = chainRepo()
        assertEquals("one\ntwo", PatchEngine.reconstruct(repo, "f", "v2"))
    }

    @Test
    fun `TC10_5 later versions store delta not full file`() = runBlocking {
        val repo = chainRepo()
        val v2Patch = repo.getPatch("v2")!!.diff
        assertTrue(v2Patch.startsWith("---") || v2Patch.contains("@@"))
        assertTrue(v2Patch.length < "one\ntwo".length + 80)
    }

    @Test
    fun `TC10_6 chain reconstruction of oldest middle newest`() = runBlocking {
        val repo = PatchTestRepo()
        var current = "v1"
        repo.put(Version("v1", "f", VersionType.SNAPSHOT, 1L, versionNumber = 1), current)
        for (n in 2..6) {
            val next = current + "\nline $n"
            val delta = PatchEngine.createDelta(current, next)
            repo.put(Version("v$n", "f", VersionType.DELTA, n.toLong(), versionNumber = n), delta)
            current = next
        }
        assertEquals("v1", PatchEngine.reconstruct(repo, "f", "v1"))
        assertEquals("v1\nline 2\nline 3", PatchEngine.reconstruct(repo, "f", "v3"))
        assertEquals(current, PatchEngine.reconstruct(repo, "f", "v6"))
    }

    private suspend fun chainRepo(): PatchTestRepo {
        val repo = PatchTestRepo()
        val t1 = "one"
        val t2 = "one\ntwo"
        val t3 = "one\ntwo\nthree"
        repo.put(Version("v1", "f", VersionType.SNAPSHOT, 1L, versionNumber = 1), t1)
        repo.put(Version("v2", "f", VersionType.DELTA, 2L, versionNumber = 2), PatchEngine.createDelta(t1, t2))
        repo.put(Version("v3", "f", VersionType.DELTA, 3L, versionNumber = 3), PatchEngine.createDelta(t2, t3))
        return repo
    }
}
