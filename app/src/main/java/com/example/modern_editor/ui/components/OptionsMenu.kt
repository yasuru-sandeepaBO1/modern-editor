package com.example.modern_editor.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText

/** Compact popup anchored top-right, opened by the Editor's overflow (three-dot) icon.
 * Confirmed optional features live here: Full Screen, Hide Toolbar, Share, and Go to Line.
 */
@Composable
fun OptionsMenu(
    open: Boolean,
    onDismiss: () -> Unit,
    wordWrap: Boolean,
    onToggleWordWrap: () -> Unit,
    fullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    readOnly: Boolean,
    onToggleReadOnly: () -> Unit,
    toolbarHidden: Boolean,
    onToggleHideToolbar: () -> Unit,
    onGoToLine: (Int) -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onVersionHistory: () -> Unit,
    onSettings: () -> Unit,
    showMarkdownPreview: Boolean = false,
    onMarkdownPreview: () -> Unit = {}
) {
    var lineInput by remember { mutableStateOf("") }

    BackHandler(enabled = open, onBack = onDismiss)

    // contentAlignment = TopEnd anchors the panel to the right edge; without it the
    // Box would place it top-start and the panel would render on the left even though
    // it animates in from the right.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss)
            )
        }

        // A popup anchored under the overflow icon rather than a full-height drawer: it
        // takes only the width it needs and only as much height as its content, so the
        // document stays visible behind it.
        AnimatedVisibility(
            visible = open,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 6 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 6 })
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 4.dp, end = 8.dp, bottom = 8.dp)
                    .width(250.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(HeaderSurface)
                    // Wraps its content, but can still scroll on a short screen.
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                ToggleMenuRow(Icons.AutoMirrored.Filled.WrapText, "Word Wrap", wordWrap, onToggleWordWrap)
                ToggleMenuRow(Icons.Filled.Fullscreen, "Full Screen", fullScreen, onToggleFullScreen)
                ToggleMenuRow(Icons.Filled.HorizontalRule, "Hide Toolbar", toolbarHidden, onToggleHideToolbar)
                ToggleMenuRow(Icons.Filled.Lock, "Read Only", readOnly, onToggleReadOnly)
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onUndo, enabled = canUndo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) PrimaryText else GutterText
                        )
                    }
                    IconButton(onClick = onRedo, enabled = canRedo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) PrimaryText else GutterText
                        )
                    }
                }
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))

                // imeAction must be set explicitly: a Number keyboard's default action is
                // not Done, so KeyboardActions.onDone would never fire and the jump would
                // silently do nothing. Dismiss after jumping so the cursor is visible.
                OutlinedTextField(
                    value = lineInput,
                    onValueChange = { lineInput = it.filter(Char::isDigit) },
                    placeholder = { Text("Go to line...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = {
                        lineInput.toIntOrNull()?.let {
                            onGoToLine(it)
                            lineInput = ""
                            onDismiss()
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ButtonSurface,
                        unfocusedBorderColor = InactiveSurface,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedPlaceholderColor = ButtonText,
                        unfocusedPlaceholderColor = ButtonText,
                        cursorColor = PrimaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))

                MenuRow(icon = Icons.Filled.Share, label = "Share") { onDismiss(); onShare() }
                if (showMarkdownPreview) {
                    MenuRow(icon = Icons.Filled.Description, label = "Preview") { onDismiss(); onMarkdownPreview() }
                }
                MenuRow(icon = Icons.Filled.DriveFileRenameOutline, label = "Rename") { onDismiss(); onRename() }
                MenuRow(icon = Icons.Filled.History, label = "Version History") { onDismiss(); onVersionHistory() }
                MenuRow(icon = Icons.Filled.Settings, label = "Settings") { onDismiss(); onSettings() }
            }
        }
    }
}

@Composable
private fun ToggleMenuRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) ButtonSurface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (checked) PrimaryText else ButtonText)
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = if (checked) PrimaryText else ButtonText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (checked) "ON" else "OFF",
            color = if (checked) PrimaryText else GutterText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
