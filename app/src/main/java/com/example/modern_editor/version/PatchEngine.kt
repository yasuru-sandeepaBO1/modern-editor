package com.example.modern_editor.version

import com.example.modern_editor.domain.model.Patch
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.repository.VersionRepository
import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils

object PatchEngine {

    fun createDelta(previous: String, current: String): String {
        val originalLines = splitLines(previous)
        val revisedLines = splitLines(current)
        val patch = DiffUtils.diff(originalLines, revisedLines)
        return UnifiedDiffUtils.generateUnifiedDiff("a", "b", originalLines, patch, 0)
            .joinToString("\n")
    }

    fun applyDelta(previous: String, delta: String): String {
        if (delta.isEmpty()) return previous
        val originalLines = splitLines(previous)
        val parsed = UnifiedDiffUtils.parseUnifiedDiff(delta.split("\n"))
        return DiffUtils.patch(originalLines, parsed).joinToString("\n")
    }

    fun reconstruct(versionsOldestFirst: List<Pair<Version, Patch>>): Map<String, String> {
        var current = ""
        val out = linkedMapOf<String, String>()
        for ((version, patch) in versionsOldestFirst) {
            current = if (version.type == VersionType.SNAPSHOT) {
                patch.diff
            } else {
                applyDelta(current, patch.diff)
            }
            out[version.id] = current
        }
        return out
    }

    suspend fun reconstruct(repository: VersionRepository, fileId: String, targetVersionId: String): String {
        val versions = repository.getVersionsForFile(fileId)
        val pairs = versions.map { version ->
            val patch = repository.getPatch(version.id)
                ?: error("Missing patch for version ${version.id}")
            version to patch
        }
        return reconstruct(pairs)[targetVersionId]
            ?: error("Version $targetVersionId not found")
    }

    private fun splitLines(text: String): List<String> =
        if (text.isEmpty()) emptyList() else text.split("\n")
}
