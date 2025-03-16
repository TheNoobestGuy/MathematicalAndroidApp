package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.mathmaster.customviews.AdvancedKeyboard

class UnknownsCalculatorActivity : ComponentActivity() {
    private lateinit var actualEquation: EditText
    private var equationAmount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unknownscalculator_activity)

        // Get label of equations
        val labelsForEquations: MutableList<TextView> = mutableListOf()

        val firstEquationLabel: TextView = findViewById(R.id.FirstEquationLabel)
        labelsForEquations.add(firstEquationLabel)

        val secondEquationLabel: TextView = findViewById(R.id.SecondEquationLabel)
        secondEquationLabel.visibility = View.GONE
        labelsForEquations.add(secondEquationLabel)

        val thirdEquationLabel: TextView = findViewById(R.id.ThirdEquationLabel)
        thirdEquationLabel.visibility = View.GONE
        labelsForEquations.add(thirdEquationLabel)

        // Get equations
        val equationsInput: MutableList<EditText> = mutableListOf()

        val firstEquation: EditText = findViewById(R.id.FirstEquation)
        firstEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        equationsInput.add(firstEquation)

        val secondEquation: EditText = findViewById(R.id.SecondEquation)
        secondEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        secondEquation.visibility = View.GONE
        equationsInput.add(secondEquation)

        val thirdEquation: EditText = findViewById(R.id.ThirdEquation)
        thirdEquation.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        thirdEquation.visibility = View.GONE
        equationsInput.add(thirdEquation)

        val blank: TextView = findViewById(R.id.Blank)
        val equalSigns = booleanArrayOf(false, false, false)
        actualEquation = firstEquation

        // Keyboard
        val keyboard: AdvancedKeyboard = findViewById(R.id.Keyboard)
        keyboard.setUnknownsCalculatorMode()
        keyboard.refreshAllClickListeners(actualEquation, blank)

        // Select equations listeners
        var equationIdx = 0
        for ((index, input) in equationsInput.withIndex()) {
            input.setOnClickListener {
                actualEquation = input

                if (keyboard.getEqualSign()) {
                    keyboard.unsetEqualSign()
                    equalSigns[equationIdx] = true
                }
                else {
                    equalSigns[equationIdx] = false
                }

                equationIdx = index
                if (equalSigns[index]) {
                    keyboard.setEqualSign()
                }
                keyboard.refreshAllClickListeners(actualEquation, blank)
            }
        }

        // Calculate unknowns
        val checkButton = keyboard.getCheckButton()
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
        val unClickedButtonStyle = R.drawable.menubutton_background

        checkButton.setOnClickListener {
            checkButton.setBackgroundResource(clickedButtonStyle)
            val equationsList = mutableListOf<String>()
            for (input in equationsInput) {
                if (input.text.isNotEmpty()) {
                    equationsList.add(input.text.toString())
                }
            }

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

        // Add equation
        val addEquationButton = keyboard.getAddEquation()
        addEquationButton.setOnClickListener {
            addEquationButton.setBackgroundResource(clickedButtonStyle)

            if (equationAmount < equationsInput.size) {
                equationsInput[equationAmount].visibility = View.VISIBLE
                labelsForEquations[equationAmount].visibility = View.VISIBLE
                equationAmount++
            }

            Handler(Looper.getMainLooper()).postDelayed({
                addEquationButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }

        // Subtract equation
        val subtractEquationButton = keyboard.getSubtractEquation()
        subtractEquationButton.setOnClickListener {
            subtractEquationButton.setBackgroundResource(clickedButtonStyle)

            if (equationAmount > 1) {
                equationAmount--
                equationsInput[equationAmount].text.clear()
                equationsInput[equationAmount].visibility = View.GONE
                labelsForEquations[equationAmount].visibility = View.GONE
            }

            Handler(Looper.getMainLooper()).postDelayed({
                subtractEquationButton.setBackgroundResource(unClickedButtonStyle)
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