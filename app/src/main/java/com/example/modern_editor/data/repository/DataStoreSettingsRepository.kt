package com.example.modern_editor.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.modern_editor.domain.model.EditorSettings
import com.example.modern_editor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "editor_settings")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.settingsDataStore

    override val settings: Flow<EditorSettings> = dataStore.data.map { prefs ->
        EditorSettings(
            fontSize = prefs[FONT_SIZE] ?: 14,
            tabSize = prefs[TAB_SIZE] ?: 4,
            wordWrap = prefs[WORD_WRAP] ?: true,
            lineNumbers = prefs[LINE_NUMBERS] ?: true,
            highlightCurrentLine = prefs[HIGHLIGHT_LINE] ?: true,
            syntaxHighlighting = prefs[SYNTAX] ?: true,
            autoSaveIntervalMs = prefs[INTERVAL] ?: 10_000L,
            readOnlyByDefault = prefs[READ_ONLY] ?: false
        )
    }

    override suspend fun update(transform: (EditorSettings) -> EditorSettings) {
        dataStore.edit { prefs ->
            val current = EditorSettings(
                fontSize = prefs[FONT_SIZE] ?: 14,
                tabSize = prefs[TAB_SIZE] ?: 4,
                wordWrap = prefs[WORD_WRAP] ?: true,
                lineNumbers = prefs[LINE_NUMBERS] ?: true,
                highlightCurrentLine = prefs[HIGHLIGHT_LINE] ?: true,
                syntaxHighlighting = prefs[SYNTAX] ?: true,
                autoSaveIntervalMs = prefs[INTERVAL] ?: 10_000L,
                readOnlyByDefault = prefs[READ_ONLY] ?: false
            )
            val next = transform(current)
            prefs[FONT_SIZE] = next.fontSize
            prefs[TAB_SIZE] = next.tabSize
            prefs[WORD_WRAP] = next.wordWrap
            prefs[LINE_NUMBERS] = next.lineNumbers
            prefs[HIGHLIGHT_LINE] = next.highlightCurrentLine
            prefs[SYNTAX] = next.syntaxHighlighting
            prefs[INTERVAL] = next.autoSaveIntervalMs
            prefs[READ_ONLY] = next.readOnlyByDefault
        }
    }

    private companion object {
        val FONT_SIZE = intPreferencesKey("font_size")
        val TAB_SIZE = intPreferencesKey("tab_size")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val LINE_NUMBERS = booleanPreferencesKey("line_numbers")
        val HIGHLIGHT_LINE = booleanPreferencesKey("highlight_line")
        val SYNTAX = booleanPreferencesKey("syntax")
        val INTERVAL = longPreferencesKey("interval")
        val READ_ONLY = booleanPreferencesKey("read_only")
    }
}
