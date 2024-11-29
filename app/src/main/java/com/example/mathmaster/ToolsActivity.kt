package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

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
        val matrixCalcButton: Button = findViewById<Button>(R.id.MatrixCalculator)
        val advancedCalcButton: Button = findViewById<Button>(R.id.AdvancedCalculator)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(matrixCalcButton, clickedButtonStyle, MatrixCalculatorMenuActivity())
        clickFunction(advancedCalcButton, clickedButtonStyle, AdvancedCalculatorActivity())
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainMenuActivity()::class.java)
        startActivity(intent)
    }
}