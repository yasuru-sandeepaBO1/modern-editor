package com.example.modern_editor.ui.screens.fileslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modern_editor.editorApp
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.relativeTimeLabel
import com.example.modern_editor.ui.AppViewModelFactory
import com.example.modern_editor.ui.components.DeleteConfirmDialog
import com.example.modern_editor.ui.components.FileNameDialog
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import kotlinx.coroutines.launch

@Composable
fun FilesListScreen(
    onOpenFile: (fileUri: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: FilesListViewModel = viewModel(factory = AppViewModelFactory(context.editorApp))
    val files by viewModel.files.collectAsState(initial = emptyList())
    var renameTarget by remember { mutableStateOf<EditorFile?>(null) }
    var deleteTarget by remember { mutableStateOf<EditorFile?>(null) }

    renameTarget?.let { target ->
        FileNameDialog(
            title = "Rename File",
            initialName = target.name,
            confirmLabel = "Rename",
            showTypeSelector = false,
            onConfirm = { newName, _ ->
                scope.launch { viewModel.rename(target, newName) }
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { target ->
        DeleteConfirmDialog(
            fileName = target.name,
            onConfirm = {
                scope.launch { viewModel.delete(target) }
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
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
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text(
                text = "Recent Files",
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (files.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = GutterText)
                Spacer(Modifier.height(8.dp))
                Text(text = "No recent files yet", color = ButtonText, fontSize = 13.sp)
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                files.forEach { file ->
                    FileRow(
                        file = file,
                        onOpen = { onOpenFile(file.filePath) },
                        onRename = { renameTarget = file },
                        onDelete = { deleteTarget = file }
                    )
                    HorizontalDivider(color = InactiveSurface)
                }
            }
        }
    }
}

@Composable
private fun FileRow(
    file: EditorFile,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ButtonSurface, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fileBadge(file.fileType),
                color = PrimaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = PrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(text = file.filePath, color = ButtonText, fontSize = 10.sp, maxLines = 1)
            Text(text = relativeTimeLabel(file.modifiedAt), color = GutterText, fontSize = 10.sp)
        }
        IconButton(onClick = onRename) {
            Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Rename", tint = ButtonText)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ButtonText)
        }
    }
}

private fun fileBadge(type: FileType): String = when (type) {
    FileType.KOTLIN -> "KT"
    FileType.MARKDOWN -> "MD"
    FileType.PLAIN_TEXT -> "TXT"
}
