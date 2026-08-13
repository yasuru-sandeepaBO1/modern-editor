package com.example.modern_editor.ui.screens.loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.modern_editor.ui.screens.PlaceholderScreen
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onLoaded: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(800)
        onLoaded()
    }
    PlaceholderScreen(title = "Loading")
}
