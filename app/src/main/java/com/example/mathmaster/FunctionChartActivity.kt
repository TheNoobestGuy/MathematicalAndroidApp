package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.mathmaster.customviews.AdvancedKeyboard
import com.example.mathmaster.customviews.FunctionChart

class FunctionChartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.functionchart_activity)

        // Chart
        val functionChart: FunctionChart = findViewById(R.id.FunctionChart)

        // Show chart button
        val slideButton: Button = findViewById(R.id.SlideButton)

        // Get equation
        val equation: TextView = findViewById(R.id.EquationText)
        val blank: TextView = findViewById(R.id.Blank)

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setFunctionChartMode()

        keyboard.numberButtonClick(equation, blank)
        keyboard.basicCalcButtonClick(equation)
        keyboard.functionButtonClick(equation)
        keyboard.openBracketButtonClick(equation)
        keyboard.closeBracketButtonClick(equation)
        keyboard.powerButtonClick(equation)
        keyboard.commaButtonClick(equation)
        keyboard.rootButtonClick(equation)
        keyboard.factorialButtonClick(equation, blank)
        keyboard.numberPIButtonClick(equation, blank)
        keyboard.numberEulerButtonClick(equation, blank)
        keyboard.percentButtonClick(equation, blank)
        keyboard.fractionButtonClick(equation)
        keyboard.variableButtonClick(equation)

        keyboard.enterButtonClick()
        keyboard.deleteButtonClick(equation, blank)
        keyboard.clearButtonClick(equation, blank)
        keyboard.degreeButtonClick()
        keyboard.changeFunctionsButtonClick()

        // Slide button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
        val unClickedButtonStyle = R.drawable.menubutton_background
        slideButton.setOnClickListener {
            slideButton.setBackgroundResource(clickedButtonStyle)

            Handler(Looper.getMainLooper()).postDelayed({
                functionChart.drawAFunction(equation.text.toString(), keyboard)
                slideButton.setBackgroundResource(unClickedButtonStyle)
            },100)
        }
    }

    override fun onStart() {
        super.onStart()

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@FunctionChartActivity, ToolsActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@FunctionChartActivity,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}