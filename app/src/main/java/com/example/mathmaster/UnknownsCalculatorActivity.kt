package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
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
        firstEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val secondEquation: EditText = findViewById(R.id.SecondEquation)
        secondEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val thirdEquation: EditText = findViewById(R.id.ThirdEquation)
        thirdEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        val blank: TextView = findViewById(R.id.Blank)
        val equalSigns = booleanArrayOf(false, false, false)
        var equationIdx = 0
        actualEquation = firstEquation

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setUnknownsCalculatorMode()
        keyboard.refreshAllClickListeners(actualEquation, blank)

        // Select equations listeners
        firstEquation.setOnClickListener {
            actualEquation = firstEquation

            if (equationIdx == 1 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 1 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            if (equationIdx == 2 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 2 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            equationIdx = 0
            if (equalSigns[equationIdx]) {
                keyboard.setEqualSign()
            }
            keyboard.refreshAllClickListeners(actualEquation, blank)
        }

        secondEquation.setOnClickListener {
            actualEquation = secondEquation

            if (equationIdx == 0 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 0 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            if (equationIdx == 2 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 2 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            equationIdx = 1
            if (equalSigns[equationIdx]) {
                keyboard.setEqualSign()
            }
            keyboard.refreshAllClickListeners(actualEquation, blank)
        }

        thirdEquation.setOnClickListener {
            actualEquation = thirdEquation

            if (equationIdx == 0 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 0 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            if (equationIdx == 1 && keyboard.getEqualSign()) {
                keyboard.unsetEqualSign()
                equalSigns[equationIdx] = true
            }
            else if (equationIdx == 1 && !keyboard.getEqualSign()) {
                equalSigns[equationIdx] = false
            }

            equationIdx = 2
            if (equalSigns[equationIdx]) {
                keyboard.setEqualSign()
            }
            keyboard.refreshAllClickListeners(actualEquation, blank)
        }

        // Calculate unknowns
        val checkButton = keyboard.getCheckButton()
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
        val unClickedButtonStyle = R.drawable.menubutton_background

        checkButton.setOnClickListener {
            checkButton.setBackgroundResource(clickedButtonStyle)
            val equationsList = mutableListOf(firstEquation.text.toString(), secondEquation.text.toString()
                , thirdEquation.text.toString())
            val value = keyboard.solveEquationsWithUnknowns(equationsList)

            if (value != null) {
                println(value)
            }
            else {
                println("No solutions")
            }

            Handler(Looper.getMainLooper()).postDelayed({
                checkButton.setBackgroundResource(unClickedButtonStyle)
                }, 100)
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