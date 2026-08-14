package com.example.modern_editor.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.domain.model.extension
import com.example.modern_editor.ui.components.FileNameDialog

@Composable
fun HomeScreen(
    onOpenEditor: () -> Unit,
    onOpenFile: (fileUri: String) -> Unit,
    onCreateFile: (fileName: String, fileType: String) -> Unit,
    onOpenFilesList: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("Home", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenEditor) { Text("Editor") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { showNewFileDialog = true }) { Text("New File") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }) { Text("Open File") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenFilesList) { Text("Files List") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenSettings) { Text("Settings") }

        Spacer(Modifier.height(24.dp))
        Text("Recent", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        recentFiles.take(5).forEach { file ->
            TextButton(onClick = { onOpenFile(file.filePath) }) { Text(file.name) }
        }
    }
}
