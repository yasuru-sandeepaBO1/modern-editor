package com.example.modern_editor.ui.screens.versionhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType
import com.example.modern_editor.domain.model.relativeTimeLabel
import com.example.modern_editor.editorApp
import com.example.modern_editor.ui.components.RollbackConfirmDialog
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import com.example.modern_editor.version.DiffSession
import com.example.modern_editor.version.RollbackManager
import com.example.modern_editor.version.VersionManager
import com.example.modern_editor.version.VersionSession
import kotlinx.coroutines.launch

@Composable
fun VersionHistoryScreen(
    fileId: String? = null,
    onOpenDiffCompare: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolvedId = fileId?.takeIf { it.isNotBlank() } ?: VersionSession.fileId
    val repo = context.editorApp.versionRepository
    var versions by remember { mutableStateOf<List<Version>>(emptyList()) }
    var showLabelDialog by remember { mutableStateOf(false) }
    var labelInput by remember { mutableStateOf("") }
    var rollbackTarget by remember { mutableStateOf<Version?>(null) }

    fun refresh() {
        scope.launch { versions = repo.getVersionsForFile(resolvedId).sortedByDescending { it.versionNumber } }
    }

    LaunchedEffect(resolvedId) { refresh() }

    if (showLabelDialog) {
        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            containerColor = HeaderSurface,
            titleContentColor = PrimaryText,
            textContentColor = ButtonText,
            title = { Text("Save Version") },
            text = {
                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it },
                    placeholder = { Text("Optional label") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ButtonSurface,
                        unfocusedBorderColor = InactiveSurface,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedPlaceholderColor = ButtonText,
                        unfocusedPlaceholderColor = ButtonText,
                        cursorColor = PrimaryText
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLabelDialog = false
                        scope.launch {
                            VersionManager(repo).createVersion(
                                fileId = resolvedId,
                                content = VersionSession.currentContent,
                                label = labelInput.trim().ifEmpty { null }
                            )
                            labelInput = ""
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLabelDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
                ) { Text("Cancel") }
            }
        )
    }

    rollbackTarget?.let { target ->
        RollbackConfirmDialog(
            versionLabel = "v${target.versionNumber}",
            onConfirm = {
                rollbackTarget = null
                scope.launch {
                    val content = RollbackManager(repo).contentAt(resolvedId, target.id)
                    VersionSession.pendingRollbackContent.value = content
                    VersionSession.currentContent = content
                    onBack()
                }
            },
            onDismiss = { rollbackTarget = null }
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
                text = "${VersionSession.fileName.ifBlank { "File" }} — Version History",
                color = PrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            IconButton(onClick = { showLabelDialog = true }) {
                Icon(Icons.Filled.Save, contentDescription = "Save Version", tint = PrimaryText)
            }
        }

        if (versions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No versions yet. Tap save to create one.", color = ButtonText, fontSize = 14.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                versions.forEach { version ->
                    VersionCard(
                        version = version,
                        onDiff = {
                            scope.launch {
                                val selected = RollbackManager(repo).contentAt(resolvedId, version.id)
                                DiffSession.oldText = selected
                                DiffSession.newText = VersionSession.currentContent
                                DiffSession.fromLabel = "v${version.versionNumber}"
                                DiffSession.toLabel = "current"
                                DiffSession.fileName = VersionSession.fileName
                                DiffSession.fromVersionId = version.id
                                DiffSession.toVersionId = "current"
                                DiffSession.rollbackFileId = resolvedId
                                DiffSession.rollbackVersionId = version.id
                                onOpenDiffCompare()
                            }
                        },
                        onRestore = { rollbackTarget = version }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun VersionCard(
    version: Version,
    onDiff: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HeaderSurface)
            .border(1.dp, InactiveSurface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (version.type == VersionType.SNAPSHOT) PrimaryText else ButtonSurface)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "v${version.versionNumber}",
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = version.type.name,
                color = ButtonText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(InactiveSurface, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(text = relativeTimeLabel(version.timestamp), color = GutterText, fontSize = 12.sp)
        }
        if (!version.label.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(text = version.label, color = ButtonText, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = InactiveSurface)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onDiff,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) {
                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("View Diff")
            }
            TextButton(
                onClick = onRestore,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) {
                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Restore")
            }
        }
    }
}
