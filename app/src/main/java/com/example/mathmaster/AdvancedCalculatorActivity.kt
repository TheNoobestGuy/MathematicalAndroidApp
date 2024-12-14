package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.mathmaster.customviews.AdvancedKeyboard

class AdvancedCalculatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.advancedcalculator_activity)

        // Get equation
        val equation: TextView = findViewById(R.id.EquationBar)
        val result: TextView = findViewById(R.id.ResultBar)

        // Get GUI
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)

        // Keyboard
        keyboard.numberButtonClick(equation, result)
        keyboard.basicCalcButtonClick(equation)
        keyboard.functionButtonClick(equation)
        keyboard.openBracketButtonClick(equation)
        keyboard.closeBracketButtonClick(equation)
        keyboard.powerButtonClick(equation)
        keyboard.commaButtonClick(equation)
        keyboard.rootButtonClick(equation)
        keyboard.factorialButtonClick(equation, result)
        keyboard.numberPIButtonClick(equation, result)
        keyboard.numberEulerButtonClick(equation, result)
        keyboard.percentButtonClick(equation, result)
        keyboard.fractionButtonClick(equation)

        keyboard.enterButtonClick()
        keyboard.deleteButtonClick(equation,result)
        keyboard.clearButtonClick(equation, result)
        keyboard.degreeButtonClick()
        keyboard.changeFunctionsButtonClick()

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@AdvancedCalculatorActivity, ToolsActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}