package com.example.modern_editor.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.PrimaryText

@Composable
fun RecoveryDialog(
    fileName: String,
    onRestore: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        textContentColor = ButtonText,
        title = { Text("Crash Recovery") },
        text = {
            Text(
                buildAnnotatedString {
                    append("Unsaved changes were found for ")
                    withStyle(SpanStyle(color = PrimaryText, fontFamily = FontFamily.Monospace)) {
                        append(fileName)
                    }
                    append(". Would you like to restore them?")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onRestore,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Restore") }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonText)
            ) { Text("Discard") }
        }
    )
}
