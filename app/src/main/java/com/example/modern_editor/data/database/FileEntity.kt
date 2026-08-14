package com.example.modern_editor.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.modern_editor.domain.model.EditorFile
import com.example.modern_editor.domain.model.FileType

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val language: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val readOnly: Boolean
)

fun FileEntity.toModel(): EditorFile = EditorFile(
    id = id,
    name = name,
    content = "",
    filePath = path,
    fileType = runCatching { FileType.valueOf(language) }.getOrDefault(FileType.PLAIN_TEXT),
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    readOnly = readOnly
)

fun EditorFile.toEntity(): FileEntity = FileEntity(
    id = id,
    name = name,
    path = filePath,
    language = fileType.name,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    readOnly = readOnly
)
