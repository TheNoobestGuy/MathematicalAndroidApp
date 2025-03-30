package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.mathmaster.customviews.AdvancedKeyboard
import com.example.mathmaster.customviews.Calculator

class DerivativeCalculatorActivity : ComponentActivity() {

    private val calculator = Calculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.derivativecalculator_activity)

        // Equations
        val derivative: TextView = findViewById(R.id.ResultEquation)
        val input: EditText = findViewById(R.id.Equation)
        val blank: TextView = findViewById(R.id.Blank)

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setDerivativeCalculatorMode()
        keyboard.setAllClickListenersForDerivativeCalculator(input, derivative, blank)

        // Handle enter button
        val enterButton = keyboard.getEnterButton()

        enterButton.setOnClickListener {
            keyboard.clickEnterButton()

            derivative.text = calculator.solveDerivative(input.text.toString())

            keyboard.unClickEnterButton()
        }

        // Handle the back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@DerivativeCalculatorActivity, ToolsActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val options = ActivityOptions.makeCustomAnimation(
                    this@DerivativeCalculatorActivity,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}