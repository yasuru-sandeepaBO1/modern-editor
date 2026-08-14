package com.example.modern_editor.version

import com.example.modern_editor.domain.model.DiffLine
import com.example.modern_editor.domain.model.DiffResult
import com.example.modern_editor.domain.model.LineChangeType
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.DeltaType

object DiffEngine {

    fun compare(
        original: String,
        revised: String,
        fromVersionId: String = "from",
        toVersionId: String = "to"
    ): DiffResult {
        val originalLines = original.split('\n')
        val revisedLines = revised.split('\n')
        val patch = DiffUtils.diff(originalLines, revisedLines)
        return DiffResult(
            fromVersionId = fromVersionId,
            toVersionId = toVersionId,
            lines = expand(originalLines, revisedLines, patch.deltas)
        )
    }

    private fun expand(
        original: List<String>,
        revised: List<String>,
        deltas: List<AbstractDelta<String>>
    ): List<DiffLine> {
        val lines = mutableListOf<DiffLine>()
        var oldIndex = 0
        var newIndex = 0
        val sorted = deltas.sortedBy { it.source.position }

        for (delta in sorted) {
            val srcPos = delta.source.position
            val dstPos = delta.target.position
            while (oldIndex < srcPos && newIndex < dstPos) {
                lines += DiffLine(LineChangeType.UNCHANGED, original[oldIndex], oldIndex + 1, newIndex + 1)
                oldIndex++
                newIndex++
            }
            when (delta.type) {
                DeltaType.INSERT -> {
                    delta.target.lines.forEach { text ->
                        lines += DiffLine(LineChangeType.ADDED, text, oldLineNumber = null, newLineNumber = newIndex + 1)
                        newIndex++
                    }
                }
                DeltaType.DELETE -> {
                    delta.source.lines.forEach { text ->
                        lines += DiffLine(LineChangeType.REMOVED, text, oldLineNumber = oldIndex + 1, newLineNumber = null)
                        oldIndex++
                    }
                }
                DeltaType.CHANGE -> {
                    delta.source.lines.forEach { text ->
                        lines += DiffLine(LineChangeType.REMOVED, text, oldLineNumber = oldIndex + 1, newLineNumber = null)
                        oldIndex++
                    }
                    delta.target.lines.forEach { text ->
                        lines += DiffLine(LineChangeType.ADDED, text, oldLineNumber = null, newLineNumber = newIndex + 1)
                        newIndex++
                    }
                }
                DeltaType.EQUAL -> {
                    delta.source.lines.forEach { text ->
                        lines += DiffLine(LineChangeType.UNCHANGED, text, oldIndex + 1, newIndex + 1)
                        oldIndex++
                        newIndex++
                    }
                }
            }
        }
        while (oldIndex < original.size && newIndex < revised.size) {
            lines += DiffLine(LineChangeType.UNCHANGED, original[oldIndex], oldIndex + 1, newIndex + 1)
            oldIndex++
            newIndex++
        }
        while (oldIndex < original.size) {
            lines += DiffLine(LineChangeType.REMOVED, original[oldIndex], oldIndex + 1, null)
            oldIndex++
        }
        while (newIndex < revised.size) {
            lines += DiffLine(LineChangeType.ADDED, revised[newIndex], null, newIndex + 1)
            newIndex++
        }
        return lines
    }
}
