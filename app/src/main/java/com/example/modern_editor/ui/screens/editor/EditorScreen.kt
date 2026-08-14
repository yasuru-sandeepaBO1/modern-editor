package com.example.modern_editor.ui.screens.editor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.modern_editor.editorApp
import com.example.modern_editor.domain.model.EditorSettings
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.extension
import com.example.modern_editor.domain.model.fileTypeFromFileName
import com.example.modern_editor.editor.highlight.KotlinHighlighter
import com.example.modern_editor.editor.highlight.MarkdownHighlighter
import com.example.modern_editor.editor.highlight.SyntaxHighlighter
import com.example.modern_editor.editor.autoCloseAfter
import com.example.modern_editor.editor.matchingBracket
import com.example.modern_editor.editor.offsetForLine
import com.example.modern_editor.editor.rememberEditorState
import com.example.modern_editor.editor.SmartEditorInput
import com.example.modern_editor.recovery.RecoveryHolder
import com.example.modern_editor.recovery.RecoveryRecord
import com.example.modern_editor.ui.AppViewModelFactory
import com.example.modern_editor.ui.components.FileMenu
import com.example.modern_editor.version.VersionSession
import com.example.modern_editor.ui.components.FileNameDialog
import com.example.modern_editor.ui.components.OptionsMenu
import com.example.modern_editor.ui.components.UnsavedChangesDialog
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.EditorSurface
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.JetBrainsMonoFamily
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import com.example.modern_editor.ui.theme.SyntaxColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ACCESSORY_KEYWORDS = listOf("val", "var", "fun", "if", "else", "for", "while", "return", "class")
// Symbols are far narrower than keywords, so this row needs more of them to fill the
// same width — with too few, SpaceBetween leaves ugly gaps. Chosen by how expensive they
// are on the system keyboard: braces, brackets, angles and `=` all sit two symbol-pages
// deep, and the multi-character Kotlin operators can't be typed in one tap at all.
private val ACCESSORY_SYMBOLS = listOf(
    "{", "}", "(", ")", "[", "]", "<", ">",
    "=", "->", "?.", "?:", "!!", ".."
)

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
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
    val viewModel: EditorViewModel = viewModel(factory = AppViewModelFactory(context.editorApp))
    val editorError by viewModel.error.collectAsState()

    val settingsRepo = context.editorApp.settingsRepository
    val settings by settingsRepo.settings.collectAsState(EditorSettings())
    val editorState = rememberEditorState()
    val undoState = editorState.undoState
    val gutterScrollState = rememberScrollState()
    val wordWrap = settings.wordWrap
    var showFind by remember { mutableStateOf(false) }
    var fileMenuOpen by remember { mutableStateOf(false) }
    var optionsMenuOpen by remember { mutableStateOf(false) }
    var fullScreen by remember { mutableStateOf(false) }
    var toolbarHidden by remember { mutableStateOf(false) }
    var showMarkdownPreview by remember { mutableStateOf(false) }

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

    // The text as it last existed on disk. Comparing against it is what tells us whether
    // leaving would lose work; a plain "edited" flag would stay set even after the user
    // undid their way back to the saved content.
    var savedSnapshot by rememberSaveable { mutableStateOf("") }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    // Set when a save was started specifically so we could leave afterwards.
    var leaveAfterSave by remember { mutableStateOf(false) }

    fun saveTo(uri: Uri) {
        scope.launch {
            val written = editorState.text.toString()
            val ok = viewModel.save(uri, written, currentFileName, currentFileType, isReadOnly)
            if (ok) {
                savedSnapshot = written
                RecoveryHolder.manager?.clear()
                if (leaveAfterSave) {
                    leaveAfterSave = false
                    onBack()
                }
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            currentUriString = uri.toString()
            saveTo(uri)
        }
    }

    fun doSave() {
        val uriStr = currentUriString
        if (uriStr != null) saveTo(Uri.parse(uriStr)) else createDocumentLauncher.launch(currentFileName)
    }

    suspend fun loadFile(uri: Uri) {
        val loaded = viewModel.load(uri) ?: return
        editorState.edit { replace(0, length, loaded.content) }
        undoState.clearHistory()
        savedSnapshot = loaded.content
        currentUriString = loaded.uri.toString()
        currentFileName = loaded.name
        currentFileType = loaded.type
        isReadOnly = loaded.readOnly
    }

    fun applyRecovery(record: RecoveryRecord) {
        editorState.edit { replace(0, length, record.content) }
        undoState.clearHistory()
        savedSnapshot = record.savedSnapshot
        currentUriString = record.fileUri
        currentFileName = record.fileName
        currentFileType = record.fileType.let {
            runCatching { FileType.valueOf(it) }.getOrNull()
        } ?: FileType.PLAIN_TEXT
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { scope.launch { loadFile(it) } } }

    LaunchedEffect(initialFileUri) {
        val restored = RecoveryHolder.takePendingApply()
        if (restored != null) {
            applyRecovery(restored)
            return@LaunchedEffect
        }
        val uriString = initialFileUri
        if (uriString != null) {
            loadFile(Uri.parse(uriString))
        }
    }

    val rollbackContent by VersionSession.pendingRollbackContent.collectAsState()
    LaunchedEffect(rollbackContent) {
        val text = rollbackContent ?: return@LaunchedEffect
        editorState.edit { replace(0, length, text) }
        VersionSession.pendingRollbackContent.value = null
    }

    var seededReadOnly by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(settings.readOnlyByDefault, initialFileUri) {
        if (!seededReadOnly && initialFileUri == null) {
            isReadOnly = settings.readOnlyByDefault
            seededReadOnly = true
        }
    }

    LaunchedEffect(settings.autoSaveIntervalMs) {
        val manager = RecoveryHolder.manager ?: return@LaunchedEffect
        manager.intervalMs = settings.autoSaveIntervalMs
        while (true) {
            delay(manager.intervalMs)
            manager.cache(
                RecoveryRecord(
                    fileUri = currentUriString,
                    fileName = currentFileName,
                    fileType = currentFileType.name,
                    content = editorState.text.toString(),
                    savedSnapshot = savedSnapshot,
                    timestamp = System.currentTimeMillis()
                )
            )
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
        val offset = offsetForLine(text, line)
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
            val selected = asCharSequence().substring(sel.min, sel.max)
            val next = asCharSequence().getOrNull(sel.max)
            val extra = autoCloseAfter(token, selected, next)
            if (extra != null) {
                replace(sel.min, sel.max, token + extra.insertAfter)
                selection = TextRange(sel.min + token.length + extra.caretOffsetFromInsertStart)
            } else {
                replace(sel.min, sel.max, token)
            }
        }
    }

    // Leaving is routed through here so the arrow, the tab's close button and the system
    // back gesture all get the same guard.
    val isDirty = editorState.text.toString() != savedSnapshot
    fun attemptBack() {
        if (isDirty) showUnsavedDialog = true else onBack()
    }

    fun startNewFile(name: String, type: FileType) {
        editorState.edit { replace(0, length, "") }
        undoState.clearHistory()
        savedSnapshot = ""
        currentUriString = null
        currentFileName = name
        currentFileType = type
        isReadOnly = settings.readOnlyByDefault
    }

    // Back with the keyboard up should close the keyboard, not prompt — but it must not
    // fall through to the navigator either, or unsaved work disappears silently. So the
    // handler stays enabled and hides the keyboard itself; a second back then prompts.
    // Overlays register their handlers later in the composition, so an open menu still
    // consumes back before this.
    val imeVisible = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    BackHandler(enabled = isDirty || imeVisible) {
        if (imeVisible) keyboardController?.hide() else attemptBack()
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            fileName = currentFileName,
            onSave = {
                showUnsavedDialog = false
                // A file that has never been saved needs a destination first; saveTo()
                // leaves once the picker comes back.
                leaveAfterSave = true
                doSave()
            },
            onDiscard = {
                showUnsavedDialog = false
                RecoveryHolder.manager?.clear()
                onBack()
            },
            onCancel = { showUnsavedDialog = false }
        )
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
                        val renamedUri = viewModel.rename(Uri.parse(uriStr), newName)
                        if (renamedUri != null) {
                            currentUriString = renamedUri.toString()
                            currentFileName = newName
                            currentFileType = fileTypeFromFileName(newName)
                        }
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
    val syntaxHighlighter: SyntaxHighlighter? = remember(currentFileType, settings.syntaxHighlighting) {
        if (!settings.syntaxHighlighting) return@remember null
        when (currentFileType) {
            FileType.KOTLIN -> KotlinHighlighter()
            FileType.MARKDOWN -> MarkdownHighlighter()
            FileType.PLAIN_TEXT -> null
        }
    }

    // includeFontPadding = false keeps every line exactly lineHeight tall. With the
    // legacy font padding on, the gutter and the field disagree about where a row
    // starts and the two drift apart as lines are added.
    val editorFontSize = settings.fontSize.sp
    val fieldTextStyle = TextStyle(
        // Unclassified code sits on Darcula's base text colour; plain-text and Markdown
        // files keep the white they had before.
        color = if (isKotlin && settings.syntaxHighlighting) SyntaxColors.CodeText else PrimaryText,
        fontSize = editorFontSize,
        lineHeight = (settings.fontSize + 4).sp,
        fontFamily = JetBrainsMonoFamily,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    val currentLineHighlight = InactiveSurface
    val showCurrentLine = settings.highlightCurrentLine
    val smartInput = remember(settings.tabSize, isReadOnly) {
        SmartEditorInput(tabSize = { settings.tabSize }, enabled = { !isReadOnly })
    }
    val layoutHolder = remember { arrayOfNulls<TextLayoutResult>(1) }
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
            layoutHolder[0] = layout
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
    val matchingPair = matchingBracket(editorState.text.toString(), caretOffset)
    val bracketHighlight = ButtonSurface.copy(alpha = 0.4f)

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
            if (editorError != null) {
                Text(
                    text = editorError ?: "",
                    color = ButtonText,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InactiveSurface)
                        .clickable { viewModel.clearError() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
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
                    IconButton(onClick = ::attemptBack, modifier = Modifier.size(32.dp)) {
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
                IconButton(onClick = ::attemptBack) {
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
                if (settings.lineNumbers) {
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
                            if (!showCurrentLine) return@drawBehind
                            currentLine?.let {
                                drawRect(
                                    color = currentLineHighlight,
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
                                    fontSize = editorFontSize,
                                    fontFamily = JetBrainsMonoFamily,
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
                }
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
                            if (showCurrentLine) {
                                currentLine?.let {
                                    drawRect(
                                        color = currentLineHighlight,
                                        topLeft = Offset(0f, lineY(it, gutterScrollState.value, this)),
                                        size = Size(size.width, it.bottom - it.top)
                                    )
                                }
                            }
                            val layout = layoutHolder[0]
                            val pair = matchingPair
                            if (layout != null && pair != null && layout.layoutInput.text.isNotEmpty()) {
                                val padX = 16.dp.toPx()
                                val padY = EDITOR_VERTICAL_PADDING.toPx()
                                val scroll = gutterScrollState.value.toFloat()
                                fun highlightChar(index: Int) {
                                    val i = index.coerceIn(0, layout.layoutInput.text.length - 1)
                                    val box = layout.getBoundingBox(i)
                                    drawRect(
                                        color = bracketHighlight,
                                        topLeft = Offset(box.left + padX, box.top + padY - scroll),
                                        size = Size(box.width.coerceAtLeast(1f), box.height)
                                    )
                                }
                                highlightChar(pair.openIndex)
                                highlightChar(pair.closeIndex)
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
                            inputTransformation = smartInput,
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
                                inputTransformation = smartInput,
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
            onToggleWordWrap = {
                scope.launch { settingsRepo.update { it.copy(wordWrap = !it.wordWrap) } }
            },
            fullScreen = fullScreen,
            onToggleFullScreen = { fullScreen = !fullScreen },
            canUndo = undoState.canUndo,
            onUndo = { undoState.undo() },
            canRedo = undoState.canRedo,
            onRedo = { undoState.redo() },
            readOnly = isReadOnly,
            onToggleReadOnly = {
                val next = !isReadOnly
                isReadOnly = next
                currentUriString?.let { uri ->
                    scope.launch { viewModel.setReadOnly(Uri.parse(uri), next) }
                }
            },
            toolbarHidden = toolbarHidden,
            onToggleHideToolbar = { toolbarHidden = !toolbarHidden },
            onGoToLine = ::goToLine,
            onShare = ::shareContent,
            showMarkdownPreview = currentFileType == FileType.MARKDOWN,
            onMarkdownPreview = { showMarkdownPreview = true },
            onRename = { showRenameDialog = true },
            onVersionHistory = {
                VersionSession.fileId = currentUriString ?: "local:$currentFileName"
                VersionSession.fileName = currentFileName
                VersionSession.currentContent = editorState.text.toString()
                onOpenVersionHistory()
            },
            onSettings = onOpenSettings
        )

        if (showMarkdownPreview) {
            MarkdownPreviewOverlay(
                source = editorState.text.toString(),
                onClose = { showMarkdownPreview = false }
            )
        }
    }
}

/**
 * Exactly two rows. Each button keeps its own natural width — forcing `{` to be as wide
 * as `return` reads badly — and the rows are stretched edge to edge with
 * [Arrangement.SpaceBetween]. The gaps take up the slack, so both rows start at the same
 * left edge and finish at the same right edge even though their tokens differ in width.
 */
@Composable
private fun AccessoryToolbar(onInsert: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AccessoryRow(ACCESSORY_KEYWORDS, onInsert)
        AccessoryRow(ACCESSORY_SYMBOLS, onInsert)
    }
}

@Composable
private fun AccessoryRow(tokens: List<String>, onInsert: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tokens.forEach { token -> AccessoryButton(token) { onInsert(token) } }
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
