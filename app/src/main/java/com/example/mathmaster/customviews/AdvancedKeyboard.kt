package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import kotlin.math.*
import com.example.mathmaster.R
import kotlinx.coroutines.*

data class PairEquation<Double, Int> (var first: Double, var second: Int)
data class Triple<Int> (var start: Int, var end: Int, var deep: Int)

class AdvancedKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val myScope = CoroutineScope(Dispatchers.Default + Job())

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

    // Special calculator buttons
    private val powerButton: Button
    private val commaButton: Button
    private val procentButton: Button
    private val factorialButton: Button
    private val numberPIButton: Button
    private val rootButton: Button

    private var powerUsed: Boolean = false
    private var commaUsed: Boolean = false

    // Functions of calculator
    private var specialFunctionLimit: Int = 0
    private var specialFunctionDeep: Int = 0
    private val specialFunctions: MutableList<Triple<Int>> = mutableListOf<Triple<Int>>()
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

        basicCalcButtons = arrayOf(
            addButton,
            subtractButton,
            multiplyButton,
            divideButton
        )

        // Special operations buttons
        powerButton = findViewById<Button>(R.id.PowerTo)
        commaButton = findViewById<Button>(R.id.Comma)
        procentButton = findViewById<Button>(R.id.Procent)
        factorialButton = findViewById<Button>(R.id.Factorial)
        numberPIButton = findViewById<Button>(R.id.PInumber)
        rootButton = findViewById<Button>(R.id.Fraction)

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
            } else {
                result *= 10
            }
        }

        return result
    }

    private fun calculateNumber(list: MutableList<Double>, length: Int, divide: Boolean): Double {
        var buffor = length
        var outputNumber: Double = 0.0

        if (divide) {
            val range = length - 1 downTo 0
            buffor++
            for (i in range) {
                outputNumber += convertNumber(list[i], buffor, true)
                buffor--
            }
        } else {
            list.forEach { num ->
                outputNumber += convertNumber(num, buffor, false)
                buffor--
            }
        }

        return outputNumber
    }

    private fun transformEquation(equation: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()

        // Equation variables
        var intConverter: Int = 0
        var lastChar: Char = '?'
        var numberBase: Double = 0.0
        val numberBuffor: MutableList<Double> = mutableListOf()

        // Equation validation
        val openedBrackets = ArrayDeque<Int>()
        val openedBracketsInput = ArrayDeque<Int>()
        val bracketsInsideFunction = ArrayDeque<Int>()
        val bracketsInsideFunctionInput = ArrayDeque<Int>()

        // Functions
        var insideFunction: Int = 0
        var whatFunction: Char = '0'

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
                    numberBuffor.clear()
                }
                // Decimal number
                else if (lastChar == ',') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, true)
                    transformedEquation.removeLast()

                    val decimalNumber: Double = numberBase + outputNumber

                    openedBrackets.addLast(1)
                    transformedEquation.add('(')
                    transformedEquation.add(decimalNumber)
                    numberBuffor.clear()
                }
            } else {
                // Clear buffors of constant numbers that has been added in number handler
                if (lastChar == '^' || lastChar == ',') {
                    numberBuffor.clear()
                }

                // Pi number
                if (element == 'π') {
                    transformedEquation.add(PI)
                }

                // Preparing brackets for multiplication and division
                if (element == '×' || element == '/') {
                    if (insideFunction > 0 && lastChar != ')') {
                        transformedEquation.add('(')
                        bracketsInsideFunction.addLast(1)
                    } else {
                        if (openedBrackets.isEmpty() && lastChar != ')') {
                            transformedEquation.add('(')
                            openedBrackets.addLast(1)
                        }
                    }
                }

                // Append number that is in buffor
                if (numberBuffor.isNotEmpty() && whatFunction == '0') {
                    if (lastChar != '^' && lastChar != ',') {
                        val outputNumber: Double =
                            calculateNumber(numberBuffor, intConverter, false)
                        transformedEquation.add(outputNumber)

                        if (element == '^' || element == ',') {
                            numberBase = outputNumber
                        }
                    }
                }

                // Handle subtracting and adding
                if (element == '+' || element == '-') {
                    if (insideFunction > 0) {
                        while (bracketsInsideFunction.isNotEmpty()) {
                            transformedEquation.add(')')
                            bracketsInsideFunction.removeLast()
                        }
                    } else {
                        while (openedBrackets.isNotEmpty()) {
                            transformedEquation.add(')')
                            openedBrackets.removeLast()
                        }
                    }
                }

                // Functions
                when (element) {
                    's' -> whatFunction = element
                    'c' -> whatFunction = element
                    't' -> whatFunction = element
                    'l' -> whatFunction = element
                }

                if (whatFunction == 'l') {
                    when (element) {
                        'g' -> whatFunction = element
                        'n' -> whatFunction = element
                    }
                }

                // Append function symbol
                if (whatFunction != '0' && element == '(') {
                    // Append number that is before function as multiplication
                    if (numberBuffor.isNotEmpty()) {
                        val outputNumber: Double = transformedEquation.removeLast() as Double
                        transformedEquation.add('(')
                        transformedEquation.add(outputNumber)
                        transformedEquation.add('×')
                        openedBrackets.addLast(1)
                    }

                    // Act as it is multiplication when some number is before function
                    if (lastChar == '^' || lastChar == ',') {
                        transformedEquation.add('×')
                    }

                    transformedEquation.add(whatFunction)
                    whatFunction = '0'
                    insideFunction++
                }

                // Handle brackets
                if (element == '(') {
                    if (insideFunction > 0) {
                        bracketsInsideFunctionInput.addLast(1)
                    } else {
                        openedBracketsInput.addLast(1)
                    }
                } else if (element == ')') {
                    if (insideFunction > 0) {
                        insideFunction--

                        if (bracketsInsideFunctionInput.isNotEmpty()) {
                            bracketsInsideFunctionInput.removeLast()

                            if (insideFunction == 0) {
                                while (bracketsInsideFunctionInput.isNotEmpty()) {
                                    transformedEquation.add(")")
                                    bracketsInsideFunctionInput.removeLast()
                                }
                            }
                        }
                    } else {
                        if (openedBracketsInput.isNotEmpty()) {
                            openedBracketsInput.removeLast()
                        }
                    }
                }

                // Append operators and get last char
                if (element == '+' || element == '-' || element == '/' || element == '×') {
                    transformedEquation.add(element)
                    lastChar = element
                } else if (element == '(' || element == ')') {
                    transformedEquation.add(element)
                    lastChar = element
                } else if (element == '^' || element == ',') {
                    lastChar = element
                }

                intConverter = 0
                if (whatFunction == '0') {
                    numberBuffor.clear()
                }
            }
        }
        // Add everything that lasts in buffors
        if (numberBuffor.isNotEmpty() && (lastChar != '^' && lastChar != ',')) {
            val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
            transformedEquation.add(outputNumber)
        }

        while (openedBrackets.isNotEmpty()) {
            transformedEquation.add(')')
            openedBrackets.removeLast()
        }

        while (openedBracketsInput.isNotEmpty()) {
            transformedEquation.add(')')
            openedBracketsInput.removeLast()
        }

        for (t in transformedEquation) {
            println("EL: " + t.toString())
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
                    } else if (equation[iterator] == ')') {
                        return result
                    } else {
                        when (equation[iterator]) {
                            '+' -> equationSign = equation[iterator] as Char
                            '-' -> equationSign = equation[iterator] as Char
                            '×' -> equationSign = equation[iterator] as Char
                            '/' -> equationSign = equation[iterator] as Char
                        }
                    }

                    val buffor = equation[iterator] as Char
                    if (buffor.isLetter()) {
                        val equationBuffor = calculate(equation, iterator + 2)
                        when (equationSign) {
                            '+' -> {
                                when (equation[iterator]) {
                                    's' -> result.first += sin(equationBuffor.first)
                                    'c' -> result.first += cos(equationBuffor.first)
                                    't' -> result.first += tan(equationBuffor.first)
                                    'g' -> result.first += log(equationBuffor.first, 10.0)
                                    'n' -> result.first += ln(equationBuffor.first)
                                }
                            }

                            '-' -> {
                                when (equation[iterator]) {
                                    's' -> result.first -= sin(equationBuffor.first)
                                    'c' -> result.first -= cos(equationBuffor.first)
                                    't' -> result.first -= tan(equationBuffor.first)
                                    'g' -> result.first -= log(equationBuffor.first, 10.0)
                                    'n' -> result.first -= ln(equationBuffor.first)
                                }
                            }

                            '×' -> {
                                when (equation[iterator]) {
                                    's' -> result.first *= sin(equationBuffor.first)
                                    'c' -> result.first *= cos(equationBuffor.first)
                                    't' -> result.first *= tan(equationBuffor.first)
                                    'g' -> result.first *= log(equationBuffor.first, 10.0)
                                    'n' -> result.first *= ln(equationBuffor.first)
                                }
                            }

                            '/' -> {
                                when (equation[iterator]) {
                                    's' -> result.first /= sin(equationBuffor.first)
                                    'c' -> result.first /= cos(equationBuffor.first)
                                    't' -> result.first /= tan(equationBuffor.first)
                                    'g' -> result.first /= log(equationBuffor.first, 10.0)
                                    'n' -> result.first /= ln(equationBuffor.first)
                                }
                            }

                            else -> {
                                when (equation[iterator]) {
                                    's' -> result.first = sin(equationBuffor.first)
                                    'c' -> result.first = cos(equationBuffor.first)
                                    't' -> result.first = tan(equationBuffor.first)
                                    'g' -> result.first = log(equationBuffor.first, 10.0)
                                    'n' -> result.first = ln(equationBuffor.first)
                                }
                            }
                        }

                        iterator = equationBuffor.second
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
            iterator++
            result.second = iterator
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
        } else {
            resultTextView.text = resultOfCalculations.first.toInt().toString()
        }
    }

    fun enterButtonClick(textView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (specialFunctionDeep > 0) {
                specialFunctionDeep--
            }

            Handler(Looper.getMainLooper()).postDelayed({
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun numberButtonClick(textView: TextView, resultTextView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                var addedNumber = false

                if (textView.text.isNotEmpty()) {
                    if (specialFunctionDeep > 0) {
                        // Check how many bracket are needed to be delete
                        var function: Triple<Int> = specialFunctions.last()
                        val range = specialFunctions.size - 1 downTo 0
                        for (i in range) {
                            if (specialFunctions[i].deep == specialFunctionDeep) {
                                function = specialFunctions[i]
                                break
                            }
                        }

                        // Update equation
                        val limit = textView.text.length - function.end

                        var counter = 0
                        while (counter < limit) {
                            textView.text = textView.text.dropLast(1)
                            counter++
                        }

                        val text = i.toString()
                        textView.append(text)

                        counter = 0
                        while (counter < limit) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        for (func in specialFunctions) {
                            if (func.deep < specialFunctionDeep) {
                                func.end += text.length
                            }
                        }
                        function.end += text.length
                        addedNumber = true
                    } else {
                        if (textView.text.last() != ')') {
                            textView.append(i.toString())
                            addedNumber = true
                        }
                    }
                } else {
                    textView.append(i.toString())
                    addedNumber = true
                }

                if (addedNumber) {
                    resultOfCalculate(textView, resultTextView)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    buttons[i].setBackgroundResource(unClickedButtonStyle)
                }, 200)
            }
        }
    }

    fun basicCalcButtonClick(textView: TextView) {
        for (i in basicCalcButtons.indices) {
            basicCalcButtons[i].setOnClickListener {
                basicCalcButtons[i].setBackgroundResource(clickedButtonStyle)

                if (textView.text.isNotEmpty()) {
                    if (specialFunctionDeep > 0) {
                        // Check how many bracket are needed to be delete
                        var function: Triple<Int> = specialFunctions.last()
                        val range = specialFunctions.size - 1 downTo 0
                        for (i in range) {
                            if (specialFunctions[i].deep == specialFunctionDeep) {
                                function = specialFunctions[i]
                                break
                            }
                        }

                        // Check how manu bracket are needed to be delete
                        var limit = textView.text.length - 1
                        var bracketCounter = 0
                        while (function.end <= limit && textView.text[limit] == ')') {
                            bracketCounter++
                            limit--
                        }

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.text = textView.text.dropLast(1)
                            counter++
                        }

                        val text = basicCalcButtons[i].text
                        textView.append(text)

                        counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        for (func in specialFunctions) {
                            if (func.deep < specialFunctionDeep) {
                                func.end += text.length
                            }
                        }
                        function.end += text.length
                    } else if (textView.text.last().isDigit() || textView.text.last() == ')') {
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
                    if (textView.text.last().isDigit()) {
                        textView.append(powerButton.text.toString())
                        powerUsed = true
                    } else if (textView.text[textView.text.length - 2].isDigit()) {
                        textView.text = textView.text.dropLast(1)
                        textView.append(powerButton.text.toString())
                        textView.append(")")
                        powerUsed = true
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun commaButtonClick(textView: TextView) {
        commaButton.setOnClickListener {
            commaButton.setBackgroundResource(clickedButtonStyle)

            if (!commaUsed) {
                if (textView.text.isNotEmpty()) {
                    if (textView.text.isNotEmpty()) {
                        if (textView.text.last().isDigit()) {
                            textView.append(commaButton.text.toString())
                            commaUsed = true
                        } else if (textView.text[textView.text.length - 2].isDigit()) {
                            textView.text = textView.text.dropLast(1)
                            textView.append(commaButton.text.toString())
                            textView.append(")")
                            commaUsed = true
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                commaButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun rootButtonClick(textView: TextView) {
        rootButton.setOnClickListener {
            rootButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.last().isDigit()) {
                if (textView.text.isNotEmpty() && textView.text.last() != '√') {
                    textView.append("√")
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                rootButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun factorialButtonClick(textView: TextView, resultTextView: TextView) {
        factorialButton.setOnClickListener {
            factorialButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit()) {
                    textView.append(factorialButton.text.toString())
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                factorialButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun numberPIButtonClick(textView: TextView, resultTextView: TextView) {
        numberPIButton.setOnClickListener {
            numberPIButton.setBackgroundResource(clickedButtonStyle)

            var addedNumber = false
            /*
            if (specialFunctionStarts.isEmpty()) {
                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != ')') {
                        textView.append("π")
                        addedNumber = true
                    }
                } else {
                    textView.append("π")
                    addedNumber = true
                }
            }

             */
            if (addedNumber) {
                resultOfCalculate(textView, resultTextView)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberPIButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun clearButtonClick(textView: TextView, resultTextView: TextView) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            specialFunctions.clear()
            specialFunctionDeep = 0
            bracketsCounter = 0
            powerUsed = false
            commaUsed = false
            textView.text = ""
            resultTextView.text = ""

            Handler(Looper.getMainLooper()).postDelayed({
                clearButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun openBracketButtonClick(textView: TextView) {
        openBracketButton.setOnClickListener {
            openBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (!textView.text.last().isDigit()) {
                    if (textView.text.last() != '^' && textView.text.last() != ',') {
                        if (textView.text.last() != ')') {
                            textView.append(openBracketButton.text)
                            bracketsCounter++
                        }
                    }
                } else if (!textView.text[textView.text.length - 2].isDigit()) {
                    if (textView.text[textView.text.length - 2] != '^' && textView.text[textView.text.length - 2] != ',') {
                        if (textView.text[textView.text.length - 2] != ')') {
                            textView.text = textView.text.dropLast(1)
                            textView.append(openBracketButton.text)
                            textView.append(")")
                            bracketsCounter++
                        }
                    }
                }
            } else {
                textView.append(openBracketButton.text)
                bracketsCounter++
            }

            Handler(Looper.getMainLooper()).postDelayed({
                openBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun closeBracketButtonClick(textView: TextView) {
        closeBracketButton.setOnClickListener {
            closeBracketButton.setBackgroundResource(clickedButtonStyle)
            /*
            if (textView.text.isNotEmpty()) {
                if (specialFunctionStarts.isEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')') {
                        if (bracketsCounter > 0) {
                            textView.append(closeBracketButton.text)
                            bracketsCounter--
                        }
                    }
                }
                else {
                    if (textView.text[specialFunctionStarts.last()].isDigit() || textView.text[specialFunctionStarts.last()] == ')') {
                        if (bracketsCounter > 0) {
                            val point = textView.text.length - specialFunctionStarts.last()
                            var counter = point
                            while (counter > 0) {
                                textView.text = textView.text.dropLast(1)
                                counter--
                            }
                            textView.append(closeBracketButton.text)
                            while (counter < point) {
                                textView.append(")")
                            }

                            val iterator = specialFunctionStarts.last() + 1
                            specialFunctionStarts.removeLast()
                            specialFunctionStarts.add(iterator)
                            bracketsCounter--
                        }
                    }
                }
            }*/

            Handler(Looper.getMainLooper()).postDelayed({
                closeBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun deleteButtonClick(textView: TextView, resultTextView: TextView) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            /*
            if (textView.text.isNotEmpty()) {
                if (specialFunctionStarts.isNotEmpty()) {
                    if (specialFunctionsEnds.isNotEmpty()) {
                        if (textView.text.length == specialFunctionsEnds.last()) {
                            textView.text = textView.text.dropLast(2)
                            while(textView.text.isNotEmpty() && textView.text.last().isLetter()) {
                                textView.text = textView.text.dropLast(1)
                            }
                        }
                        else
                        {
                            textView.text = textView.text.dropLast(2)
                            textView.append(")")
                        }
                    }
                }
                else {
                    if (specialFunctionsEnds.isNotEmpty() && textView.text.length == specialFunctionsEnds.last()) {
                        textView.text = textView.text.dropLast(2)
                        textView.append(")")
                        specialFunctionsEnds.removeLast()
                    }
                    else {
                        textView.text = textView.text.dropLast(1)
                    }
                }*/

            resultOfCalculate(textView, resultTextView)
            if (textView.text.isEmpty()) {
                resultTextView.text = ""
            }

            Handler(Looper.getMainLooper()).postDelayed({
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun functionButtonClick(textView: TextView) {
        functionsButtons.forEach { button ->
            button.setOnClickListener {
                button.setBackgroundResource(clickedButtonStyle)

                if (specialFunctions.isEmpty()) {
                    if (textView.text.isEmpty()) {
                        val function: Triple<Int> = Triple<Int>(0, 0, 0)

                        val text = button.text.toString() + "()"
                        textView.append(text)

                        // Set function
                        function.start = textView.text.length - 2
                        function.end = textView.text.length - 1
                        function.deep = ++specialFunctionDeep

                        specialFunctions.add(function)
                    }
                    else if (textView.text.last() != ')') {
                        if (textView.text.last() != '^' && textView.text.last() != ',') {
                            val function: Triple<Int> = Triple<Int>(0, 0, 0)

                            val text = button.text.toString() + "()"
                            textView.append(text)

                            // Set function
                            function.start = textView.text.length - 2
                            function.end = textView.text.length - 1
                            function.deep = ++specialFunctionDeep

                            specialFunctions.add(function)
                        }
                    }
                }
                else {
                    if (specialFunctionDeep == 0) {
                        if (textView.text.last() != ')') {
                            if (textView.text.last() != '^' && textView.text.last() != ',') {
                                val function: Triple<Int> = Triple<Int>(0, 0, 0)

                                val text = button.text.toString() + "()"
                                textView.append(text)

                                // Set function
                                function.start = textView.text.length - 2
                                function.end = textView.text.length - 1
                                function.deep = ++specialFunctionDeep

                                specialFunctions.add(function)
                            }
                        }
                    }
                    else {
                        // Find deeper embedded function
                        var function: Triple<Int> = specialFunctions.last()
                        val range = specialFunctions.size - 1 downTo 0

                        if (function.deep > specialFunctionDeep) {
                            for (i in range) {
                                if (specialFunctions[i].deep == specialFunctionDeep) {
                                    function = specialFunctions[i]
                                    break
                                }
                            }
                        }

                        // Check how manu bracket are needed to be delete
                        var limit = textView.text.length - 1
                        var bracketCounter = 0
                        while (function.end <= limit && textView.text[limit] == ')') {
                            bracketCounter++
                            limit--
                        }

                        val newFunction: Triple<Int> = Triple<Int>(0, 0, 0)
                        if (textView.text[limit] != '^' && textView.text[limit] != ',') {
                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.text = textView.text.dropLast(1)
                                counter++
                            }

                            if (textView.text[limit] == ')') {
                                textView.append("×")
                            }
                            val text = button.text.toString() + "()"
                            textView.append(text)

                            counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            newFunction.start = textView.text.length - bracketCounter - 2
                            newFunction.end = textView.text.length - bracketCounter - 1
                            newFunction.deep = ++specialFunctionDeep
                            for (func in specialFunctions) {
                                if (func.deep < specialFunctionDeep) {
                                    func.end += text.length
                                }
                            }
                            specialFunctions.add(newFunction)
                        }
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    button.setBackgroundResource(unClickedButtonStyle)
                }, 200)
            }
        }
    }
}