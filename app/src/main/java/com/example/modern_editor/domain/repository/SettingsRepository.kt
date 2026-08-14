package com.example.modern_editor.domain.repository

import com.example.modern_editor.domain.model.EditorSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<EditorSettings>
    suspend fun update(transform: (EditorSettings) -> EditorSettings)
}
