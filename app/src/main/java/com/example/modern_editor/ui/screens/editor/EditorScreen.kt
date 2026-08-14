package com.example.modern_editor.ui.screens.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.fileTypeFromFileName
import com.example.modern_editor.editor.rememberEditorState
import com.example.modern_editor.ui.components.FileNameDialog
import com.example.modern_editor.ui.theme.GutterBorder
import com.example.modern_editor.ui.theme.TertiaryMutedText
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorScreen(
    initialFileUri: String?,
    initialFileName: String?,
    initialFileType: String?,
    onOpenVersionHistory: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val editorState = rememberEditorState()
    val undoState = editorState.undoState
    val gutterScrollState = rememberScrollState()
    var wordWrap by remember { mutableStateOf(true) }
    var showFind by remember { mutableStateOf(false) }

    var currentUriString by rememberSaveable { mutableStateOf(initialFileUri) }
    var currentFileName by rememberSaveable { mutableStateOf(initialFileName ?: "untitled.txt") }
    var currentFileType by rememberSaveable {
        mutableStateOf(
            initialFileType?.let { runCatching { FileType.valueOf(it) }.getOrNull() } ?: FileType.PLAIN_TEXT
        )
    }
    var isReadOnly by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    fun upsertRecent(uri: Uri, name: String, type: FileType) {
        scope.launch {
            val now = System.currentTimeMillis()
            val existing = InMemoryFileRepository.getById(uri.toString())
            InMemoryFileRepository.save(
                EditorFile(
                    id = uri.toString(),
                    name = name,
                    content = "",
                    filePath = uri.toString(),
                    fileType = type,
                    createdAt = existing?.createdAt ?: now,
                    modifiedAt = now
                )
            )
        }
    }

    fun saveTo(uri: Uri) {
        scope.launch {
            FileStorage.writeText(context, uri, editorState.text.toString())
            upsertRecent(uri, currentFileName, currentFileType)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            FileStorage.takePersistablePermission(context, uri)
            currentUriString = uri.toString()
            currentFileName = FileStorage.displayNameOf(context, uri) ?: currentFileName
            currentFileType = fileTypeFromFileName(currentFileName)
            saveTo(uri)
        }
    }

    suspend fun loadFile(uri: Uri) {
        FileStorage.takePersistablePermission(context, uri)
        val content = FileStorage.readText(context, uri)
        editorState.edit { replace(0, length, content) }
        undoState.clearHistory()
        val displayName = FileStorage.displayNameOf(context, uri) ?: (uri.lastPathSegment ?: "untitled.txt")
        currentUriString = uri.toString()
        currentFileName = displayName
        currentFileType = fileTypeFromFileName(displayName)
        upsertRecent(uri, displayName, currentFileType)
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { scope.launch { loadFile(it) } } }

    LaunchedEffect(initialFileUri) {
        val uriString = initialFileUri
        if (uriString != null) {
            loadFile(Uri.parse(uriString))
        }
    }

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

    if (showRenameDialog) {
        FileNameDialog(
            title = "Rename File",
            initialName = currentFileName,
            confirmLabel = "Rename",
            showTypeSelector = false,
            onConfirm = { newName, _ ->
                scope.launch {
                    val uriStr = currentUriString
                    if (uriStr != null) {
                        val uri = Uri.parse(uriStr)
                        val renamedUri = FileStorage.renameDocument(context, uri, newName) ?: uri
                        InMemoryFileRepository.delete(uri.toString())
                        currentUriString = renamedUri.toString()
                        currentFileName = newName
                        upsertRecent(renamedUri, newName, currentFileType)
                    } else {
                        currentFileName = newName
                    }
                }
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
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
            TextButton(
                onClick = { openDocumentLauncher.launch(arrayOf("*/*")) },
                colors = neutralButtonColors
            ) { Text("Open") }
            TextButton(
                onClick = {
                    val uriStr = currentUriString
                    if (uriStr != null) saveTo(Uri.parse(uriStr)) else createDocumentLauncher.launch(currentFileName)
                },
                colors = neutralButtonColors
            ) { Text("Save") }
            TextButton(
                onClick = { createDocumentLauncher.launch(currentFileName) },
                colors = neutralButtonColors
            ) { Text("Save As") }
            TextButton(onClick = { showRenameDialog = true }, colors = neutralButtonColors) { Text("Rename") }
            TextButton(
                onClick = { isReadOnly = !isReadOnly },
                colors = if (isReadOnly) ButtonDefaults.textButtonColors() else neutralButtonColors
            ) { Text(if (isReadOnly) "Read-only: On" else "Read-only: Off") }
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
                        onTextLayout = onFieldTextLayout,
                        readOnly = isReadOnly
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
                            onTextLayout = onFieldTextLayout,
                            readOnly = isReadOnly
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
