package com.example.modern_editor.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface VersionDao {
    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY versionNumber ASC")
    suspend fun getForFile(fileId: String): List<VersionEntity>

    @Upsert
    suspend fun upsert(entity: VersionEntity)

    @Query("DELETE FROM versions WHERE fileId = :fileId")
    suspend fun deleteForFile(fileId: String)
}
