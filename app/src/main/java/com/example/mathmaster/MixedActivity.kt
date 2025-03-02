package com.example.mathmaster

import android.app.ActivityOptions
import com.example.mathmaster.customviews.Keyboard
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback

class MixedActivity : ComponentActivity() {

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

    private fun noDecimalPart(value: Float): Boolean {
        return value == value.toInt().toFloat()
    }

    private fun notNegative(value: Int): Boolean {
        return value > 0
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
        val numbersFromQuestions: MutableList<MutableList<Int>> = mutableListOf(mutableListOf(), mutableListOf(), mutableListOf())
        val correctAnswersArray = IntArray(numberOfQuestions)
        val equationSignsArray = charArrayOf('+', '-', '/', '×')
        val firstEquationSigns = CharArray(numberOfQuestions)
        val secondEquationSigns = CharArray(numberOfQuestions)
        val answersArray = IntArray(numberOfQuestions)

        // Create list of questions
        for (i in 0 until numberOfQuestions) {
            // Get two random signs
            val randomSignRange = 0..3
            var randomSign = randomSignRange.random()
            firstEquationSigns[i] = equationSignsArray[randomSign]

            randomSign = randomSignRange.random()
            secondEquationSigns[i] = equationSignsArray[randomSign]

            // Variables
            val signs = arrayOf(firstEquationSigns[i], secondEquationSigns[i])
            val numberRange = 1..99
            val firstNumber = numberRange.random()+1
            numbersFromQuestions[0].add(firstNumber)

            val numbers = mutableListOf(firstNumber)
            var higherAttention = false

            if (signs.last() == '×' || signs.last() == '/') {
                higherAttention = true
            }

            //Generate equation
            for (num in signs.indices) {
                var numBuffer = numberRange.random()
                when (signs[num]) {
                    '+' -> {
                        if (higherAttention) {
                            numbers.add(numBuffer)
                        }
                        else {
                            numbers[numbers.lastIndex] = numbers.last() + numBuffer
                        }
                    }
                    '-' -> {
                        val possibilities = mutableListOf<Int>()
                        var step = 1
                        while (step < numbers.last()) {
                            if (notNegative(numbers.last()-step)) {
                                possibilities.add(step)
                            }
                            else {
                                break
                            }
                            step++
                        }
                        numBuffer = if (possibilities.isEmpty()) {
                            1
                        } else {
                            possibilities.random()
                        }

                        if (higherAttention) {
                            numbers.add(numBuffer)
                        }
                        else {
                            numbers[numbers.lastIndex] = numbers.last() - numBuffer
                        }
                    }
                    '×' -> {
                        if (signs.first() == '-') {
                            val possibilities = mutableListOf<Int>()
                            var step = 1
                            while (true) {
                                if (notNegative(numbers.first() - (step*numbers.last()))) {
                                    possibilities.add(step)
                                }
                                else {
                                    break
                                }
                                step++
                            }
                            numBuffer = possibilities.random()
                        }

                        if (higherAttention) {
                            numbers.add(numbers.removeLast() * numBuffer)
                        }
                        else {
                            numbers.add(numbers.last() * numBuffer)
                        }
                    }
                    '/' -> {
                        val possibilities = mutableListOf<Int>()
                        var step = 1
                        while (step <= numbers.last()) {
                            if (noDecimalPart(numbers.last().toFloat() / step.toFloat())) {
                                possibilities.add(step)
                            }
                            step++
                        }

                        if (possibilities.size > 2) {
                            possibilities.removeLast()
                            possibilities.removeFirst()
                        }

                        numBuffer = possibilities.random()

                        if (higherAttention) {
                            numbers.add(numbers.removeLast() / numBuffer)
                        }
                        else {
                            numbers.add(numbers.last() / numBuffer)
                        }
                    }
                }

                numbersFromQuestions[num+1].add(numBuffer)
            }

            // Append correct answer
            var result = numbers.last()

            if (higherAttention) {
                when (signs[0]) {
                    '+' -> {
                        result = numbers[0] + numbers[1]
                    }
                    '-' -> {
                        result = numbers[0] - numbers[1]
                    }
                }
            }

            correctAnswersArray[i] = result
        }

        // Show question counter and equation
        var questionCounterValueBuffer = questionCounterValue + 1
        var bufferQuestionCounter = "$questionCounterValueBuffer/$numberOfQuestions"
        questionCounter.text = bufferQuestionCounter

        var firstNum = numbersFromQuestions[0][questionCounterValue]
        var secondNum = numbersFromQuestions[1][questionCounterValue]
        var thirdNum = numbersFromQuestions[2][questionCounterValue]

        var firstSign = firstEquationSigns[questionCounterValue]
        var secondSign = secondEquationSigns[questionCounterValue]

        var bufferEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
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
                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()

                // Count correctness of answers
                val correctnessOfAnswers = BooleanArray(numberOfQuestions)
                for (i in answersArray.indices) {
                    correctnessOfAnswers[i] = answersArray[i] == correctAnswersArray[i]
                }

                // Go to end page
                val intent = Intent(this, MixedEndActivity()::class.java)

                // Extract numbers from questions
                val firstNumbersFromQuestions = IntArray(numberOfQuestions)
                val secondNumbersFromQuestions = IntArray(numberOfQuestions)
                val thirdNumbersFromQuestions = IntArray(numberOfQuestions)

                for (i in numbersFromQuestions[0].indices)
                {
                    firstNumbersFromQuestions[i] = numbersFromQuestions[0][i]
                }

                for (i in numbersFromQuestions[0].indices)
                {
                    secondNumbersFromQuestions[i] = numbersFromQuestions[1][i]
                }

                for (i in numbersFromQuestions[0].indices)
                {
                    thirdNumbersFromQuestions[i] = numbersFromQuestions[2][i]
                }

                // Pass information that is needed for end statistic overview
                intent.putExtra("numberOfQuestions", numberOfQuestions)
                intent.putExtra("firstNumbersFromQuestions", firstNumbersFromQuestions)
                intent.putExtra("secondNumbersFromQuestions", secondNumbersFromQuestions)
                intent.putExtra("thirdNumbersFromQuestions", thirdNumbersFromQuestions)
                intent.putExtra("firstEquationSigns", firstEquationSigns)
                intent.putExtra("secondEquationSigns", secondEquationSigns)
                intent.putExtra("correctnessOfAnswers", correctnessOfAnswers)
                intent.putExtra("correctAnswersArray", correctAnswersArray)
                intent.putExtra("answersArray", answersArray)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            // Update question counter and change equation also append answer
            else {
                questionCounterValueBuffer = questionCounterValue + 1
                bufferQuestionCounter = "$questionCounterValueBuffer/$numberOfQuestions"
                questionCounter.text = bufferQuestionCounter

                firstNum = numbersFromQuestions[0][questionCounterValue]
                secondNum = numbersFromQuestions[1][questionCounterValue]
                thirdNum = numbersFromQuestions[2][questionCounterValue]

                firstSign = firstEquationSigns[questionCounterValue]
                secondSign = secondEquationSigns[questionCounterValue]

                bufferEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                equation.text = bufferEquation

                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()
            }

            keyboard.unClickEnterButton()
        }

        // Handle the back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@MixedActivity, PracticeActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@MixedActivity,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}