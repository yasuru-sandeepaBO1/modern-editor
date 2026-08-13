package com.example.modern_editor.ui.screens.editor

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.modern_editor.ui.screens.PlaceholderScreen

@Composable
fun EditorScreen(
    onOpenVersionHistory: () -> Unit,
    onBack: () -> Unit
) {
    PlaceholderScreen(title = "Editor", onBack = onBack) {
        Button(onClick = onOpenVersionHistory) { Text("Version History") }
    }
}
