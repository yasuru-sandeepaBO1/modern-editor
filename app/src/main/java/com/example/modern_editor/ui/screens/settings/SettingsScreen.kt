package com.example.modern_editor.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modern_editor.domain.model.EditorSettings
import com.example.modern_editor.editorApp
import com.example.modern_editor.ui.AppViewModelFactory
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(context.editorApp))
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderSurface)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text("Settings", color = PrimaryText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection("Editor Preferences") {
                FontSizeRow(settings.fontSize) { viewModel.update { s -> s.copy(fontSize = it) } }
                DropdownRow("Tab Size", "${settings.tabSize} spaces", listOf(2, 4, 8).map { "$it spaces" }) { label ->
                    viewModel.update { s -> s.copy(tabSize = label.substringBefore(' ').toInt()) }
                }
                ToggleRow("Word Wrap", settings.wordWrap) { viewModel.update { s -> s.copy(wordWrap = !s.wordWrap) } }
                ToggleRow("Line Numbers", settings.lineNumbers) { viewModel.update { s -> s.copy(lineNumbers = !s.lineNumbers) } }
                ToggleRow("Highlight Current Line", settings.highlightCurrentLine) {
                    viewModel.update { s -> s.copy(highlightCurrentLine = !s.highlightCurrentLine) }
                }
            }
            SettingsSection("Appearance") {
                ToggleRow("Syntax Highlighting", settings.syntaxHighlighting) {
                    viewModel.update { s -> s.copy(syntaxHighlighting = !s.syntaxHighlighting) }
                }
            }
            SettingsSection("System & Recovery") {
                val labels = listOf(5_000L to "5s", 10_000L to "10s", 30_000L to "30s", 60_000L to "1m")
                val current = labels.firstOrNull { it.first == settings.autoSaveIntervalMs }?.second ?: "10s"
                DropdownRow("Auto-save Interval", current, labels.map { it.second }) { label ->
                    val ms = labels.first { it.second == label }.first
                    viewModel.update { s -> s.copy(autoSaveIntervalMs = ms) }
                }
                ToggleRow("Read-only by Default", settings.readOnlyByDefault) {
                    viewModel.update { s -> s.copy(readOnlyByDefault = !s.readOnlyByDefault) }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), color = GutterText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HeaderSurface)
        ) { content() }
    }
}

@Composable
private fun FontSizeRow(value: Int, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Font Size", color = PrimaryText, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.toInt().coerceIn(10, 24)) },
                valueRange = 10f..24f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryText,
                    activeTrackColor = ButtonText,
                    inactiveTrackColor = InactiveSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value}px",
                color = PrimaryText,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(InactiveSurface, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = PrimaryText, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryText,
                checkedTrackColor = ButtonSurface,
                uncheckedThumbColor = ButtonText,
                uncheckedTrackColor = InactiveSurface
            )
        )
    }
}

@Composable
private fun DropdownRow(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = PrimaryText, fontSize = 14.sp)
        Box {
            Text(
                text = value,
                color = PrimaryText,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(InactiveSurface)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
