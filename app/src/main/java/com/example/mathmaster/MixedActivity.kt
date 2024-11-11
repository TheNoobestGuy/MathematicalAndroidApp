package com.example.mathmaster

import com.example.mathmaster.customviews.Keyboard
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.random.Random
import java.util.ArrayDeque

class MixedActivity : ComponentActivity() {

    // Variables
    private var waitCounterValue = 3
    private val handler = Handler(Looper.getMainLooper())

    // Waiter for counting down before start of practice
    private val countBeforeStart = object : Runnable {
        override fun run() {
            // Get content
            val timeCounter: TextView = findViewById<TextView>(R.id.TimeCounter)
            val questionCounter: TextView = findViewById<TextView>(R.id.QuestionCounter)
            val equation: TextView = findViewById<TextView>(R.id.Equation)
            val keyboard: Keyboard = findViewById<Keyboard>(R.id.Keyboard)
            val exitButton: Button = findViewById<Button>(R.id.Exit)

            // Disable visibility of content
            questionCounter.visibility = View.INVISIBLE
            equation.visibility = View.INVISIBLE
            keyboard.visibility = View.INVISIBLE
            exitButton.visibility = View.INVISIBLE

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
                exitButton.visibility = View.VISIBLE

                handler.removeCallbacks(this)
            }

            waitCounterValue--
        }
    }

    // Change page with intent after click
    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    private fun hasDecimalPart(value: Float): Boolean {
        return value != value.toInt().toFloat()
    }

    private fun calculateEquation(equation: String): Int {
        val bufforEquation = equation.split(' ')

        // Stacks
        val nums = ArrayDeque<Int>()
        val signs = ArrayDeque<String>()

        // Get nums and signs from equation
        for (i in bufforEquation.indices) {
            if (bufforEquation[i] == "+" || bufforEquation[i] == "-") {
                signs.push(bufforEquation[i])
            }
            else if (bufforEquation[i] == "*" || bufforEquation[i] == "/") {
                signs.push(bufforEquation[i])
            }
            else {
                nums.push(bufforEquation[i].toInt())
            }
        }

        // Evaluate equation
        var result: Int = nums.removeLast()!!
        var bufforNum: Int

        while(signs.isNotEmpty()) {
            if (nums.isNotEmpty()) {
                if (signs.peekLast() == "+") {
                    bufforNum = nums.removeLast()!!
                    signs.removeLast()
                    if (signs.isNotEmpty()) {
                        if (signs.peekLast() == "*") {
                            result += bufforNum * nums.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                        else if (signs.peekLast() == "/") {
                            result += bufforNum / nums.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                    }
                    result += bufforNum
                }
                else if (signs.peekLast() == "-") {
                    bufforNum = nums.removeLast()!!
                    signs.removeLast()
                    if (signs.isNotEmpty()) {
                        if (signs.peekLast() == "*") {
                            result -= bufforNum * nums.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                        else if (signs.peekLast() == "/") {
                            result -= bufforNum / nums.removeLast()!!
                            signs.removeLast()
                            continue
                        }
                    }
                    result -= bufforNum
                }
                else if (signs.peekLast() == "/") {
                    result /= nums.removeLast()!!
                    signs.removeLast()
                }
                else {
                    result *= nums.removeLast()!!
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
        val questionCounter: TextView = findViewById<TextView>(R.id.QuestionCounter)
        val equation: TextView = findViewById<TextView>(R.id.Equation)
        val keyboard: Keyboard = findViewById<Keyboard>(R.id.Keyboard)

        // Menu buttons
        val exitButton: Button = findViewById<Button>(R.id.Exit)

        // Count before start of game
        handler.post(countBeforeStart)

        // Get height of a screen
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        val equationTopMargin: Int = (screenHeight * 0.15).toInt()

        // Adjust screen height to logo and first button from menu
        val layoutParamsEquation = equation.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsEquation.topMargin = equationTopMargin
        equation.layoutParams = layoutParamsEquation

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

                    // Restrict second divisioning so it doesnt go float
                    if (k == 1) {
                        if (firstEquationSigns[i] == '*') {
                            val bufforEquation = numbersFromQuestions[0].last() * numbersFromQuestions[1].last()
                            firstNumber = bufforEquation
                        }
                        else if (firstEquationSigns[i] == '/') {
                            val bufforEquation = numbersFromQuestions[0].last() / numbersFromQuestions[1].last()
                            firstNumber = bufforEquation
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
                            val bufforNum = numbersFromQuestions[0].last()
                            var partValue: Int
                            var counter = 1

                            do {
                                partValue = numbersFromQuestions[1].last() * counter
                                if (partValue <= bufforNum) {
                                    listOfMultipliers.add(counter)
                                }
                                counter++
                            } while (partValue <= bufforNum)

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
                        var bufforEquation: Int = 0

                        if (firstEquationSigns[i] == '*') {
                            bufforEquation = numbersFromQuestions[0].last() * numbersFromQuestions[1].last()
                        }
                        else if (firstEquationSigns[i] == '/') {
                            bufforEquation = numbersFromQuestions[0].last() / numbersFromQuestions[1].last()
                        }

                        var partValue: Int
                        var counter = 1

                        do {
                            partValue = bufforEquation * counter
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
                            var bufforEquation: Int = 0

                            if (firstEquationSigns[i] == '*') {
                                bufforEquation = firstNum * secondNum
                            }
                            else if (firstEquationSigns[i] == '/') {
                                bufforEquation = firstNum / secondNum
                            }
                            else if (firstEquationSigns[i] == '-') {
                                bufforEquation = firstNum - secondNum
                            }
                            else {
                                bufforEquation = firstNum + secondNum
                            }

                            limiter = if (bufforEquation > 99) 99 else bufforEquation
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
        var questionCounterValueBuffor = questionCounterValue + 1
        var bufforQuestionCounter = "$questionCounterValueBuffor/$numberOfQuestions"
        questionCounter.text = bufforQuestionCounter

        var firstNum = numbersFromQuestions[0][questionCounterValue]
        var secondNum = numbersFromQuestions[1][questionCounterValue]
        var thirdNum = numbersFromQuestions[2][questionCounterValue]

        var firstSign = firstEquationSigns[questionCounterValue]
        var secondSign = secondEquationSigns[questionCounterValue]

        var bufforEquation: String = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
        equation.text = bufforEquation

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(exitButton, clickedButtonStyle, PracticeActivity())

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

                bufforEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                correctAnswersArray[questionCounterValue-1] = calculateEquation(bufforEquation)

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
                val firstNumsFromQuestions = IntArray(numberOfQuestions)
                val secondNumsFromQuestions = IntArray(numberOfQuestions)
                val thirdNumsFromQuestions = IntArray(numberOfQuestions)

                for (i in numbersFromQuestions[0].indices)
                {
                    firstNumsFromQuestions[i] = numbersFromQuestions[0][i]
                }

                for (i in numbersFromQuestions[0].indices)
                {
                    secondNumsFromQuestions[i] = numbersFromQuestions[1][i]
                }

                for (i in numbersFromQuestions[0].indices)
                {
                    thirdNumsFromQuestions[i] = numbersFromQuestions[2][i]
                }

                // Pass informations that are needed for end statistic overview
                intent.putExtra("numberOfQuestions", numberOfQuestions)
                intent.putExtra("firstNumsFromQuestions", firstNumsFromQuestions)
                intent.putExtra("secondNumsFromQuestions", secondNumsFromQuestions)
                intent.putExtra("thirdNumsFromQuestions", thirdNumsFromQuestions)
                intent.putExtra("firstEquationSigns", firstEquationSigns)
                intent.putExtra("secondEquationSigns", secondEquationSigns)
                intent.putExtra("correctnessOfAnswers", correctnessOfAnswers)
                intent.putExtra("correctAnswersArray", correctAnswersArray)
                intent.putExtra("answersArray", answersArray)

                startActivity(intent)
            }
            // Update question counter and change equation also append answer
            else {
                questionCounterValueBuffor = questionCounterValue + 1
                bufforQuestionCounter = "$questionCounterValueBuffor/$numberOfQuestions"
                questionCounter.text = bufforQuestionCounter

                firstNum = numbersFromQuestions[0][questionCounterValue]
                secondNum = numbersFromQuestions[1][questionCounterValue]
                thirdNum = numbersFromQuestions[2][questionCounterValue]

                firstSign = firstEquationSigns[questionCounterValue]
                secondSign = secondEquationSigns[questionCounterValue]

                bufforEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                equation.text = bufforEquation

                firstNum = numbersFromQuestions[0][questionCounterValue-1]
                secondNum = numbersFromQuestions[1][questionCounterValue-1]
                thirdNum = numbersFromQuestions[2][questionCounterValue-1]

                firstSign = firstEquationSigns[questionCounterValue-1]
                secondSign = secondEquationSigns[questionCounterValue-1]

                bufforEquation = "$firstNum $firstSign $secondNum $secondSign $thirdNum"
                correctAnswersArray[questionCounterValue-1] = calculateEquation(bufforEquation)

                answersArray[questionCounterValue-1] = keyboard.getTextField()
                keyboard.resetTextField()
            }

            keyboard.unClickEnterButton()
        }
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}