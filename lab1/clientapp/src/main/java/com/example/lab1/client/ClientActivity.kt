package com.example.lab1.client

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val result = findViewById<TextView>(R.id.provider_result)
        findViewById<Button>(R.id.read_provider).setOnClickListener {
            readProvider(result)
        }
    }

    private fun readProvider(result: TextView) {
        contentResolver.query(PROVIDER_URI, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE))
                Log.d("BAM", "ClientActivity received: $message")
                result.text = message
            }
        } ?: run {
            result.setText(R.string.provider_unavailable)
        }
    }

    companion object {
        private val PROVIDER_URI = Uri.parse("content://com.example.lab1.provider/data")
        private const val COLUMN_MESSAGE = "message"
    }
}
