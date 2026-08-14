package com.example.modern_editor.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DeltaDao {
    @Query("SELECT * FROM deltas WHERE versionId = :versionId LIMIT 1")
    suspend fun getForVersion(versionId: String): DeltaEntity?

    @Upsert
    suspend fun upsert(entity: DeltaEntity)

    @Query("DELETE FROM deltas WHERE versionId IN (SELECT id FROM versions WHERE fileId = :fileId)")
    suspend fun deleteForFile(fileId: String)
}
