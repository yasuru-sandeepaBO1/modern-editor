package com.example.modern_editor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared scaffold for Component 0's placeholder screens.
 * Real per-screen UI arrives in Component 4 — this only proves navigation works.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(24.dp))
            actions()
            if (onBack != null) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}
