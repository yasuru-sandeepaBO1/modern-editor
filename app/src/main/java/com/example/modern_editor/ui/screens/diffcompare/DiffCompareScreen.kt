package com.example.modern_editor.ui.screens.diffcompare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.domain.model.DiffLine
import com.example.modern_editor.domain.model.LineChangeType
import com.example.modern_editor.editorApp
import com.example.modern_editor.ui.components.RollbackConfirmDialog
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import com.example.modern_editor.version.DiffEngine
import com.example.modern_editor.version.DiffSession
import com.example.modern_editor.version.RollbackManager
import com.example.modern_editor.version.VersionSession
import kotlinx.coroutines.launch

@Composable
fun DiffCompareScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val result = remember(DiffSession.oldText, DiffSession.newText, DiffSession.fromLabel, DiffSession.toLabel) {
        DiffEngine.compare(
            DiffSession.oldText,
            DiffSession.newText,
            DiffSession.fromVersionId,
            DiffSession.toVersionId
        )
    }
    val fromLabel = DiffSession.fromLabel
    val toLabel = DiffSession.toLabel
    val fileName = DiffSession.fileName
    var showRollbackConfirm by remember { mutableStateOf(false) }

    if (showRollbackConfirm) {
        RollbackConfirmDialog(
            versionLabel = fromLabel,
            onConfirm = {
                showRollbackConfirm = false
                scope.launch {
                    val fileId = DiffSession.rollbackFileId
                    val versionId = DiffSession.rollbackVersionId
                    if (fileId.isNotBlank() && versionId.isNotBlank()) {
                        val content = RollbackManager(context.editorApp.versionRepository)
                            .contentAt(fileId, versionId)
                        VersionSession.pendingRollbackContent.value = content
                        VersionSession.currentContent = content
                    }
                    onBack()
                }
            },
            onDismiss = { showRollbackConfirm = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderSurface)
                .statusBarsPadding()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Comparing $fromLabel → $toLabel",
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (fileName.isNotBlank()) {
                    Text(text = fileName, color = ButtonText, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.width(48.dp))
        }

        if (!result.hasChanges) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No differences between $fromLabel and $toLabel",
                    color = ButtonText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                result.lines.forEach { line -> DiffRow(line) }
            }
        }

        if (DiffSession.rollbackVersionId.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBackground)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ButtonSurface)
                        .clickable { showRollbackConfirm = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.History, contentDescription = null, tint = PrimaryText)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Rollback to $fromLabel",
                        color = PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffRow(line: DiffLine) {
    val background = when (line.type) {
        LineChangeType.ADDED -> ButtonSurface
        LineChangeType.REMOVED -> InactiveSurface
        LineChangeType.UNCHANGED -> ScreenBackground
    }
    val marker = when (line.type) {
        LineChangeType.ADDED -> "+"
        LineChangeType.REMOVED -> "−"
        LineChangeType.UNCHANGED -> ""
    }
    val markerColor = when (line.type) {
        LineChangeType.ADDED -> PrimaryText
        LineChangeType.REMOVED -> ButtonText
        LineChangeType.UNCHANGED -> GutterText
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = line.oldLineNumber?.toString() ?: "−",
            color = GutterText,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp).padding(end = 6.dp)
        )
        Text(
            text = line.newLineNumber?.toString() ?: "−",
            color = GutterText,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp).padding(end = 6.dp)
        )
        Text(
            text = marker,
            color = markerColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = line.text,
            color = PrimaryText,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(start = 4.dp)
        )
    }
}
