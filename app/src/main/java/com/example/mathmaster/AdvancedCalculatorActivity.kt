package com.example.mathmaster

import android.app.ActivityOptions
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
        val history: TextView = findViewById(R.id.History)
        val result: TextView = findViewById(R.id.ResultBar)

        // Get GUI
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)

        // Keyboard
        keyboard.setAllClickListenersForAdvanceCalculator(equation, result, history)

        // Handle the back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@AdvancedCalculatorActivity, ToolsActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@AdvancedCalculatorActivity,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}