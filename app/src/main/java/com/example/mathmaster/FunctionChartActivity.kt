package com.example.mathmaster

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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

    private var keyboardIsVisible: Boolean = true
    private var screenHeight: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.functionchart_activity)

        // Screen height
        screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Chart
        val functionChart: FunctionChart = findViewById(R.id.FunctionChart)
        functionChart.translationY = -screenHeight+functionChart.y

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
            // Change button text
            if (slideButton.text == "↑") {
                slideButton.text = "↓"
            }
            else {
                slideButton.text = "↑"

                // Draw a function
                functionChart.drawAFunction(equation.text.toString(), keyboard)
            }

            // Start proper animation
            val animatorSet = AnimatorSet()
            if (keyboardIsVisible) {
                val slideDownChart = ObjectAnimator.ofFloat(functionChart, "translationY", 0f)
                val slideDownButton = ObjectAnimator.ofFloat(slideButton, "translationY", screenHeight-slideButton.y-slideButton.height-20f)
                val slideDownEquation = ObjectAnimator.ofFloat(equation, "translationY", screenHeight-equation.y)
                val slideDownKeyboard = ObjectAnimator.ofFloat(keyboard, "translationY", screenHeight-keyboard.y)

                animatorSet.playTogether(slideDownChart, slideDownButton, slideDownEquation, slideDownKeyboard)
                animatorSet.duration = 800
                animatorSet.start()
            } else {
                val slideUpChart = ObjectAnimator.ofFloat(functionChart, "translationY", -screenHeight+functionChart.y)
                val slideUpButton = ObjectAnimator.ofFloat(slideButton, "translationY", 0f)
                val slideUpEquation = ObjectAnimator.ofFloat(equation, "translationY",0f)
                val slideUpKeyboard = ObjectAnimator.ofFloat(keyboard, "translationY", 0f)

                animatorSet.playTogether(slideUpChart, slideUpButton, slideUpEquation, slideUpKeyboard)
                animatorSet.duration = 800
                animatorSet.start()
            }

            keyboardIsVisible = !keyboardIsVisible

            Handler(Looper.getMainLooper()).postDelayed({
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
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}