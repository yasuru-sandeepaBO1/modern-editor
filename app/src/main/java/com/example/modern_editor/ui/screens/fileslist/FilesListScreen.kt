package com.example.modern_editor.ui.screens.fileslist

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.modern_editor.data.file.FileStorage
import com.example.modern_editor.data.repository.InMemoryFileRepository
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.ui.components.DeleteConfirmDialog
import com.example.modern_editor.ui.components.FileNameDialog
import kotlinx.coroutines.launch

@Composable
fun FilesListScreen(
    onOpenFile: (fileUri: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val files by InMemoryFileRepository.files.collectAsState()
    var renameTarget by remember { mutableStateOf<EditorFile?>(null) }
    var deleteTarget by remember { mutableStateOf<EditorFile?>(null) }

    renameTarget?.let { target ->
        FileNameDialog(
            title = "Rename File",
            initialName = target.name,
            confirmLabel = "Rename",
            showTypeSelector = false,
            onConfirm = { newName, _ ->
                scope.launch {
                    val uri = Uri.parse(target.filePath)
                    val renamedUri = FileStorage.renameDocument(context, uri, newName) ?: uri
                    InMemoryFileRepository.delete(target.id)
                    InMemoryFileRepository.save(
                        target.copy(
                            id = renamedUri.toString(),
                            name = newName,
                            filePath = renamedUri.toString(),
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                }
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { target ->
        DeleteConfirmDialog(
            fileName = target.name,
            onConfirm = {
                scope.launch {
                    FileStorage.deleteDocument(context, Uri.parse(target.filePath))
                    InMemoryFileRepository.delete(target.id)
                }
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.width(8.dp))
            Text("Files List", color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            files.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onOpenFile(file.filePath) },
                        modifier = Modifier.weight(1f)
                    ) { Text(file.name) }
                    TextButton(onClick = { renameTarget = file }) { Text("Rename") }
                    TextButton(onClick = { deleteTarget = file }) { Text("Delete") }
                }
            }
        }
    }
}
