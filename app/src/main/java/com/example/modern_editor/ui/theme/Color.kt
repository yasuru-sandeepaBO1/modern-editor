package com.example.modern_editor.ui.theme

import androidx.compose.ui.graphics.Color

// Design system: docs/design_system.md — the locked 8-color palette.
// Nothing outside this file's values may appear anywhere in the app.
// There is no accent color in this project: #475163 doubles as the surface for
// primary actions and active states.

/** Screen / status bar background. */
val ScreenBackground = Color(0xFF31323F)

/** Header / top bar / toolbar background. */
val HeaderSurface = Color(0xFF353B47)

/** Editor content surface / gutter background / active tab. */
val EditorSurface = Color(0xFF1E2430)

/** Line-number / gutter text. */
val GutterText = Color(0xFF4C556B)

/** Inactive tab background — also the separator/divider tone. */
val InactiveSurface = Color(0xFF272B36)

/** Button surface / primary actions / active states. */
val ButtonSurface = Color(0xFF475163)

/** Button text / secondary UI text. */
val ButtonText = Color(0xFFC4C8D1)

/** Primary/emphasized text (titles, filenames, headings). */
val PrimaryText = Color(0xFFFFFFFF)
