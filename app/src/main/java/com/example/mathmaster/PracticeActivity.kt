package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class PracticeActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.practice_activity)

        // Statistics
        val menuBlockView: LinearLayout = findViewById<LinearLayout>(R.id.MenuBlock)

        // Menu buttons
        val multiplyButton: Button = findViewById<Button>(R.id.Multiply)
        val divideButton: Button = findViewById<Button>(R.id.Divide)
        val addSubtractButton: Button = findViewById<Button>(R.id.AddSubtract)
        val mixedButton: Button = findViewById<Button>(R.id.Mixed)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(multiplyButton, clickedButtonStyle, MultiplyActivity())
        clickFunction(divideButton, clickedButtonStyle, DivideActivity())
        clickFunction(addSubtractButton, clickedButtonStyle, AddSubtractActivity())
        clickFunction(mixedButton, clickedButtonStyle, MixedActivity())
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainMenuActivity()::class.java)
        startActivity(intent)
    }
}