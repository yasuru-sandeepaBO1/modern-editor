package com.example.modern_editor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Every Material role is mapped onto the locked 8-color palette so that any
// component we don't style by hand still lands inside docs/design_system.md.
// error/onError intentionally reuse palette colors too — the palette has no red,
// and the design system allows no color outside its table.
private val EditorDarkColorScheme = darkColorScheme(
    primary = ButtonSurface,
    onPrimary = PrimaryText,
    primaryContainer = ButtonSurface,
    onPrimaryContainer = PrimaryText,
    secondary = ButtonSurface,
    onSecondary = ButtonText,
    background = ScreenBackground,
    onBackground = PrimaryText,
    surface = EditorSurface,
    onSurface = PrimaryText,
    surfaceVariant = HeaderSurface,
    onSurfaceVariant = ButtonText,
    surfaceContainer = HeaderSurface,
    surfaceContainerHigh = HeaderSurface,
    outline = InactiveSurface,
    outlineVariant = InactiveSurface,
    error = PrimaryText,
    onError = EditorSurface,
    errorContainer = ButtonSurface,
    onErrorContainer = PrimaryText,
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
