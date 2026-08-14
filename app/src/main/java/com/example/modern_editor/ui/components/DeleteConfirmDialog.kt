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
fun DeleteConfirmDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        textContentColor = ButtonText,
        title = { Text("Delete file") },
        text = { Text("Delete $fileName? This cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
            ) { Text("Cancel") }
        }
    )
}
