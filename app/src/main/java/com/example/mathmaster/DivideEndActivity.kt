package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import android.graphics.Typeface

class DivideEndActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.practice_end_activity)

        // Database
        val firstNumsFromQuestions: IntArray = intent.getIntArrayExtra("firstNumsFromQuestions")!!
        val secondNumsFromQuestions: IntArray = intent.getIntArrayExtra("secondNumsFromQuestions")!!

        val numberOfQuestions: Int = intent.getIntExtra("numberOfQuestions", 0)
        val answersArray: IntArray = intent.getIntArrayExtra("answersArray")!!
        val correctAnswersArray: IntArray = intent.getIntArrayExtra("correctAnswersArray")!!
        val correctnessOfAnswers: BooleanArray = intent.getBooleanArrayExtra("correctnessOfAnswers")!!

        // Statistics
        val totalQuestions: TextView = findViewById<TextView>(R.id.MultiplyStatisticsTotalValue)
        val correctAnswersValue: TextView = findViewById<TextView>(R.id.MultiplyStatisticsCorrectValue)
        val incorrectAnswersValue: TextView = findViewById<TextView>(R.id.MultiplyStatisticsIncorrectValue)

        // Get relative layout that is inside scroll view
        val scrollBoxLinearLayout: LinearLayout = findViewById<LinearLayout>(R.id.ScrollBox)

        // Database to create row in scroll box
        val equationList = mutableListOf<TextView>()
        val answersList = mutableListOf<TextView>()
        val icons = listOf(R.drawable.error, R.drawable.check)

        // Create the list of equation
        for (i in answersArray.indices) {
            val firstNum = firstNumsFromQuestions[i]
            val secondNum = secondNumsFromQuestions[i]
            val answer = answersArray[i]

            val counter = i + 1
            val whiteSpace = if (counter < 10) "      " else "    "
            val equation = "Equation $counter: $whiteSpace$firstNum / $secondNum = $answer "
            val equationTextView = TextView(this).apply {
                id = View.generateViewId()
                text = equation
                setTextColor(Color.WHITE)
                layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
            }

            equationList.add(equationTextView)
        }

        // Create the list of correct answers
        for (i in correctAnswersArray.indices) {
            val correctAnswer = correctAnswersArray[i].toString() + " "

            val correctAnswerTextView = TextView(this).apply {
                id = View.generateViewId()
                text = correctAnswer
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
            }

            answersList.add(correctAnswerTextView)
        }

        // Generate constraints and append all content to parent linear layout
        for (i in equationList.indices) {
            // Create a new ConstraintLayout programmatically for row in scroll box
            val newConstraintLayout = ConstraintLayout(this).apply {
                id = View.generateViewId()
                setPadding(0, 10, 0, 10)
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 4
                }
            }

            // Check does equation is right or not
            var equationIsCorrect = false
            val piecesOfEquation = equationList[i].text.toString().split(' ')

            val equationNum = piecesOfEquation[piecesOfEquation.size - 2]
            val answerNum = answersList[i].text.toString().dropLast(1)

            if (equationNum == answerNum) {
                equationIsCorrect = true
            }

            // Generate error and check icons
            var errorIcon: ImageView = ImageView(this)
            val checkIcon = ImageView(this).apply {
                id = View.generateViewId()
                setImageResource(icons[1])
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            if (equationIsCorrect) {
                errorIcon= ImageView(this).apply {
                    id = View.generateViewId()
                    setImageResource(icons[1])
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } else {
                errorIcon = ImageView(this).apply {
                    id = View.generateViewId()
                    setImageResource(icons[0])
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }

            // Create an append text views to constraint layout
            newConstraintLayout.addView(equationList[i])
            newConstraintLayout.addView(answersList[i])

            // Append correct icons to constraint layout
            newConstraintLayout.addView(errorIcon)
            newConstraintLayout.addView(checkIcon)

            // Define and apply constraints using ConstraintSet
            val constraintSet = ConstraintSet()
            constraintSet.clone(newConstraintLayout)

            // Set constraints for the equation
            constraintSet.connect(equationList[i].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(equationList[i].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(equationList[i].id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            constraintSet.constrainWidth(equationList[i].id, 0) // 0 width to use constraint

            // Set constraints for the error icon or check icon if equation is correct
            constraintSet.connect(errorIcon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(errorIcon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(errorIcon.id, ConstraintSet.LEFT, equationList[i].id, ConstraintSet.RIGHT)

            // Set constraints for the check icon
            constraintSet.connect(checkIcon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(checkIcon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(checkIcon.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

            // Set constraints for the answer
            constraintSet.connect(answersList[i].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(answersList[i].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(answersList[i].id, ConstraintSet.RIGHT, checkIcon.id, ConstraintSet.LEFT)
            constraintSet.constrainWidth(answersList[i].id, 0) // 0 width to use constraint

            // Apply the constraints to the layout
            constraintSet.applyTo(newConstraintLayout)

            // Finally, add the new ConstraintLayout to the parent menu layout
            scrollBoxLinearLayout.addView(newConstraintLayout)
        }

        // Calculate statistics
        var correctAnswersCounter = 0
        var incorrectAnswersCounter = 0

        for (i in correctnessOfAnswers.indices) {
            if (correctnessOfAnswers[i]) {
                correctAnswersCounter++
            } else {
                incorrectAnswersCounter++
            }
        }

        // Apply calculated statistics
        totalQuestions.text = numberOfQuestions.toString()
        correctAnswersValue.text = correctAnswersCounter.toString()
        incorrectAnswersValue.text = incorrectAnswersCounter.toString()
    }

    override fun onBackPressed() {
        val intent = Intent(this, PracticeActivity()::class.java)
        startActivity(intent)
    }
}