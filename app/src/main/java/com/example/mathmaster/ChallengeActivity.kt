package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

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
        val menuBlockView: LinearLayout = findViewById<LinearLayout>(R.id.MenuBlock)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity()::class.java)
        startActivity(intent)
    }
}