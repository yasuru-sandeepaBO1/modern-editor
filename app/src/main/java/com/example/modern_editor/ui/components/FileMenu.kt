package com.example.modern_editor.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface

/** Slides in from the LEFT edge — opened by the Editor's hamburger icon. */
@Composable
fun FileMenu(
    open: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onNewFile: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onRename: () -> Unit,
    onSettings: () -> Unit
) {
    BackHandler(enabled = open, onBack = onDismiss)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(HeaderSurface)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                MenuRow(icon = Icons.Filled.FileOpen, label = "Open") { onDismiss(); onOpen() }
                MenuRow(icon = Icons.Filled.PostAdd, label = "New File") { onDismiss(); onNewFile() }
                // Present for design consistency only — folder browsing is out of scope.
                MenuRow(icon = Icons.Filled.Folder, label = "Open Folder", enabled = false) {}
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))
                MenuRow(icon = Icons.Filled.Save, label = "Save") { onDismiss(); onSave() }
                MenuRow(icon = Icons.Filled.SaveAs, label = "Save As") { onDismiss(); onSaveAs() }
                MenuRow(icon = Icons.Filled.DriveFileRenameOutline, label = "Rename") { onDismiss(); onRename() }
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))
                MenuRow(icon = Icons.Filled.Settings, label = "Settings") { onDismiss(); onSettings() }
            }
        }
    }
}
