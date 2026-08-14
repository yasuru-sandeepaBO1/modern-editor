package com.example.modern_editor.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY modifiedAt DESC")
    fun observeAll(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY modifiedAt DESC")
    suspend fun getAll(): List<FileEntity>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getById(id: String): FileEntity?

    @Upsert
    suspend fun upsert(entity: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteById(id: String)
}
