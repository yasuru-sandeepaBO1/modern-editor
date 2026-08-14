package com.example.modern_editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.modern_editor.domain.model.EditorSettings
import com.example.modern_editor.recovery.RecoveryHolder
import com.example.modern_editor.ui.navigation.EditorNavGraph
import com.example.modern_editor.ui.theme.ModerneditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecoveryHolder.init(this)
        enableEdgeToEdge()
        setContent {
            val settings by editorApp.settingsRepository.settings.collectAsState(EditorSettings())
            ModerneditorTheme(theme = settings.theme) {
                EditorNavGraph()
            }
        }
    }
}
