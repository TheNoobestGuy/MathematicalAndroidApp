package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainMenuActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainmenu_activity)

        // Menu content
        val logo: TextView = findViewById<TextView>(R.id.Logo)
        val learnButton: Button = findViewById<Button>(R.id.Learn)
        val practiceButton: Button = findViewById<Button>(R.id.Practice)
        val challengeButton: Button = findViewById<Button>(R.id.Challenge)
        val toolsButton: Button = findViewById<Button>(R.id.Tools)
        val statisticsButton: Button = findViewById<Button>(R.id.Statistics)

        // Get height of a screen
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        val logoTopMargin: Int = (screenHeight * 0.12).toInt()
        val menuTopMargin: Int = (screenHeight * 0.3).toInt()

        // Adjust screen height to logo and first button from menu
        val layoutParamsLogo = logo.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsLogo.topMargin = logoTopMargin
        logo.layoutParams = layoutParamsLogo

        val layoutParamsMenu = learnButton.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsMenu.topMargin = menuTopMargin
        learnButton.layoutParams = layoutParamsMenu

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(learnButton, clickedButtonStyle, LearnActivity())
        clickFunction(practiceButton, clickedButtonStyle, PracticeActivity())
        clickFunction(challengeButton, clickedButtonStyle, ChallengeActivity())
        clickFunction(toolsButton, clickedButtonStyle, ToolsActivity())
        clickFunction(statisticsButton, clickedButtonStyle, StatisticsActivity())
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}