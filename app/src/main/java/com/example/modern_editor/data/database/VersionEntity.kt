package com.example.modern_editor.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.modern_editor.domain.model.Version
import com.example.modern_editor.domain.model.VersionType

@Entity(
    tableName = "versions",
    foreignKeys = [
        ForeignKey(
            entity = FileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fileId")]
)
data class VersionEntity(
    @PrimaryKey val id: String,
    val fileId: String,
    val versionNumber: Int,
    val type: String,
    val timestamp: Long,
    val label: String?
)

fun VersionEntity.toModel(): Version = Version(
    id = id,
    fileId = fileId,
    type = runCatching { VersionType.valueOf(type) }.getOrDefault(VersionType.DELTA),
    timestamp = timestamp,
    label = label,
    versionNumber = versionNumber
)

fun Version.toEntity(): VersionEntity = VersionEntity(
    id = id,
    fileId = fileId,
    versionNumber = versionNumber,
    type = type.name,
    timestamp = timestamp,
    label = label
)
