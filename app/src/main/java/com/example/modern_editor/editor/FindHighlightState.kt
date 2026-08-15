package com.example.modern_editor.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FindHighlightState {
    var matches: List<IntRange> by mutableStateOf(emptyList())
    var currentIndex: Int by mutableIntStateOf(0)
}
