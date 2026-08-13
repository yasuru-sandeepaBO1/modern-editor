package com.example.modern_editor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EditorDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = PrimaryText,
    secondary = ToolbarButtonSurface,
    onSecondary = ToolbarButtonText,
    background = ScreenBackground,
    onBackground = PrimaryText,
    surface = EditorSurface,
    onSurface = PrimaryText,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = SecondaryText,
    outline = BorderDivider,
    outlineVariant = GutterBorder,
)

@Composable
fun ModerneditorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EditorDarkColorScheme,
        typography = Typography,
        content = content
    )
}
