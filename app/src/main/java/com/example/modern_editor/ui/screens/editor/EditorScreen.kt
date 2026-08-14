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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.extension
import com.example.modern_editor.domain.model.fileTypeFromFileName
import com.example.modern_editor.editor.highlight.KotlinHighlighter
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
import com.example.modern_editor.ui.theme.SyntaxColors
import kotlinx.coroutines.launch

private val ACCESSORY_KEYWORDS = listOf("val", "var", "fun", "if", "else", "for", "while", "return", "class")
private val ACCESSORY_SYMBOLS = listOf("{", "}", "(", ")", ";", ":", "->", "\"")

/**
 * The current-line highlight. InactiveSurface (#272b36) sits one step up from the
 * editor surface (#1e2430) in the locked palette, which reads as a light tint rather
 * than a hard band — no new color is introduced.
 */
private val CurrentLineHighlight = InactiveSurface

/** One *visual* (wrapped) row of the editor: its gutter label and layout geometry. */
private data class EditorLine(
    val label: String,
    val startOffset: Int,
    val endOffset: Int,
    val top: Float,
    val bottom: Float
)

/** Vertical padding above the first line — must match on the gutter and the field. */
private val EDITOR_VERTICAL_PADDING = 8.dp
private val GUTTER_NUMBER_WIDTH = 32.dp
private val GUTTER_WIDTH = 48.dp

/**
 * Y position of a line inside a viewport that shows the field's scrolled content.
 * Both the gutter and the code area resolve row positions through this one function so
 * they cannot drift apart.
 */
private fun lineY(line: EditorLine, scroll: Int, density: Density): Float =
    line.top - scroll + with(density) { EDITOR_VERTICAL_PADDING.toPx() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorScreen(
    initialFileUri: String?,
    initialFileName: String?,
    initialFileType: String?,
    onOpenVersionHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
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

    // Kotlin files get syntax colouring; everything else stays plain. The transformation
    // is remembered because BasicTextField keys internal state on the instance, and it
    // carries a lex cache that we don't want thrown away on every recomposition.
    val isKotlin = currentFileType == FileType.KOTLIN
    val syntaxHighlighter = remember(isKotlin) { if (isKotlin) KotlinHighlighter() else null }

    // includeFontPadding = false keeps every line exactly lineHeight tall. With the
    // legacy font padding on, the gutter and the field disagree about where a row
    // starts and the two drift apart as lines are added.
    val fieldTextStyle = TextStyle(
        // Unclassified code sits on Darcula's base text colour; plain-text and Markdown
        // files keep the white they had before.
        color = if (isKotlin) SyntaxColors.CodeText else PrimaryText,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    // One entry per *visual* (wrapped) row so the gutter stays aligned with wrapped
    // lines: only the first visual row of each logical line gets a number. We keep the
    // derived List<EditorLine> rather than the raw TextLayoutResult because
    // TextLayoutResult has no equals(), so storing it in mutableStateOf made every
    // layout pass look like a change and could spiral into runaway recomposition.
    // The top/bottom offsets also drive the current-line highlight.
    var editorLines by remember { mutableStateOf(listOf(EditorLine("1", 0, 0, 0f, 0f))) }
    val onFieldTextLayout: (androidx.compose.ui.unit.Density.(() -> TextLayoutResult?) -> Unit) =
        { getResult ->
            val layout = getResult()
            val currentText = editorState.text
            editorLines = if (layout == null) {
                listOf(EditorLine("1", 0, 0, 0f, 0f))
            } else {
                var logicalLine = 1
                (0 until layout.lineCount).map { visualLine ->
                    val start = layout.getLineStart(visualLine)
                    val isLineStart = visualLine == 0 || currentText.getOrNull(start - 1) == '\n'
                    EditorLine(
                        label = if (isLineStart) (logicalLine++).toString() else "",
                        startOffset = start,
                        endOffset = layout.getLineEnd(visualLine),
                        top = layout.getLineTop(visualLine),
                        bottom = layout.getLineBottom(visualLine)
                    )
                }
            }
        }

    // Which visual row the caret sits on — drives the current-line highlight. Reading
    // editorState.selection here is what makes the highlight follow a moving cursor
    // even when the text (and therefore the layout) hasn't changed.
    val caretOffset = editorState.selection.start
    val currentLine = editorLines.lastOrNull { caretOffset >= it.startOffset } ?: editorLines.firstOrNull()
    val density = LocalDensity.current

    // Tell the highlighter which characters are on screen so it only styles those. The
    // line geometry and scroll offset are already tracked for the gutter, so this reuses
    // them rather than measuring anything new.
    var codeViewportHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(syntaxHighlighter, editorLines, gutterScrollState.value, codeViewportHeight) {
        val highlighter = syntaxHighlighter ?: return@LaunchedEffect
        if (editorLines.isEmpty() || codeViewportHeight == 0) return@LaunchedEffect
        val top = gutterScrollState.value.toFloat()
        val bottom = top + codeViewportHeight
        val firstVisible = editorLines.lastOrNull { it.top <= top } ?: editorLines.first()
        val lastVisible = editorLines.firstOrNull { it.bottom >= bottom } ?: editorLines.last()
        highlighter.setVisibleRange(firstVisible.startOffset, lastVisible.endOffset)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .imePadding()
        ) {
            // Two-row header, per UIs/editor.html: the hamburger and the file tab sit on
            // the first row, the back arrow / context label / actions on the second.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderSurface)
                    .padding(start = 4.dp, end = 8.dp, top = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = { fileMenuOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "File menu", tint = PrimaryText)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(EditorSurface)
                        .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = ButtonText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = currentFileName,
                        color = PrimaryText,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    // Closing the only open tab leaves the editor, which is the same
                    // destination as the back arrow.
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close file",
                            tint = ButtonText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderSurface)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }
                Text(
                    text = if (currentUriString == null) "New file" else "Saved file",
                    color = ButtonText,
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
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
                IconButton(onClick = { showFind = !showFind }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = PrimaryText)
                }
                IconButton(onClick = { doSave() }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = PrimaryText)
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
                // The gutter deliberately has NO scroll container of its own. Each number
                // is positioned from the field's reported line top, shifted by the field's
                // scroll offset — the same math as the current-line highlight — so the two
                // stay in lockstep at any scroll position. Giving the gutter its own
                // verticalScroll made it clamp at a different maximum than the field, so
                // the numbers slid out of step once the buffer was long enough to scroll.
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(GUTTER_WIDTH)
                        .background(EditorSurface)
                        .clipToBounds()
                        .drawBehind {
                            currentLine?.let {
                                drawRect(
                                    color = CurrentLineHighlight,
                                    topLeft = Offset(0f, lineY(it, gutterScrollState.value, this)),
                                    size = Size(size.width, it.bottom - it.top)
                                )
                            }
                        }
                ) {
                    val viewportPx = with(density) { maxHeight.toPx() }
                    val scroll = gutterScrollState.value
                    val padTopPx = with(density) { EDITOR_VERTICAL_PADDING.toPx() }
                    val labelInsetPx = with(density) { 8.dp.roundToPx() }
                    editorLines.forEach { line ->
                        val y = line.top - scroll + padTopPx
                        // Only compose rows actually on screen, so a long file doesn't
                        // create a composable per line.
                        if (line.label.isNotEmpty() && y > -viewportPx && y < viewportPx * 2) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(labelInsetPx, y.roundToInt()) }
                                    .width(GUTTER_NUMBER_WIDTH)
                                    .height(with(density) { (line.bottom - line.top).toDp() }),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = line.label,
                                    color = GutterText,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
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
                        .onSizeChanged { codeViewportHeight = it.height }
                        // The field scrolls its own content, so unlike the gutter (whose
                        // Column is moved by verticalScroll) the line offsets have to be
                        // shifted by the scroll position and the field's top padding.
                        .drawBehind {
                            currentLine?.let {
                                drawRect(
                                    color = CurrentLineHighlight,
                                    topLeft = Offset(0f, lineY(it, gutterScrollState.value, this)),
                                    size = Size(size.width, it.bottom - it.top)
                                )
                            }
                        }
                ) {
                    if (wordWrap) {
                        BasicTextField(
                            state = editorState,
                            scrollState = gutterScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = EDITOR_VERTICAL_PADDING),
                            lineLimits = TextFieldLineLimits.MultiLine(),
                            textStyle = fieldTextStyle,
                            cursorBrush = SolidColor(PrimaryText),
                            onTextLayout = onFieldTextLayout,
                            outputTransformation = syntaxHighlighter,
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = EDITOR_VERTICAL_PADDING),
                                lineLimits = TextFieldLineLimits.MultiLine(),
                                textStyle = fieldTextStyle,
                                cursorBrush = SolidColor(PrimaryText),
                                onTextLayout = onFieldTextLayout,
                                outputTransformation = syntaxHighlighter,
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

/**
 * The token rows wrap instead of scrolling horizontally. The reference markup scrolls,
 * but at this screen width the keyword row overflows and the last token gets sliced
 * mid-glyph, which reads as broken and hides keys behind a scroll gesture. Wrapping
 * keeps every token fully drawn and one tap away at any width.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccessoryToolbar(onInsert: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ACCESSORY_KEYWORDS.forEach { token -> AccessoryButton(token) { onInsert(token) } }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = ButtonText, fontSize = 12.sp)
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
