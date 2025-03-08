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
data class TripleSolve<T, C, D>(var list: T, var iterator: C, var compute: D)
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
        var closeBrackets = 1

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
            else if (transformedEquation[i] == '+' || transformedEquation[i] == '-') {
                if (closeBrackets == 1) {
                    return i+1
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
                            println("!multiplyDivide")
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
                            if (transformedEquation.isNotEmpty()) {
                                if (transformedEquation.last().toString()[0].isDigit()) {
                                    transformedEquation.add('×')
                                }
                            }

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
        println(transformedEquation)
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

    // Solve Equation Calculator
    private fun connectUnknowns(unknowns: MutableList<Char>,
                                multipliers: MutableList<Double>, flag: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()

        if (multipliers.isNotEmpty()) {
            var numberBuffer = multipliers[0]
            for (i in  1 until multipliers.size) {
                if (flag) {
                    numberBuffer *= multipliers[i]
                }
                else {
                    numberBuffer /= multipliers[i]
                }
            }
            result.add(numberBuffer)
        }

        if (unknowns.isNotEmpty()) {
            for (letter in unknowns) {
                result.add(letter)
            }
        }

        return result
    }

    private fun getUnknown(unknowns: MutableList<Char>,
                                         stack: MutableList<Char>?, negativeMul: Boolean, negativeStack: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()
        val unknown = mutableListOf<Char>()

        unknown.addAll(unknowns)

        // Count unknowns
        val map = HashMap<Char, Int>()
        for (letter in unknown) {
            if (negativeMul) {
                map[letter] = map.getOrDefault(letter, 0) - 1
            }
            else {
                map[letter] = map.getOrDefault(letter, 0) + 1
            }
        }

        // Divide by unknowns
        if (stack != null) {
            for (letter in stack) {
                if (negativeStack) {
                    map[letter] = map.getOrDefault(letter, 0) - 1
                }
                else {
                    map[letter] = map.getOrDefault(letter, 0) + 1
                }
            }
        }

        // Prepare form of x^2y^3...
        for ((key, value) in map) {
            if (value != 0) {
                result.add(key)
                result.add('^')
                println("VALUE OF KEY: $value")
                result.add(value.toDouble())
            }
        }

        return result
    }

    private fun calculateUnknownsPreparation(equation: MutableList<Any>,
                                             unknowns: MutableList<Char>,
                                             multipliers: MutableList<Double>,
                                             multiply: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()
        val subEquation = connectUnknowns(unknowns, multipliers, true)
        var flag = false
        if (!multiply) {
            flag = true
        }

        println("SUBEQUATION: $subEquation")
        // Get multipliers
        val multiplierNumber = if (subEquation.isEmpty()) {
            1.0
        }else {
            when (subEquation[0]) {
                is Double -> subEquation[0] as Double
                else -> {1.0}
            }
        }

        val multiplierUnknowns = mutableListOf<Char>()
        for (i in 1 until subEquation.size) {
            multiplierUnknowns.add(subEquation[i] as Char)
        }

        println(multiplierUnknowns)
        println("EQUATION ORG: $equation")
        println(subEquation)
        println(multiplierUnknowns)

        var powerTo = false
        var negative = false
        val stack = mutableListOf<Char>()
        for (element in equation) {
            when (element) {
                is Double -> {
                    if (!powerTo) {
                        if (!flag) {
                            result.add(element * multiplierNumber)
                        }
                        else {
                            println("No power")
                            println(multiplierNumber)
                            println(element)
                            result.add(element / multiplierNumber)
                        }
                    }
                    else {
                        if (element < 0) {
                            negative = true
                        }

                        if (stack.isNotEmpty()) {
                            for (i in 1 until abs(element.toInt())) {
                                stack.add(stack.last())
                            }
                        }
                    }
                }
                '^' -> {
                    powerTo = true
                }
                '+', '-' -> {
                    // Append unknown in form of x^1y^2..
                    if (stack.isNotEmpty()) {
                        if (negative) {
                            val unknown = getUnknown(multiplierUnknowns, stack, flag, true)
                            println("ELEMENT: $element")
                            println("STACK: $stack")
                            result.addAll(unknown)
                            stack.clear()
                        }
                        else {
                            val unknown = getUnknown(multiplierUnknowns, stack, flag, false)
                            result.addAll(unknown)
                            stack.clear()
                        }
                    }
                    else {
                        if (negative) {
                            val unknown = getUnknown(multiplierUnknowns, null, flag, true)
                            result.addAll(unknown)
                        }
                        else {
                            val unknown = getUnknown(multiplierUnknowns, null, flag, false)
                            result.addAll(unknown)
                        }
                    }

                    println(element)
                    result.add(element)
                    powerTo = false
                    negative = false
                }
                else -> {
                    stack.add(element as Char)
                    powerTo = false
                    negative = false
                }
            }
        }
        if (result.isNotEmpty()) {
            if (stack.isNotEmpty()) {
                if (negative) {
                    val unknown = getUnknown(multiplierUnknowns, stack, flag, true)
                    println("DONE")
                    println("STACK: $stack")
                    result.addAll(unknown)
                    stack.clear()
                }
                else {
                    val unknown = getUnknown(multiplierUnknowns, stack, flag, false)
                    result.addAll(unknown)
                    stack.clear()
                }
            }
            else if (result.last() is Double && !powerTo) {
                if (negative) {
                    val unknown = getUnknown(multiplierUnknowns, null,
                        negativeMul = flag,
                        negativeStack = true
                    )
                    result.addAll(unknown)
                    stack.clear()
                }
                else {
                    val unknown = getUnknown(multiplierUnknowns, null,
                        negativeMul = flag,
                        negativeStack = true
                    )
                    result.addAll(unknown)
                    stack.clear()
                }
            }
        }

        return result
    }

    private fun getUnknowns(equation: MutableList<Any>): MutableList<Char> {
        val result = mutableListOf<Char>()

        var lastUnknown = '0'
        for (element in equation) {
            when (element) {
                is Char -> {
                    if (element.isLetter()) {
                        lastUnknown = element
                    }
                }
                is Double -> {
                    if (lastUnknown != '0') {
                        for (i in 0 until element.toInt()) {
                            result.add(lastUnknown)
                        }
                    }
                    lastUnknown = '0'
                }
            }
        }

        return result
    }

    private fun getMultipliers(equation: MutableList<Any>): MutableList<Double> {
        val result = mutableListOf<Double>()

        var lastUnknown = '0'
        for (element in equation) {
            when (element) {
                is Char -> {
                    lastUnknown = element
                }
                is Double -> {
                    if (!lastUnknown.isLetter() && lastUnknown != '^') {
                        result.add(element)
                    }
                }
            }
        }

        return result
    }

    private fun tearIntoPiecesEquation(equation: MutableList<Any>): MutableList<MutableList<Any>> {
        val result = mutableListOf<MutableList<Any>>()

        val buffer = mutableListOf<Any>()
        for (i in equation) {
            when (i) {
                '+', '-' -> {
                    if (buffer.isNotEmpty() && buffer.last() is Char) {
                        if ((buffer.last() as Char).isLetter()) {
                            buffer.add('^')
                            buffer.add(1.0)
                        }
                    }
                    val copy = mutableListOf<Any>()
                    copy.addAll(buffer)
                    result.add(copy)
                    buffer.clear()
                }
                else -> {
                    if (buffer.isNotEmpty() && buffer.last() is Char) {
                        if ((buffer.last() as Char).isLetter()) {
                            if (i is Char && i.isLetter()) {
                                if (buffer != i) {
                                    buffer.add('^')
                                    buffer.add(1.0)
                                }
                            }
                        }
                    }
                    buffer.add(i)
                }
            }
        }
        if (buffer.isNotEmpty()) {
            if (buffer.last() is Char) {
                if ((buffer.last() as Char).isLetter()) {
                    buffer.add('^')
                    buffer.add(1.0)
                }
            }
            result.add(buffer)
            println("BUFFER $buffer")
        }

        return result
    }

    private fun calculateUnknownsAndMultipliers(unknowns: MutableList<Char>,
                                                multipliers: MutableList<Double>, flag: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()
        val subEquation = connectUnknowns(unknowns, multipliers, flag)
        println("SUB $subEquation")
        var number = 1.0
        var first = true

        val stack = mutableListOf<Char>()
        for (element in subEquation) {
            when (element) {
                is Double -> {
                    if (first) {
                        number = element
                        first = false
                    }
                    else if (flag) {
                        number *= element
                    }
                    else {
                        number /= element
                    }
                }
                is Char -> {
                    stack.add(element)
                }
            }
        }

        result.add(number)
        result.addAll(getUnknown(stack, null, flag, false))
        return result
    }

    private fun findEndBracketIndex(equation: MutableList<Any>, iterator: Int): Int {
        var i = iterator
        var brackets = 1
        while (i < equation.size) {
            when (equation[i]) {
                '+', '-' -> {
                    if (brackets == 1) {
                        return i+1
                    }
                }
                '(' -> brackets++
                ')' -> brackets--
            }
            if (brackets == 0) {
                return i+1
            }

            i++
        }

        return i
    }

    private fun calculateTwoEquations(first: MutableList<Any>, second: MutableList<Any>, flag: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()

        // Get every piece of first and second equation
        val firstPieces = tearIntoPiecesEquation(first)
        val secondPieces = tearIntoPiecesEquation(second)
        println("calculateTwoEquations")
        println(firstPieces)
        println(secondPieces)
        // Get operators from first equation
        val stack = mutableListOf<Char>()
        for (operator in first) {
            when (operator) {
                '+', '-' -> stack.add(operator as Char)
            }
        }

        // Calculate equations
        for (element in firstPieces) {
            for (tab in secondPieces) {
                val unknowns = getUnknowns(tab)
                val multipliers = getMultipliers(tab)
                println("CALC")
                println(calculateUnknownsPreparation(element, unknowns, multipliers, flag))
                result.addAll(calculateUnknownsPreparation(element, unknowns, multipliers, flag))
                if (stack.isNotEmpty()) {
                    result.add(stack.removeFirst())
                }
                else {
                    result.add('+')
                }
            }
        }
        if (result.isNotEmpty()) {
            if (result.last() == '+') {
                result.removeLast()
            }
        }
            /*
            for (element in secondPieces) {
                result.addAll(element)
                result.add('/')
                result.add('(')
                for (el in first) {
                    result.add(el)
                }
                if (stack.isNotEmpty()) {
                    result.add(stack.removeFirst())
                }
                else {
                    result.add('+')
                }
            }
            if (result.isNotEmpty()) {
                if (result.last() == '+') {
                    result.removeLast()
                }
                result.add(')')
            }
             */

        return result
    }

    private fun transformEquationForSolvingUnknowns(equation: MutableList<Any>, index: Int, flag: Boolean): TripleSolve<MutableList<Any>, Int, Boolean> {
        // Variables
        val result = mutableListOf<Any>()
        val unknowns = mutableListOf<Char>()
        val multipliers = mutableListOf<Double>()

        var lastElement: Any = 0
        var multiply = false
        var divide = false
        var keepMultiply = false
        var compute = flag

        println("EQ: $equation")
        var number = false
        var iterator = index
        while (iterator < equation.size) {
            when (equation[iterator]) {
                // Check special chars
                '(' -> {
                    if (result.isNotEmpty()) {
                        if (result.last() == '×') {
                            result.removeLast()
                        }
                    }
                    println("BEFORE; $result")
                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1, true)
                    println("AFTER ${subEquation.list}")
                    iterator = subEquation.iterator
                    compute = subEquation.compute
                    if (result.isNotEmpty()) {
                        if (result.last() == '+' || result.last() == '-') {
                            keepMultiply = false
                        }
                    }
                    println(iterator)
                    println(equation.size)
                    // Check for brackets multiplication or division
                    if (compute) {
                        var run = false
                        if (iterator < equation.size) {
                            println("ITERATOR IN: ${equation[iterator]}")
                            if (equation[iterator] == '×') {
                                println("KEEP MULTIPLY")
                                multiply = true
                                keepMultiply = true
                                iterator++

                                val endBracket = findEndBracketIndex(equation, iterator)
                                equation.add(iterator, '(')
                                equation.add(endBracket, ')')
                                run = true
                                println(equation)
                            }
                            else if (equation[iterator] == '/') {
                                println("KEEP DIVIDE")
                                divide = true
                                iterator++
                                keepMultiply = true
                                compute = false

                                val endBracket = findEndBracketIndex(equation, iterator)
                                equation.add(iterator, '(')
                                equation.add(endBracket, ')')
                                run = true
                            }
                        }
                        println(result)
                        if (!keepMultiply || run) {
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (divide) {
                                    println("W")
                                    println(result)
                                    println(subEquation.list)
                                    result.addAll(calculateUnknownsPreparation(subEquation.list, unknowns, multipliers, true))

                                    unknowns.clear()
                                    multipliers.clear()
                                    compute = false
                                }
                                else if (multiply){
                                    println("|S")
                                    result.addAll(calculateUnknownsPreparation(subEquation.list, unknowns, multipliers, true))
                                    unknowns.clear()
                                    multipliers.clear()
                                }
                                else {
                                    result.addAll(subEquation.list)
                                }
                            }
                            else {
                                result.addAll(subEquation.list)
                            }
                        }
                        else {
                            val buffer: MutableList<Any>
                            if (divide) {
                                println("CALCULATION")
                                println(result)
                                println(subEquation.list)
                                buffer = calculateTwoEquations(result, subEquation.list, false)
                                result.clear()
                                result.addAll(buffer)
                                buffer.clear()
                            }
                            else {
                                buffer = calculateTwoEquations(result, subEquation.list, true)
                                result.clear()
                                result.addAll(buffer)
                                buffer.clear()
                            }
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }
                    else {
                        println("Add to list")

                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            if (divide) {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                            }
                            else {
                                println(unknowns)
                                println(multipliers)
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            }
                            unknowns.clear()
                            multipliers.clear()
                        }
                        else {
                            result.addAll(subEquation.list)
                        }
                    }
                    continue
                }
                ')' -> {
                    println("BEFORE CONN: $result")
                    // Append multiplication or division
                    if (!keepMultiply) {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            if (divide) {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                            }
                            else {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            }
                        }
                    }
                    else {
                       if (multiply && !divide){
                            val buffer = calculateUnknownsPreparation(result, unknowns, multipliers, true)
                            result.clear()
                            result.addAll(buffer)
                            buffer.clear()
                        }
                        else if (divide) {
                           if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                               val buffer = calculateUnknownsPreparation(result, unknowns, multipliers, false)
                               result.clear()
                               result.addAll(buffer)
                               buffer.clear()
                           }
                       }
                    }

                    println("AFTER CONN: $result")
                    return TripleSolve(result, iterator+1, compute)
                }
                '+', '-' -> {
                    println("+")
                    if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                        if (divide) {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                        }
                        else {
                            println("BEFORE +: $result")
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            println("AFFTER -: $result")
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }

                    multiply = false
                    divide = false
                }
                '×' -> {
                    if ((divide || !multiply) && !keepMultiply) {
                        unknowns.clear()
                        multipliers.clear()

                        if (result.isNotEmpty()) {
                            result.removeLast()
                        }

                        // Find number that should be multiply
                        var step = iterator-1
                        while (step >= 0) {
                            if (equation[step] != '+' && equation[step] != '-'
                                && equation[step] != '/' &&  equation[step] != '(' &&  equation[step] != ')') {
                                when (equation[step]) {
                                    is Double -> multipliers.add(equation[step] as Double)
                                    is Char -> unknowns.add(equation[step] as Char)
                                }
                            }
                            else {
                                break
                            }
                            step--
                        }
                    }

                    if (divide && !keepMultiply) {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            unknowns.clear()
                            multipliers.clear()
                        }
                        divide = false
                        keepMultiply = true
                    }

                    multiply = true
                }
                '/' -> {
                    if ((multiply || !divide) && !keepMultiply) {
                        unknowns.clear()
                        multipliers.clear()

                        if (result.isNotEmpty()) {
                            result.removeLast()
                        }

                        // Find number that should be divided
                        var step = iterator-1
                        while (step >= 0) {
                            if (equation[step] != '+' && equation[step] != '-'
                                && equation[step] != '/' &&  equation[step] != '(' &&  equation[step] != ')')  {
                                when (equation[step]) {
                                    is Double -> multipliers.add(equation[step] as Double)
                                    is Char -> {
                                        if (equation[step] != '×') {
                                            unknowns.add(equation[step] as Char)
                                        }
                                    }
                                }
                            }
                            else {
                                break
                            }
                            step--
                        }
                    }

                    if (multiply && !keepMultiply) {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            println("CHECK!?")
                            println(unknowns)
                            println(multipliers)
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                            unknowns.clear()
                            multipliers.clear()
                            println("NO")
                        }
                        multiply = false
                    }

                    val endBracket = findEndBracketIndex(equation, iterator+1)
                    equation.add(iterator+1, '(')
                    equation.add(endBracket, ')')
                    iterator++

                    divide = true
                }
                '^', '√' -> {
                    println("BEFORE POWER $result")
                    val endBracket = findEndBracketIndex(equation, iterator+1)
                    equation.add(endBracket, ')')

                    if (result.isEmpty()) {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            result.addAll(
                                calculateUnknownsAndMultipliers(
                                    unknowns,
                                    multipliers,
                                    false
                                )
                            )
                            unknowns.clear()
                            multipliers.clear()
                        }
                    }

                    println("POWER")
                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1, flag)
                    iterator = subEquation.iterator

                    println("APPEND")
                    // Handle root and power
                    var appended = false
                    var onlyNumbers = true
                    val list = mutableListOf<Double>()
                    for (element in subEquation.list) {
                        if (element is Char) {
                            if (element.isLetter()) {
                                onlyNumbers = false
                                break
                            }
                            else if (element == '^' || element == '√') {
                                onlyNumbers = false
                                break
                            }
                        }
                        if (element is Double) {
                            list.add(element)
                        }
                    }

                    if (onlyNumbers) {
                        subEquation.list.clear()
                        subEquation.list = mutableListOf(list.sum())
                    }

                    if (subEquation.list.size == 1) {
                        if (subEquation.list.last() is Double) {
                            if (result.isNotEmpty() && result.last() is Double) {
                                if (divide) {
                                    result[result.size-1] = result[result.size-1] as Double - subEquation.list.last() as Double + 1
                                    appended = true
                                }
                                else {
                                    result[result.size-1] = result[result.size-1] as Double + subEquation.list.last() as Double - 1
                                    appended = true
                                }
                            }
                        }
                    }

                    if (!appended && subEquation.list.isNotEmpty()) {
                        result.add(0, '(')
                        result.add(')')
                        result.add('^')
                        result.add('(')
                        result.addAll(subEquation.list)
                        result.add(')')
                    }

                    println("AFTER POWER $result")
                    println(equation[iterator])
                    println(iterator)
                    return TripleSolve(result, iterator+1, appended)
                }

                // Get unknowns
                is Char -> {
                    if (!multiply) {
                        when (lastElement) {
                            is Double -> multipliers.add(result.removeLast() as Double)
                            is Char -> unknowns.add(result.removeLast() as Char)
                        }
                    }

                    if (multiply || divide) {
                        if (result.isNotEmpty()) {
                            if (result.last() == '×') {
                                result.removeLast()
                            }
                        }
                        unknowns.add(equation[iterator] as Char)
                    }
                }
                // Get numbers
                is Double -> {
                    if (multiply || divide) {
                        multipliers.add(equation[iterator] as Double)
                    }
                    if (!multiply && !divide) {
                        result.add(equation[iterator] as Double)
                    }
                    number = true
                }
            }

            lastElement = equation[iterator]
            if (!multiply && !divide && !number) {
                result.add(equation[iterator])
            }
            println(equation[iterator])
            number = false
            iterator++
        }

        return TripleSolve(result, iterator+1, compute)
    }

    private fun groupUnknowns(equation: MutableList<Any>): MutableList<Any> {
        val result = mutableListOf<Any>()

        val map = HashMap<String, MutableList<Any>>()
        var value = mutableListOf<Any>()
        var key = ""

        for (element in equation) {
        }

        return result
    }

    private fun getCoefficientsLinearEquation(firstEquation: String, secondEquation: String): Array<DoubleArray> {
        val result = Array(2) { DoubleArray(3) }

        val transformedEquations = arrayOf(transformEquation(firstEquation), transformEquation(secondEquation))

        // Put 1 before every unknown without number next to it
        for (equation in transformedEquations) {
            var lastElement: Any = '0'
            var index = 0
            var limit = equation.size
            while (index < limit) {
                when (equation[index]) {
                    is Char -> {
                        if (equation[index].toString()[0].isLetter()) {
                            when (lastElement) {
                                is Char -> {
                                    if (lastElement != '^') {
                                        equation.add(index, '×')
                                        equation.add(index, 1.0)
                                        index += 2
                                        limit += 2
                                    }
                                }
                            }
                        }
                    }
                }
                lastElement = equation[index]
                index++
            }
        }

        val equations = arrayOf(transformEquationForSolvingUnknowns(transformedEquations[0], 0, false),
            transformEquationForSolvingUnknowns(transformedEquations[1], 0, false))

        println("END")
        println(equations[0].list)
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