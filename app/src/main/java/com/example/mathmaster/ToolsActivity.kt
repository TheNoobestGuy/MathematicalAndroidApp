package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class ToolsActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tools_activity)

        // Menu buttons
        val matrixCalcButton: Button = findViewById(R.id.MatrixCalculator)
        val advancedCalcButton: Button = findViewById(R.id.AdvancedCalculator)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(matrixCalcButton, clickedButtonStyle, MatrixCalculatorMenuActivity())
        clickFunction(advancedCalcButton, clickedButtonStyle, AdvancedCalculatorActivity())

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ToolsActivity, MainMenuActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}