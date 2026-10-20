package com.example.lab1

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

class LabContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.d("BAM", "LabContentProvider onCreate")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        Log.d("BAM", "LabContentProvider query: $uri")
        return MatrixCursor(arrayOf(COLUMN_MESSAGE)).apply {
            addRow(arrayOf("Dane z aplikacji provider"))
        }
    }

    override fun getType(uri: Uri): String {
        Log.d("BAM", "LabContentProvider getType: $uri")
        return "vnd.android.cursor.item/vnd.com.example.lab1.provider.data"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d("BAM", "LabContentProvider insert: $uri")
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d("BAM", "LabContentProvider delete: $uri")
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        Log.d("BAM", "LabContentProvider update: $uri")
        return 0
    }

    companion object {
        const val AUTHORITY = "com.example.lab1.provider"
        const val COLUMN_MESSAGE = "message"
    }
}
