package com.example.mathmaster.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import kotlin.math.*
import com.example.mathmaster.R
import kotlinx.coroutines.*

data class PairEquation<Double, Int> (var first: Double, var second: Int)

class AdvancedKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

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

    // Functions of calculator
    private var specialFunctionInUse = false
    private val functionsButtons: Array<Button>
    private val logaritmButton: Button
    private val naturalLogaritmButton: Button
    private val sinButton: Button
    private val cosButton: Button
    private val tgButton: Button

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

        // Function operators
        logaritmButton = findViewById<Button>(R.id.Logaritm)
        naturalLogaritmButton = findViewById<Button>(R.id.NLogaritm)
        sinButton = findViewById<Button>(R.id.Sin)
        cosButton = findViewById<Button>(R.id.Cos)
        tgButton = findViewById<Button>(R.id.Tan)

        functionsButtons = arrayOf(
            logaritmButton,
            naturalLogaritmButton,
            sinButton,
            cosButton,
            tgButton
        )

        // Brackets
        openBracketButton = findViewById<Button>(R.id.FirstBracket)
        closeBracketButton = findViewById<Button>(R.id.SecondBracket)
    }

    private fun convertNumber(number: Double, power: Int, divide: Boolean): Double {
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

        if (divide) {
            val range = length - 1  downTo 0
            buffor++
            for (i in range) {
                outputNumber += convertNumber(list[i], buffor, true)
                buffor--
            }
        }
        else {
            list.forEach { num ->
                outputNumber += convertNumber(num, buffor, false)
                buffor--
            }
        }

        return outputNumber
    }

    private fun transformEquation(equation: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()

        // Equation validation
        var intConverter: Int = 0
        var lastChar: Char = '?'
        var numberBase: Double = 0.0
        val openedBrackets = ArrayDeque<Int>()
        val bracketsStack = ArrayDeque<Int>()
        val numberBuffor: MutableList<Double> = mutableListOf()

        // Functions
        var sin: Boolean = false
        var cos: Boolean = false
        var tg: Boolean = false

        var logaritm: Boolean = false
        var lg: Boolean = false
        var ln: Boolean = false

        equation.forEach { element ->
            if (element.isDigit()) {
                // Add digit to buffor
                numberBuffor.add((element.code - 48).toDouble())
                intConverter++

                // Power to
                if (lastChar == '^') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
                    transformedEquation.removeLast()

                    var buffor: Double = numberBase
                    for (i in 1 until outputNumber.toInt()) {
                        buffor *= numberBase
                    }

                    openedBrackets.addLast(1)
                    transformedEquation.add('(')
                    transformedEquation.add(buffor)
                }
                // Decimal number
                else if (lastChar == ',') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, true)
                    transformedEquation.removeLast()

                    val decimalNumber: Double = numberBase + outputNumber

                    openedBrackets.addLast(1)
                    transformedEquation.add('(')
                    transformedEquation.add(decimalNumber)
                }

                // Functions
                if (sin || cos || tg || lg || ln) {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
                    if (numberBuffor.size > 1) {
                        transformedEquation.removeLast()
                    }

                    var number: Double = 0.0
                    if (sin) {
                        number = sin(outputNumber)
                    }
                    else if (cos) {
                        number = cos(outputNumber)
                    }
                    else if (tg) {
                        number = tan(outputNumber)
                    }
                    else if (lg) {
                        number = log(outputNumber, 10.0)
                    }
                    else if (ln) {
                        number = ln(outputNumber)
                    }

                    openedBrackets.addLast(1)
                    transformedEquation.add('(')
                    transformedEquation.add(number)
                }
            }
            else {
                // Clear buffors of constant numbers that has been added in number handler
                if (lastChar == '^' ||  lastChar == ',') {
                    numberBuffor.clear()
                }

                // Functions
                if (sin || cos || tg || lg || ln) {
                    if (element != '(') {
                        numberBuffor.clear()
                    }
                }

                // Preparing brackets for multiplication and division
                if (element == '×' || element == '/') {
                    if (openedBrackets.isEmpty()) {
                        transformedEquation.add('(')
                        openedBrackets.addLast(1)
                    }
                }

                // Append number that is in buffor
                if (numberBuffor.isNotEmpty()) {
                    if (lastChar != '^' && lastChar != ',') {
                        val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
                        transformedEquation.add(outputNumber)

                        if (element == '^' || element == ',') {
                            numberBase = outputNumber
                        }
                    }
                }

                // Handle subtracting and adding
                if (element == '+' || element == '-') {
                    while (openedBrackets.isNotEmpty()) {
                        transformedEquation.add(')')
                        openedBrackets.removeLast()
                    }
                }

                // Handle brackets
                var closeBracketAfterFunction: Boolean = false
                if (element == '(') {
                    if (!sin && !cos && !tg && !lg && !ln) {
                        bracketsStack.addLast(1)
                    }
                }
                else if (element == ')') {
                    if (sin || cos || tg || lg || ln) {
                        sin = false
                        cos = false
                        tg = false
                        lg = false
                        ln = false
                        logaritm = false
                        closeBracketAfterFunction = true
                    }
                    else {
                        if (bracketsStack.isNotEmpty()) {
                            bracketsStack.removeLast()
                        }
                    }
                }

                // Functions
                if (!sin && !cos && !tg && !lg && !ln) {
                    if (logaritm) {
                        when (element) {
                            'g' -> lg = true
                            'n' -> ln = true
                        }
                    }

                    when (element) {
                        's' -> sin = true
                        'c' -> cos = true
                        't' -> tg = true
                        'l' -> logaritm = true
                    }

                    if (sin || cos || tg || logaritm) {
                        if (lastChar == '^' || lastChar == ',') {
                            transformedEquation.add('×')
                        }

                        // Append number that is before function as multiplication
                        if (numberBuffor.isNotEmpty()) {
                            val outputNumber: Double = transformedEquation.removeLast() as Double
                            transformedEquation.add(outputNumber)
                            transformedEquation.add('×')
                        }
                    }
                }

                // Append operators and brackets
                if (element != '^' && element != ',') {
                    if (!sin && !cos && !tg && !logaritm && !closeBracketAfterFunction) {
                        transformedEquation.add(element)
                    }
                }

                // Save last char for later calculations
                if (element == '+' || element == '-' || element == '/' || element == '×') {
                    lastChar = element
                }
                else if (element == '^' || element == ',') {
                    lastChar = element
                }
                else if (element == '(' || element == ')') {
                    lastChar = element
                }

                intConverter = 0
                numberBuffor.clear()
            }
        }
        // Add everything that lasts in buffors
        if (numberBuffor.isNotEmpty() && (lastChar !=  '^' && lastChar != ',')) {
            if (!sin && !cos && !tg && !logaritm) {
                val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
                transformedEquation.add(outputNumber)
            }
        }

        while (openedBrackets.isNotEmpty()) {
            transformedEquation.add(')')
            openedBrackets.removeLast()
        }

        while(bracketsStack.isNotEmpty()) {
            transformedEquation.add(')')
            bracketsStack.removeLast()
        }

        return transformedEquation
    }

    private fun calculate(equation: MutableList<Any>, index: Int): PairEquation<Double, Int> {
        var equationSign: Char = 'E'
        var result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index

        while (iterator < equation.size) {
            when (equation[iterator]) {
                is Char -> {
                    if (equation[iterator] == '(') {
                        val equationBuffor = calculate(equation, iterator + 1)
                        when (equationSign) {
                            '+' -> result.first += equationBuffor.first
                            '-' -> result.first -= equationBuffor.first
                            '×' -> result.first *= equationBuffor.first
                            '/' -> result.first /= equationBuffor.first
                            'E' -> result.first = equationBuffor.first
                        }
                        iterator = equationBuffor.second
                    }
                    else if (equation[iterator] == ')') {
                        return result
                    }
                    else {
                        equationSign = equation[iterator] as Char
                    }
                }
                is Double -> {
                    when (equationSign) {
                        '+' -> result.first += equation[iterator] as Double
                        '-' -> result.first -= equation[iterator] as Double
                        '×' -> result.first *= equation[iterator] as Double
                        '/' -> result.first /= equation[iterator] as Double
                        'E' -> result.first = equation[iterator] as Double
                    }
                }
            }

            result.second = ++iterator
        }

        return result
    }

    private fun checkIsItDouble(number: Double): Boolean {
        return number % 1 != 0.0
    }

    private fun resultOfCalculate(textView: TextView, resultTextView: TextView) {
        // Calculation
        val equation = transformEquation(textView.text.toString())
        val resultOfCalculations = calculate(equation, 0)

        resultTextView.append("= ")
        if (checkIsItDouble(resultOfCalculations.first)) {
            resultTextView.text = resultOfCalculations.first.toString()
        }
        else {
            resultTextView.text = resultOfCalculations.first.toInt().toString()
        }
    }

    fun enterButtonClick() {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (specialFunctionInUse) {
                specialFunctionInUse = false
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun numberButtonClick(textView: TextView, resultTextView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                var addedNumber = false

                if (!specialFunctionInUse) {
                    if (textView.text.isNotEmpty()) {
                        if (textView.text.last() != ')') {
                            textView.append(i.toString())
                            addedNumber = true
                        }
                    } else {
                        textView.append(i.toString())
                        addedNumber = true
                    }
                }
                else {
                    val currentText = textView.text.toString().dropLast(1)
                    textView.text = currentText
                    textView.append(i.toString())
                    textView.append(")")
                    addedNumber = true
                }

                if (addedNumber) {
                    resultOfCalculate(textView, resultTextView)
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

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')') {
                        powerUsed = false
                        commaUsed = false
                        textView.append(basicCalcButtons[i].text)

                        if (specialFunctionInUse) {
                            specialFunctionInUse = false
                        }
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

            if (!powerUsed && !specialFunctionInUse) {
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

            if (!commaUsed && !specialFunctionInUse) {
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

    fun clearButtonClick(textView: TextView, resultTextView: TextView) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            specialFunctionInUse = false
            bracketsCounter = 0
            powerUsed = false
            commaUsed = false
            textView.text = ""
            resultTextView.text = ""

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
                if (!textView.text.last().isDigit() && !specialFunctionInUse) {
                    if (textView.text.last() != '^' && textView.text.last() != ',') {
                        if(textView.text.last() != ')') {
                            textView.append(openBracketButton.text)
                            bracketsCounter++
                        }
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

            if (textView.text.isNotEmpty() && !specialFunctionInUse) {
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

    fun deleteButtonClick(textView: TextView, resultTextView: TextView) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if(!specialFunctionInUse) {
                    if (textView.text.last() == '^') {
                        powerUsed = false
                    }
                    else if (textView.text.last() == ',') {
                        commaUsed = false
                    }

                    var currentText = textView.text.toString()

                    // Check for functions
                    if (currentText.last() == ')') {
                        val range = currentText.length-1 downTo 0
                        var letter: Boolean = false
                        for (i in range) {
                            if (currentText[i].isLetter()) {
                                letter = true
                            }

                            when (currentText[i]) {
                                '+' -> break
                                '-' -> break
                                '/' -> break
                                '×' -> break
                            }
                        }
                        if (letter) {
                            currentText = currentText.dropLast(1)
                            if (currentText.last().isDigit()) {
                                currentText = currentText.dropLast(1)
                                currentText += ")"
                                specialFunctionInUse = true
                            }
                            else {
                                bracketsCounter++
                            }
                        }
                        else
                        {
                            bracketsCounter++
                            currentText = currentText.dropLast(1)
                        }
                    }
                    else {
                        currentText = currentText.dropLast(1)
                    }

                    textView.text = currentText
                }
                else {
                    var currentText = textView.text.toString().dropLast(2)

                    if (!currentText.last().isDigit() && currentText.last() != '(') {
                        while (currentText.isNotEmpty()) {
                            when (currentText.last()) {
                                '+' -> break
                                '-' -> break
                                '/' -> break
                                '×' -> break
                            }

                            if (currentText.last().isDigit()) {
                                break
                            }

                            currentText = currentText.substring(0, currentText.length - 1)
                        }

                        specialFunctionInUse = false
                        textView.text = currentText
                    }
                    else {
                        textView.text = currentText
                        textView.append(")")
                    }
                }

                resultOfCalculate(textView, resultTextView)
                if (textView.text.isEmpty()) {
                    resultTextView.text = ""
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun functionButtonClick(textView: TextView) {
        functionsButtons.forEach { button ->
            button.setOnClickListener {
                button.setBackgroundResource(clickedButtonStyle)

                if (textView.text.isEmpty()) {
                    specialFunctionInUse = true
                    val buffor = button.text.toString() + "()"
                    textView.append(buffor)
                }
                else if (textView.text.last() != ')' && textView.text.last() != '(') {
                    if (textView.text.last() != '^' && textView.text.last() != ',') {
                        specialFunctionInUse = true
                        val buffor = button.text.toString() + "()"
                        textView.append(buffor)
                    }
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    button.setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

}