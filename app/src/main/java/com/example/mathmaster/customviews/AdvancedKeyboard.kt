package com.example.mathmaster.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import com.example.mathmaster.R
import kotlinx.coroutines.*

data class PairEquation<Double, Int> (var first: Double, var second: Int)

class AdvancedKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var showedResult: Boolean = false
    private var result: Any = 0

    // Buttons styles
    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    // Main buttons
    private val enterButton: Button
    private val deleteButton: Button
    private val clearButton: Button

    // Number buttons
    private val buttons: Array<Button>
    private val oneButton: Button
    private val twoButton: Button
    private val threeButton: Button
    private val fourButton: Button
    private val fiveButton: Button
    private val sixButton: Button
    private val sevenButton: Button
    private val eightButton: Button
    private val nineButton: Button
    private val zeroButton: Button

    // Basic calculator buttons
    private val basicCalcButtons: Array<Button>
    private val addButton: Button
    private val subtractButton: Button

    private val multiplyButton: Button
    private val divideButton: Button

    private val procentButton: Button
    private val factorialButton: Button

    // Special calculator buttons
    private val powerButton: Button
    private val commaButton: Button

    private var powerUsed: Boolean = false
    private var commaUsed: Boolean = false

    // Brackets
    private var bracketsCounter: Int = 0
    private val openBracketButton: Button
    private val closeBracketButton: Button

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.advancedcalculator_layout, this, true)

        // Main buttons
        enterButton = findViewById<Button>(R.id.Equal)
        deleteButton = findViewById<Button>(R.id.Delete)
        clearButton = findViewById<Button>(R.id.Clear)

        // Numbers
        zeroButton = findViewById<Button>(R.id.Zero)
        oneButton = findViewById<Button>(R.id.One)
        twoButton = findViewById<Button>(R.id.Two)
        threeButton = findViewById<Button>(R.id.Three)
        fourButton = findViewById<Button>(R.id.Four)
        fiveButton = findViewById<Button>(R.id.Five)
        sixButton = findViewById<Button>(R.id.Six)
        sevenButton = findViewById<Button>(R.id.Seven)
        eightButton = findViewById<Button>(R.id.Eight)
        nineButton = findViewById<Button>(R.id.Nine)

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
        buttons = arrayOf(
            zeroButton,
            oneButton,
            twoButton,
            threeButton,
            fourButton,
            fiveButton,
            sixButton,
            sevenButton,
            eightButton,
            nineButton
        )

        // Basic operations buttons
        addButton = findViewById<Button>(R.id.Plus)
        subtractButton = findViewById<Button>(R.id.Minus)
        multiplyButton = findViewById<Button>(R.id.Multiply)
        divideButton = findViewById<Button>(R.id.Divide)
        procentButton = findViewById<Button>(R.id.Procent)
        factorialButton = findViewById<Button>(R.id.Factorial)

        basicCalcButtons = arrayOf(
            addButton,
            subtractButton,
            multiplyButton,
            divideButton,
            procentButton,
            factorialButton,
        )

        // Special operations buttons
        powerButton = findViewById<Button>(R.id.PowerTo)
        commaButton = findViewById<Button>(R.id.Comma)

        // Brackets
        openBracketButton = findViewById<Button>(R.id.FirstBracket)
        closeBracketButton = findViewById<Button>(R.id.SecondBracket)
    }

    private fun convertNumber(number:Double, power: Int, divide: Boolean): Double {
        var result = number

        for (i in 1 until power) {
            if (divide) {
                result /= 10
            }
            else {
                result *= 10
            }
        }

        return result
    }

    private fun calculateNumber(list: MutableList<Double>, length: Int, divide: Boolean): Double {
        var buffor = length
        var outputNumber: Double = 0.0
        list.forEach { num ->
            outputNumber += convertNumber(num, buffor, divide)
            buffor--
        }

        return outputNumber
    }

    private fun transformEquation(equation: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()
        transformedEquation.add('+')

        var intConverter: Int = 0
        var lastChar: Char = '?'
        var doubleBuffor: Double = 0.0
        var openedBrackets = false
        val bracketsStack = ArrayDeque<Int>()
        val numberBuffor: MutableList<Double> = mutableListOf()

        equation.forEach { element ->
            if (element.isDigit()) {
                numberBuffor.add((element.code - 48).toDouble())
                intConverter++
            }
            else {
                if (lastChar == '^') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)

                    for (i in 1 until outputNumber.toInt()) {
                        transformedEquation.add('x')
                        transformedEquation.add(numberBuffor.last())
                    }

                    transformedEquation.add(')')
                }

                if (lastChar == ',') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, true)
                    doubleBuffor += outputNumber
                    transformedEquation.add(doubleBuffor)
                }

                if (element == 'X' || element == '/') {
                    if (!openedBrackets) {
                        transformedEquation.add('(')
                    }
                    openedBrackets = true
                }
                else if (element == '^') {
                    transformedEquation.add('(')
                }

                if (numberBuffor.isNotEmpty()) {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)

                    if (element != ',') {
                        transformedEquation.add(outputNumber)
                    }
                    else {
                        doubleBuffor = outputNumber
                    }
                }

                if (element == '+' || element == '-') {
                    if (openedBrackets) {
                        transformedEquation.add(')')
                    }

                    openedBrackets = false
                }

                if (element == '(') {
                    bracketsStack.addLast(1)
                }
                else if (element == ')') {
                    bracketsStack.removeLast()
                }

                transformedEquation.add(element)

                intConverter = 0
                lastChar = element
                numberBuffor.clear()
            }
        }
        if (numberBuffor.isNotEmpty()) {
            val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
            transformedEquation.add(outputNumber)
        }

        if (openedBrackets) {
            transformedEquation.add(')')
        }

        while(bracketsStack.isNotEmpty()) {
            transformedEquation.add(')')
            bracketsStack.removeLast()
        }
        return transformedEquation
    }

    private fun calculate(equation: MutableList<Any>, index: Int, size: Int): PairEquation<Double, Int> {
        var equationSign: Char = 'E'
        var result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index + 1

        for (element in equation) {
            if (iterator <= result.second) {
                iterator++
                continue
            }

            result.second++
            when (element) {
                is Char -> {
                    if (element == '(') {
                        val slice = iterator - (size - equation.size)
                        val subArray = equation.drop(slice).toMutableList()
                        val equationBuffor = calculate(subArray, iterator, size)
                        when (equationSign) {
                            '+' -> result.first += equationBuffor.first
                            '-' -> result.first -= equationBuffor.first
                            'X' -> result.first *= equationBuffor.first
                            '/' -> result.first /= equationBuffor.first
                            'E' -> result.first = equationBuffor.first
                        }
                        result.second = equationBuffor.second
                    }
                    else if (element == ')') {
                        return result
                    }
                    else {
                        equationSign = element
                    }
                }
                is Double -> {
                    when (equationSign) {
                        '+' -> result.first += element
                        '-' -> result.first -= element
                        'X' -> result.first *= element
                        '/' -> result.first /= element
                        'E' -> result.first = element
                    }
                }
            }

            iterator++
        }

        return result
    }

    private fun checkIsItDouble(number: Double): Boolean {
        return number % 1 != 0.0
    }

    fun equalityButtonClick(textView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && !showedResult) {
                showedResult = true
                bracketsCounter = 0
                powerUsed = false
                commaUsed = false

                // Calculation
                val equation = transformEquation(textView.text.toString())
                val resultOfCalculations = calculate(equation, 0, equation.size)

                textView.append("=")
                if (checkIsItDouble(resultOfCalculations.first)) {
                    result = resultOfCalculations.first
                    textView.append(result.toString())
                }
                else {
                    result = resultOfCalculations.first.toInt()
                    textView.append(result.toString())
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun numberButtonClick(textView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                // Delete result if showed
                if (showedResult) {
                    textView.text = ""
                    showedResult = false
                }

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != ')') {
                        textView.append(i.toString())
                    }
                }
                else {
                    textView.append(i.toString())
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    buttons[i].setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

    fun basicCalcButtonClick(textView: TextView) {
        for (i in basicCalcButtons.indices) {
            basicCalcButtons[i].setOnClickListener {
                basicCalcButtons[i].setBackgroundResource(clickedButtonStyle)

                // Edit result if showed
                if (showedResult) {
                    powerUsed = false
                    commaUsed = false
                    textView.text = result.toString()
                    showedResult = false
                    result = 0
                }


                if (textView.text.isNotEmpty() && !showedResult) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')') {
                        powerUsed = false
                        commaUsed = false
                        textView.append(basicCalcButtons[i].text)
                    }
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    basicCalcButtons[i].setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

    fun powerButtonClick(textView: TextView) {
        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(clickedButtonStyle)

            if (!powerUsed) {
                if (textView.text.isNotEmpty()) {
                    if(textView.text.last().isDigit()) {
                        textView.append(powerButton.text.toString())
                        powerUsed = true
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                powerButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun commaButtonClick(textView: TextView) {
        commaButton.setOnClickListener {
            commaButton.setBackgroundResource(clickedButtonStyle)

            if (!commaUsed) {
                if (textView.text.isNotEmpty()) {
                    if(textView.text.last().isDigit()) {
                        textView.append(commaButton.text.toString())
                        commaUsed = true
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                commaButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun clearButtonClick(textView: TextView) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            result = 0
            showedResult = false
            bracketsCounter = 0
            powerUsed = false
            commaUsed = false
            textView.text = ""

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                clearButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun openBracketButtonClick(textView: TextView) {
        openBracketButton.setOnClickListener {
            openBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (!textView.text.last().isDigit()) {
                    if(textView.text.last() != ')') {
                        textView.append(openBracketButton.text)
                        bracketsCounter++
                    }
                }
            }
            else {
                textView.append(openBracketButton.text)
                bracketsCounter++
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                openBracketButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun closeBracketButtonClick(textView: TextView) {
        closeBracketButton.setOnClickListener {
            closeBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')') {
                    if (bracketsCounter > 0) {
                        textView.append(closeBracketButton.text)
                        bracketsCounter--
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                closeBracketButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun deleteButtonClick(textView: TextView) {
        deleteButton.setOnClickListener {
            if (textView.text.isNotEmpty()) {
                deleteButton.setBackgroundResource(clickedButtonStyle)

                if (textView.text.last() == '^') {
                    powerUsed = false
                }
                else if (textView.text.last() == ',') {
                    commaUsed = false
                }

                val currentText = textView.text.toString().dropLast(1)
                textView.text = currentText

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    deleteButton.setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }
}