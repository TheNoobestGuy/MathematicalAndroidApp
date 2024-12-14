package com.example.mathmaster

import com.example.mathmaster.customviews.Keyboard
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import kotlin.random.Random

class AddSubtractActivity : ComponentActivity() {

    // Variables
    private var waitCounterValue = 3
    private val handler = Handler(Looper.getMainLooper())

    // Counter function for counting down before start of practice
    private val countBeforeStart = object : Runnable {
        override fun run() {
            // Get content
            val timeCounter: TextView = findViewById(R.id.TimeCounter)
            val questionCounter: TextView = findViewById(R.id.QuestionCounter)
            val equation: TextView = findViewById(R.id.Equation)
            val keyboard: Keyboard = findViewById(R.id.Keyboard)

            // Disable visibility of content
            questionCounter.visibility = View.INVISIBLE
            equation.visibility = View.INVISIBLE
            keyboard.visibility = View.INVISIBLE

            // Update counter
            if (waitCounterValue > 0) {
                timeCounter.text = waitCounterValue.toString()
                handler.postDelayed(this, 1000)

                // Show content
            } else {
                timeCounter.visibility = View.INVISIBLE

                questionCounter.visibility = View.VISIBLE
                equation.visibility = View.VISIBLE
                keyboard.visibility = View.VISIBLE

                handler.removeCallbacks(this)
            }

            waitCounterValue--
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.practice_game_activity)

        // Interactive menu
        val questionCounter: TextView = findViewById(R.id.QuestionCounter)
        val equation: TextView = findViewById(R.id.Equation)
        val keyboard: Keyboard = findViewById(R.id.Keyboard)

        // Count before start of game
        handler.post(countBeforeStart)

        // Database
        var questionCounterValue = 0
        val numberOfQuestions = 15
        val twoNumbersRange = 10..99
        val numbersFromQuestions: MutableList<MutableList<Int>> = mutableListOf(mutableListOf(), mutableListOf())
        val correctAnswersArray = IntArray(numberOfQuestions)
        val equationSigns = BooleanArray(numberOfQuestions)
        val answersArray = IntArray(numberOfQuestions)

        // Create list of questions
        for (i in 0 until numberOfQuestions) {
            val randomNumber = twoNumbersRange.random()
            val randomSign = Random.nextBoolean()
            var secondNumber: Int

            if(!randomSign) {
                val secondNumberRange = 1..99
                do {
                    secondNumber = secondNumberRange.random()
                } while(secondNumber >= randomNumber)
            } else {
                secondNumber = twoNumbersRange.random()
            }

            equationSigns[i] = randomSign
            numbersFromQuestions[0].add(randomNumber)
            numbersFromQuestions[1].add(secondNumber)
        }

        // Show question counter and equation
        var questionCounterValueBuffer = questionCounterValue + 1
        var bufferQuestionCounter = "$questionCounterValueBuffer/$numberOfQuestions"
        questionCounter.text = bufferQuestionCounter

        var firstNum = numbersFromQuestions[0][questionCounterValue]
        var secondNum = numbersFromQuestions[1][questionCounterValue]

        var bufferEquation: String = if(equationSigns[0]) {
            "$firstNum + $secondNum"
        } else {
            "$firstNum - $secondNum"
        }
        equation.text = bufferEquation

        // Keyboard
        keyboard.numberButtonClick()
        keyboard.deleteButtonClick()

        // Enter button
        keyboard.getEnterButton().setOnClickListener {
            keyboard.clickEnterButton()
            questionCounterValue++

            // End game statement
            if (questionCounterValue >= numberOfQuestions) {
                // Append last answer
                firstNum = numbersFromQuestions[0][questionCounterValue-1]
                secondNum = numbersFromQuestions[1][questionCounterValue-1]

                if (equationSigns[questionCounterValue-1]) {
                    correctAnswersArray[questionCounterValue-1] = firstNum + secondNum
                } else {
                    correctAnswersArray[questionCounterValue-1] = firstNum - secondNum
                }

                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()

                // Count correctness of answers
                val correctnessOfAnswers = BooleanArray(numberOfQuestions)
                for (i in answersArray.indices) {
                    correctnessOfAnswers[i] = answersArray[i] == correctAnswersArray[i]
                }

                // Go to end page
                val intent = Intent(this, AddSubtractEndActivity()::class.java)

                // Extract numbers from questions
                val firstNumbersFromQuestions = IntArray(numberOfQuestions)
                val secondNumbersFromQuestions = IntArray(numberOfQuestions)

                for (i in numbersFromQuestions[0].indices)
                {
                    firstNumbersFromQuestions[i] = numbersFromQuestions[0][i]
                }

                for (i in numbersFromQuestions[0].indices)
                {
                    secondNumbersFromQuestions[i] = numbersFromQuestions[1][i]
                }

                // Pass information that is needed for end statistic overview
                intent.putExtra("numberOfQuestions", numberOfQuestions)
                intent.putExtra("firstNumbersFromQuestions", firstNumbersFromQuestions)
                intent.putExtra("secondNumbersFromQuestions", secondNumbersFromQuestions)
                intent.putExtra("correctnessOfAnswers", correctnessOfAnswers)
                intent.putExtra("correctAnswersArray", correctAnswersArray)
                intent.putExtra("answersArray", answersArray)
                intent.putExtra("equationSigns", equationSigns)

                startActivity(intent)
            }
            // Update question counter and change equation also append answer
            else {
                questionCounterValueBuffer = questionCounterValue + 1
                bufferQuestionCounter = "$questionCounterValueBuffer/$numberOfQuestions"
                questionCounter.text = bufferQuestionCounter

                firstNum = numbersFromQuestions[0][questionCounterValue]
                secondNum = numbersFromQuestions[1][questionCounterValue]

                bufferEquation = if(equationSigns[questionCounterValue]) {
                    "$firstNum + $secondNum"
                } else {
                    "$firstNum - $secondNum"
                }
                equation.text = bufferEquation

                firstNum = numbersFromQuestions[0][questionCounterValue-1]
                secondNum = numbersFromQuestions[1][questionCounterValue-1]

                if (equationSigns[questionCounterValue-1]) {
                    correctAnswersArray[questionCounterValue-1] = firstNum + secondNum
                } else {
                    correctAnswersArray[questionCounterValue-1] = firstNum - secondNum
                }

                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()
            }

            keyboard.unClickEnterButton()
        }

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@AddSubtractActivity, PracticeActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}