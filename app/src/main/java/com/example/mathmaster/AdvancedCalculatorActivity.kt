package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.AdvancedKeyboard

class AdvancedCalculatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.advancedcalculator_activity)

        // Get equation
        val equation: TextView = findViewById<TextView>(R.id.EquationBar)

        // Get GUI
        val keyboard: AdvancedKeyboard = findViewById<AdvancedKeyboard>(R.id.Keyboard)

        // Keyboard
        keyboard.numberButtonClick(equation)
        keyboard.basicCalcButtonClick(equation)
        keyboard.openBracketButtonClick(equation)
        keyboard.closeBracketButtonClick(equation)

        keyboard.equalityButtonClick(equation)
        keyboard.deleteButtonClick(equation)
    }

    override fun onBackPressed() {
        val intent = Intent(this, ToolsActivity()::class.java)
        startActivity(intent)
    }
}