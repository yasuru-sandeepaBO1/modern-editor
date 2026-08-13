package com.example.modern_editor.domain.model

data class Patch(
    val id: String,
    val versionId: String,
    val diff: String,
    val timestamp: Long
)
