package com.example.lab1

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab1.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BAM", "SecondActivity onCreate")
        enableEdgeToEdge()

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonBack.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BAM", "SecondActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("BAM", "SecondActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BAM", "SecondActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("BAM", "SecondActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BAM", "SecondActivity onDestroy")
    }
}
