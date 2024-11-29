package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class LearnActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learn_activity)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainMenuActivity()::class.java)
        startActivity(intent)
    }
}