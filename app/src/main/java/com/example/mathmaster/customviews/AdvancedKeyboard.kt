package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import kotlin.math.*
import com.example.mathmaster.R

data class PairEquation<Double, Int> (var first: Double, var second: Int)
data class Operators<Boolean, Int> (var occurrence: Int, var degrees: Boolean,
                                    var level: Int, var functionIndex: Int)

class AdvancedKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Grid layout
    private val gridLayout: GridLayout

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
    private val dotButton: Button
    private val percentButton: Button
    private val factorialButton: Button
    private val fractionButton: Button
    private val numberPIButton: Button
    private val numberEulerButton: Button
    private val rootButton: Button

    private var dotUsed: Boolean = false

    // Functions of calculator
    private var functionsBeginnings: MutableList<Int> = mutableListOf()
    private val functionsButtons: Array<Button>
    private val logarithmButton: Button
    private val naturalLogarithmButton: Button
    private val sinButton: Button
    private val cosButton: Button
    private val tgButton: Button

    // Brackets
    private var bracketsCounter: Int = 0
    private val openBracketButton: Button
    private val closeBracketButton: Button

    private var bracketsLevel: ArrayDeque<Boolean> = ArrayDeque()
    private var functionEnds: ArrayDeque<Int> = ArrayDeque()
    private var functionIndex: Int = 0
    private var functionLevel: Int  = 0

    private var operatorsOccurrence: ArrayDeque<Operators<Boolean, Int>> = ArrayDeque()
    private var degreesInUse: Boolean = false
    private var addDegree: Boolean = false

    // Options
    private val degreeButton: Button
    private val changeFunctionsButton: Button
    private var secondFunctions: Boolean = false
    private var radians: Boolean = true

    // Optional variable button
    private var functionChartMode: Boolean = false
    private var unknownsCalculatorMode: Boolean = false
    private var variableButton: Button
    private var equalSign: Boolean = false

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.advancedcalculator_layout, this, true)
        gridLayout = findViewById(R.id.AdvanceCalculatorKeyboard)

        // Main buttons
        enterButton = findViewById(R.id.Equal)
        deleteButton = findViewById(R.id.Delete)
        clearButton = findViewById(R.id.Clear)

        // Numbers
        zeroButton = findViewById(R.id.Zero)
        oneButton = findViewById(R.id.One)
        twoButton = findViewById(R.id.Two)
        threeButton = findViewById(R.id.Three)
        fourButton = findViewById(R.id.Four)
        fiveButton = findViewById(R.id.Five)
        sixButton = findViewById(R.id.Six)
        sevenButton = findViewById(R.id.Seven)
        eightButton = findViewById(R.id.Eight)
        nineButton = findViewById(R.id.Nine)

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
        addButton = findViewById(R.id.Plus)
        subtractButton = findViewById(R.id.Minus)
        multiplyButton = findViewById(R.id.Multiply)
        divideButton = findViewById(R.id.Divide)

        basicCalcButtons = arrayOf(
            addButton,
            subtractButton,
            multiplyButton,
            divideButton
        )

        // Special operations buttons
        powerButton = findViewById(R.id.PowerTo)
        dotButton = findViewById(R.id.Dot)
        percentButton = findViewById(R.id.Procent)
        factorialButton = findViewById(R.id.Factorial)
        fractionButton = findViewById(R.id.Fraction)
        numberPIButton = findViewById(R.id.PInumber)
        numberEulerButton = findViewById(R.id.Euler)
        rootButton = findViewById(R.id.Root)

        // Function operators
        logarithmButton = findViewById(R.id.Logaritm)
        naturalLogarithmButton = findViewById(R.id.NLogaritm)
        sinButton = findViewById(R.id.Sin)
        cosButton = findViewById(R.id.Cos)
        tgButton = findViewById(R.id.Tan)

        functionsButtons = arrayOf(
            logarithmButton,
            naturalLogarithmButton,
            sinButton,
            cosButton,
            tgButton
        )

        // Brackets
        openBracketButton = findViewById(R.id.FirstBracket)
        closeBracketButton = findViewById(R.id.SecondBracket)

        // Options
        degreeButton = findViewById(R.id.Degree)
        changeFunctionsButton = findViewById(R.id.Change)

        // Optional variable button
        variableButton = findViewById(R.id.Variable)
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
        var tempLength = length
        var outputNumber = 0.0

        if (divide) {
            val range = list.size-1 downTo 0
            for (i in range) {
                outputNumber += convertNumber(list[i], tempLength+1, true)
                tempLength--
            }
            tempLength++
        } else {
            list.forEach { num ->
                outputNumber += convertNumber(num, tempLength, false)
                tempLength--
            }
        }
        return outputNumber
    }

    private fun findNewBracketIndex(transformedEquation: MutableList<Any>): Int {
        var openBrackets = 0
        var closeBrackets = 0

        val range = transformedEquation.size - 1 downTo 0

        for (i in range) {
            if (transformedEquation[i] == ')') {
                closeBrackets++
            }
            else if (transformedEquation[i] == '(') {
                openBrackets++

                if (closeBrackets == openBrackets) {
                    if (i > 0) {
                        if (transformedEquation[i-1].toString()[0].isLetter()) {
                            if (transformedEquation[i] != 'x' && transformedEquation[i] != 'y') {
                                return i - 1
                            }
                        }
                    }
                }
            }
            else if (transformedEquation[i] == '√') {
                openBrackets++
                if (closeBrackets == openBrackets) {
                    if (i > 0) {
                        return i - 1
                    }
                }
            }

            if (closeBrackets == openBrackets) {
                return i
            }
        }

        return 0
    }

    fun transformEquation(input: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()
        var equation = input

        // Equation variables
        var intConverter = 0
        var whatFunction = '0'
        var lastChar = '?'

        var commaInUse = false
        var numberBase = 0.0
        val numBuffer: MutableList<Double> = mutableListOf()

        // Equation validation
        var multiplyDivide = false
        var inDegree = false
        var addDegree = false
        var inRoot = false
        var negativeNumber = false

        // Brackets
        val bracketsInput: MutableList<Char> = mutableListOf()

        var addBracketIndex: Int
        val powerToOpenedBrackets: MutableList<Int> = mutableListOf()
        val additionalOpenedBrackets: MutableList<MutableList<Char>> = mutableListOf()
        additionalOpenedBrackets.add(mutableListOf())

        // Delete last redundant bracket or function for further transform
        if (equation.isNotEmpty()) {
            if (equation.last() == '(') {
                while (equation.isNotEmpty()) {
                    if (equation.last() == '+' || equation.last() == '-'
                        || equation.last() == '×' || equation.last() == '/'
                        || equation.last() == ')') {
                        break
                    }
                    equation = equation.dropLast(1)
                }
                if (equation.isNotEmpty()) {
                    equation = equation.dropLast(1)
                }
            }
        }

        // Transform equation for calculations
        equation.forEach { element ->
            if (element.isDigit()) {
                // Add digit to buffer
                numBuffer.add((element.code - 48).toDouble())
                intConverter++

                // Decimal number
                if (lastChar == '.') {
                    val decimalNumber: Double = calculateNumber(numBuffer, intConverter, true)
                    val outputNumber: Double = numberBase + decimalNumber

                    if (!commaInUse) {
                        commaInUse = true
                    }
                    else {
                        transformedEquation.removeLast()
                    }

                    if (negativeNumber) {
                        transformedEquation.removeLast()
                        transformedEquation.add(-outputNumber)
                        negativeNumber = false
                    }
                    else {
                        transformedEquation.add(outputNumber)
                    }
                }
            }
            else {
                // Append number that is in buffer
                if (numBuffer.isNotEmpty()) {
                    if (lastChar != '.') {
                        val outputNumber: Double = calculateNumber(numBuffer, intConverter, false)

                        if (element == '.') {
                            numberBase = outputNumber
                            numBuffer.clear()
                        }
                        else {
                            if (negativeNumber) {
                                transformedEquation.removeLast()
                                transformedEquation.add(-outputNumber)
                                negativeNumber = false
                            }
                            else {
                                transformedEquation.add(outputNumber)
                            }
                        }
                    }
                }

                // Recognize function
                if (whatFunction != 'a') {
                    if (whatFunction == 'l') {
                        when (element) {
                            'g' -> whatFunction = element
                            'n' -> whatFunction = element
                        }
                    }
                    else {
                        when (element) {
                            's' -> if (whatFunction != 'c' && whatFunction != 'o') whatFunction = element
                            'c' -> whatFunction = element
                            't' -> whatFunction = element
                            'l' -> whatFunction = element
                            'a' -> if (whatFunction != 't')whatFunction = element
                        }
                    }
                }
                else {
                    when (element) {
                        'i' -> whatFunction = element
                        'o' -> whatFunction = element
                        'a' -> whatFunction = element
                    }
                }

                // Handle operations
                when (element) {
                    '(', '√'-> {
                        var addMultiplication = false

                        // Append multiplication if before number is other function or constants
                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                                && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                                && transformedEquation.last() != '(' && transformedEquation.last() != '√'
                                && transformedEquation.last() != '^') {
                                addMultiplication = true
                            }
                        }

                        if (addMultiplication) {
                            if (inRoot) {
                                if (additionalOpenedBrackets.last().isNotEmpty()) {
                                    transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                                }
                            }

                            if (powerToOpenedBrackets.isNotEmpty()) {
                                while (powerToOpenedBrackets.isNotEmpty() &&
                                    powerToOpenedBrackets.last() >= additionalOpenedBrackets.size - 1
                                ) {
                                    while (additionalOpenedBrackets.last().isNotEmpty()) {
                                        transformedEquation.add(
                                            additionalOpenedBrackets.last().removeLast()
                                        )
                                    }
                                    powerToOpenedBrackets.removeLast()
                                }

                                if (powerToOpenedBrackets.isEmpty() && addDegree) {
                                    transformedEquation.add(')')
                                    transformedEquation.add('°')
                                    transformedEquation.add(')')
                                    addDegree = false
                                }
                            }

                            if(!multiplyDivide) {
                                addBracketIndex = findNewBracketIndex(transformedEquation)
                                transformedEquation.add(addBracketIndex, '(')
                                additionalOpenedBrackets.last().add(')')
                            }

                            transformedEquation.add('×')
                            multiplyDivide = true
                        }

                        if (element == '√') {
                            transformedEquation.add(element)
                            additionalOpenedBrackets.last().add(')')
                            inRoot = true
                        }
                        else {
                            if (whatFunction != '0') {
                                transformedEquation.add(whatFunction)
                                negativeNumber = false
                                whatFunction = '0'
                                inRoot = false
                            }

                            additionalOpenedBrackets.add(mutableListOf())
                            bracketsInput.add(')')
                            transformedEquation.add(element)
                        }
                    }
                    ')' -> {
                        if (powerToOpenedBrackets.isNotEmpty()) {
                            if (powerToOpenedBrackets.last() == additionalOpenedBrackets.size-1) {
                                powerToOpenedBrackets.removeLast()
                            }
                        }

                        while (additionalOpenedBrackets.last().isNotEmpty()) {
                            transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                        }
                        additionalOpenedBrackets.removeLast()

                        if (powerToOpenedBrackets.isEmpty() && addDegree) {
                            transformedEquation.add(')')
                            transformedEquation.add('°')
                            transformedEquation.add(')')
                            addDegree = false
                        }

                        bracketsInput.removeLast()
                        transformedEquation.add(element)
                    }
                    '+', '-' -> {
                        while (additionalOpenedBrackets.last().isNotEmpty()) {
                            transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                        }

                        if (lastChar == '(' && element == '-' && !transformedEquation.last().toString().last().isDigit()) {
                            negativeNumber = true
                        }

                        if (powerToOpenedBrackets.isNotEmpty()) {
                            while (powerToOpenedBrackets.isNotEmpty() &&
                                powerToOpenedBrackets.last() >= additionalOpenedBrackets.size - 1) {
                                powerToOpenedBrackets.removeLast()
                            }

                            if (powerToOpenedBrackets.isEmpty() && addDegree) {
                                transformedEquation.add(')')
                                transformedEquation.add('°')
                                transformedEquation.add(')')
                                addDegree = false
                            }
                        }

                        transformedEquation.add(element)
                        multiplyDivide = false
                        inRoot = false
                        inDegree = false
                    }
                    '×', '/' -> {
                        if (inRoot) {
                            transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                        }

                        if (powerToOpenedBrackets.isNotEmpty()) {
                            while (powerToOpenedBrackets.isNotEmpty() &&
                                powerToOpenedBrackets.last() >= additionalOpenedBrackets.size-1) {
                                while (additionalOpenedBrackets.last().isNotEmpty()) {
                                    transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                                }
                                powerToOpenedBrackets.removeLast()
                            }

                            if (powerToOpenedBrackets.isEmpty() && addDegree) {
                                transformedEquation.add(')')
                                transformedEquation.add('°')
                                transformedEquation.add(')')
                                addDegree = false
                            }
                        }

                        if (!multiplyDivide) {
                            addBracketIndex = findNewBracketIndex(transformedEquation)
                            transformedEquation.add(addBracketIndex, '(')
                            additionalOpenedBrackets.last().add(')')
                        }

                        transformedEquation.add(element)

                        multiplyDivide = true
                        inRoot = false
                        inDegree = false
                    }
                    '^' -> {
                        if (inRoot) {
                            if (additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                        }

                        if (inDegree) {
                            for (i in 0 until 3) {
                                transformedEquation.removeLast()
                            }
                            addDegree = true
                        }

                        addBracketIndex = findNewBracketIndex(transformedEquation)
                        transformedEquation.add(addBracketIndex, '(')
                        transformedEquation.add('^')
                        additionalOpenedBrackets.last().add(')')

                        powerToOpenedBrackets.add(additionalOpenedBrackets.size-1)
                        inRoot = false
                        inDegree = false
                    }
                    'π', 'e', 'x', 'y' -> {
                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                                && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                                && transformedEquation.last() != '(' && transformedEquation.last() != '√') {
                                if (!multiplyDivide) {
                                    addBracketIndex = findNewBracketIndex(transformedEquation)
                                    transformedEquation.add(addBracketIndex, '(')
                                    additionalOpenedBrackets.last().add(')')

                                    transformedEquation.add('×')
                                    multiplyDivide = true
                                }
                            }
                        }

                        if (element == 'π' || element == 'e') {
                            val constant = if (element == 'π') PI else Math.E
                            transformedEquation.add(constant)
                        }
                        else {
                            transformedEquation.add(element)
                        }
                        inDegree = false
                    }
                    '!', '%', '°' -> {
                        if (inRoot) {
                            if (additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                        }

                        addBracketIndex = findNewBracketIndex(transformedEquation)

                        for (i in 0 until 2) {
                            transformedEquation.add(addBracketIndex, '(')
                        }

                        transformedEquation.add(')')
                        transformedEquation.add(element)
                        transformedEquation.add(')')

                        if (element == '°') {
                            inDegree = true
                        }

                        inRoot = false
                    }
                    '=' -> {
                        while(bracketsInput.isNotEmpty()) {
                            transformedEquation.add(bracketsInput.removeLast())
                        }

                        while(additionalOpenedBrackets.isNotEmpty()) {
                            while(additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                            additionalOpenedBrackets.removeLast()
                        }

                        transformedEquation.add(element)
                    }
                }

                commaInUse = false
                intConverter = 0

                lastChar = element
                numBuffer.clear()
            }
        }
        // Add number that lasts in buffer
        if (numBuffer.isNotEmpty() && lastChar != '.') {
            val outputNumber: Double = calculateNumber(numBuffer, intConverter, false)

            if (negativeNumber) {
                transformedEquation.removeLast()
                transformedEquation.add(-outputNumber)
                negativeNumber = false
            }
            else {
                transformedEquation.add(outputNumber)
            }
        }

        // Add degree if lasts
        if (addDegree) {
            transformedEquation.add(')')
            transformedEquation.add('°')
            transformedEquation.add(')')
        }

        // Add all the brackets that lasts in buffer
        while(bracketsInput.isNotEmpty()) {
            transformedEquation.add(bracketsInput.removeLast())
        }

        while(additionalOpenedBrackets.isNotEmpty()) {
            while(additionalOpenedBrackets.last().isNotEmpty()) {
                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
            }
            additionalOpenedBrackets.removeLast()
        }

        return transformedEquation
    }

    private fun factorial(number: Double): Double {
        if (number <= 1.0) {
            return 1.0
        }
        return number * factorial(number-1)
    }

    fun calculate(equation: MutableList<Any>, index: Int): PairEquation<Double, Int> {
        var equationSign = 'E'
        val result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index
        val threshold = 1E-10
        var lastChar = '0'

        while (iterator < equation.size) {
            when (equation[iterator]) {
                is Char -> {
                    if (equation[iterator] == '(') {
                        val equationBuffer = calculate(equation, iterator + 1)
                        when (equationSign) {
                            '+' -> result.first += equationBuffer.first
                            '-' -> result.first -= equationBuffer.first
                            '×' -> result.first *= equationBuffer.first
                            '/' -> result.first /= equationBuffer.first
                            '^' -> result.first = (result.first).pow(equationBuffer.first)
                            'E' -> result.first = equationBuffer.first
                        }

                        iterator = equationBuffer.second
                    }
                    else if (equation[iterator] == '√') {
                        val equationBuffer = calculate(equation, iterator + 1)
                        val rootResult = sqrt(equationBuffer.first)

                        when (equationSign) {
                            '+' -> result.first += rootResult
                            '-' -> result.first -= rootResult
                            '×' -> result.first *= rootResult
                            '/' -> result.first /= rootResult
                            '^' -> result.first = (rootResult).pow(rootResult)
                            'E' -> result.first = rootResult
                        }

                        iterator = equationBuffer.second
                    }
                    else if (equation[iterator] == ')') {
                        result.first = if (abs(result.first) < threshold) 0.0 else result.first
                        return result
                    } else if ((equation[iterator] as Char).isLetter()) {
                        val equationBuffer = calculate(equation, iterator + 2)

                        if (lastChar == '-') {
                            equationSign = '-'
                        }

                        when (equationSign) {
                            '+' -> {
                                when (equation[iterator]) {
                                    's' -> result.first += sin(equationBuffer.first)
                                    'c' -> result.first += cos(equationBuffer.first)
                                    't' -> result.first += tan(equationBuffer.first)
                                    'g' -> result.first += log(equationBuffer.first, 2.0)
                                    'n' -> result.first += ln(equationBuffer.first)
                                    'i' -> result.first += asin(equationBuffer.first)
                                    'o' -> result.first += acos(equationBuffer.first)
                                    'a' -> result.first += atan(equationBuffer.first)
                                }
                            }

                            '-' -> {
                                when (equation[iterator]) {
                                    's' -> result.first -= sin(equationBuffer.first)
                                    'c' -> result.first -= cos(equationBuffer.first)
                                    't' -> result.first -= tan(equationBuffer.first)
                                    'g' -> result.first -= log(equationBuffer.first, 2.0)
                                    'n' -> result.first -= ln(equationBuffer.first)
                                    'i' -> result.first -= asin(equationBuffer.first)
                                    'o' -> result.first -= acos(equationBuffer.first)
                                    'a' -> result.first -= atan(equationBuffer.first)
                                }
                            }

                            '×' -> {
                                when (equation[iterator]) {
                                    's' -> result.first *= sin(equationBuffer.first)
                                    'c' -> result.first *= cos(equationBuffer.first)
                                    't' -> result.first *= tan(equationBuffer.first)
                                    'g' -> result.first *= log(equationBuffer.first, 2.0)
                                    'n' -> result.first *= ln(equationBuffer.first)
                                    'i' -> result.first *= asin(equationBuffer.first)
                                    'o' -> result.first *= acos(equationBuffer.first)
                                    'a' -> result.first *= atan(equationBuffer.first)
                                }
                            }

                            '/' -> {
                                when (equation[iterator]) {
                                    's' -> result.first /= sin(equationBuffer.first)
                                    'c' -> result.first /= cos(equationBuffer.first)
                                    't' -> result.first /= tan(equationBuffer.first)
                                    'g' -> result.first /= log(equationBuffer.first, 2.0)
                                    'n' -> result.first /= ln(equationBuffer.first)
                                    'i' -> result.first /= asin(equationBuffer.first)
                                    'o' -> result.first /= acos(equationBuffer.first)
                                    'a' -> result.first /= atan(equationBuffer.first)
                                }
                            }

                            '^' -> {
                                when (equation[iterator]) {
                                    's' -> result.first =
                                        (result.first).pow(sin(equationBuffer.first))

                                    'c' -> result.first =
                                        (result.first).pow(cos(equationBuffer.first))

                                    't' -> result.first =
                                        (result.first).pow(tan(equationBuffer.first))

                                    'g' -> result.first =
                                        (result.first).pow(log(equationBuffer.first, 2.0))

                                    'n' -> result.first =
                                        (result.first).pow(ln(equationBuffer.first))

                                    'i' -> result.first +=
                                        (result.first).pow(asin(equationBuffer.first))

                                    'o' -> result.first +=
                                        (result.first).pow(acos(equationBuffer.first))

                                    'a' -> result.first +=
                                        (result.first).pow(atan(equationBuffer.first))
                                }
                            }
                            else -> {
                                when (equation[iterator]) {
                                    's' -> result.first = sin(equationBuffer.first)
                                    'c' -> result.first = cos(equationBuffer.first)
                                    't' -> result.first = tan(equationBuffer.first)
                                    'g' -> result.first = log(equationBuffer.first, 2.0)
                                    'n' -> result.first = ln(equationBuffer.first)
                                    'i' -> result.first = asin(equationBuffer.first)
                                    'o' -> result.first = acos(equationBuffer.first)
                                    'a' -> result.first = atan(equationBuffer.first)
                                }
                            }
                        }
                        iterator = equationBuffer.second
                    } else {
                        when (equation[iterator]) {
                            '+' -> equationSign = equation[iterator] as Char
                            '-' -> equationSign = equation[iterator] as Char
                            '×' -> equationSign = equation[iterator] as Char
                            '/' -> equationSign = equation[iterator] as Char
                            '^' -> equationSign = equation[iterator] as Char
                        }

                        if (equation[iterator] == '!') {
                            result.first = factorial(result.first)
                        }
                        else if (equation[iterator] == '%') {
                            result.first /= 100
                        }
                        else if (equation[iterator] == '°') {
                            result.first = Math.toRadians(result.first) % (2*Math.PI)
                        }
                    }

                    if (iterator < equation.size) {
                        lastChar = equation[iterator] as Char
                    }
                }

                is Double -> {
                    when (equationSign) {
                        '+' -> result.first += equation[iterator] as Double
                        '-' -> result.first -= equation[iterator] as Double
                        '×' -> result.first *= equation[iterator] as Double
                        '/' -> result.first /= equation[iterator] as Double
                        '^' -> result.first = (result.first).pow(equation[iterator] as Double)
                        'E' -> result.first = equation[iterator] as Double
                    }
                }
            }

            iterator++
            result.second = iterator
        }

        result.first = if (abs(result.first) < threshold) 0.0 else result.first
        return result
    }

    private fun checkIsItDouble(number: Double): Boolean {
        return number % 1 != 0.0
    }

    private fun resultOfCalculate(textView: TextView, resultTextView: TextView) {
        if (clearButton.text == "AC") {
            clearButton.text = "C"
        }

        // Calculation
        val equation = transformEquation(textView.text.toString())
        val resultOfCalculations = calculate(equation, 0)

        if (checkIsItDouble(resultOfCalculations.first)) {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            }
            else {
                val text = "= " + resultOfCalculations.first.toFloat().toString()
                resultTextView.text = text
            }
        } else {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            }
            else {
                val text = "= " + resultOfCalculations.first.toInt().toString()
                resultTextView.text = text
            }
        }
    }

    fun enterButtonClick(textView: TextView, resultTextView: TextView, historyTextView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            // Create modified string for history text view
            if (resultTextView.text.isNotEmpty()) {
                var textBuffer = textView.text.toString()
                var counter = 0
                for (char in textBuffer) {
                    if (char == '(') {
                        counter++
                    }
                    else if (char == ')') {
                        counter--
                    }
                }

                while (counter > 0) {
                    textBuffer += ')'
                    counter--
                }

                // Update history text view with created string
                historyTextView.append("\n")
                historyTextView.append("\n")
                historyTextView.append(textBuffer)
                historyTextView.append("\n")
                historyTextView.append(resultTextView.text)
            }

            // Update input and result text views
            if (resultTextView.text.isNotEmpty()) {
                textView.text = resultTextView.text.substring(2)
            }

            resultTextView.text = ""

            Handler(Looper.getMainLooper()).postDelayed({
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun numberButtonClick(textView: TextView, resultTextView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                var addedNumber = false

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != ')' && textView.text.last() != 'π'
                        && textView.text.last() != '!' && textView.text.last() != 'e'
                        && textView.text.last() != '%'  && textView.text.last() != 'x'
                        && textView.text.last() != 'y') {
                        if (textView.text.last() == '°') {
                            textView.text = textView.text.dropLast(1)
                        }
                        if (!radians && functionLevel > 0) {
                            if (operatorsOccurrence.isNotEmpty()) {
                                if (operatorsOccurrence.last().functionIndex == functionIndex) {
                                    if (operatorsOccurrence.last().level == functionLevel) {
                                        if (operatorsOccurrence.last().degrees) {
                                            textView.append(i.toString())
                                            textView.append("°")
                                            degreesInUse = true
                                        }
                                        else {
                                            textView.append(i.toString())
                                        }
                                    }
                                }
                                else if (bracketsLevel.last()) {
                                    textView.append(i.toString())
                                    textView.append("°")
                                    degreesInUse = true
                                }
                                else {
                                    textView.append(i.toString())
                                }
                            }
                            else {
                                textView.append(i.toString())
                                textView.append("°")
                                degreesInUse = true
                            }
                        }
                        else {
                            textView.append(i.toString())
                        }

                        addedNumber = true
                    }
                } else {
                    textView.append(i.toString())
                    addedNumber = true
                }

                if (addedNumber) {
                    if (!functionChartMode && !unknownsCalculatorMode) {
                        resultOfCalculate(textView, resultTextView)
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    buttons[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    fun basicCalcButtonClick(textView: TextView) {
        for (i in basicCalcButtons.indices) {
            basicCalcButtons[i].setOnClickListener {
                basicCalcButtons[i].setBackgroundResource(clickedButtonStyle)

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == 'π' || textView.text.last() == 'e'
                        || textView.text.last() == '!' || textView.text.last() == '%'
                        || textView.text.last() == 'x' || textView.text.last() == 'y') {

                        textView.append(basicCalcButtons[i].text)

                        if (!radians && functionLevel > 0) {
                            if (bracketsLevel.last()) {
                                operatorsOccurrence.addLast(
                                    Operators(textView.text.length-1, false,
                                        functionLevel, functionIndex))
                                addDegree = true
                            }
                        }

                        dotUsed = false
                    }
                    else if (textView.text.last() == '(') {
                        if (basicCalcButtons[i].text == "-") {
                            textView.append(basicCalcButtons[i].text)
                        }
                        dotUsed = false
                    }
                    else if (textView.text.last() == '°') {
                        val operator = basicCalcButtons[i].text
                        textView.append(operator)

                        if (operator == "×" || operator == "/") {
                            operatorsOccurrence.addLast(
                                Operators(textView.text.length-1, false,
                                    functionLevel, functionIndex))
                        }

                        dotUsed = false
                    }
                }
                else {
                    if (basicCalcButtons[i].text == "-") {
                        textView.append(basicCalcButtons[i].text)
                    }
                    dotUsed = false
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    basicCalcButtons[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    fun powerButtonClick(textView: TextView) {
        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'π' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == '°'
                    || textView.text.last() == 'y') {
                    textView.append(powerButton.text.toString())

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(textView.text.length-1, false,
                                    functionLevel, functionIndex))
                            addDegree = true
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun dotButtonClick(textView: TextView) {
        dotButton.setOnClickListener {
            dotButton.setBackgroundResource(clickedButtonStyle)

            if (!dotUsed) {
                if (textView.text.isNotEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == '°') {
                        if (textView.text.last() == '°') {
                            textView.text = textView.text.dropLast(1)
                        }

                        val text = dotButton.text
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        dotUsed = true
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                dotButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun rootButtonClick(textView: TextView) {
        rootButton.setOnClickListener {
            rootButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == '+'
                    || textView.text.last() == '-' || textView.text.last() == '×'
                    || textView.text.last() == '/' || textView.text.last() == 'π'
                    || textView.text.last() == '(' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == 'y') {
                    textView.append("√")

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(textView.text.length-1, false,
                                    functionLevel, functionIndex))
                            addDegree = true
                        }
                    }
                }
            }
            else {
                textView.append("√")
            }

            Handler(Looper.getMainLooper()).postDelayed({
                rootButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun factorialButtonClick(textView: TextView, resultTextView: TextView) {
        factorialButton.setOnClickListener {
            factorialButton.setBackgroundResource(clickedButtonStyle)
            var appendedFactorial = false
            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'x' || textView.text.last() == '°'
                    || textView.text.last() == 'y') {
                    textView.append("!")
                    appendedFactorial = true
                }
            }

            if (appendedFactorial) {
                if (!functionChartMode && !unknownsCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                factorialButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun fractionButtonClick(textView: TextView) {
        fractionButton.setOnClickListener {
            fractionButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'π' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == 'y') {
                    textView.append("^(-")
                    bracketsCounter++

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(textView.text.length-1, false,
                                    functionLevel, functionIndex))
                            addDegree = true
                        }
                    }
                    else {
                        bracketsLevel.addLast(false)
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                fractionButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun percentButtonClick(textView: TextView, resultTextView: TextView) {
        percentButton.setOnClickListener {
            percentButton.setBackgroundResource(clickedButtonStyle)

            var appendedPercent = false
            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'x' || textView.text.last() == 'y') {
                    textView.append("%")
                    appendedPercent = true
                }
            }

            if (appendedPercent) {
                if (!functionChartMode && !unknownsCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                percentButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun numberPIButtonClick(textView: TextView, resultTextView: TextView) {
        numberPIButton.setOnClickListener {
            numberPIButton.setBackgroundResource(clickedButtonStyle)

            var addedNumber = false

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'π'
                    && textView.text.last() != 'e' && textView.text.last() != '°') {
                    textView.append(numberPIButton.text.toString())
                    addedNumber = true
                }
            } else {
                textView.append(numberPIButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                if (!functionChartMode && !unknownsCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberPIButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun numberEulerButtonClick(textView: TextView, resultTextView: TextView) {
        numberEulerButton.setOnClickListener {
            numberEulerButton.setBackgroundResource(clickedButtonStyle)

            var addedNumber = false

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'π'
                    && textView.text.last() != 'e' && textView.text.last() != '°') {
                    textView.append(numberEulerButton.text.toString())
                    addedNumber = true
                }
            } else {
                textView.append(numberEulerButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                if (!functionChartMode && !unknownsCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberEulerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clearButtonClick(textView: TextView, resultTextView: TextView, historyTextView: TextView) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            bracketsCounter = 0
            operatorsOccurrence.clear()
            bracketsLevel.clear()
            functionLevel = 0
            functionEnds.clear()
            dotUsed = false
            equalSign = false
            textView.text = ""

            if (!functionChartMode && !unknownsCalculatorMode) {
                if (clearButton.text == "AC") {
                    historyTextView.text = ""
                }

                clearButton.text = context.getString(R.string.AC)
                resultTextView.text = ""
            }

            Handler(Looper.getMainLooper()).postDelayed({
                clearButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun openBracketButtonClick(textView: TextView) {
        openBracketButton.setOnClickListener {
            openBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '°') {
                    val text = openBracketButton.text.toString()
                    textView.append(text)
                    bracketsCounter++
                    bracketsLevel.addLast(false)
                }
            } else {
                textView.append(openBracketButton.text)
                bracketsCounter++
                bracketsLevel.addLast(false)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                openBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun closeBracketButtonClick(textView: TextView) {
        closeBracketButton.setOnClickListener {
            closeBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && bracketsCounter > 0) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == '!' || textView.text.last() == 'π'
                    || textView.text.last() == 'e' || textView.text.last() == '°'
                    || textView.text.last() == '%' || textView.text.last() == 'x'
                    || textView.text.last() == 'y') {
                    val text = closeBracketButton.text.toString()
                    textView.append(text)
                    bracketsCounter--

                    if (bracketsLevel.last()) {
                        functionEnds.addLast(textView.text.length-1)
                        functionLevel--

                        if (functionLevel == 0) {
                            degreesInUse = false
                            addDegree = false
                        }
                    }
                    bracketsLevel.removeLast()
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                closeBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun deleteButtonClick(textView: TextView, resultTextView: TextView) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() == '(') {
                    var deleted = false
                    for (end in functionsBeginnings) {
                        if (textView.text.length-1 == end) {
                            textView.text = textView.text.dropLast(1)

                            while (textView.text.isNotEmpty() && textView.text.last().isLetter()) {
                                textView.text = textView.text.dropLast(1)
                            }
                            deleted = true
                            break
                        }
                    }
                    if (!deleted) {
                        textView.text = textView.text.dropLast(1)
                    }
                    bracketsCounter--

                    if (bracketsLevel.last()) {
                        functionLevel--
                    }
                    bracketsLevel.removeLast()

                    if (functionLevel == 0) {
                        functionIndex--
                    }
                }
                else if (textView.text.last() == ')') {
                    if (functionEnds.isNotEmpty()) {
                        if (textView.text.length-1 == functionEnds.last()) {
                            bracketsLevel.addLast(true)
                            functionLevel++
                            functionEnds.removeLast()
                            if (degreesInUse) {
                                addDegree = true
                            }
                        }
                        else {
                            bracketsLevel.addLast(false)
                        }
                    }
                    else {
                        bracketsLevel.addLast(false)
                    }

                    textView.text = textView.text.dropLast(1)
                    bracketsCounter++
                }
                else if (textView.text.last() == '°') {
                    textView.text = textView.text.dropLast(1)

                    if (textView.text.last().isDigit()) {
                        textView.text = textView.text.dropLast(1)

                        if (textView.text.last().isDigit()) {
                            textView.append("°")
                        }
                    }
                }
                else if (textView.text.last() == '.') {
                    dotUsed = false
                    textView.text = textView.text.dropLast(1)
                }
                else if (textView.text.last() == '=') {
                    equalSign = false
                    textView.text = textView.text.dropLast(1)
                }
                else {
                    if(operatorsOccurrence.isNotEmpty()) {
                        if (textView.text.length-1 == operatorsOccurrence.last().occurrence) {
                            addDegree = operatorsOccurrence.last().degrees
                            operatorsOccurrence.removeLast()
                        }
                    }
                    textView.text = textView.text.dropLast(1)
                }
            }

            if (!functionChartMode && !unknownsCalculatorMode) {
                if (textView.text.isNotEmpty()) {
                    resultOfCalculate(textView, resultTextView)
                }
                else {
                    resultTextView.text = ""
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun functionButtonClick(textView: TextView) {
        functionsButtons.forEach { button ->
            button.setOnClickListener {
                button.setBackgroundResource(clickedButtonStyle)

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != '.') {
                        if (functionLevel == 0) {
                            functionIndex++
                        }

                        val text = button.text.toString() + "("
                        textView.append(text)
                        functionsBeginnings.add(textView.text.length-1)
                        bracketsCounter++
                        dotUsed = false
                        functionLevel++
                        bracketsLevel.addLast(true)

                        if (!radians) {
                            degreesInUse = true
                            addDegree = true
                        }
                    }
                }
                else {
                    if (functionLevel == 0) {
                        functionIndex++
                    }

                    val text = button.text.toString() + "("
                    textView.append(text)
                    functionsBeginnings.add(textView.text.length-1)
                    bracketsCounter++
                    dotUsed = false
                    functionLevel++
                    bracketsLevel.addLast(true)

                    if (!radians) {
                        degreesInUse = true
                        addDegree = true
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    button.setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    fun degreeButtonClick() {
        degreeButton.setOnClickListener {
            degreeButton.setBackgroundResource(clickedButtonStyle)

            if (degreeButton.text == "deg") {
                degreeButton.text = context.getString(R.string.RadiansCalc)
                radians = true
            }
            else if (!secondFunctions) {
                degreeButton.text = context.getString(R.string.DegreeCalc)
                radians = false
                if (!degreesInUse) {
                    addDegree = true
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                degreeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun changeFunctionsButtonClick() {
        changeFunctionsButton.setOnClickListener {
            changeFunctionsButton.setBackgroundResource(clickedButtonStyle)

            if (changeFunctionsButton.text == context.getString(R.string.SecondCalc)) {
                changeFunctionsButton.text = context.getString(R.string.FirstCalc)
                sinButton.text = context.getString(R.string.Sin)
                cosButton.text = context.getString(R.string.Cos)
                tgButton.text = context.getString(R.string.Tan)

                sinButton.textSize = 16f
                cosButton.textSize = 16f
                tgButton.textSize = 16f

                secondFunctions = false
            }
            else {
                changeFunctionsButton.text = context.getString(R.string.SecondCalc)
                sinButton.text = context.getString(R.string.ASin)
                cosButton.text = context.getString(R.string.ACos)
                tgButton.text = context.getString(R.string.ATan)

                sinButton.textSize = 10f
                cosButton.textSize = 10f
                tgButton.textSize = 10f

                secondFunctions = true
                degreeButton.text = context.getString(R.string.RadiansCalc)
                radians = true
            }

            Handler(Looper.getMainLooper()).postDelayed({
                changeFunctionsButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setFunctionChartMode() {
        functionChartMode = true
        variableButton.text = "x"
        enterButton.text = ""

        var params = enterButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(6, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        enterButton.layoutParams = params

        params = variableButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(6, 1f)
        params.columnSpec = GridLayout.spec(4, 1f)
        variableButton.layoutParams = params
    }

    fun variableButtonClick(textView: TextView) {
        variableButton.setOnClickListener {
            variableButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y') {
                    textView.append(variableButton.text.toString())
                }
            } else {
                textView.append(variableButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                variableButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setUnknownsCalculatorMode() {
        unknownsCalculatorMode = true

        gridLayout.removeView(changeFunctionsButton)
        gridLayout.removeView(degreeButton)
        gridLayout.removeView(sinButton)
        gridLayout.removeView(cosButton)
        gridLayout.removeView(tgButton)

        logarithmButton.text ="x"
        naturalLogarithmButton.text = "y"
        variableButton.text = "✓"
    }

    private fun xVariableClick(textView: TextView) {
        logarithmButton.setOnClickListener {
            logarithmButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y') {
                    textView.append(logarithmButton.text.toString())
                }
            } else {
                textView.append(logarithmButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                logarithmButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun yVariableClick(textView: TextView) {
        naturalLogarithmButton.setOnClickListener {
            naturalLogarithmButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'y') {
                    textView.append(naturalLogarithmButton.text.toString())
                }
            } else {
                textView.append(naturalLogarithmButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                naturalLogarithmButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun getCheckButton(): Button {
        return variableButton
    }

    private fun enterWhenInUnknownsCalculatorMode(textView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && !equalSign) {
                if (textView.text.last().isDigit() || textView.text.last() == 'x'
                    || textView.text.last() == 'y' || textView.text.last() == ')') {
                    textView.append(enterButton.text.toString())
                    equalSign = true
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setEqualSign() {
        equalSign = true
    }

    fun unsetEqualSign() {
        equalSign = false
    }

    fun getEqualSign(): Boolean {
        return equalSign
    }

    fun refreshAllClickListeners(textView: TextView, blank: TextView) {
        numberButtonClick(textView, blank)
        basicCalcButtonClick(textView)
        powerButtonClick(textView)
        dotButtonClick(textView)
        rootButtonClick(textView)
        factorialButtonClick(textView, blank)
        fractionButtonClick(textView)
        percentButtonClick(textView, blank)
        numberPIButtonClick(textView, blank)
        numberEulerButtonClick(textView, blank)
        clearButtonClick(textView, blank, blank)
        openBracketButtonClick(textView)
        closeBracketButtonClick(textView)
        deleteButtonClick(textView, blank)
        xVariableClick(textView)
        yVariableClick(textView)
        enterWhenInUnknownsCalculatorMode(textView)
    }

    private fun transformEquationForSolvingUnknowns(input: String): MutableList<Any> {
        // Get equation
        val equation = mutableListOf<Any>()
        var brackets = 0
        var number = ""

        for (char in input) {
            if (char == '(') {
                brackets++
            }
            else if (char == ')') {
                brackets--
            }

            if (char.isDigit()) {
                number += char
            }
            else if (char == '.') {
                number += char
            }
            else {
                if (char.isLetter() && number == "") {
                    equation.add(1.0)
                }

                if (number != "") {
                    equation.add(number.toDouble())
                    number = ""
                }
                equation.add(char)
            }
        }
        if (number != "") {
            equation.add(number.toDouble())
        }
        for (i in 0 until brackets) {
            equation.add(')')
        }

        val result = mutableListOf<Any>()
        val multiplicative = mutableListOf<Double>()
        val unknowns = mutableListOf<Char>()
        var lastNumber = Double.MIN_VALUE
        var lastUnknown = '0'
        var lastChar: Any = '0'
        var deep = -1
        var level = -1
        var omit = false
        var add = false
        var multiplication = false
        var appended = false

        var iterator = 0
        while (iterator < equation.size) {
            when (equation[iterator]) {
                is Char -> {
                    when (equation[iterator]) {
                        'x', 'y', 'z', 'a' -> {
                            lastUnknown = equation[iterator] as Char

                            if (result.isNotEmpty()) {
                                if (result.last().toString()[0].isDigit()) {
                                    multiplication = true
                                }
                            }

                            if (deep >= 0 && !appended) {
                                println("CHAR")
                                add = true
                            }
                            else {
                                appended = false
                            }
                        }
                        '(' -> {
                            var added = false
                            if (multiplication && lastUnknown != '0') {
                                val numbers = mutableListOf<Double>()
                                val unknownsBuffer = mutableListOf<Char>()
                                println("MULTIPLICATION")
                                var idx = iterator-1
                                while (idx >= 0 && (equation[idx] != '+' && equation[idx] != '-')) {
                                    when (equation[idx]) {
                                        is Double -> numbers.add(equation[idx] as Double)
                                        is Char -> {
                                            if (equation[idx].toString()[0].isLetter()) {
                                                unknownsBuffer.add(equation[idx] as Char)
                                            }
                                        }

                                    }
                                    idx--
                                }

                                if (multiplicative.isNotEmpty() || unknownsBuffer.isNotEmpty()) {
                                    for (num in numbers) {
                                        multiplicative.add(num)
                                    }
                                    for (char in unknownsBuffer) {
                                        unknowns.add(char)
                                    }

                                    level++
                                    deep++
                                    added = true
                                    omit = true
                                    add = false
                                }
                            }

                            if (lastChar != '+' && lastChar != '-' && lastChar != '/' && !added) {
                                if ((lastUnknown != '0' || lastNumber != Double.MIN_VALUE)){
                                    if (lastNumber != Double.MIN_VALUE) {
                                        multiplicative.add(lastNumber)
                                        lastNumber = 0.0
                                        level++
                                    }

                                    if (lastUnknown != '0') {
                                        deep++
                                        unknowns.add(lastUnknown)
                                        lastUnknown = '0'
                                    }

                                    omit = true
                                }
                            }
                        }
                        ')' -> {
                            if (deep >= 0) {
                                unknowns.removeLast()
                                deep--
                            }
                            if (level >= 0) {
                                level--
                            }
                        }
                        '×' -> {
                            if (lastUnknown != '0') {
                                multiplication = true
                            }
                        }
                        '/' -> {
                            multiplication = false

                            var numberBuffer = Double.MIN_VALUE
                            var unknownBuffer = '0'

                            // Find first part of divide
                            var begin = iterator
                            while (begin > 0) {
                                if (equation[begin] == '+' || equation[begin] == '-' || equation[begin] == '×') {
                                    begin++
                                    break
                                }
                                begin--
                            }

                            // Find second part of divide
                            var i = iterator+1
                            while (i < equation.size && (equation[i].toString()[0].isDigit() || equation[i].toString()[0].isLetter())) {
                                if (equation[i].toString()[0].isDigit()) {
                                    numberBuffer = equation[i] as Double
                                }
                                else if (equation[i].toString()[0].isLetter()) {
                                    unknownBuffer = equation[i] as Char
                                }
                                i++
                            }

                            // Append to equation result of divide
                            if (numberBuffer != Double.MIN_VALUE || unknownBuffer != '0') {
                                iterator = i

                                var num = Double.MIN_VALUE

                                if (numberBuffer != Double.MIN_VALUE) {
                                    num = lastNumber / numberBuffer
                                }

                                val unknown = if (lastChar == unknownBuffer) {
                                    ""
                                } else {
                                    "$lastChar/$unknownBuffer"
                                }

                                // Remove calculation from equation
                                i = begin
                                while (i < iterator) {
                                    equation.removeAt(begin)
                                    iterator--
                                }

                                // Remove calculation from result
                                i = result.size-1
                                while (i >= 0 && result[i] != '+' && result[i] != '-' && result[i] != '×') {
                                    result.removeLast()
                                    i--
                                }

                                // Append results
                                if (num != Double.MIN_VALUE && num != 1.0) {
                                    equation.add(begin, num)
                                    result.add(num)
                                }
                                if (unknown != "") {
                                    equation.add(begin, unknown)
                                    result.add(unknown)
                                }

                                // Break if iterator out of scope
                                if (iterator >= equation.size) {
                                    if (num == 1.0) {
                                        result.add(1.0)
                                    }
                                    break
                                }
                                omit = true
                            }
                        }
                        else -> {
                            if (deep < 0 && level < 0) {
                                multiplicative.clear()
                                unknowns.clear()
                                lastUnknown = '0'
                                lastNumber = Double.MIN_VALUE
                            }
                            add = false
                            appended = false
                            multiplication = false
                        }
                    }
                }

                is Double -> {
                    lastNumber = equation[iterator] as Double
                    lastUnknown = '0'
                    if (deep < 0) {
                        unknowns.clear()
                    }
                    if (level >= 0) {
                        var run = true

                        var i = iterator
                        while (i < equation.size) {
                            if (equation[i] == '(') {
                                run = false
                                break
                            }
                            else if (equation[i] == '+' || equation[i] == '-' || equation[i] == ')') {
                                break
                            }
                            i++
                        }

                        if (run) {
                            println("DOUBLE")
                            add = true
                            omit = true
                            appended = true
                        }
                    }
                }
            }

            if (add) {
                println("ADD")
                if (multiplication) {
                    // Delete multiplication
                    var i = result.size-1
                    while (i >= 0) {
                        if (result[i] == '+' || result[i] == '-') {
                            break
                        }
                        println("s")
                        result.removeAt(i)
                        i--
                    }
                }

                if (multiplicative.isNotEmpty()) {
                    for (num in multiplicative) {
                        lastNumber *= num
                    }

                    if (result.isNotEmpty()) {
                        if (result.last().toString()[0].isDigit()) {
                            result.removeLast()
                        }
                    }
                    result.add(lastNumber)
                }

                if (deep >= 0) {
                    for (unknown in unknowns) {
                        result.add(unknown)
                    }
                }
                add = false
            }

            lastChar = equation[iterator]
            println("RESULT = $result")
            println(equation)
            if (!omit) {
                result.add(equation[iterator])
            }
            else {
                omit = false
            }
            iterator++
        }

        println(result)
        return result
    }

    private fun getCoefficientsLinearEquation(firstEquation: String, secondEquation: String): Array<DoubleArray> {
        val result = Array(2) { DoubleArray(3) }

        val equations = arrayOf(transformEquationForSolvingUnknowns(firstEquation)
            , transformEquationForSolvingUnknowns(secondEquation))

        println("FINAL ${equations[0]}")
        /*
        // Get coefficients for further calculations
        for (index in equations.indices) {
            val firstUnknownCoefficients = mutableListOf<Double>()
            val secondUnknownCoefficients = mutableListOf<Double>()
            val numbersQueue = mutableListOf<Double>()
            var leftSide = true
            var lastChar: Any = equations[index].first()

            for (char in 1 until equations[index].size) {
                when (equations[index][char]) {
                    'x' -> {
                        when (lastChar) {
                            '×' -> {
                                if (leftSide) {
                                    firstUnknownCoefficients.add(numbersQueue.removeLast())
                                }
                                else {
                                    firstUnknownCoefficients.add(-numbersQueue.removeLast())
                                }
                            }
                        }
                    }
                    'y' -> {
                        when (lastChar) {
                            '×' -> {
                                if (leftSide) {
                                    secondUnknownCoefficients.add(numbersQueue.removeLast())
                                }
                                else {
                                    secondUnknownCoefficients.add(-numbersQueue.removeLast())
                                }
                            }
                        }
                    }
                    '=' -> {
                        leftSide = false
                    }
                    is Double -> {
                        if (leftSide) {
                            numbersQueue.add(equations[index][char] as Double)
                        }
                        else {
                            numbersQueue.add(-(equations[index][char]as Double))
                        }
                    }
                    else -> {
                        when (lastChar) {
                            '×' -> {
                                when (equations[index][char]) {
                                    is Double -> {
                                        if (leftSide) {
                                            numbersQueue[numbersQueue.lastIndex] = -(numbersQueue.removeLast() * equations[index][char] as Double)
                                        }
                                        else {
                                            numbersQueue[numbersQueue.lastIndex] = numbersQueue.removeLast() * equations[index][char] as Double
                                        }
                                    }
                                }
                            }
                            '/' -> {
                                when (equations[index][char]) {
                                    is Double -> {
                                        if (leftSide) {
                                            numbersQueue[numbersQueue.lastIndex] = -(numbersQueue.removeLast() / equations[index][char] as Double)
                                        }
                                        else {
                                            numbersQueue[numbersQueue.lastIndex] = numbersQueue.removeLast() / equations[index][char] as Double
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                lastChar = equations[index][char]
            }

            // Push coefficients to result array
            result[index][0] = firstUnknownCoefficients.sum()
            result[index][1] = secondUnknownCoefficients.sum()
            result[index][2] = -(numbersQueue.sum())

            println()

            println(result[index][0])
            println(result[index][1])
            println(result[index][2])

            println()
        }*/

        return result
    }

    fun solveLinearEquation(firstEquation: String, secondEquation: String): Pair<Double, Double>? {
        // Simplify equations
        val equations = getCoefficientsLinearEquation(firstEquation, secondEquation)

        // Calculate equation
        val determinant = equations[0][0] * equations[1][1] - equations[1][0] * equations[0][1]
        if (determinant == 0.0) {
            return null
        }

        val determinantX = equations[0][2] * equations[1][2] - equations[1][2] * equations[0][1]
        val determinantY = equations[0][0] * equations[1][2] - equations[1][0] * equations[0][2]
        val x = determinantX / determinant
        val y = determinantY / determinant

        return Pair(x, y)
    }

}