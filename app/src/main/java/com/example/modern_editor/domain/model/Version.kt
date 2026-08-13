package com.example.modern_editor.domain.model

enum class VersionType {
    SNAPSHOT,
    DELTA
}

data class Version(
    val id: String,
    val fileId: String,
    val type: VersionType,
    val timestamp: Long,
    val label: String? = null
)
