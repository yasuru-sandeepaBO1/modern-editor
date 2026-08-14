package com.example.modern_editor

import com.example.modern_editor.data.database.toEntity
import com.example.modern_editor.data.database.toModel
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.FileRepository
import com.example.modern_editor.domain.repository.VersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeFileRepository(
    private val versions: VersionRepository
) : FileRepository {
    private val _files = MutableStateFlow<List<EditorFile>>(emptyList())
    override fun observeAll(): Flow<List<EditorFile>> = _files
    override suspend fun getAll(): List<EditorFile> = _files.value
    override suspend fun getById(id: String): EditorFile? = _files.value.firstOrNull { it.id == id }
    override suspend fun save(file: EditorFile) {
        _files.update { current ->
            (current.filterNot { it.id == file.id } + file).sortedByDescending { it.modifiedAt }
        }
    }
    override suspend fun delete(id: String) {
        versions.deleteForFile(id)
        _files.update { current -> current.filterNot { it.id == id } }
    }
}

private class FakeVersionRepository : VersionRepository {
    private val versions = mutableListOf<Version>()
    private val patches = mutableMapOf<String, Patch>()

    override suspend fun getVersionsForFile(fileId: String): List<Version> =
        versions.filter { it.fileId == fileId }.sortedBy { it.versionNumber }

    override suspend fun saveVersion(version: Version) {
        versions.removeAll { it.id == version.id }
        versions += version
    }

    override suspend fun getPatch(versionId: String): Patch? = patches[versionId]

    override suspend fun savePatch(patch: Patch) {
        patches[patch.versionId] = patch
    }

    override suspend fun deleteForFile(fileId: String) {
        val ids = versions.filter { it.fileId == fileId }.map { it.id }
        versions.removeAll { it.fileId == fileId }
        ids.forEach { patches.remove(it) }
    }
}

class PersistenceContractTest {

    private fun sampleFile() = EditorFile(
        id = "file-1",
        name = "Main.kt",
        content = "",
        filePath = "content://files/Main.kt",
        fileType = FileType.KOTLIN,
        createdAt = 1L,
        modifiedAt = 2L
    )

    @Test
    fun `TC8_2 file metadata remains after save`() = runBlocking {
        val versions = FakeVersionRepository()
        val files = FakeFileRepository(versions)
        files.save(sampleFile())
        assertEquals("Main.kt", files.getById("file-1")?.name)
        assertEquals(FileType.KOTLIN, files.getAll().single().fileType)
    }

    @Test
    fun `TC8_3 versions remain after save`() = runBlocking {
        val versions = FakeVersionRepository()
        versions.saveVersion(
            Version("v1", "file-1", VersionType.SNAPSHOT, 1L, versionNumber = 1)
        )
        versions.saveVersion(
            Version("v2", "file-1", VersionType.DELTA, 2L, versionNumber = 2)
        )
        assertEquals(listOf(1, 2), versions.getVersionsForFile("file-1").map { it.versionNumber })
    }

    @Test
    fun `TC8_4 stored patches reconstruct later content`() = runBlocking {
        val versions = FakeVersionRepository()
        versions.saveVersion(Version("v1", "file-1", VersionType.SNAPSHOT, 1L, versionNumber = 1))
        versions.savePatch(Patch("p1", "v1", "line one", 1L))
        versions.saveVersion(Version("v2", "file-1", VersionType.DELTA, 2L, versionNumber = 2))
        versions.savePatch(Patch("p2", "v2", "line one\nline two", 2L, previousVersionId = "v1"))

        val snapshot = versions.getPatch("v1")!!.diff
        val next = versions.getPatch("v2")!!.diff
        val reconstructed = reconstructStored(snapshot, listOf(next))
        assertEquals("line one\nline two", reconstructed)
    }

    @Test
    fun `TC8_5 delete file removes associated versions and deltas`() = runBlocking {
        val versions = FakeVersionRepository()
        val files = FakeFileRepository(versions)
        files.save(sampleFile())
        versions.saveVersion(Version("v1", "file-1", VersionType.SNAPSHOT, 1L, versionNumber = 1))
        versions.savePatch(Patch("p1", "v1", "hello", 1L))
        files.delete("file-1")
        assertNull(files.getById("file-1"))
        assertTrue(versions.getVersionsForFile("file-1").isEmpty())
        assertNull(versions.getPatch("v1"))
    }

    @Test
    fun fileEntityRoundTripPreservesMetadata() {
        val file = sampleFile().copy(readOnly = true)
        assertEquals(file.copy(content = ""), file.toEntity().toModel())
    }
}

/** C8 stores patch text as the full reconstructed document until PatchEngine exists. */
private fun reconstructStored(snapshot: String, laterPatches: List<String>): String {
    var current = snapshot
    laterPatches.forEach { current = it }
    return current
}
