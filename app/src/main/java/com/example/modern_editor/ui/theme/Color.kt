package com.example.modern_editor.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Design system: docs/design_system.md — the locked 8-color palette.
// Nothing outside this file's values may appear anywhere in the app.
// There is no accent color in this project: #475163 doubles as the surface for
// primary actions and active states.

data class EditorPalette(
    val screenBackground: Color,
    val headerSurface: Color,
    val editorSurface: Color,
    val gutterText: Color,
    val inactiveSurface: Color,
    val buttonSurface: Color,
    val buttonText: Color,
    val primaryText: Color,
)

val DarkPalette = EditorPalette(
    screenBackground = Color(0xFF31323F),
    headerSurface = Color(0xFF353B47),
    editorSurface = Color(0xFF1E2430),
    gutterText = Color(0xFF4C556B),
    inactiveSurface = Color(0xFF272B36),
    buttonSurface = Color(0xFF475163),
    buttonText = Color(0xFFC4C8D1),
    primaryText = Color(0xFFFFFFFF),
)

val LocalEditorPalette = staticCompositionLocalOf { DarkPalette }

/** Screen / status bar background. */
val ScreenBackground: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.screenBackground

/** Header / top bar / toolbar background. */
val HeaderSurface: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.headerSurface

/** Editor content surface / gutter background / active tab. */
val EditorSurface: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.editorSurface

/** Line-number / gutter text. */
val GutterText: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.gutterText

/** Inactive tab background — also the separator/divider tone. */
val InactiveSurface: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.inactiveSurface

/** Button surface / primary actions / active states. */
val ButtonSurface: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.buttonSurface

/** Button text / secondary UI text. */
val ButtonText: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.buttonText

/** Primary/emphasized text (titles, filenames, headings). */
val PrimaryText: Color
    @Composable @ReadOnlyComposable get() = LocalEditorPalette.current.primaryText
