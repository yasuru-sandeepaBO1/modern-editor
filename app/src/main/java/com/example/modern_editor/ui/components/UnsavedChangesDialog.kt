package com.example.modern_editor.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.PrimaryText

/**
 * Shown when leaving the editor with unsaved edits.
 *
 * Three outcomes rather than two: discarding someone's work silently is the one
 * genuinely destructive thing this screen can do, so "Cancel" (stay and keep editing)
 * has to be reachable — dismissing the dialog does the same.
 */
@Composable
fun UnsavedChangesDialog(
    fileName: String,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        textContentColor = ButtonText,
        title = { Text("Unsaved changes") },
        text = { Text("Save changes to $fileName before leaving?") },
        confirmButton = {
            TextButton(
                onClick = onSave,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
                ) { Text("Cancel") }
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
                ) { Text("Discard") }
            }
        }
    )
}
