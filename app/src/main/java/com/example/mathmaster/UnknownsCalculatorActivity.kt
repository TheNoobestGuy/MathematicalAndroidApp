package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.mathmaster.customviews.AdvancedKeyboard

class UnknownsCalculatorActivity : ComponentActivity() {
    private lateinit var actualEquation: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unknownscalculator_activity)

        // Get equations
        val firstEquation: EditText = findViewById(R.id.FirstEquation)
        val secondEquation: EditText = findViewById(R.id.SecondEquation)
        val blank: TextView = findViewById(R.id.Blank)
        actualEquation = firstEquation

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setUnknownsCalculatorMode()

        keyboard.numberButtonClick(actualEquation, blank)
        keyboard.basicCalcButtonClick(actualEquation)
        keyboard.functionButtonClick(actualEquation)
        keyboard.openBracketButtonClick(actualEquation)
        keyboard.closeBracketButtonClick(actualEquation)
        keyboard.powerButtonClick(actualEquation)
        keyboard.dotButtonClick(actualEquation)
        keyboard.rootButtonClick(actualEquation)
        keyboard.factorialButtonClick(actualEquation, blank)
        keyboard.numberPIButtonClick(actualEquation, blank)
        keyboard.numberEulerButtonClick(actualEquation, blank)
        keyboard.percentButtonClick(actualEquation, blank)
        keyboard.fractionButtonClick(actualEquation)
        keyboard.variableButtonClick(actualEquation)

        keyboard.xVariableClick(actualEquation)
        keyboard.yVariableClick(actualEquation)

        keyboard.enterWhenInUnknownsCalculatorMode(actualEquation)
        keyboard.deleteButtonClick(actualEquation, blank)
        keyboard.clearButtonClick(actualEquation, blank, blank)

        // Select equations listeners
        firstEquation.setOnClickListener {
            actualEquation = firstEquation
            keyboard.refreshAllClickListeners(actualEquation, blank)
        }

        secondEquation.setOnClickListener {
            actualEquation = secondEquation
            keyboard.refreshAllClickListeners(actualEquation, blank)
        }

        // Handle the back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@UnknownsCalculatorActivity, ToolsActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@UnknownsCalculatorActivity,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}