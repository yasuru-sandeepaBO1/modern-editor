package com.example.modern_editor.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.PrimaryText

@Composable
fun RollbackConfirmDialog(
    versionLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        textContentColor = ButtonText,
        title = { Text("Roll back to $versionLabel?") },
        text = { Text("Any unsaved changes in the current file will be lost.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Roll Back") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
            ) { Text("Cancel") }
        }
    )
}
