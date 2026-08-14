package com.example.modern_editor.ui.screens.versionhistory

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.modern_editor.ui.screens.PlaceholderScreen

@Composable
fun VersionHistoryScreen(
    fileId: String? = null,
    onOpenDiffCompare: () -> Unit,
    onBack: () -> Unit
) {
    PlaceholderScreen(title = "Version History", onBack = onBack) {
        Button(onClick = onOpenDiffCompare) { Text("Diff Comparison") }
    }
}
