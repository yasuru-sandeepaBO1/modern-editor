package com.example.modern_editor

import android.app.Application
import android.content.Context
import com.example.modern_editor.data.database.AppDatabase
import com.example.modern_editor.data.repository.InMemorySettingsRepository
import com.example.modern_editor.data.repository.RoomFileRepository
import com.example.modern_editor.data.repository.RoomVersionRepository
import com.example.modern_editor.domain.repository.FileRepository
import com.example.modern_editor.domain.repository.SettingsRepository
import com.example.modern_editor.domain.repository.VersionRepository

class EditorApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }

    val versionRepository: VersionRepository by lazy {
        RoomVersionRepository(database.versionDao(), database.deltaDao())
    }

    val fileRepository: FileRepository by lazy {
        RoomFileRepository(database.fileDao(), versionRepository)
    }

    val settingsRepository: SettingsRepository by lazy { InMemorySettingsRepository() }
}

val Context.editorApp: EditorApplication
    get() = applicationContext as EditorApplication
