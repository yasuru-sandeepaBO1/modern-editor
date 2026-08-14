package com.example.modern_editor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modern_editor.EditorApplication
import com.example.modern_editor.ui.screens.editor.EditorViewModel
import com.example.modern_editor.ui.screens.fileslist.FilesListViewModel
import com.example.modern_editor.ui.screens.settings.SettingsViewModel
import com.example.modern_editor.ui.screens.versionhistory.HistoryViewModel

class AppViewModelFactory(
    private val app: EditorApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(EditorViewModel::class.java) -> EditorViewModel(app)
            modelClass.isAssignableFrom(FilesListViewModel::class.java) -> FilesListViewModel(app)
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(app)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(app)
            else -> error("Unknown ViewModel ${modelClass.name}")
        }
        return created as T
    }
}
