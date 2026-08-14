package com.example.modern_editor.data.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Thin wrappers around SAF's [android.content.ContentResolver] / [DocumentsContract]. */
object FileStorage {

    suspend fun readText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.reader(Charsets.UTF_8).readText()
        } ?: ""
    }

    suspend fun writeText(context: Context, uri: Uri, text: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            output.writer(Charsets.UTF_8).use { it.write(text) }
        }
        Unit
    }

    fun takePersistablePermission(context: Context, uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
    }

    suspend fun deleteDocument(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }.getOrDefault(false)
    }

    suspend fun renameDocument(context: Context, uri: Uri, newName: String): Uri? = withContext(Dispatchers.IO) {
        runCatching { DocumentsContract.renameDocument(context.contentResolver, uri, newName) }.getOrNull()
    }

    fun displayNameOf(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) it.getString(index) else null
            } else {
                null
            }
        }
    }
}
