package com.example.modern_editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.domain.model.FileType
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText

private val TYPE_OPTIONS = listOf(
    Triple(FileType.KOTLIN, ".kt", "Kotlin"),
    Triple(FileType.MARKDOWN, ".md", "Markdown"),
    Triple(FileType.PLAIN_TEXT, ".txt", "Text")
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
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        textContentColor = PrimaryText,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("filename") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ButtonSurface,
                        unfocusedBorderColor = InactiveSurface,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedPlaceholderColor = ButtonText,
                        unfocusedPlaceholderColor = ButtonText,
                        cursorColor = PrimaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showTypeSelector) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TYPE_OPTIONS.forEach { (type, extension, label) ->
                            val selected = selectedType == type
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) ButtonSurface else Color.Transparent)
                                    .border(1.dp, InactiveSurface, RoundedCornerShape(8.dp))
                                    .clickable { selectedType = type }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = extension, color = if (selected) PrimaryText else ButtonText)
                                Text(
                                    text = label,
                                    color = if (selected) PrimaryText else ButtonText,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), if (showTypeSelector) selectedType else null) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = PrimaryText,
                    disabledContentColor = ButtonText
                )
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
            ) { Text("Cancel") }
        }
    )
}
