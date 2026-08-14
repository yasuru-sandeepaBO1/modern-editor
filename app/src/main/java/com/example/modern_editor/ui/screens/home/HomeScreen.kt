package com.example.modern_editor.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.extension
import com.example.modern_editor.domain.model.relativeTimeLabel
import com.example.modern_editor.ui.components.AppDrawer
import com.example.modern_editor.ui.components.FileNameDialog
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.EditorSurface
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground

@Composable
fun HomeScreen(
    onOpenFile: (fileUri: String) -> Unit,
    onCreateFile: (fileName: String, fileType: String) -> Unit,
    onOpenFilesList: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var drawerOpen by remember { mutableStateOf(false) }
    val recentFiles by InMemoryFileRepository.files.collectAsState()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onOpenFile(it.toString()) } }

    if (showNewFileDialog) {
        FileNameDialog(
            title = "New File",
            initialName = "untitled",
            confirmLabel = "Create",
            showTypeSelector = true,
            onConfirm = { name, type ->
                val finalType = type ?: FileType.PLAIN_TEXT
                val finalName = if (name.endsWith(finalType.extension)) name else name + finalType.extension
                onCreateFile(finalName, finalType.name)
                showNewFileDialog = false
            },
            onDismiss = { showNewFileDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { drawerOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PrimaryText)
                }
                Text(
                    text = "Kotlin Editor",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(EditorSurface, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "K", color = PrimaryText, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Kotlin Editor", color = PrimaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Code Kotlin, anywhere.", color = ButtonText, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeActionButton(
                    icon = Icons.Filled.Add,
                    label = "New File",
                    modifier = Modifier.weight(1f)
                ) { showNewFileDialog = true }
                HomeActionButton(
                    icon = Icons.Filled.FolderOpen,
                    label = "Open File",
                    modifier = Modifier.weight(1f)
                ) { openDocumentLauncher.launch(arrayOf("*/*")) }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "RECENT FILES", color = ButtonText, fontSize = 10.sp, letterSpacing = 1.sp)
                Text(
                    text = "VIEW ALL",
                    color = PrimaryText,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onOpenFilesList)
                )
            }
            Spacer(Modifier.height(8.dp))

            if (recentFiles.isEmpty()) {
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
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    recentFiles.take(5).forEach { file ->
                        RecentFileRow(file = file, onClick = { onOpenFile(file.filePath) })
                    }
                }
            }
        }

        AppDrawer(
            open = drawerOpen,
            onDismiss = { drawerOpen = false },
            onSettings = onOpenSettings
        )
    }
}

@Composable
private fun HomeActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ButtonSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryText)
        Text(text = label, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecentFileRow(file: EditorFile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = file.name,
                    color = PrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(text = relativeTimeLabel(file.modifiedAt), color = GutterText, fontSize = 10.sp)
            }
            Text(text = file.filePath, color = ButtonText, fontSize = 10.sp, maxLines = 1)
        }
    }
    HorizontalDivider(color = InactiveSurface)
}

private fun fileBadge(type: FileType): String = when (type) {
    FileType.KOTLIN -> "KT"
    FileType.MARKDOWN -> "MD"
    FileType.PLAIN_TEXT -> "TXT"
}
