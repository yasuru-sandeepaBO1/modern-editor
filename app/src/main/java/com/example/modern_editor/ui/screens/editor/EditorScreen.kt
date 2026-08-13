package com.example.modern_editor.ui.screens.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.editor.rememberEditorState
import com.example.modern_editor.ui.theme.GutterBorder
import com.example.modern_editor.ui.theme.TertiaryMutedText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorScreen(
    onOpenVersionHistory: () -> Unit,
    onBack: () -> Unit
) {
    val editorState = rememberEditorState()
    val undoState = editorState.undoState
    val gutterScrollState = rememberScrollState()
    var wordWrap by remember { mutableStateOf(true) }
    var showFind by remember { mutableStateOf(false) }

    val neutralButtonColors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val fieldTextStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
    // One label per *visual* (wrapped) row so the gutter stays aligned with wrapped
    // lines: only the first visual row of each logical line gets a number. Stored as
    // the final List<String> (not the raw TextLayoutResult) so Compose's structural
    // equality check on mutableStateOf can actually skip redundant recomposition when
    // a layout pass produces an identical result — TextLayoutResult itself doesn't
    // implement equals(), so storing it directly caused every layout pass to look like
    // a change and could spiral into runaway recomposition.
    var gutterRowLabels by remember { mutableStateOf(listOf("1")) }
    val onFieldTextLayout: (androidx.compose.ui.unit.Density.(() -> TextLayoutResult?) -> Unit) =
        { getResult ->
            val layout = getResult()
            val currentText = editorState.text
            gutterRowLabels = if (layout == null) {
                listOf("1")
            } else {
                var logicalLine = 1
                (0 until layout.lineCount).map { visualLine ->
                    val start = layout.getLineStart(visualLine)
                    val isLineStart = visualLine == 0 || currentText.getOrNull(start - 1) == '\n'
                    if (isLineStart) (logicalLine++).toString() else ""
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onBack, colors = neutralButtonColors) { Text("Back") }
            TextButton(
                onClick = { undoState.undo() },
                enabled = undoState.canUndo,
                colors = neutralButtonColors
            ) { Text("Undo") }
            TextButton(
                onClick = { undoState.redo() },
                enabled = undoState.canRedo,
                colors = neutralButtonColors
            ) { Text("Redo") }
            TextButton(
                onClick = { wordWrap = !wordWrap },
                colors = if (wordWrap) ButtonDefaults.textButtonColors() else neutralButtonColors
            ) { Text(if (wordWrap) "Wrap: On" else "Wrap: Off") }
            TextButton(onClick = { showFind = !showFind }, colors = neutralButtonColors) { Text("Find") }
            TextButton(onClick = onOpenVersionHistory, colors = neutralButtonColors) { Text("Version History") }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(gutterScrollState)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                for (label in gutterRowLabels) {
                    Text(
                        text = label,
                        color = TertiaryMutedText,
                        style = fieldTextStyle,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
            Box(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(GutterBorder)
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (wordWrap) {
                    BasicTextField(
                        state = editorState,
                        scrollState = gutterScrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        lineLimits = TextFieldLineLimits.MultiLine(),
                        textStyle = fieldTextStyle,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        onTextLayout = onFieldTextLayout
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        BasicTextField(
                            state = editorState,
                            scrollState = gutterScrollState,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            lineLimits = TextFieldLineLimits.MultiLine(),
                            textStyle = fieldTextStyle,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            onTextLayout = onFieldTextLayout
                        )
                    }
                }

                if (showFind) {
                    FindReplaceBar(
                        editorState = editorState,
                        onClose = { showFind = false },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
