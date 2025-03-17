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
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mathmaster.customviews.AdvancedKeyboard
import com.example.mathmaster.customviews.FunctionChart

class FunctionChartActivity : ComponentActivity() {

    private var keyboardIsVisible: Boolean = true
    private var screenHeight: Float = 0f

    private var showZeroPlaces:Boolean = false
    private var showHorizontalAsymptote: Boolean = false
    private var showVerticalAsymptotes: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.functionchart_activity)

        // Screen height
        screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Chart
        val functionChart: FunctionChart = findViewById(R.id.FunctionChart)
        functionChart.translationY = -screenHeight

        // Information
        val informationBlock: ConstraintLayout = findViewById(R.id.Information)
        informationBlock.translationY = -screenHeight

        val zeroPlacesText: TextView = findViewById(R.id.ZeroPlaces)
        val horizontalAsymptote: TextView = findViewById(R.id.HorizontalAsymptote)
        val asymptotesText: TextView = findViewById(R.id.Asymptotes)

        // Show chart button
        val slideButton: Button = findViewById(R.id.SlideButton)

        // Get equation
        val equation: TextView = findViewById(R.id.EquationText)
        val blank: TextView = findViewById(R.id.Blank)

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setFunctionChartMode()

        keyboard.setAllClickListenersForFunctionChar(equation, blank)

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
                functionChart.drawAFunction(equation.text.toString())
                functionChart.updateInformation(zeroPlacesText, asymptotesText, horizontalAsymptote)
            }

            // Start proper animation
            val animatorSet = AnimatorSet()
            if (keyboardIsVisible) {
                val slideDownChart = ObjectAnimator.ofFloat(functionChart, "translationY", 0f)
                val slideDownInformation = ObjectAnimator.ofFloat(informationBlock, "translationY", 0f)
                val slideDownButton = ObjectAnimator.ofFloat(slideButton, "translationY", screenHeight-slideButton.y-slideButton.height-20f)
                val slideDownEquation = ObjectAnimator.ofFloat(equation, "translationY", screenHeight-equation.y)
                val slideDownKeyboard = ObjectAnimator.ofFloat(keyboard, "translationY", screenHeight-keyboard.y)

                animatorSet.playTogether(slideDownChart, slideDownInformation, slideDownButton, slideDownEquation, slideDownKeyboard)
                animatorSet.duration = 800
                animatorSet.start()
            } else {
                val slideUpChart = ObjectAnimator.ofFloat(functionChart, "translationY", -screenHeight)
                val slideUpInformation = ObjectAnimator.ofFloat(informationBlock, "translationY", -screenHeight)
                val slideUpButton = ObjectAnimator.ofFloat(slideButton, "translationY", 0f)
                val slideUpEquation = ObjectAnimator.ofFloat(equation, "translationY",0f)
                val slideUpKeyboard = ObjectAnimator.ofFloat(keyboard, "translationY", 0f)

                animatorSet.playTogether(slideUpChart, slideUpInformation, slideUpButton, slideUpEquation, slideUpKeyboard)
                animatorSet.duration = 800
                animatorSet.start()
            }

            keyboardIsVisible = !keyboardIsVisible

            Handler(Looper.getMainLooper()).postDelayed({
                slideButton.setBackgroundResource(unClickedButtonStyle)
            },100)
        }

        // Show zero places
        zeroPlacesText.setOnClickListener {
            if (showZeroPlaces) {
                zeroPlacesText.setTextColor(getColor(R.color.White))
                functionChart.setZeroPlaces()
            }
            else {
                zeroPlacesText.setTextColor(getColor(R.color.VeryLightGrey))
                functionChart.setZeroPlaces()
            }

            if (showHorizontalAsymptote) {
                horizontalAsymptote.setTextColor(getColor(R.color.White))
                showHorizontalAsymptote = !showHorizontalAsymptote
            }
            if (showVerticalAsymptotes) {
                asymptotesText.setTextColor(getColor(R.color.White))
                showVerticalAsymptotes = !showVerticalAsymptotes
            }

            showZeroPlaces = !showZeroPlaces
        }

        // Show horizontal asymptote point
        horizontalAsymptote.setOnClickListener {
            if (showHorizontalAsymptote) {
                horizontalAsymptote.setTextColor(getColor(R.color.White))
                functionChart.setHorizontalAsymptote()
            }
            else {
                horizontalAsymptote.setTextColor(getColor(R.color.VeryLightGrey))
                functionChart.setHorizontalAsymptote()
            }

            if (showVerticalAsymptotes) {
                asymptotesText.setTextColor(getColor(R.color.White))
                showVerticalAsymptotes = !showVerticalAsymptotes
            }
            if (showZeroPlaces) {
                zeroPlacesText.setTextColor(getColor(R.color.White))
                showZeroPlaces = !showZeroPlaces
            }

            showHorizontalAsymptote = !showHorizontalAsymptote
        }

        // Show vertical asymptotes points
        asymptotesText.setOnClickListener {
            if (showVerticalAsymptotes) {
                asymptotesText.setTextColor(getColor(R.color.White))
                functionChart.setVerticalAsymptotes()
            }
            else {
                asymptotesText.setTextColor(getColor(R.color.VeryLightGrey))
                functionChart.setVerticalAsymptotes()
            }

            if (showHorizontalAsymptote) {
                horizontalAsymptote.setTextColor(getColor(R.color.White))
                showHorizontalAsymptote = !showHorizontalAsymptote
            }
            if (showZeroPlaces) {
                zeroPlacesText.setTextColor(getColor(R.color.White))
                showZeroPlaces = !showZeroPlaces
            }

            showVerticalAsymptotes = !showVerticalAsymptotes
        }
    }

    override fun onStart() {
        super.onStart()

        // Handle the back button press
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