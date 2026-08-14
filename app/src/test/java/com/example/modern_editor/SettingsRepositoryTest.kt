package com.example.modern_editor

import com.example.modern_editor.data.repository.InMemorySettingsRepository
import com.example.modern_editor.domain.model.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun `TC13 settings repository stores updates in memory`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(fontSize = 18, theme = AppTheme.LIGHT) }
        val settings = repo.settings.first()
        assertEquals(18, settings.fontSize)
        assertEquals(AppTheme.LIGHT, settings.theme)
        assertTrue(settings.syntaxHighlighting)
    }
}
