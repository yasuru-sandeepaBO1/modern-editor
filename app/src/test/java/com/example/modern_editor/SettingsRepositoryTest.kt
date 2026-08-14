package com.example.modern_editor

import com.example.modern_editor.data.repository.InMemorySettingsRepository
import com.example.modern_editor.domain.model.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun `TC14_1 font size update is stored`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(fontSize = 20) }
        assertEquals(20, repo.settings.first().fontSize)
    }

    @Test
    fun `TC14_3 word wrap toggle is stored`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(wordWrap = false) }
        assertFalse(repo.settings.first().wordWrap)
    }

    @Test
    fun `TC14_4 line numbers toggle is stored`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(lineNumbers = false) }
        assertFalse(repo.settings.first().lineNumbers)
    }

    @Test
    fun `TC14_5 theme change is stored`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(theme = AppTheme.SYSTEM) }
        assertEquals(AppTheme.SYSTEM, repo.settings.first().theme)
    }

    @Test
    fun `TC14_6 syntax highlighting toggle is stored`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update { it.copy(syntaxHighlighting = false) }
        assertFalse(repo.settings.first().syntaxHighlighting)
    }

    @Test
    fun `TC14_7 multiple settings persist together`() = runBlocking {
        val repo = InMemorySettingsRepository()
        repo.update {
            it.copy(
                fontSize = 12,
                tabSize = 2,
                wordWrap = false,
                lineNumbers = false,
                highlightCurrentLine = false,
                theme = AppTheme.LIGHT,
                syntaxHighlighting = false,
                autoSaveIntervalMs = 5_000L,
                readOnlyByDefault = true
            )
        }
        val settings = repo.settings.first()
        assertEquals(12, settings.fontSize)
        assertEquals(2, settings.tabSize)
        assertEquals(5_000L, settings.autoSaveIntervalMs)
        assertTrue(settings.readOnlyByDefault)
        assertEquals(AppTheme.LIGHT, settings.theme)
    }
}
