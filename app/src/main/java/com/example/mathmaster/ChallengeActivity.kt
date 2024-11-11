package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
        val statisticsView: CardView = findViewById<CardView>(R.id.ChallengeStatistics)

        // Menu buttons
        val backButton: Button = findViewById<Button>(R.id.Back)

        // Get height of a screen
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        val statisticsTopMargin: Int = (screenHeight * 0.10).toInt()

        // Adjust screen height to statistics
        val layoutParamsStatistics = statisticsView.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsStatistics.topMargin = statisticsTopMargin
        statisticsView.layoutParams = layoutParamsStatistics

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(backButton, clickedButtonStyle, MainActivity())
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}