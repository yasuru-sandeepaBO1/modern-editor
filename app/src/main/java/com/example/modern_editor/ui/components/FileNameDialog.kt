package com.example.modern_editor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modern_editor.domain.model.FileType

private val TYPE_OPTIONS = listOf(
    FileType.KOTLIN to ".kt",
    FileType.MARKDOWN to ".md",
    FileType.PLAIN_TEXT to ".txt"
)

/**
 * Shared New File / Save As / Rename dialog (per rules.md: one dialog component for all
 * three) — the type selector only shows for New File.
 */
@Composable
fun FileNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    showTypeSelector: Boolean,
    initialType: FileType = FileType.PLAIN_TEXT,
    onConfirm: (name: String, type: FileType?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedType by remember { mutableStateOf(initialType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("filename") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showTypeSelector) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TYPE_OPTIONS.forEach { (type, extension) ->
                            TextButton(
                                onClick = { selectedType = type },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (selectedType == type) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) { Text(extension) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), if (showTypeSelector) selectedType else null) },
                enabled = name.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
