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
import kotlin.random.Random
import java.util.ArrayDeque

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

    private fun hasDecimalPart(value: Float): Boolean {
        return value != value.toInt().toFloat()
    }

    private fun calculateEquation(equation: String): Int {
        val bufferEquation = equation.split(' ')

        // Stacks
        val numbers = ArrayDeque<Int>()
        val signs = ArrayDeque<String>()

        // Get numbers and signs from equation
        for (i in bufferEquation.indices) {
            if (bufferEquation[i] == "+" || bufferEquation[i] == "-") {
                signs.push(bufferEquation[i])
            }
            else if (bufferEquation[i] == "*" || bufferEquation[i] == "/") {
                signs.push(bufferEquation[i])
            }
            else {
                numbers.push(bufferEquation[i].toInt())
            }
        }

        // Evaluate equation
        var result: Int = numbers.removeLast()!!
        var bufferNum: Int

        while(signs.isNotEmpty()) {
            if (numbers.isNotEmpty()) {
                if (signs.peekLast() == "+") {
                    bufferNum = numbers.removeLast()!!
                    signs.removeLast()
                    if (signs.isNotEmpty()) {
                        if (signs.peekLast() == "*") {
                            result += bufferNum * numbers.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                        else if (signs.peekLast() == "/") {
                            result += bufferNum / numbers.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                    }
                    result += bufferNum
                }
                else if (signs.peekLast() == "-") {
                    bufferNum = numbers.removeLast()!!
                    signs.removeLast()
                    if (signs.isNotEmpty()) {
                        if (signs.peekLast() == "*") {
                            result -= bufferNum * numbers.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                        else if (signs.peekLast() == "/") {
                            result -= bufferNum / numbers.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                    }
                    result -= bufferNum
                }
                else if (signs.peekLast() == "/") {
                    result /= numbers.removeLast()!!
                    signs.removeLast()
                }
                else {
                    result *= numbers.removeLast()!!
                    signs.removeLast()
                }
            }
        }

        return result
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
        val twoNumbersRange = 8..99
        val numbersFromQuestions: MutableList<MutableList<Int>> = mutableListOf(mutableListOf(), mutableListOf(), mutableListOf())
        val correctAnswersArray = IntArray(numberOfQuestions)
        val equationSignsArray = charArrayOf('+', '-', '/', '*')
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

            // Numbers
            var firstNumber: Int
            for (k in 0..1) {
                val equationSign = if (k == 0) firstEquationSigns[i] else secondEquationSigns[i]

                if (equationSign == '/') {
                    var passNumber = false
                    firstNumber = if (k == 0)  twoNumbersRange.random() else numbersFromQuestions[1][i]

                    // Restrict second division so it doesn't go float
                    if (k == 1) {
                        if (firstEquationSigns[i] == '*') {
                            val bufferEquation = numbersFromQuestions[0].last() * numbersFromQuestions[1].last()
                            firstNumber = bufferEquation
                        }
                        else if (firstEquationSigns[i] == '/') {
                            val bufferEquation = numbersFromQuestions[0].last() / numbersFromQuestions[1].last()
                            firstNumber = bufferEquation
                        }
                    }

                    // Append divider
                    while (!passNumber) {
                        val listOfDividers: MutableList<Int> = mutableListOf()

                        var counter = 1
                        var run = true

                        // Find dividers of picked number
                        while (run) {
                            val divider: Float = firstNumber.toFloat() / counter.toFloat()

                            if (!hasDecimalPart(divider)) {
                                listOfDividers.add(counter)
                            }

                            if (firstNumber / 2 <= counter) {
                                run = false
                            }

                            counter++
                        }

                        // If list of dividers contains true dividers pass numbers
                        if (listOfDividers.size >= 1) {
                            var pickRandom = Random.nextInt(listOfDividers.size)
                            var pickedNumber = listOfDividers[pickRandom]

                            if (pickedNumber == 1 && listOfDividers.size > 1) {
                                while (pickedNumber == 1) {
                                    pickRandom = Random.nextInt(listOfDividers.size)
                                    pickedNumber = listOfDividers[pickRandom]
                                }
                            }

                            if (k == 0) {
                                numbersFromQuestions[0].add(firstNumber)
                                numbersFromQuestions[1].add(pickedNumber)
                            } else {
                                numbersFromQuestions[2].add(pickedNumber)
                            }

                            passNumber = true
                        }
                    }
                }
                else if (equationSign == '*') {
                    val randomSize = Random.nextBoolean()
                    val oneNumberRangeMultiply = 2..9
                    val twoNumbersRangeMultiply = 10..99
                    firstNumber = if (k == 0)  twoNumbersRange.random() else numbersFromQuestions[1][i]

                    // Check for special conditions on second sign
                    if (k == 1) {
                        // Prevent multiplication from being greater than the number from which it will be subtracted
                        if (firstEquationSigns[i] == '-') {
                            val listOfMultipliers: MutableList<Int> = mutableListOf()
                            val bufferNumber = numbersFromQuestions[0].last()
                            var partValue: Int
                            var counter = 1

                            do {
                                partValue = numbersFromQuestions[1].last() * counter
                                if (partValue <= bufferNumber) {
                                    listOfMultipliers.add(counter)
                                }
                                counter++
                            } while (partValue <= bufferNumber)

                            val randomMultiplier = Random.nextInt(listOfMultipliers.size)
                            numbersFromQuestions[2].add(listOfMultipliers[randomMultiplier])
                            continue
                        }
                        // Append multiplication normally when is about to be added
                        else if (firstEquationSigns[i] == '+') {
                            if(randomSize) {
                                val randomNumberMultiply = oneNumberRangeMultiply.random()
                                numbersFromQuestions[2].add(randomNumberMultiply)
                            } else {
                                val randomNumberMultiply = twoNumbersRangeMultiply.random()
                                numbersFromQuestions[2].add(randomNumberMultiply)
                            }
                            continue
                        }

                        // Prevent result of equation to be greater than 9999
                        val listOfMultipliers: MutableList<Int> = mutableListOf()
                        var bufferEquation = 0

                        if (firstEquationSigns[i] == '*') {
                            bufferEquation = numbersFromQuestions[0].last() * numbersFromQuestions[1].last()
                        }
                        else if (firstEquationSigns[i] == '/') {
                            bufferEquation = numbersFromQuestions[0].last() / numbersFromQuestions[1].last()
                        }

                        var partValue: Int
                        var counter = 1

                        do {
                            partValue = bufferEquation * counter
                            listOfMultipliers.add(counter)
                            counter++

                            if (counter > 21) {
                                break
                            }
                        } while (partValue <= 9999)

                        val randomMultiplier = Random.nextInt(listOfMultipliers.size)
                        numbersFromQuestions[2].add(listOfMultipliers[randomMultiplier])
                        continue
                    }

                    if(randomSize) {
                        val randomNumberMultiply = oneNumberRangeMultiply.random()

                        numbersFromQuestions[0].add(firstNumber)
                        numbersFromQuestions[1].add(randomNumberMultiply)
                    } else {
                        val randomNumberMultiply = twoNumbersRangeMultiply.random()

                        numbersFromQuestions[0].add(firstNumber)
                        numbersFromQuestions[1].add(randomNumberMultiply)
                    }
                }
                else {
                    var secondNumber: Int

                    firstNumber = if (k == 0)  twoNumbersRange.random() else numbersFromQuestions[1][i]
                    val sign = if (k == 0) firstEquationSigns[i] else secondEquationSigns[i]

                    if (sign == '-') {
                        var limiter = firstNumber

                        // Prevent result of equation to go under 0
                        if (k == 1) {
                            val firstNum = numbersFromQuestions[0].last()
                            val secondNum = numbersFromQuestions[1].last()

                            val bufferEquation = if (firstEquationSigns[i] == '*') {
                                firstNum * secondNum
                            } else if (firstEquationSigns[i] == '/') {
                                firstNum / secondNum
                            } else if (firstEquationSigns[i] == '-') {
                                firstNum - secondNum
                            } else {
                                firstNum + secondNum
                            }

                            limiter = if (bufferEquation > 99) 99 else bufferEquation
                        }

                        val secondNumberRange = 1 until limiter
                        secondNumber = secondNumberRange.random()
                    } else {
                        secondNumber = twoNumbersRange.random()
                    }

                    if (k == 0) {
                        numbersFromQuestions[0].add(firstNumber)
                        numbersFromQuestions[1].add(secondNumber)
                    } else {
                        numbersFromQuestions[2].add(secondNumber)
                    }
                }
            }
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
                // Append last answer
                firstNum = numbersFromQuestions[0][questionCounterValue-1]
                secondNum = numbersFromQuestions[1][questionCounterValue-1]
                thirdNum = numbersFromQuestions[2][questionCounterValue-1]

                firstSign = firstEquationSigns[questionCounterValue-1]
                secondSign = secondEquationSigns[questionCounterValue-1]

                bufferEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                correctAnswersArray[questionCounterValue-1] = calculateEquation(bufferEquation)

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

                firstNum = numbersFromQuestions[0][questionCounterValue-1]
                secondNum = numbersFromQuestions[1][questionCounterValue-1]
                thirdNum = numbersFromQuestions[2][questionCounterValue-1]

                firstSign = firstEquationSigns[questionCounterValue-1]
                secondSign = secondEquationSigns[questionCounterValue-1]

                bufferEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                correctAnswersArray[questionCounterValue-1] = calculateEquation(bufferEquation)

                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()
            }

            keyboard.unClickEnterButton()
        }

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@MixedActivity, PracticeActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@MixedActivity,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}