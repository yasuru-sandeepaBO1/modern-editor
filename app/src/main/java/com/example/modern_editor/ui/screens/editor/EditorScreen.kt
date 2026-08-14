package com.example.modern_editor.ui.screens.editor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.extension
import com.example.modern_editor.domain.model.fileTypeFromFileName
import com.example.modern_editor.editor.rememberEditorState
import com.example.modern_editor.ui.components.FileMenu
import com.example.modern_editor.ui.components.FileNameDialog
import com.example.modern_editor.ui.components.OptionsMenu
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.EditorSurface
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import kotlinx.coroutines.launch

private val ACCESSORY_KEYWORDS = listOf("val", "var", "fun", "if", "else", "for", "while", "return", "class")
private val ACCESSORY_SYMBOLS = listOf("{", "}", "(", ")", ";", ":", "->", "\"")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorScreen(
    initialFileUri: String?,
    initialFileName: String?,
    initialFileType: String?,
    onOpenVersionHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val editorState = rememberEditorState()
    val undoState = editorState.undoState
    val gutterScrollState = rememberScrollState()
    var wordWrap by remember { mutableStateOf(true) }
    var showFind by remember { mutableStateOf(false) }
    var fileMenuOpen by remember { mutableStateOf(false) }
    var optionsMenuOpen by remember { mutableStateOf(false) }
    var fullScreen by remember { mutableStateOf(false) }
    var toolbarHidden by remember { mutableStateOf(false) }

    var currentUriString by rememberSaveable { mutableStateOf(initialFileUri) }
    var currentFileName by rememberSaveable { mutableStateOf(initialFileName ?: "untitled.txt") }
    var currentFileType by rememberSaveable {
        mutableStateOf(
            initialFileType?.let { runCatching { FileType.valueOf(it) }.getOrNull() } ?: FileType.PLAIN_TEXT
        )
    }
    var isReadOnly by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }

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

    fun doSave() {
        val uriStr = currentUriString
        if (uriStr != null) saveTo(Uri.parse(uriStr)) else createDocumentLauncher.launch(currentFileName)
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

    val view = LocalView.current
    LaunchedEffect(fullScreen) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        if (fullScreen) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun goToLine(line: Int) {
        val text = editorState.text.toString()
        val lines = text.split("\n")
        val targetIndex = (line - 1).coerceIn(0, lines.size - 1)
        var offset = 0
        for (i in 0 until targetIndex) offset += lines[i].length + 1
        editorState.edit { selection = TextRange(offset.coerceIn(0, length)) }
    }

    fun shareContent() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, editorState.text.toString())
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    fun insertToken(token: String) {
        editorState.edit {
            val sel = selection
            replace(sel.min, sel.max, token)
        }
    }

    fun startNewFile(name: String, type: FileType) {
        editorState.edit { replace(0, length, "") }
        undoState.clearHistory()
        currentUriString = null
        currentFileName = name
        currentFileType = type
        isReadOnly = false
    }

    if (showNewFileDialog) {
        FileNameDialog(
            title = "New File",
            initialName = "untitled",
            confirmLabel = "Create",
            showTypeSelector = true,
            onConfirm = { name, type ->
                val finalType = type ?: FileType.PLAIN_TEXT
                val finalName = if (name.endsWith(finalType.extension)) name else name + finalType.extension
                startNewFile(finalName, finalType)
                showNewFileDialog = false
            },
            onDismiss = { showNewFileDialog = false }
        )
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

    val fieldTextStyle = TextStyle(
        color = PrimaryText,
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderSurface)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { fileMenuOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "File menu", tint = PrimaryText)
                }
                Text(
                    text = currentFileName,
                    color = PrimaryText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showFind = !showFind }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = PrimaryText)
                }
                IconButton(onClick = { doSave() }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = PrimaryText)
                }
                IconButton(onClick = { undoState.undo() }, enabled = undoState.canUndo) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (undoState.canUndo) PrimaryText else GutterText
                    )
                }
                IconButton(onClick = { undoState.redo() }, enabled = undoState.canRedo) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (undoState.canRedo) PrimaryText else GutterText
                    )
                }
                IconButton(onClick = { optionsMenuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = PrimaryText)
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(gutterScrollState)
                        .background(EditorSurface)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    for (label in gutterRowLabels) {
                        Text(
                            text = label,
                            color = GutterText,
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
                        .background(InactiveSurface)
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(EditorSurface)
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
                            cursorBrush = SolidColor(PrimaryText),
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
                                cursorBrush = SolidColor(PrimaryText),
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

            if (!toolbarHidden) {
                AccessoryToolbar(onInsert = ::insertToken)
            }

            StatusBar(
                text = editorState.text.toString(),
                selectionStart = editorState.selection.start,
                fileType = currentFileType,
                isReadOnly = isReadOnly
            )
        }

        FileMenu(
            open = fileMenuOpen,
            onDismiss = { fileMenuOpen = false },
            onOpen = { openDocumentLauncher.launch(arrayOf("*/*")) },
            onNewFile = { showNewFileDialog = true },
            onSave = { doSave() },
            onSaveAs = { createDocumentLauncher.launch(currentFileName) },
            onRename = { showRenameDialog = true },
            onSettings = onOpenSettings
        )

        OptionsMenu(
            open = optionsMenuOpen,
            onDismiss = { optionsMenuOpen = false },
            wordWrap = wordWrap,
            onToggleWordWrap = { wordWrap = !wordWrap },
            fullScreen = fullScreen,
            onToggleFullScreen = { fullScreen = !fullScreen },
            canUndo = undoState.canUndo,
            onUndo = { undoState.undo() },
            canRedo = undoState.canRedo,
            onRedo = { undoState.redo() },
            readOnly = isReadOnly,
            onToggleReadOnly = { isReadOnly = !isReadOnly },
            toolbarHidden = toolbarHidden,
            onToggleHideToolbar = { toolbarHidden = !toolbarHidden },
            onGoToLine = ::goToLine,
            onShare = ::shareContent,
            onRename = { showRenameDialog = true },
            onVersionHistory = onOpenVersionHistory,
            onSettings = onOpenSettings
        )
    }
}

@Composable
private fun AccessoryToolbar(onInsert: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderSurface)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ACCESSORY_KEYWORDS.forEach { token -> AccessoryButton(token) { onInsert(token) } }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ACCESSORY_SYMBOLS.forEach { token -> AccessoryButton(token) { onInsert(token) } }
        }
    }
}

@Composable
private fun AccessoryButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ButtonSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = ButtonText, fontSize = 13.sp)
    }
}

@Composable
private fun StatusBar(text: String, selectionStart: Int, fileType: FileType, isReadOnly: Boolean) {
    val (line, col) = remember(text, selectionStart) { computeLineCol(text, selectionStart) }
    val languageLabel = when (fileType) {
        FileType.KOTLIN -> "Kotlin"
        FileType.MARKDOWN -> "Markdown"
        FileType.PLAIN_TEXT -> "Plain Text"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorSurface)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Line $line, Col $col", color = GutterText, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "UTF-8", color = GutterText, fontSize = 11.sp)
            Text(text = languageLabel, color = GutterText, fontSize = 11.sp)
            if (isReadOnly) {
                Box(
                    modifier = Modifier
                        .background(ButtonSurface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "READ-ONLY", color = ButtonText, fontSize = 9.sp)
                }
            }
        }
    }
}

private fun computeLineCol(text: String, offset: Int): Pair<Int, Int> {
    val safeOffset = offset.coerceIn(0, text.length)
    var line = 1
    var lastNewline = -1
    for (i in 0 until safeOffset) {
        if (text[i] == '\n') {
            line++
            lastNewline = i
        }
    }
    val col = safeOffset - lastNewline
    return line to col
}
