package com.example.modern_editor.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.modern_editor.domain.model.Patch

@Entity(
    tableName = "deltas",
    foreignKeys = [
        ForeignKey(
            entity = VersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["versionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("versionId")]
)
data class DeltaEntity(
    @PrimaryKey val id: String,
    val versionId: String,
    val previousVersionId: String?,
    val patch: String,
    val timestamp: Long
)

fun DeltaEntity.toModel(): Patch = Patch(
    id = id,
    versionId = versionId,
    diff = patch,
    timestamp = timestamp,
    previousVersionId = previousVersionId
)

fun Patch.toEntity(): DeltaEntity = DeltaEntity(
    id = id,
    versionId = versionId,
    previousVersionId = previousVersionId,
    patch = diff,
    timestamp = timestamp
)
