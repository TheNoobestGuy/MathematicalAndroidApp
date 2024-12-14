package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class ChallengeActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_activity)

        // Statistics
        val menuBlockView: LinearLayout = findViewById(R.id.MenuBlock)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ChallengeActivity, MainMenuActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}