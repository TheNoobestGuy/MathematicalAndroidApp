package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

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
        val learnButton: Button = findViewById(R.id.Learn)
        val practiceButton: Button = findViewById(R.id.Practice)
        val challengeButton: Button = findViewById(R.id.Challenge)
        val toolsButton: Button = findViewById(R.id.Tools)
        val statisticsButton: Button = findViewById(R.id.Statistics)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(learnButton, clickedButtonStyle, LearnActivity())
        clickFunction(practiceButton, clickedButtonStyle, PracticeActivity())
        clickFunction(challengeButton, clickedButtonStyle, ChallengeActivity())
        clickFunction(toolsButton, clickedButtonStyle, ToolsActivity())
        clickFunction(statisticsButton, clickedButtonStyle, StatisticsActivity())
    }
}