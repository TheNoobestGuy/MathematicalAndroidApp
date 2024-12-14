package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class LearnActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learn_activity)

        // Style of clicked button
        // val clickedButtonStyle = R.drawable.menuButton_background_clicked

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LearnActivity, MainMenuActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}