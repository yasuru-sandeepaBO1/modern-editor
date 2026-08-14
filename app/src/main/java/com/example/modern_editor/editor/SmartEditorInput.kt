package com.example.modern_editor.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Applies auto-close, tab-to-spaces, and auto-indent for a single typed character.
 */
@OptIn(ExperimentalFoundationApi::class)
class SmartEditorInput(
    private val tabSize: () -> Int,
    private val enabled: () -> Boolean
) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!enabled()) return
        if (changes.changeCount != 1) return
        val range = changes.getRange(0)
        val originalRange = changes.getOriginalRange(0)
        if (range.length != 1) return
        val inserted = asCharSequence().substring(range.min, range.max)
        val nextChar = asCharSequence().getOrNull(range.max)
        val selected = if (originalRange.length > 0) {
            originalText.subSequence(originalRange.min, originalRange.max).toString()
        } else {
            ""
        }

        when {
            inserted == "\t" -> {
                val spaces = spacesForTab(tabSize())
                replace(range.min, range.max, spaces)
                placeCursorBeforeCharAt(range.min + spaces.length)
            }
            inserted == "\n" -> {
                val text = asCharSequence()
                val lineStart = if (range.min == 0) {
                    0
                } else {
                    val idx = text.lastIndexOf('\n', startIndex = range.min - 1)
                    if (idx < 0) 0 else idx + 1
                }
                val lineBefore = text.substring(lineStart, range.min)
                val indent = indentAfterNewline(lineBefore, tabSize())
                if (indent.isNotEmpty()) {
                    replace(range.max, range.max, indent)
                    placeCursorBeforeCharAt(range.max + indent.length)
                }
            }
            else -> {
                val extra = autoCloseAfter(inserted, selected, nextChar) ?: return
                replace(range.max, range.max, extra.insertAfter)
                placeCursorBeforeCharAt(range.max + extra.caretOffsetFromInsertStart)
            }
        }
    }
}
