package com.example.modern_editor.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modern_editor.EditorApplication
import com.example.modern_editor.domain.model.EditorSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as EditorApplication).settingsRepository

    val settings = repo.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EditorSettings()
    )

    fun update(transform: (EditorSettings) -> EditorSettings) {
        viewModelScope.launch { repo.update(transform) }
    }
}
