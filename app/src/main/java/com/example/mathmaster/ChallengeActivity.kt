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

        // Menu buttons
        val backButton: Button = findViewById<Button>(R.id.Back)

        // Get height of a screen
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        val menuBlockTopMargin: Int = (screenHeight * 0.12).toInt()

        val layoutParamsStatistics = menuBlockView.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsStatistics.topMargin = menuBlockTopMargin
        menuBlockView.layoutParams = layoutParamsStatistics

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(backButton, clickedButtonStyle, MainActivity())
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}