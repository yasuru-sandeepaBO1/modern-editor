package com.example.modern_editor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FileEntity::class, VersionEntity::class, DeltaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun versionDao(): VersionDao
    abstract fun deltaDao(): DeltaDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "editor.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
