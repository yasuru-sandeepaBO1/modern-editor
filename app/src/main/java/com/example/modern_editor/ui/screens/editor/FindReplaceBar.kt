package com.example.modern_editor.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.editor.findMatches
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText

@Composable
fun FindReplaceBar(
    editorState: TextFieldState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }

    BackHandler(onBack = onClose)

    val text = editorState.text.toString()
    val matches = remember(text, query) { findMatches(text, query) }

    fun selectMatch(index: Int) {
        if (matches.isEmpty()) return
        val safeIndex = ((index % matches.size) + matches.size) % matches.size
        currentIndex = safeIndex
        val range = matches[safeIndex]
        editorState.edit { selection = TextRange(range.first, range.last + 1) }
    }

    LaunchedEffect(matches) {
        currentIndex = 0
        if (matches.isNotEmpty()) selectMatch(0)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ButtonSurface,
        unfocusedBorderColor = InactiveSurface,
        focusedTextColor = PrimaryText,
        unfocusedTextColor = PrimaryText,
        focusedPlaceholderColor = ButtonText,
        unfocusedPlaceholderColor = ButtonText,
        cursorColor = PrimaryText
    )

    Row(
        modifier = modifier
            .padding(8.dp)
            .background(HeaderSurface, RoundedCornerShape(12.dp))
            .border(1.dp, InactiveSurface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = ButtonText)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Find") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when {
                        query.isEmpty() -> ""
                        matches.isEmpty() -> "No matches"
                        else -> "${currentIndex + 1}/${matches.size}"
                    },
                    color = ButtonText,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = replacement,
                    onValueChange = { replacement = it },
                    placeholder = { Text("Replace") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        val range = matches[currentIndex]
                        editorState.edit { replace(range.first, range.last + 1, replacement) }
                    },
                    enabled = matches.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryText,
                        disabledContentColor = GutterText
                    )
                ) { Text("Replace", fontSize = 12.sp) }
                TextButton(
                    onClick = {
                        editorState.edit {
                            val newText = asCharSequence().toString().replace(query, replacement, ignoreCase = true)
                            replace(0, length, newText)
                        }
                    },
                    enabled = matches.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryText,
                        disabledContentColor = GutterText
                    )
                ) { Text("Replace All", fontSize = 12.sp) }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { selectMatch(currentIndex - 1) }, enabled = matches.isNotEmpty()) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = "Previous",
                        tint = if (matches.isNotEmpty()) PrimaryText else GutterText
                    )
                }
                IconButton(onClick = { selectMatch(currentIndex + 1) }, enabled = matches.isNotEmpty()) {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = "Next",
                        tint = if (matches.isNotEmpty()) PrimaryText else GutterText
                    )
                }
            }
        }
    }
}
