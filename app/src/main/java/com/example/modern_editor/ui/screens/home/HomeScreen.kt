package com.example.modern_editor.ui.screens.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modern_editor.ui.screens.PlaceholderScreen

@Composable
fun HomeScreen(
    onOpenEditor: () -> Unit,
    onOpenFilesList: () -> Unit,
    onOpenSettings: () -> Unit
) {
    PlaceholderScreen(title = "Home") {
        Button(onClick = onOpenEditor) { Text("Editor") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenFilesList) { Text("Files List") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenSettings) { Text("Settings") }
    }
}
