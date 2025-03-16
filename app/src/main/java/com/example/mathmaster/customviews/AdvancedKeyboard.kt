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

    private fun findNewBracketIndex(transformedEquation: MutableList<Any>, variable: Boolean = false): Int {
        var openBrackets = 0
        var closeBrackets = 0

        if (variable) {
            closeBrackets++
        }

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
                            if (transformedEquation[i] != 'x' && transformedEquation[i] != 'y'
                                && transformedEquation[i] != 'z') {
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
            if (transformedEquation[i] == '=') {
                return i+1
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
                        negativeNumber = false

                        while (additionalOpenedBrackets.last().isNotEmpty()) {
                            transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                        }

                        if (lastChar == '(' && element == '-' && transformedEquation.last() !is Double) {
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
                        negativeNumber = false
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

                        if (negativeNumber) {
                            if (transformedEquation.last() is Double) {
                                transformedEquation[transformedEquation.size-1] = -(transformedEquation[transformedEquation.size-1] as Double)
                                transformedEquation.add(addBracketIndex, '-')
                            }
                        }

                        transformedEquation.add('^')
                        additionalOpenedBrackets.last().add(')')

                        powerToOpenedBrackets.add(additionalOpenedBrackets.size-1)
                        inRoot = false
                        inDegree = false
                        negativeNumber = false
                    }
                    'π', 'e' -> {
                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                                && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                                && transformedEquation.last() != '(' && transformedEquation.last() != '√') {
                                if (!multiplyDivide) {
                                    addBracketIndex = findNewBracketIndex(transformedEquation)
                                    transformedEquation.add(addBracketIndex, '(')
                                    additionalOpenedBrackets.last().add(')')

                                    multiplyDivide = true
                                }
                            }
                        }

                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last().toString()[0].isDigit()) {
                                transformedEquation.add('×')
                            }
                        }

                        val constant = if (element == 'π') PI else Math.E
                        transformedEquation.add(constant)
                        inDegree = false
                    }
                    'x', 'y', 'z' -> {
                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                                && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                                && transformedEquation.last() != '(' && transformedEquation.last() != '√') {
                                if (!multiplyDivide) {
                                    addBracketIndex = findNewBracketIndex(transformedEquation, true)
                                    transformedEquation.add(addBracketIndex, '(')
                                    additionalOpenedBrackets.last().add(')')

                                    multiplyDivide = true
                                }
                            }
                        }

                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last().toString()[0].isDigit()) {
                                transformedEquation.add('×')
                            }
                        }

                        transformedEquation.add(element)
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
                        negativeNumber = false
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

                        // Equation variables
                        intConverter = 0
                        whatFunction = '0'
                        lastChar = '?'

                        commaInUse = false
                        numberBase = 0.0
                        numBuffer.clear()

                        // Equation validation
                        multiplyDivide = false
                        inDegree = false
                        addDegree = false
                        inRoot = false
                        negativeNumber = false

                        // Brackets
                        bracketsInput.clear()
                        powerToOpenedBrackets.clear()
                        additionalOpenedBrackets.clear()
                        additionalOpenedBrackets.add(mutableListOf())

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
                        && textView.text.last() != 'y' && textView.text.last() != 'z') {
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
                        || textView.text.last() == 'x' || textView.text.last() == 'y'
                        || textView.text.last() == 'z') {

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
                    || textView.text.last() == 'y' || textView.text.last() == 'z') {
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
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z') {
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
                    || textView.text.last() == 'y' || textView.text.last() == 'z') {
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
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z') {
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
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z') {
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
                    || textView.text.last() == 'y' || textView.text.last() == 'z') {
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
                    && textView.text.last() != 'y' || textView.text.last() != 'z') {
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

        gridLayout.removeView(sinButton)
        gridLayout.removeView(cosButton)
        gridLayout.removeView(tgButton)
        gridLayout.removeView(factorialButton)
        gridLayout.removeView(fractionButton)

        var params = rootButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(4, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        rootButton.layoutParams = params

        params = powerButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(3, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        powerButton.layoutParams = params

        params = changeFunctionsButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(1, 1f)
        changeFunctionsButton.layoutParams = params

        params = openBracketButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(2, 1f)
        openBracketButton.layoutParams = params

        params = closeBracketButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(3, 1f)
        closeBracketButton.layoutParams = params

        params = changeFunctionsButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        changeFunctionsButton.layoutParams = params

        params = degreeButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(3, 1f)
        degreeButton.layoutParams = params

        params = deleteButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(1, 1f)
        deleteButton.layoutParams = params

        params = clearButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        clearButton.layoutParams = params

        params = percentButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(4, 1f)
        percentButton.layoutParams = params

        logarithmButton.text ="x"
        naturalLogarithmButton.text = "y"
        degreeButton.text = "z"
        changeFunctionsButton.text = context.getString(R.string.EqAdd)
        percentButton.text = context.getString(R.string.EqSubtract)
        variableButton.text = "✓"
    }

    private fun xVariableClick(textView: TextView) {
        logarithmButton.setOnClickListener {
            logarithmButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z') {
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
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z') {
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

    private fun zVariableClick(textView: TextView) {
        degreeButton.setOnClickListener {
            degreeButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z') {
                    textView.append(degreeButton.text.toString())
                }
            } else {
                textView.append(degreeButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                degreeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

   fun getAddEquation(): Button {
        return changeFunctionsButton
    }

   fun getSubtractEquation(): Button {
        return percentButton
    }

    fun getCheckButton(): Button {
        return variableButton
    }

    private fun enterWhenInUnknownsCalculatorMode(textView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && !equalSign) {
                if (textView.text.last().isDigit() || textView.text.last() == 'x'
                    || textView.text.last() == 'y' || textView.text.last() == 'z'
                    || textView.text.last() == ')') {
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
        numberPIButtonClick(textView, blank)
        numberEulerButtonClick(textView, blank)
        clearButtonClick(textView, blank, blank)
        openBracketButtonClick(textView)
        closeBracketButtonClick(textView)
        deleteButtonClick(textView, blank)
        xVariableClick(textView)
        yVariableClick(textView)
        zVariableClick(textView)
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
        }

        return result
    }

    private fun calculateUnknownsAndMultipliers(unknowns: MutableList<Char>,
                                                multipliers: MutableList<Double>, flag: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()
        val subEquation = connectUnknowns(unknowns, multipliers, flag)
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
                    else if (!flag) {
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
        result.addAll(getUnknown(stack, null, !flag, false))
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

        return i+1
    }

    private fun calculateTwoEquations(first: MutableList<Any>, second: MutableList<Any>, flag: Boolean): MutableList<Any> {
        val result = mutableListOf<Any>()

        // Get every piece of first and second equation
        val firstPieces = tearIntoPiecesEquation(first)
        val secondPieces = tearIntoPiecesEquation(second)

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

        return result
    }

    private fun addBracketsForSpecialOperations(equation: MutableList<Any>): MutableList<Any>{
        val transformedEquation = mutableListOf<Any>()
        transformedEquation.addAll(equation)

        var i = 0
        while (i < transformedEquation.size) {
            if (transformedEquation[i] == '(') {
                if (i-1 >= 0 && (transformedEquation[i-1] == '×' || transformedEquation[i-1]  == '/')) {
                    var j = i-2
                    var brackets = 0
                    var bracket = false
                    var run = false
                    while (j >= 0) {
                        when (transformedEquation[j]) {
                            '+', '-' -> {
                                if (brackets == 0) {
                                    break
                                }
                            }
                            '/' -> run = true
                            '(' -> brackets--
                            ')' -> {
                                bracket = true
                                brackets++
                            }
                        }
                        if ((brackets == 0 && bracket) || brackets < 0) {
                            break
                        }

                        j--
                    }

                    if (run) {
                        transformedEquation.add(j, '(')
                        transformedEquation.add(i, ')')
                        i +=2
                    }
                }
            }
            i++
        }

        // Put 1 before every unknown without number next to it
        var lastElement: Any = '0'
        var index = 0
        var limit = equation.size
        while (index < limit) {
            when (transformedEquation[index]) {
                is Char -> {
                    if (transformedEquation[index].toString()[0].isLetter()) {
                        when (lastElement) {
                            is Char -> {
                                if (lastElement != '^') {
                                    transformedEquation.add(index, '×')
                                    transformedEquation.add(index, 1.0)
                                    index += 2
                                    limit += 2
                                }
                            }
                        }
                    }
                }
            }
            lastElement = transformedEquation[index]
            index++
        }

        return transformedEquation
    }

    private fun transformEquationForSolvingUnknowns(equation: MutableList<Any>, index: Int, flag: Boolean, subtract: Boolean = false): TripleSolve<MutableList<Any>, Int, Boolean> {
        // Variables
        val result = mutableListOf<Any>()
        val unknowns = mutableListOf<Char>()
        val multipliers = mutableListOf<Double>()

        var lastElement: Any = '0'
        var multiply = false
        var divide = false
        var keepMultiply = false
        var compute = flag

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

                    var subtraction = false
                    if (lastElement == '-') {
                        subtraction = true
                    }

                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1, true, subtraction)
                    iterator = subEquation.iterator
                    compute = subEquation.compute
                    if (result.isNotEmpty()) {
                        if (result.last() == '+' || result.last() == '-') {
                            keepMultiply = false
                        }
                    }

                    // Check for brackets multiplication or division
                    if (compute) {
                        var run = false
                        if (iterator < equation.size) {
                            if (equation[iterator] == '×') {
                                multiply = true
                                keepMultiply = true
                                iterator++

                                val endBracket = findEndBracketIndex(equation, iterator)
                                equation.add(iterator, '(')
                                equation.add(endBracket, ')')
                                run = true
                            }
                            else if (equation[iterator] == '/') {
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
                        if (!keepMultiply || run) {
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (divide) {
                                    val buffer = calculateTwoEquations(calculateUnknownsAndMultipliers(unknowns, multipliers, true), subEquation.list, false)
                                    result.addAll(buffer)

                                    unknowns.clear()
                                    multipliers.clear()
                                    compute = false
                                }
                                else if (multiply){
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
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            if (divide) {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                                if (subEquation.list.isNotEmpty()) {
                                    result.add('/')
                                }
                            }
                            else {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                                if (subEquation.list.isNotEmpty()) {
                                    result.add('×')
                                }
                            }
                            unknowns.clear()
                            multipliers.clear()
                        }
                        result.addAll(subEquation.list)
                    }
                    number = false
                    continue
                }
                ')' -> {
                    // Append multiplication or division
                    if (!keepMultiply) {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            if (divide) {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            }
                            else {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
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
                        else if (divide && !multiply) {
                           if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                               val buffer = calculateUnknownsPreparation(result, unknowns, multipliers, false)
                               result.clear()
                               result.addAll(buffer)
                               buffer.clear()
                           }
                       }
                    }

                    return TripleSolve(result, iterator+1, compute)
                }
                '+', '-' -> {
                    if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                        if (divide) {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                        }
                        else {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }

                    multiply = false
                    divide = false
                    number = false
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
                    number = false
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

                    // Add brackets for divide
                    var i = iterator+1
                    var brackets = 0
                    var iteratorBefore = 0
                    var multiplication = false
                    var variable = false
                    var bracket = false
                    while (i < equation.size) {
                        when (equation[i]) {
                            '+', '-' -> {
                                if (brackets == 0) {
                                    i++
                                    break
                                }
                            }
                            '/' -> {
                                if (brackets == 0) {
                                    i++
                                    break
                                }
                            }
                            '×' -> {
                                if (!variable) {
                                    iteratorBefore = i + 1
                                }
                                multiplication = true
                            }
                            '(' -> {
                                bracket = true
                                brackets++
                            }
                            ')' -> brackets--
                            is Char -> {
                                if ((equation[i] as Char).isLetter()) {
                                    variable = true
                                }
                            }
                        }
                        if ((brackets == 0 && bracket) || brackets < 0) {
                            i++
                            break
                        }

                        i++
                    }
                    equation.add(iterator+1, '(')
                    if (!variable && multiplication) {
                        equation.add(iteratorBefore, ')')
                    }
                    else {
                        equation.add(i, ')')
                    }

                    divide = true
                    number = false
                }
                '^', '√' -> {
                    var subtraction = false
                    if (lastElement == '-') {
                        subtraction = true
                    }

                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1, flag)
                    iterator = subEquation.iterator

                    var appended = false
                    // If power to is equal 0
                    if (result.isEmpty()) {
                        if (unknowns.isNotEmpty()) {
                            if (subEquation.list.size == 1) {
                                if (subEquation.list.last() is Double) {
                                    if (subEquation.list.first() == 0.0) {
                                        unknowns.clear()
                                        multipliers.add(1.0)
                                        appended = true
                                    }
                                }
                            }

                            if (divide) {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            }
                            else {
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                            }
                        }
                    }

                    // Handle root and power
                    if (subEquation.list.size == 1 && unknowns.isNotEmpty() && !appended) {
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
                        if (subtraction) {
                            result.add(0, '-')
                        }
                        result.add(0, '(')
                        result.add(0, '(')
                        result.add(')')
                        result.add('^')
                        result.add('(')
                        result.addAll(subEquation.list)
                        result.add(')')
                        result.add(')')
                    }

                    unknowns.clear()
                    multipliers.clear()

                    return TripleSolve(result, iterator, appended)
                }
                '=' -> {
                    if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                        if (divide) {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                        }
                        else {
                            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }

                    multiply = false
                    divide = false
                    number = false

                    result.add('=')
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

            if (!multiply && !divide && !number && equation[iterator] != '=') {
                if (equation[iterator] == '+' && subtract) {
                    result.add('-')
                }
                else if (equation[iterator] == '-' && subtract) {
                    result.add('+')
                }
                else {
                    result.add(equation[iterator])
                }
            }

            iterator++
        }
        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
            result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
            unknowns.clear()
            multipliers.clear()
        }

        return TripleSolve(result, iterator+1, compute)
    }

    private fun appendKeyWithValue(map: LinkedHashMap<MutableList<Any>, Double>, key: MutableList<Any>, value: Double, operator: Char, operatorsIndexes: MutableList<Int>, result: MutableList<Char>, equalSign: Boolean, insideRec: Boolean) {
        var inserted = false
        var valueNumber = false
        if (key.isEmpty()) {
            key.add("value")
        }

        if (key.first() == "value") {
            valueNumber = true
        }

        var positive = true
        val number = map.getOrDefault(key, 0.0)
        if (number != 0.0) {
            inserted = true

            if (number < 0) {
                positive = false
            }
        }

        if (valueNumber) {
            if (equalSign) {
                if (operator == '-') {
                    map[key] = number - value
                }
                else {
                    map[key] = number + value
                }
            }
            else {
                if (operator == '-') {
                    map[key] = number + value
                }
                else {
                    map[key] = number - value
                }
            }
        }
        else if (operator == '-') {
            if (equalSign && !insideRec) {
                map[key] = number + value
            }
            else {
                map[key] = number - value
            }

            if (map[key]!! < 0 && positive) {
                val index = operatorsIndexes[map.keys.indexOf(key)]
                if (result[index] == '-') {
                    result[index] = '+'
                }
            }
        } else {
            if (equalSign && !insideRec) {
                map[key] = number - value
            }
            else {
                map[key] = number + value
            }

            if (map[key]!! > 0 && !positive) {
                val index = operatorsIndexes[map.keys.indexOf(key)]
                if (result[index] == '-') {
                    result[index] = '+'
                }
            }
        }

        if (map.getOrDefault(key, 0.0) == 0.0 && inserted) {
            val index = operatorsIndexes[map.keys.indexOf(key)]
            result.removeAt(index)
            if (result.isNotEmpty()) {
                if (result.size == 1) {
                    result.clear()
                } else {
                    result.removeAt(result.size - 2)
                }
            }

            map.remove(key)
        } else if (inserted) {
            if (result.isNotEmpty()) {
                result.removeLast()
            }
        }
    }

    private fun appendEquationToResult(result: MutableList<Any>, map :LinkedHashMap<MutableList<Any>, Double>,
                                       value: Double, key: MutableList<Any>, operator: Char,
                                       operatorsList: MutableList<Char>, operatorsIndexes: MutableList<Int>,
                                       closeBracket: Boolean = false, mapChanged: Boolean = false, equalSign: Boolean,
                                       lastAddition: Boolean = false, insideRec: Boolean) {
        if (key.isNotEmpty() && value != 0.0) {
            appendKeyWithValue(map, key, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
        }
        else if (value != 0.0){
            appendKeyWithValue(map, key, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
        }

        // Get equation with operators
        val equationWithOperators = mutableListOf<Any>()
        var i = 0
        for ((k, v) in map) {
            var append = true
            if (lastAddition && k.first() == "value" && !insideRec) {
                append = false
            }

            // Append operator
            if (append) {
                if (i < operatorsList.size) {
                    if (operatorsList[i] == '=') {
                        operatorsList.removeAt(i)
                        i--
                    }
                    equationWithOperators.add(operatorsList[i])
                }

                // Append value
                equationWithOperators.add(v)

                // Check key
                var addKey = true
                if (k.first() is String)
                {
                    addKey = false
                }

                if (addKey) {
                    equationWithOperators.addAll(k)
                }
            }

            i++
        }
        if (!closeBracket) {
            if (equationWithOperators.isNotEmpty()) {
                if (equationWithOperators.first() is Char) {
                    equationWithOperators.removeFirst()
                }
            }
        }

        if (lastAddition) {
            equationWithOperators.add('=')
            equationWithOperators.add(map.getOrDefault(mutableListOf("value"), 0.0))
        }

        if (mapChanged && equationWithOperators.isEmpty()) {
            equationWithOperators.add(0.0)
        }

        result.addAll(equationWithOperators)

        // Clear lists
        map.clear()
        key.clear()
        operatorsList.clear()
        operatorsIndexes.clear()
    }

    private fun groupUnknowns(equation: MutableList<Any>, eqSign: Boolean, insideRec: Boolean, iterator: Int): Pair<MutableList<Any>, Int> {
        val result = mutableListOf<Any>()
        val operatorsList = mutableListOf<Char>()

        val map = LinkedHashMap<MutableList<Any>, Double>()
        var value = 0.0
        val key = mutableListOf<Any>()

        var powerTo = false
        var operator = '0'
        val operatorsIndexes = mutableListOf<Int>()
        var closeBracket = false
        var numberWas = false
        var equalSign = eqSign
        var i = iterator
        while (i < equation.size) {
            when (equation[i]) {
                is Double -> {
                    if (powerTo) {
                        key.add(equation[i])
                    }
                    else {
                        value = equation[i] as Double

                        numberWas = true
                        if (operatorsList.isEmpty()) {
                            operatorsList.add('+')
                        }
                    }
                }
                is Char -> {
                    when  (equation[i]) {
                        '=' -> {
                            operatorsList.add('=')
                            operatorsIndexes.add(operatorsList.size-1)
                            val buffer = mutableListOf<Any>()
                            buffer.addAll(key)
                            appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, false, insideRec)
                            equalSign = true
                            value = 0.0
                            key.clear()
                            powerTo = false
                            operator = '0'
                            closeBracket = true
                        }
                        '^' -> {
                            if (powerTo) {
                                key.add(equation[i])
                            }
                            else {
                                result.add('^')
                            }
                        }
                        '+', '-' -> {
                            operatorsList.add('+')
                            operatorsIndexes.add(operatorsList.size-1)

                            if (key.isNotEmpty()) {
                                val buffer = mutableListOf<Any>()
                                buffer.addAll(key)
                                appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            }
                            else if (value != 0.0) {
                                val buffer = mutableListOf<Any>("value")
                                appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            }

                            operator = equation[i] as Char
                            key.clear()
                            powerTo = false
                            value = 0.0
                        }
                        '(' ->  {
                            if (key.isNotEmpty()) {
                                val buffer = mutableListOf<Any>()
                                buffer.addAll(key)
                                appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            }
                            else if (value != 0.0) {
                                val buffer = mutableListOf<Any>("value")
                                appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            }

                            // If there is a multiplier for a brackets get it
                            val list = mutableListOf<Double>()
                            val operators = mutableListOf<Char>()
                            var thereIsKey = false
                            var multiplicand: Double

                            if (key.isNotEmpty()) {
                                multiplicand = map.getOrDefault(key, 0.0)
                                if (multiplicand != 0.0) {
                                    map.remove(key)
                                    thereIsKey = true
                                }
                            }
                            else {
                                multiplicand = map.getOrDefault(mutableListOf("multiplication", key), 0.0)
                                for (j in operatorsList.size-1 downTo 0) {
                                    if (operatorsList[j] == '×') {
                                        if (multiplicand != 0.0) {
                                            operators.add(operatorsList.removeLast())
                                            list.add(multiplicand)
                                            map.remove(mutableListOf("multiplication", key))
                                        }
                                        else {
                                            operatorsList.removeLast()
                                        }
                                    }
                                    else if (operatorsList[j] == '/') {
                                        operatorsList.removeLast()
                                        map.remove(mutableListOf("division", key))
                                    }
                                    else {
                                        break
                                    }
                                }
                            }

                            val specialAddition = mutableListOf<Any>()
                            if (thereIsKey) {
                                if (operator == '-' && multiplicand < 0) {
                                    specialAddition.add(-multiplicand)
                                }
                                else {
                                    specialAddition.add(multiplicand)
                                }
                                specialAddition.addAll(key)
                                key.clear()
                            }
                            else {
                                if (list.isNotEmpty()) {
                                    var k = 0
                                    while (k < operators.size) {
                                        specialAddition.add(operators[k])
                                        specialAddition.add(list[k])
                                        k++
                                    }
                                }
                            }

                            if (thereIsKey && specialAddition.isNotEmpty()) {
                                result.addAll(specialAddition)
                                specialAddition.clear()
                            }

                            // Put results together
                            val bufferEquation = groupUnknowns(equation, eqSign = true, insideRec = true, i+1)
                            bufferEquation.first.add(0, '(')
                            if (operator != '0' && result.isNotEmpty()) {
                                if (operator == '-') {
                                    result.add('-')
                                }
                                else {
                                    bufferEquation.first.add(0, '+')
                                }
                            }
                            else if (operator == '-') {
                                result.add('-')
                            }
                            result.addAll(bufferEquation.first)
                            if (specialAddition.isNotEmpty()) {
                                specialAddition.add(1, '(')
                                specialAddition.add(')')
                                result.addAll(specialAddition)
                            }
                            i = bufferEquation.second
                            value = 0.0
                            key.clear()
                            powerTo = false
                            operator = '0'
                            closeBracket = true
                            continue
                        }
                        ')' -> {
                            appendEquationToResult(result, map, value, key, operator, operatorsList, operatorsIndexes, closeBracket, numberWas, equalSign, insideRec = insideRec)
                            result.add(')')
                            return Pair(result, i+1)
                        }
                        '×' -> {
                            operatorsList.add(equation[i] as Char)
                            operatorsIndexes.add(operatorsList.size-1)
                            val buffer = mutableListOf("multiplication", key)
                            appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            value = 0.0
                        }
                        '/' -> {
                            operatorsList.add(equation[i] as Char)
                            operatorsIndexes.add(operatorsList.size-1)
                            val buffer = mutableListOf("division", key)
                            appendKeyWithValue(map, buffer, value, operator, operatorsIndexes, operatorsList, equalSign, insideRec)
                            value = 0.0
                        }
                        else -> {
                            powerTo = true
                            key.add(equation[i])
                        }
                    }
                }
            }
            i++
        }

        appendEquationToResult(result, map, value, key, operator, operatorsList, operatorsIndexes, closeBracket, mapChanged = false, equalSign, lastAddition = true, insideRec = insideRec)

        return Pair(result, i+1)
    }

    private fun calcPowerForStandardEquations(equation: MutableList<Any>, iterator: Int, subtract: Boolean = false, amountOfVariables: MutableList<Char>): Triple<Char?, Double, Int> {
        var result = 0.0
        var variable: Char? = null
        var lastNumber:Double? = null
        var base = 0.0
        var lastChar = '0'
        var powerTo = false
        var added = false
        var i = iterator
        while (i < equation.size) {
            when(equation[i]) {
                '+' -> {
                    if (lastNumber != null) {
                        result += lastNumber
                        added = true
                    }
                    base = 0.0
                    powerTo = false
                    lastChar = equation[i] as Char
                }
                '-' -> {
                    if (lastNumber != null) {
                        result -= lastNumber
                        added = true
                    }
                    base = 0.0
                    powerTo = false
                    lastChar = equation[i] as Char
                }
                '(' -> {
                    val nextBracket = if (lastChar == '-') {
                        calcPowerForStandardEquations(equation, iterator = i+1, subtract = true, amountOfVariables = amountOfVariables)
                    }
                    else {
                        calcPowerForStandardEquations(equation, iterator = i+1, amountOfVariables = amountOfVariables)
                    }

                    if (variable == null) {
                        variable = nextBracket.first
                    }

                    if (variable == null && powerTo && base != 0.0) {
                        result += base.pow(nextBracket.second)
                        powerTo = false
                    }
                    else if (variable == null) {
                        base = nextBracket.second
                        result += base
                    }
                    else {
                        result += nextBracket.second
                    }

                    i = nextBracket.third
                    continue
                }
                ')' -> {
                    if (lastNumber != null && !added) {
                        if (lastChar == '-') {
                            result -= lastNumber
                        }
                        else {
                            result += lastNumber
                        }
                    }

                    if (subtract) {
                        return Triple(variable, -result, i+1)
                    }
                    return Triple(variable, result, i+1)
                }
                '^' -> {
                    if (variable != null) {
                        result = 0.0
                        base = 0.0
                    }
                    result -= base
                    powerTo = true
                }
                is Double -> {
                    lastNumber = equation[i] as Double
                }
                is Char -> {
                    if (variable == null) {
                        variable = equation[i] as Char
                        amountOfVariables.add(variable)
                    }
                    result = 0.0
                    lastNumber = 0.0
                }
            }
            i++
        }

        return Triple(variable, result, i+1)
    }

    private fun getDegreeOfEquation(equation: MutableList<Any>): Double? {
        var result: Double? = null
        var powerTo = false
        var iterator = 0
        while (iterator < equation.size) {
            when(equation[iterator]) {
                '^' -> powerTo = true
                '(' -> {
                    val amountOfVariables: MutableList<Char> = mutableListOf()
                    val powerResult = calcPowerForStandardEquations(equation, iterator+1, amountOfVariables = amountOfVariables)
                    if (amountOfVariables.size > 1) {
                        return null
                    }
                    if (result == null) {
                        result = powerResult.second
                    }
                    else {
                        if (result < powerResult.second) {
                            result = powerResult.second
                        }
                    }
                    iterator = powerResult.third
                    powerTo = false
                    continue
                }
                is Double -> {
                    if (powerTo) {
                        if (result == null) {
                            result = equation[iterator] as Double
                        }
                        else {
                            if (result < equation[iterator] as Double) {
                                result = equation[iterator] as Double
                            }
                        }
                    }
                }
                else -> {
                    powerTo = false
                }
            }
            iterator++
        }

        return result
    }

    private fun transformEquationsForSolve(equationsList: MutableList<String>): MutableList<MutableList<Any>> {
        val list = mutableListOf<MutableList<Any>>()

        for (equation in equationsList) {
            val eq = groupUnknowns(transformEquationForSolvingUnknowns(addBracketsForSpecialOperations(transformEquation(equation)), 0 ,false).list, eqSign = false, insideRec = false,0).first
            list.add(eq)
        }

        return list
    }

    private fun getCoefficientsForSolveStandard(equationsList: MutableList<MutableList<Any>>): MutableList<MutableList<Triple<String, Double, Double>>> {
        val result = mutableListOf<MutableList<Triple<String, Double, Double>>>()
        val unknowns = mutableListOf<Char>()

        // Get unknowns
        for (equation in equationsList) {
            for (element in equation) {
                if (element is Char) {
                    if (element.isLetter()) {
                        var found = false

                        for (unknown in unknowns) {
                            if (unknown == element) {
                                found = true
                                break
                            }
                        }

                        if (!found) {
                            unknowns.add(element)
                        }
                    }
                }
            }
        }

        // Get coefficients
        for (equation in equationsList) {
            // Find every coefficient
            val map: HashMap<String, Pair<Double, Double>> = HashMap()
            var number = 0.0
            var powerTo = 0.0
            var equalSign = false
            var key: MutableList<Any>? = null
            var powerKey: MutableList<Any>? = null
            val amountOfVariables: MutableList<Char> = mutableListOf()
            var i = 0
            while (i < equation.size) {
                when(equation[i]) {
                    '+', '-' -> {
                        if (key != null) {
                            map[key.toString()] = Pair(number, powerTo)
                        }

                        if (powerKey != null) {
                            if (number == 0.0) {
                                number = 1.0
                            }
                            map[powerKey.toString()] = Pair(number, powerTo)
                        }

                        powerKey = null
                        key = null
                    }
                    '×' -> {
                        i++
                        continue
                    }
                    '(' -> {
                        if (powerKey == null) {
                            val equationInBrackets = calcPowerForStandardEquations(equation, iterator = i+1, amountOfVariables = amountOfVariables)
                            i = equationInBrackets.third

                            powerKey = if (equationInBrackets.first != null) {
                                mutableListOf(equationInBrackets.first.toString(), '^', equationInBrackets.second)
                            } else {
                                mutableListOf("value")
                            }

                            number = 0.0
                            powerTo = equationInBrackets.second
                            continue
                        }
                        else {
                            val equationInBrackets = calcPowerForStandardEquations(equation, iterator = i+1, amountOfVariables = amountOfVariables)
                            i = equationInBrackets.third
                            number = equationInBrackets.second
                            continue
                        }
                    }
                    ')' -> {
                        i++
                        continue
                    }
                    '=' -> {
                        if (key != null) {
                            map[key.toString()] = Pair(number, powerTo)
                        }
                        if (powerKey != null) {
                            if (number == 0.0) {
                                number = 1.0
                            }
                            map[powerKey.toString()] = Pair(number, powerTo)
                        }

                        powerKey = null
                        key = null
                        equalSign = true
                    }
                    is Char -> {
                        if (key == null) {
                            key = mutableListOf()
                        }
                        key.add(equation[i])
                    }
                    is Double -> {
                        if (equalSign) {
                            map["value"] = Pair(equation[i] as Double, 1.0)
                            break
                        }
                        if (key == null) {
                            number = equation[i] as Double
                        }
                        else {
                            key.add(equation[i])
                            powerTo = equation[i] as Double
                        }
                    }
                }
                i++
            }

            // Append coefficients and add them to result
            val coefficients: MutableList<Triple<String,Double,Double>> = mutableListOf()
            val unknownsCopy = mutableListOf<Char>()
            unknownsCopy.addAll(unknowns)

            for ((k, v) in map) {
                for (unknown in unknownsCopy) {
                    if (k[1] == unknown) {
                        unknownsCopy.remove(unknown)
                        break
                    }
                }

                coefficients.add(Triple(k, v.first, v.second))
            }

            // Append what lasts in unknowns
            for (unknown in unknownsCopy) {
                val buffer = mutableListOf<Any>(unknown, '^', 1.0)
                coefficients.add(Triple(buffer.toString(), 0.0, 1.0))
            }

            println(coefficients)
            result.add(coefficients)
        }

        return result
    }

    private fun sarrusMethodImplementation(equations: MutableList<MutableList<Double>>): Double {
        if (equations.size == 2) {
            return equations[0][0] * equations[1][1] - equations[1][0] * equations[0][1]
        }

        // Fill list
        equations.add(equations[0])
        equations.add(equations[1])

        // Variables
        var index = 0
        var steps = 0
        var lastNumber: Double? = null

        // Additions
        val addition = mutableListOf<Double>()
        var limit = 3
        var iterator = 0
        while (steps < 3) {
            while (iterator < limit) {
                if (lastNumber == null) {
                    lastNumber = equations[iterator][index]
                } else {
                    lastNumber *= equations[iterator][index]
                }
                index++
                iterator++
            }
            addition.add(lastNumber!!)
            lastNumber = null
            index = 0
            steps++
            limit++
            iterator = steps
        }

        // Subtractions
        val subtract = mutableListOf<Double>()
        index = 2
        steps = 0
        lastNumber = null
        limit = 3
        iterator = 0
        while (steps < 3) {
            while (iterator < limit) {
                if (lastNumber == null) {
                    lastNumber = equations[iterator][index]
                }
                else {
                    lastNumber *= equations[iterator][index]
                }
                index--
                iterator++
            }
            subtract.add(lastNumber!!)
            lastNumber = null
            index = 2
            steps++
            limit++
            iterator = steps
        }

        return addition.sum() - subtract.sum()
    }

    private fun replaceColumnWithResults(equations: MutableList<MutableList<Double>>, index: Int): MutableList<MutableList<Double>> {
        val result = mutableListOf<MutableList<Double>>()
        val dimension = equations.size

        for (row in equations.indices) {
            result.add(mutableListOf())
            for (col in equations[row].indices) {
                if (col == dimension) {
                    break
                }
                if (col == index) {
                    result.last().add(equations[row].last())
                }
                else {
                    result.last().add(equations[row][col])
                }
            }
        }

        return result
    }

    private fun removeResultsFromMatrix(equations: MutableList<MutableList<Double>>): MutableList<MutableList<Double>> {
        val result = mutableListOf<MutableList<Double>>()
        val dimension = equations.size

        for (row in equations.indices) {
            result.add(mutableListOf())
            for (col in equations[row].indices) {
                if (col == dimension) {
                    break
                }
                result.last().add(equations[row][col])
            }
        }

        return result
    }

    private fun solveLinearEquation(coefficients: MutableList<MutableList<Triple<String, Double, Double>>>): MutableList<Pair<Char, Double>>? {
        val result: MutableList<Pair<Char, Double>> = mutableListOf()
        val equations: MutableList<MutableList<Double>> = mutableListOf()
        val unknowns = mutableListOf<Char>()

        // Get unknowns
        for (equation in coefficients) {
            for (element in equation) {
                if (element.first != "value") {
                    var add = true
                    for (char in unknowns) {
                        if (char == element.first[1]) {
                            add = false
                            break
                        }
                    }
                    if (add) {
                        unknowns.add(element.first[1])
                    }
                }
            }
        }
        unknowns.sort()

        // Get coefficients
        for (equation in coefficients) {
            if (equation.size == 1) {
                continue
            }
            else if (equation.size == 2) {
                var variable:Char? = null
                var value = 0.0
                var number = 0.0
                for (element in equation) {
                    if (element.first == "value") {
                        value = element.second / number
                    }
                    else {
                        number = element.second
                        variable = element.first[1]
                    }
                }

                if (variable != null) {
                    if (result.isNotEmpty()) {
                        if (result.last().first != variable) {
                            result.add(Pair(variable, value))
                        }
                    }
                    else {
                        result.add(Pair(variable, value))
                    }

                    if (result.size == unknowns.size) {
                        return result
                    }
                }
            }
            else {
                val eq: MutableList<Double> = mutableListOf()
                val origin = equation.sortedBy { it.first }
                for (element in origin) {
                    if (element.first != "value") {
                        eq.add(element.second)
                    }
                }
                for (element in origin) {
                    if (element.first == "value") {
                        eq.add(element.second)
                        break
                    }
                }
                equations.add(eq)

                if (equations.size == unknowns.size) {
                    break
                }
                if (result.isNotEmpty()) {
                    break
                }
            }
        }

        // Prepare equation that is needed for calculating if found some variable before
        if (result.size >= 1 && equations.size == unknowns.size-result.size) {
            for (element in result) {
                val additionalEquation = mutableListOf<Double>()
                var index = 0
                for (unknown in unknowns) {
                    if (unknown == element.first) {
                        break
                    }
                    index++
                }

                for (i in 0 until unknowns.size) {
                    if (i == index) {
                        additionalEquation.add(1.0)
                    }
                    else {
                        additionalEquation.add(0.0)
                    }
                }
                additionalEquation.add(element.second)
                equations.add(additionalEquation)
            }
            result.clear()
        }

        // Calculate equation for two unknowns
        if (equations.size == unknowns.size) {
            val baseMatrix = removeResultsFromMatrix(equations)

            val determinant = sarrusMethodImplementation(baseMatrix)
            if (determinant == 0.0) {
                return null
            }

            for (i in 0 until unknowns.size) {
                val determinantUnknown = sarrusMethodImplementation(replaceColumnWithResults(equations, i))
                val unknown = determinantUnknown / determinant

                result.add(Pair(unknowns[i], unknown))
            }

            return result
        }

        if (result.size == 1 && equations.isEmpty()) {
            return mutableListOf(Pair(result[0].first, result[0].second))
        }

        return null
    }

    fun solveEquationsWithUnknowns(equationsList: MutableList<String>): MutableList<Pair<Char, Double>>? {
        val listOfEquations = transformEquationsForSolve(equationsList)

        // Get degree of equation
        var degreeOfEquation:Double? = 0.0
        for (equation in listOfEquations) {
            val degree = getDegreeOfEquation(equation)
            if (degree == null) {
                degreeOfEquation = null
                break
            }
            else {
                if (degree > degreeOfEquation!!) {
                    degreeOfEquation = degree
                }
            }
        }

        // Get coefficients
        val coefficients: MutableList<MutableList<Triple<String, Double, Double>>>
        if (degreeOfEquation != null) {
            coefficients = getCoefficientsForSolveStandard(listOfEquations)

            // Calculate result for linear equation
            if (degreeOfEquation == 1.0) {
                return solveLinearEquation(coefficients)
            }

            // Calculate result for quadratic equation
            else if (degreeOfEquation == 2.0) {
                return null
            }
        }

        return null
    }
}