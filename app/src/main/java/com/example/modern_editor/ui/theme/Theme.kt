package com.example.modern_editor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle

// Every Material role is mapped onto the locked 8-color palette so that any
// component we don't style by hand still lands inside docs/design_system.md.
// error/onError intentionally reuse palette colors too — the palette has no red,
// and the design system allows no color outside its table.
private fun schemeOf(palette: EditorPalette) = darkColorScheme(
    primary = palette.buttonSurface,
    onPrimary = palette.primaryText,
    primaryContainer = palette.buttonSurface,
    onPrimaryContainer = palette.primaryText,
    secondary = palette.buttonSurface,
    onSecondary = palette.buttonText,
    background = palette.screenBackground,
    onBackground = palette.primaryText,
    surface = palette.editorSurface,
    onSurface = palette.primaryText,
    surfaceVariant = palette.headerSurface,
    onSurfaceVariant = palette.buttonText,
    surfaceContainer = palette.headerSurface,
    surfaceContainerHigh = palette.headerSurface,
    outline = palette.inactiveSurface,
    outlineVariant = palette.inactiveSurface,
    error = palette.primaryText,
    onError = palette.editorSurface,
    errorContainer = palette.buttonSurface,
    onErrorContainer = palette.primaryText,
)

@Composable
fun ModerneditorTheme(content: @Composable () -> Unit) {
    val palette = DarkPalette
    CompositionLocalProvider(LocalEditorPalette provides palette) {
        MaterialTheme(
            colorScheme = schemeOf(palette),
            typography = Typography,
            content = {
                CompositionLocalProvider(
                    LocalTextStyle provides TextStyle(fontFamily = InterFontFamily)
                ) {
                    content()
                }
            }
        )
    }
}
