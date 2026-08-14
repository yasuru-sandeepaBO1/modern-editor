package com.example.modern_editor.data.repository

import com.example.modern_editor.domain.model.EditorSettings
import com.example.modern_editor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(EditorSettings())
    override val settings: StateFlow<EditorSettings> = _settings

    override suspend fun update(transform: (EditorSettings) -> EditorSettings) {
        _settings.update(transform)
    }
}
