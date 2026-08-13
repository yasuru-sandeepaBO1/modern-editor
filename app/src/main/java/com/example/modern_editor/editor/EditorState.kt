package com.example.modern_editor.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberEditorState(): TextFieldState =
    rememberSaveable(saver = TextFieldState.Saver) { TextFieldState() }
