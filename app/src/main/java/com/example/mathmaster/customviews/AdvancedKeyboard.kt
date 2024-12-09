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

data class PairEquation<Double, Int> (var first: Double, var second: Int)
data class Function<Int> (var start: Int, var end: Int, var deep: Int, var brackets: Int)

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

    // Special calculator buttons
    private val powerButton: Button
    private val commaButton: Button
    private val percentButton: Button
    private val factorialButton: Button
    private val fractionButton: Button
    private val numberPIButton: Button
    private val numberEulerButton: Button
    private val rootButton: Button

    private var commaUsed: Boolean = false

    // Functions of calculator
    private var specialFunctionDeep: Int = 0
    private val specialFunctions: MutableList<Function<Int>> = mutableListOf<Function<Int>>()
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

    // Options
    private val degreeButton: Button
    private val changeFunctionsButton: Button
    private var secondFunctions: Boolean = false

    private var addedDegrees: Boolean = false
    private var radians: Boolean = true

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
        percentButton = findViewById<Button>(R.id.Procent)
        factorialButton = findViewById<Button>(R.id.Factorial)
        fractionButton = findViewById<Button>(R.id.Fraction)
        numberPIButton = findViewById<Button>(R.id.PInumber)
        numberEulerButton = findViewById<Button>(R.id.Euler)
        rootButton = findViewById<Button>(R.id.Root)

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

        // Options
        degreeButton = findViewById<Button>(R.id.Degree)
        changeFunctionsButton = findViewById<Button>(R.id.Change)
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
        var inRoot = false
        var addBracketIndex = -1
        val openedBrackets = ArrayDeque<Int>()
        val openedBracketsInput = ArrayDeque<Int>()
        val bracketsInsideFunction: MutableList<MutableList<Int>> = mutableListOf()
        val bracketsInsideFunctionInput: MutableList<MutableList<Int>> = mutableListOf()

        // Functions
        var iterator: Int = 0
        var functionIndex: Int = -1
        var commaInUse: Boolean = true
        var whatFunction: Char = '0'
        var multiplicationDivisionLength: Int = 0

        equation.forEach { element ->
            if (element.isDigit()) {
                // Add digit to buffor
                numberBuffor.add((element.code - 48).toDouble())
                intConverter++

                // Decimal number
                if (lastChar == ',') {
                    val outputNumber: Double = calculateNumber(numberBuffor, intConverter, true)
                    transformedEquation.removeLast()

                    val decimalNumber: Double = numberBase + outputNumber

                    if (!commaInUse) {
                        if (!inRoot) {
                            transformedEquation.add('(')
                            openedBrackets.addLast(1)
                        }
                        commaInUse = true
                    }
                    transformedEquation.add(decimalNumber)
                }
            } else {
                // Clear buffors of constant numbers that has been added in number handler
                if (lastChar == ',') {
                    numberBuffor.clear()
                }

                // Append number that is in buffor
                if (numberBuffor.isNotEmpty() && whatFunction == '0') {
                    if (lastChar != ',') {
                        val outputNumber: Double = calculateNumber(numberBuffor, intConverter, false)
                        transformedEquation.add(outputNumber)

                        if (element == ',') {
                            numberBase = outputNumber
                        }
                    }
                }

                // Preparing brackets for multiplication and division
                if (element == '×' || element == '/') {
                    if (transformedEquation.isNotEmpty()) {
                        var openBrackets = 0
                        var closeBrackets = 0
                        var indexbuffor = 0
                        var index = 0
                        val range = transformedEquation.size - 1 downTo 0
                        for (i in range) {
                            if (transformedEquation[i] == ')') {
                                closeBrackets++
                            } else if (transformedEquation[i] == '(') {
                                openBrackets++
                            } else if (indexbuffor == 0 &&
                                (transformedEquation[i] == '+' || transformedEquation[i] == '-')) {
                                indexbuffor = i
                            }

                            if (closeBrackets == openBrackets) {
                                index = i
                                break
                            }
                        }
                        if (closeBrackets != openBrackets) {
                            if (indexbuffor == 0) {
                                addBracketIndex = 0
                            } else {
                                addBracketIndex = ++indexbuffor
                            }
                        } else if (index >= 1) {
                            if (transformedEquation[index - 1].toString()[0].isLetter()) {
                                index--
                            }
                            addBracketIndex = index
                        } else {
                            addBracketIndex = 0
                        }
                    }
                }

                // Root
                if (element == '√') {
                    if (functionIndex >= 0) {
                        if (inRoot && transformedEquation.last().toString()[0].isDigit()) {
                            bracketsInsideFunction[functionIndex].removeLast()
                            transformedEquation.add(')')
                            transformedEquation.add('×')
                        }
                        else if (transformedEquation.last().toString()[0].isDigit()) {
                            transformedEquation.add('×')
                        }
                        transformedEquation.add(element)
                        bracketsInsideFunction[functionIndex].add(1)
                    } else {
                        if (inRoot) {
                            openedBrackets.removeLast()
                            transformedEquation.add(')')
                            transformedEquation.add('×')
                        }
                        else if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last().toString()[0].isDigit()) {
                                transformedEquation.add('×')
                            }
                        }
                        transformedEquation.add(element)
                        openedBrackets.addLast(1)
                    }
                    inRoot = true
                }

                // Handle subtract and add
                if (element == '+' || element == '-') {
                    var run = false
                    if (multiplicationDivisionLength > 0) {
                        run = true
                    }
                    if (functionIndex >= 0) {
                        if (lastChar != '^' && lastChar != '(' && lastChar != '-'
                            && lastChar != '+') {
                            while (bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                if (run) {
                                    multiplicationDivisionLength--

                                    if (multiplicationDivisionLength <= 0) {
                                        break
                                    }
                                }

                                transformedEquation.add(')')
                                bracketsInsideFunction[functionIndex].removeLast()
                            }
                        }
                    } else {
                        if (lastChar != '^' && lastChar != '(' && lastChar != '-'
                            && lastChar != '+') {
                            while (openedBrackets.isNotEmpty()) {
                                if (run) {
                                    multiplicationDivisionLength--

                                    if (multiplicationDivisionLength <= 0) {
                                        break
                                    }
                                }

                                transformedEquation.add(')')
                                openedBrackets.removeLast()
                            }
                        }
                    }
                }

                // Recognize functions
                if (whatFunction != 'a') {
                    when (element) {
                        's' -> if (whatFunction != 'c') whatFunction = element
                        'c' -> whatFunction = element
                        't' -> whatFunction = element
                        'l' -> whatFunction = element
                        'a' -> whatFunction = element
                    }

                    if (whatFunction == 'l') {
                        when (element) {
                            'g' -> whatFunction = element
                            'n' -> whatFunction = element
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

                // Append function symbol and handle brackets
                if (whatFunction != '0' && element == '(') {
                    // Append multiplication if before number is other function or constants
                    if (lastChar == 'π' || lastChar == 'e') {
                        transformedEquation.add('×')
                        lastChar = '0'
                    }
                    else if (transformedEquation.isNotEmpty() && numberBuffor.isEmpty()) {
                        if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                            && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                            && transformedEquation.last() != '(') {
                            transformedEquation.add('×')
                        }
                    }

                    // Prepare brackets lists
                    bracketsInsideFunction.add(mutableListOf())
                    bracketsInsideFunctionInput.add(mutableListOf())

                    // Append number that is before function as multiplication
                    if (numberBuffor.isNotEmpty()) {
                        val outputNumber: Double = transformedEquation.removeLast() as Double
                        transformedEquation.add('(')
                        transformedEquation.add(outputNumber)
                        transformedEquation.add('×')
                        bracketsInsideFunction[functionIndex].add(1)
                    }

                    // Act as it is multiplication when some number is before function
                    if (lastChar == ',') {
                        transformedEquation.add('×')
                    }

                    transformedEquation.add(whatFunction)
                    whatFunction = '0'
                    functionIndex++
                }

                // Brackets handling
                else if (element == '(') {
                    if (functionIndex >= 0) {
                        bracketsInsideFunctionInput[functionIndex].add(1)
                    } else {
                        openedBracketsInput.addLast(1)
                    }
                } else if (element == ')') {
                    if (functionIndex >= 0) {
                        if (iterator == specialFunctions[functionIndex].end) {
                            while(bracketsInsideFunctionInput[functionIndex].isNotEmpty()) {
                                transformedEquation.add(')')
                                bracketsInsideFunctionInput[functionIndex].removeLast()
                            }
                            while(bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(')')
                                bracketsInsideFunction[functionIndex].removeLast()
                            }
                            functionIndex--
                        }
                        else {
                            if (bracketsInsideFunctionInput[functionIndex].isNotEmpty()) {
                                bracketsInsideFunctionInput[functionIndex].removeLast()
                            }
                        }
                    } else {
                        if (openedBracketsInput.isNotEmpty()) {
                            openedBracketsInput.removeLast()
                        }
                    }
                }

                // Pi and Euler number
                if (element == 'π' || element == 'e') {
                    val num = if (element == 'π') PI else Math.E

                    if (transformedEquation.isEmpty()) {
                        transformedEquation.add(num)
                    }
                    else if ((lastChar == '+' || lastChar == '-' || lastChar == '/' || lastChar == '×'
                        || lastChar == '^') && !transformedEquation.last().toString()[0].isDigit()) {
                        if (numberBuffor.isEmpty()) {
                            transformedEquation.add('(')
                            transformedEquation.add(num)
                            if (functionIndex >= 0) {
                                bracketsInsideFunction[functionIndex].add(1)
                            }
                            else {
                                openedBrackets.addLast(1)
                            }
                        }
                        else {
                            transformedEquation.add('×')
                            transformedEquation.add(num)
                        }
                    }
                    else {
                        if (numberBuffor.isEmpty() && !transformedEquation.last().toString()[0].isDigit()) {
                            transformedEquation.add('(')
                            transformedEquation.add(num)
                            if (functionIndex >= 0) {
                                bracketsInsideFunction[functionIndex].add(1)
                            }
                            else {
                                openedBrackets.addLast(1)
                            }
                        }
                        else {
                            transformedEquation.add('×')
                            transformedEquation.add(num)
                        }
                    }
                }

                // Handle power to
                if (element == '^') {
                    if (transformedEquation.last() == ')') {
                        // Count brackets that are before factorial
                        var openedBracketsCounter = 0
                        var closeBracketsCounter = 1
                        var index = transformedEquation.size-2

                        while(index >= 0 && openedBracketsCounter != closeBracketsCounter) {
                            if (transformedEquation[index] == '(') {
                                openedBracketsCounter++
                            }
                            else if (transformedEquation[index] == ')') {
                                closeBracketsCounter++
                            }
                            index--
                        }
                        if (index <= 0) {
                            index = 0
                        }
                        else {
                            index++
                        }

                        transformedEquation.add(index, '(')
                        transformedEquation.add(')')
                        transformedEquation.add('^')
                    }
                    else {
                        var index = 0
                        if (transformedEquation.size-1 >= 0) {
                            index = transformedEquation.size-1
                        }

                        transformedEquation.add(index, '(')
                        transformedEquation.add(')')
                        transformedEquation.add('^')
                    }
                }

                // Factorial and percent
                if (element == '!' || element == '%' || element == '°') {
                    if (transformedEquation.last() == ')') {
                        // Count brackets that are before factorial
                        var openedBracketsCounter = 0
                        var closeBracketsCounter = 1
                        var index = transformedEquation.size-2

                        while(index >= 0 && openedBracketsCounter != closeBracketsCounter) {
                            if (transformedEquation[index] == '(') {
                                openedBracketsCounter++
                            }
                            else if (transformedEquation[index] == ')') {
                                closeBracketsCounter++
                            }
                            index--
                        }
                        if (index <= 0) {
                            index = 0
                        }
                        else {
                            index++
                        }

                        transformedEquation.add(index, '(')
                        transformedEquation.add(')')
                        if (element == '!') {
                            transformedEquation.add('!')
                        }
                        else if (element == '%') {
                            transformedEquation.add('%')
                        }
                        else {
                            transformedEquation.add('°')
                        }

                        if (lastChar == '√' && element == '°') {
                            transformedEquation.removeLast()
                            transformedEquation.add(')')
                            transformedEquation.add('°')
                        }
                        else {
                            transformedEquation.add(')')
                        }
                    }
                    else {
                        var index = 0
                        if (transformedEquation.size-1 >= 0) {
                            index = transformedEquation.size-1
                        }
                        if (lastChar != '^') {
                            transformedEquation.add(index, '(')
                        }
                        transformedEquation.add(index, '(')
                        transformedEquation.add(')')
                        if (element == '!') {
                            transformedEquation.add('!')
                        }
                        else if (element == '%') {
                            transformedEquation.add('%')
                        }
                        else {
                            transformedEquation.add('°')
                        }
                        if (lastChar != '^') {
                            if (lastChar == '√' && element == '°') {
                                println("X")
                                transformedEquation.removeLast()
                                transformedEquation.add(')')
                                transformedEquation.add(')')
                                transformedEquation.add('°')
                            }
                            else {
                                transformedEquation.add(')')
                            }
                        }
                    }
                }

                // Append operators and get last char
                if (element == '+' || element == '-') {
                    transformedEquation.add(element)
                    lastChar = element
                    inRoot = false
                }
                else if (element == '/' || element == '×') {
                    if (addBracketIndex >= 0) {
                        transformedEquation.add(addBracketIndex, '(')
                        println("func" + functionIndex)
                        if (functionIndex >= 0) {
                            bracketsInsideFunction[functionIndex].add(1)
                        }
                        else {
                            openedBrackets.addLast(1)
                        }

                        transformedEquation.add(element)
                    }
                    else {
                        val index = if (transformedEquation.size - 1 >= 2)
                            transformedEquation.size - 2 else 0

                        transformedEquation.add(index, '(')
                        transformedEquation.add(element)

                        if (functionIndex >= 0) {
                            bracketsInsideFunction[functionIndex].add(1)
                        }
                        else {
                            openedBrackets.addLast(1)
                        }
                    }

                    multiplicationDivisionLength++
                    lastChar = element
                    inRoot = false
                } else if (element == '(' || element == ')') {
                    transformedEquation.add(element)
                    lastChar = element
                    inRoot = false
                }
                else if (element == '^') {
                    lastChar = element
                    inRoot = false
                }
                else if (element == ',' || element == '√' || element == 'π' || element == 'e') {
                    lastChar = element
                }
                else if (element == '!' || element == '%' || element == '°') {
                    inRoot = false
                }

                commaInUse = false
                addBracketIndex = -1
                intConverter = 0
                if (whatFunction == '0') {
                    numberBuffor.clear()
                }
            }
            iterator++
        }
        // Add everything that lasts in buffors
        if (numberBuffor.isNotEmpty() && lastChar != ',') {
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

        return transformedEquation
    }

    private fun factorial(number: Double): Double {
        if (number <= 1.0) {
            return 1.0
        }
        return number * factorial(number-1)
    }

    private fun calculate(equation: MutableList<Any>, index: Int): PairEquation<Double, Int> {
        var equationSign: Char = 'E'
        val result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index
        val threshold = 1E-10

        println(equation)

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
                            '^' -> result.first = (result.first).pow(equationBuffor.first)
                            'E' -> result.first = equationBuffor.first
                        }

                        iterator = equationBuffor.second
                    }
                    else if (equation[iterator] == '√') {
                        val equationBuffor = calculate(equation, iterator + 1)
                        val rootResult = sqrt(equationBuffor.first)

                        when (equationSign) {
                            '+' -> result.first += rootResult
                            '-' -> result.first -= rootResult
                            '×' -> result.first *= rootResult
                            '/' -> result.first /= rootResult
                            '^' -> result.first = (rootResult).pow(rootResult)
                            'E' -> result.first = rootResult
                        }

                        iterator = equationBuffor.second
                    }
                    else if (equation[iterator] == ')') {
                        result.first = if (abs(result.first) < threshold) 0.0 else result.first
                        return result
                    } else if ((equation[iterator] as Char).isLetter()) {
                        val equationBuffor = calculate(equation, iterator + 2)
                        when (equationSign) {
                            '+' -> {
                                when (equation[iterator]) {
                                    's' -> result.first += sin(equationBuffor.first)
                                    'c' -> result.first += cos(equationBuffor.first)
                                    't' -> result.first += tan(equationBuffor.first)
                                    'g' -> result.first += log(equationBuffor.first, 10.0)
                                    'n' -> result.first += ln(equationBuffor.first)
                                    'i' -> result.first += asin(equationBuffor.first)
                                    'o' -> result.first += acos(equationBuffor.first)
                                    'a' -> result.first += atan(equationBuffor.first)
                                }
                            }

                            '-' -> {
                                when (equation[iterator]) {
                                    's' -> result.first -= sin(equationBuffor.first)
                                    'c' -> result.first -= cos(equationBuffor.first)
                                    't' -> result.first -= tan(equationBuffor.first)
                                    'g' -> result.first -= log(equationBuffor.first, 10.0)
                                    'n' -> result.first -= ln(equationBuffor.first)
                                    'i' -> result.first -= asin(equationBuffor.first)
                                    'o' -> result.first -= acos(equationBuffor.first)
                                    'a' -> result.first -= atan(equationBuffor.first)
                                }
                            }

                            '×' -> {
                                when (equation[iterator]) {
                                    's' -> result.first *= sin(equationBuffor.first)
                                    'c' -> result.first *= cos(equationBuffor.first)
                                    't' -> result.first *= tan(equationBuffor.first)
                                    'g' -> result.first *= log(equationBuffor.first, 10.0)
                                    'n' -> result.first *= ln(equationBuffor.first)
                                    'i' -> result.first *= asin(equationBuffor.first)
                                    'o' -> result.first *= acos(equationBuffor.first)
                                    'a' -> result.first *= atan(equationBuffor.first)
                                }
                            }

                            '/' -> {
                                when (equation[iterator]) {
                                    's' -> result.first /= sin(equationBuffor.first)
                                    'c' -> result.first /= cos(equationBuffor.first)
                                    't' -> result.first /= tan(equationBuffor.first)
                                    'g' -> result.first /= log(equationBuffor.first, 10.0)
                                    'n' -> result.first /= ln(equationBuffor.first)
                                    'i' -> result.first /= asin(equationBuffor.first)
                                    'o' -> result.first /= acos(equationBuffor.first)
                                    'a' -> result.first /= atan(equationBuffor.first)
                                }
                            }

                            '^' -> {
                                when (equation[iterator]) {
                                    's' -> result.first =
                                        (result.first).pow(sin(equationBuffor.first))

                                    'c' -> result.first =
                                        (result.first).pow(cos(equationBuffor.first))

                                    't' -> result.first =
                                        (result.first).pow(tan(equationBuffor.first))

                                    'g' -> result.first =
                                        (result.first).pow(log(equationBuffor.first, 10.0))

                                    'n' -> result.first =
                                        (result.first).pow(ln(equationBuffor.first))

                                    'i' -> result.first +=
                                        (result.first).pow(asin(equationBuffor.first))

                                    'o' -> result.first +=
                                        (result.first).pow(acos(equationBuffor.first))

                                    'a' -> result.first +=
                                        (result.first).pow(atan(equationBuffor.first))
                                }
                            }
                            else -> {
                                when (equation[iterator]) {
                                    's' -> result.first = sin(equationBuffor.first)
                                    'c' -> result.first = cos(equationBuffor.first)
                                    't' -> result.first = tan(equationBuffor.first)
                                    'g' -> result.first = log(equationBuffor.first, 10.0)
                                    'n' -> result.first = ln(equationBuffor.first)
                                    'i' -> result.first = asin(equationBuffor.first)
                                    'o' -> result.first = acos(equationBuffor.first)
                                    'a' -> result.first = atan(equationBuffor.first)
                                }
                            }
                        }
                        iterator = equationBuffor.second
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
        // Calculation
        val equation = transformEquation(textView.text.toString())
        val resultOfCalculations = calculate(equation, 0)

        if (checkIsItDouble(resultOfCalculations.first)) {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            }
            else {
                resultTextView.append("= ")
                resultTextView.text = resultOfCalculations.first.toFloat().toString()
            }
        } else {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            }
            else {
                resultTextView.append("= ")
                resultTextView.text = resultOfCalculations.first.toInt().toString()
            }
        }
    }

    fun enterButtonClick() {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (specialFunctionDeep > 0) {
                specialFunctionDeep--
            }

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
                    if (specialFunctionDeep > 0) {
                        // Check how many bracket are needed to be delete
                        var function: Function<Int> = specialFunctions.last()
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

                        val bufforText = textView.text.dropLast(bracketCounter)
                        if (bufforText.last() != ')' && bufforText.last() != 'π'
                            && bufforText.last() != 'e') {
                            textView.text = bufforText

                            if (textView.text.last() == '°') {
                                textView.text = textView.text.dropLast(1)
                            }

                            var text = i.toString()
                            if (!radians) {
                                text += "°"
                                addedDegrees = true
                            }

                            textView.append(text)

                            if (addedDegrees) {
                                text = text.dropLast(1)
                            }

                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            // Update functions that are in use
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end += text.length
                                        buffor--
                                    }
                                }
                            }
                            addedNumber = true
                        }
                    } else {
                        if (textView.text.last() != ')' && textView.text.last() != 'π'
                                && textView.text.last() != 'e') {
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
                }, 100)
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
                        var function: Function<Int> = specialFunctions.last()
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

                        val bufforText = textView.text.dropLast(bracketCounter)

                        if (bufforText.last().isDigit() || bufforText.last() == '!'
                            || bufforText.last() == ')' || bufforText.last() == 'π'
                            || bufforText.last() == 'e' || bufforText.last() == '('
                            || bufforText.last() == '°') {

                            var run = true
                            if (bufforText.last() == '(') {
                                run = basicCalcButtons[i].text == "-"
                            }

                            if (run){
                                textView.text = bufforText

                                val text = basicCalcButtons[i].text
                                textView.append(text)

                                var counter = 0
                                while (counter < bracketCounter) {
                                    textView.append(")")
                                    counter++
                                }

                                // Update functions that are in use
                                val rangeFunctions = specialFunctions.size-1 downTo 0
                                var buffor = specialFunctionDeep
                                for (i in rangeFunctions) {
                                    if (specialFunctions[i].deep <= specialFunctionDeep) {
                                        if (buffor <= 0) {
                                            break
                                        }
                                        if (buffor == specialFunctions[i].deep) {
                                            specialFunctions[i].end += text.length
                                            buffor--
                                        }
                                    }
                                }
                                commaUsed = false
                                addedDegrees = false

                                if (basicCalcButtons[i].text == "×" ||
                                    basicCalcButtons[i].text == "/") {
                                    degreeButton.text = context.getString(R.string.RadiansCalc)
                                    radians = true
                                }
                            }
                        }

                    } else if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == 'π' || textView.text.last() == 'e'
                        || textView.text.last() == '!') {
                        textView.append(basicCalcButtons[i].text)
                        commaUsed = false
                        addedDegrees = false
                    }
                    else if (textView.text.last() == '(') {
                        if (basicCalcButtons[i].text == "-") {
                            textView.append(basicCalcButtons[i].text)
                        }
                        commaUsed = false
                        addedDegrees = false
                    }
                }
                else {
                    if (basicCalcButtons[i].text == "-") {
                        textView.append(basicCalcButtons[i].text)
                    }
                    commaUsed = false
                    addedDegrees = false
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
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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
                    var bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last().isDigit() || bufforText.last() == ')' ||
                        bufforText.last() == 'π' || bufforText.last() == 'e'
                        || bufforText.last() == '°') {
                        if (bufforText.last() == '°') {
                            bufforText = bufforText.dropLast(1)
                        }

                        textView.text = bufforText
                        val text = powerButton.text
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }
                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size - 1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end += text.length
                                    buffor++
                                }
                            }
                        }
                    }
                } else {
                    if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == 'π' || textView.text.last() == 'e') {
                        textView.append(powerButton.text.toString())
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun commaButtonClick(textView: TextView) {
        commaButton.setOnClickListener {
            commaButton.setBackgroundResource(clickedButtonStyle)

            if (!commaUsed) {
                if (textView.text.isNotEmpty()) {
                    if (specialFunctionDeep > 0) {
                        // Check how many bracket are needed to be delete
                        var function: Function<Int> = specialFunctions.last()
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

                        var bufforText = textView.text.dropLast(bracketCounter)

                        if (bufforText.last().isDigit() || bufforText.last() == '°') {
                            if (bufforText.last() == '°') {
                                bufforText = bufforText.dropLast(1)
                            }

                            textView.text = bufforText

                            val text = commaButton.text
                            textView.append(text)

                            if (!radians) {
                                textView.append("°")
                            }

                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            // Update functions that are in use
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end += text.length
                                        buffor++
                                    }
                                }
                            }

                            commaUsed = true
                        }
                    }
                    else {
                        if (textView.text.last().isDigit()) {
                            textView.append(commaButton.text.toString())
                            commaUsed = true
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                commaButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun rootButtonClick(textView: TextView) {
        rootButton.setOnClickListener {
            rootButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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
                    val bufforText = textView.text.dropLast(bracketCounter)
                    var pass = false
                    if (function.start == textView.text.dropLast(bracketCounter).length-1) {
                        pass = true
                    }

                    if (bufforText.last().isDigit() || bufforText.last() == '+' ||
                        bufforText.last() == '-' || bufforText.last() == '×' ||
                        bufforText.last() == '/' || bufforText.last() == 'e' ||
                        bufforText.last() == 'π' || bufforText.last() == '(' || pass
                        ) {
                        textView.text = bufforText

                        textView.append("√")

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }
                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size - 1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end++
                                    buffor++
                                }
                            }
                        }
                    }
                } else {
                    if (textView.text.last().isDigit() || textView.text.last() == '+'
                        || textView.text.last() == '-' || textView.text.last() == '×'
                        || textView.text.last() == '/' || textView.text.last() == 'π'
                        || textView.text.last() == '(' || textView.text.last() == 'e') {
                        textView.append("√")
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
            if (textView.text.isNotEmpty() && !commaUsed) {
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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

                    val bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last().isDigit() || bufforText.last() == ')') {
                        var run = true

                        // Check does some function was encountered or not
                        if (bufforText.last() == ')') {
                            var openBrackets = 0
                            var closeBrackets = 0
                            var index = textView.text.length - (bracketCounter + 1)
                            index = if (index < 0) 0 else index
                            do {
                                if (textView.text[index] == ')') {
                                    for (func in specialFunctions) {
                                        if(func.end == index) {
                                            run = false
                                            break
                                        }
                                    }
                                    closeBrackets++
                                }
                                else if (textView.text[index] == '(') {
                                    openBrackets++
                                }
                                else if (textView.text[index] == ',') {
                                    run = false
                                    break
                                }
                                index--
                            } while(index >= 0 && closeBrackets != openBrackets)
                        }

                        if (run) {
                            textView.text = bufforText

                            val text = "!"
                            textView.append(text)

                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            // Update functions that are in use
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end++
                                        buffor--
                                    }
                                }
                            }

                            appendedFactorial = true
                        }
                    }
                } else if (textView.text.last().isDigit() || textView.text.last() == ')') {
                    var found = false
                    var openBrackets = 0
                    var closeBrackets = 0
                    var index = textView.text.length - 1
                    index = if (index < 0) 0 else index
                    do {
                        if (textView.text[index] == ')') {
                            for (func in specialFunctions) {
                                if(func.end == index) {
                                    found = true
                                    break
                                }
                            }
                            closeBrackets++
                        }
                        else if (textView.text[index] == '(') {
                            openBrackets++
                        }
                        else if (textView.text[index] == ',') {
                            found = true
                            break
                        }
                        index--
                    } while(index >= 0 && closeBrackets != openBrackets)

                    if (!found) {
                        textView.append("!")
                        appendedFactorial = true
                    }
                }
            }

            if (appendedFactorial) {
                resultOfCalculate(textView, resultTextView)
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
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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
                    var bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last().isDigit() || bufforText.last() == ')' ||
                        bufforText.last() == 'π' || bufforText.last() == 'e'
                        || bufforText.last() == '°') {
                        if (bufforText.last() == '°') {
                            bufforText = bufforText.dropLast(1)
                        }

                        textView.text = bufforText
                        val text = "^(-"
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        bracketsCounter++

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size - 1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end += text.length
                                    buffor++
                                }
                            }
                        }
                    }
                } else {
                    if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == 'π' || textView.text.last() == 'e') {
                        textView.append("^(-")
                        bracketsCounter++
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

            var appendedProcent = false
            if (textView.text.isNotEmpty()) {
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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

                    val bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last().isDigit() || bufforText.last() == ')') {
                        var run = true

                        // Check does some function was encountered or not
                        if (bufforText.last() == ')') {
                            var openBrackets = 0
                            var closeBrackets = 0
                            var index = textView.text.length - (bracketCounter + 1)
                            index = if (index < 0) 0 else index
                            do {
                                if (textView.text[index] == ')') {
                                    for (func in specialFunctions) {
                                        if(func.end == index) {
                                            run = false
                                            break
                                        }
                                    }
                                    closeBrackets++
                                }
                                else if (textView.text[index] == '(') {
                                    openBrackets++
                                }
                                index--
                            } while(index >= 0 && closeBrackets != openBrackets)
                        }

                        if (run) {
                            textView.text = bufforText

                            val text = "%"
                            textView.append(text)

                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            // Update functions that are in use
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end++
                                        buffor--
                                    }
                                }
                            }

                            appendedProcent = true
                        }
                    }
                } else if (textView.text.last().isDigit() || textView.text.last() == ')') {
                    var found = false
                    var openBrackets = 0
                    var closeBrackets = 0
                    var index = textView.text.length - 1
                    index = if (index < 0) 0 else index
                    do {
                        if (textView.text[index] == ')') {
                            for (func in specialFunctions) {
                                if(func.end == index) {
                                    found = true
                                    break
                                }
                            }
                            closeBrackets++
                        }
                        else if (textView.text[index] == '(') {
                            openBrackets++
                        }
                        index--
                    } while(index >= 0 && closeBrackets != openBrackets)

                    if (!found) {
                        textView.append("%")
                        appendedProcent = true
                    }
                }
            }

            if (appendedProcent) {
                resultOfCalculate(textView, resultTextView)
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
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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

                    val bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last() != ')' && bufforText.last() != ',') {
                        textView.text = bufforText
                        val text = numberPIButton.text.toString()
                        textView.append(text)

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size-1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end += text.length
                                    buffor--
                                }
                            }
                        }
                        addedNumber = true
                    }
                } else {
                    if (textView.text.last() != ')') {
                        textView.append(numberPIButton.text.toString())
                        addedNumber = true
                    }
                }
            } else {
                textView.append(numberPIButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                resultOfCalculate(textView, resultTextView)
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
                if (specialFunctionDeep > 0) {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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

                    val bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last() != ')' && bufforText.last() != ',') {
                        textView.text = bufforText
                        val text = numberEulerButton.text.toString()
                        textView.append(text)

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size-1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end += text.length
                                    buffor--
                                }
                            }
                        }
                        addedNumber = true
                    }
                } else {
                    if (textView.text.last() != ')') {
                        textView.append(numberEulerButton.text.toString())
                        addedNumber = true
                    }
                }
            } else {
                textView.append(numberEulerButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                resultOfCalculate(textView, resultTextView)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberEulerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clearButtonClick(textView: TextView, resultTextView: TextView) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            specialFunctions.clear()
            specialFunctionDeep = 0
            bracketsCounter = 0
            commaUsed = false
            addedDegrees = false
            textView.text = ""
            resultTextView.text = ""

            Handler(Looper.getMainLooper()).postDelayed({
                clearButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun openBracketButtonClick(textView: TextView) {
        openBracketButton.setOnClickListener {
            openBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (specialFunctionDeep == 0) {
                    if (textView.text.last() != ')' && textView.text.last() != '!'
                        && !textView.text.last().isDigit())  {
                        textView.append(openBracketButton.text)
                        bracketsCounter++
                    }
                }
                else {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
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

                    val bufforText = textView.text.dropLast(bracketCounter)

                    if (bufforText.last() != ')' && bufforText.last() != '!'
                        && !bufforText.last().isDigit()) {
                        textView.text = bufforText
                        textView.append(openBracketButton.text.toString())
                        bracketsCounter++
                        function.brackets++

                        var counter = 0
                        while (counter < bracketCounter) {
                            textView.append(")")
                            counter++
                        }

                        // Update functions that are in use
                        val rangeFunctions = specialFunctions.size - 1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end++
                                    buffor--
                                }
                            }
                        }
                    }
                }
            } else {
                textView.append(openBracketButton.text)
                bracketsCounter++
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
                if (specialFunctionDeep == 0) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == '!' || textView.text.last() == 'π'
                        || textView.text.last() == 'e') {
                        textView.append(closeBracketButton.text)
                        bracketsCounter--
                    }
                } else {
                    // Check how many bracket are needed to be delete
                    var function: Function<Int> = specialFunctions.last()
                    val range = specialFunctions.size - 1 downTo 0
                    for (i in range) {
                        if (specialFunctions[i].deep == specialFunctionDeep) {
                            function = specialFunctions[i]
                            break
                        }
                    }

                    if (function.brackets > 0) {
                        // Check how manu bracket are needed to be delete
                        var limit = textView.text.length - 1
                        var bracketCounter = 0
                        while (function.end <= limit && textView.text[limit] == ')') {
                            bracketCounter++
                            limit--
                        }

                        var bufforText = textView.text.dropLast(bracketCounter)
                        if (bufforText.last().isDigit() || bufforText.last() == ')'
                            || bufforText.last() == '!' || bufforText.last() == 'π'
                            || bufforText.last() == 'e' || bufforText.last() == '°') {

                            if(bufforText.last() == '°') {
                                bufforText = bufforText.dropLast(1)
                            }

                            textView.text = bufforText
                            textView.append(closeBracketButton.text.toString())
                            bracketsCounter--
                            function.brackets--

                            if (!radians) {
                                textView.append("°")
                            }

                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.append(")")
                                counter++
                            }

                            // Update functions that are in use
                            val rangeFunctions = specialFunctions.size - 1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end++
                                        buffor--
                                    }
                                }
                            }
                        }
                    }
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
                if (specialFunctionDeep == 0) {
                    if (textView.text.last() == ')') {
                        var found = false
                        val range = specialFunctions.size-1 downTo 0
                        for (i in range) {
                            if (specialFunctions[i].end == textView.text.length-1) {
                                found = true
                                break
                            }
                        }

                        if (found) {
                            specialFunctionDeep++
                        }
                        else {
                            textView.text = textView.text.dropLast(1)
                            bracketsCounter++
                        }
                    }
                    else {
                        if (textView.text.last() == ',') {
                            commaUsed = false
                        }

                        textView.text = textView.text.dropLast(1)
                    }
                }
                else {
                    // Find actual function
                    var function = Function<Int>(0, 0, 0, 0)
                    val range = specialFunctions.size-1 downTo 0
                    for (i in range) {
                        if (specialFunctions[i].deep == specialFunctionDeep) {
                            function = specialFunctions[i]
                            break
                        }
                    }

                    // Check how manu bracket are needed to be delete
                    var limit = textView.text.length - 1
                    var bracketsToDelete = 0
                    while (function.end <= limit && textView.text[limit] == ')') {
                        bracketsToDelete++
                        limit--
                    }

                    textView.text = textView.text.dropLast(bracketsToDelete)

                    if (textView.text.last() == ')') {
                        // Check is it second function
                        var secondFunction =
                            Function<Int>(0, 0, 0, 0)
                        for (i in range) {
                            if (specialFunctions[i].deep == specialFunctionDeep+1) {
                                secondFunction = specialFunctions[i]
                                break
                            }
                        }

                        if (secondFunction.deep != 0) {
                            specialFunctionDeep = secondFunction.deep
                            while (bracketsToDelete > 0) {
                                textView.append(")")
                                bracketsToDelete--
                            }
                        }
                        else {
                            if (textView.text.last() == ',') {
                                commaUsed = false
                            }

                            textView.text = textView.text.dropLast(1)
                            while (bracketsToDelete > 0) {
                                textView.append(")")
                                bracketsToDelete--
                            }

                            // Update functions ends that are higher order
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end--
                                        buffor--
                                    }
                                }
                            }

                            bracketsCounter++
                        }
                    }
                    else if (textView.text.last() == '(') {
                        // Check is it start of function
                        var secondFunction =
                            Function<Int>(0, 0, 0, 0)
                        for (i in range) {
                            if (specialFunctions[i].start == textView.text.length-1) {
                                secondFunction = specialFunctions[i]
                                break
                            }
                        }

                        if (secondFunction.deep != 0) {
                            // Count space that function takes
                            var counter = 1
                            var length = textView.text.length-2
                            while (length >= 0 && textView.text[length].isLetter()
                                && textView.text[length] != 'π' && textView.text[length] != 'e') {
                                length--
                                counter++
                            }

                            // Delete function
                            if (textView.text.dropLast(counter-1).last() == ',') {
                                commaUsed = false
                            }
                            textView.text = textView.text.dropLast(counter)

                            // Append remaining brackets
                            while (bracketsToDelete > 1) {
                                textView.append(")")
                                bracketsToDelete--
                            }

                            // Update functions
                            specialFunctions.removeLast()
                            specialFunctionDeep--
                            if (bracketsCounter > 0) {
                                bracketsCounter--
                            }

                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end -= counter+1
                                        buffor--
                                    }
                                }
                            }
                        }
                        else {
                            if (textView.text.last() == ',') {
                                commaUsed = false
                            }

                            textView.text = textView.text.dropLast(1)
                            while (bracketsToDelete > 0) {
                                textView.append(")")
                                bracketsToDelete--
                            }
                            if (bracketsCounter > 0) {
                                bracketsCounter--
                            }

                            // Update functions ends that are higher order
                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep <= specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end--
                                        buffor--
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (textView.text.last() == ',') {
                            commaUsed = false
                        }

                        if (textView.text.last() == '°') {
                            addedDegrees = false
                            textView.text = textView.text.dropLast(1)

                            if (textView.text.last() != '(') {
                                if (textView.text.last() == ',') {
                                    commaUsed = false
                                }
                                textView.text = textView.text.dropLast(1)
                                if (textView.text.last().isDigit() || textView.text.last() == ',') {
                                    textView.append("°")
                                }
                            }
                        }
                        else {
                            textView.text = textView.text.dropLast(1)
                        }

                        while (bracketsToDelete > 0) {
                            textView.append(")")
                            bracketsToDelete--
                        }
                        if (bracketsCounter > 0) {
                            bracketsCounter--
                        }

                        // Update functions ends that are higher order
                        val rangeFunctions = specialFunctions.size-1 downTo 0
                        var buffor = specialFunctionDeep
                        for (i in rangeFunctions) {
                            if (specialFunctions[i].deep <= specialFunctionDeep) {
                                if (buffor <= 0) {
                                    break
                                }
                                if (buffor == specialFunctions[i].deep) {
                                    specialFunctions[i].end--
                                    buffor--
                                }
                            }
                        }
                    }
                }

                if (textView.text.isEmpty()) {
                    resultTextView.text = ""
                }

                resultOfCalculate(textView, resultTextView)
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

                if (specialFunctions.isEmpty()) {
                    if (textView.text.isEmpty()) {
                        val function: Function<Int> =
                            Function<Int>(0, 0, 0, 0)

                        val text = button.text.toString() + "()"
                        textView.append(text)

                        // Set function
                        function.start = textView.text.length - 2
                        function.end = textView.text.length - 1
                        function.deep = ++specialFunctionDeep

                        specialFunctions.add(function)
                        commaUsed = false
                    }
                    else if (textView.text.last() != ')') {
                        if (textView.text.last() != ',') {
                            val function: Function<Int> =
                                Function<Int>(0, 0, 0, 0)

                            val text = button.text.toString() + "()"
                            textView.append(text)

                            // Set function
                            function.start = textView.text.length - 2
                            function.end = textView.text.length - 1
                            function.deep = ++specialFunctionDeep

                            specialFunctions.add(function)
                            commaUsed = false
                        }
                    }
                }
                else {
                    if (specialFunctionDeep == 0) {
                        if (textView.text.last() != ',') {
                            val function: Function<Int> =
                                Function<Int>(0, 0, 0, 0)

                            val text = button.text.toString() + "()"
                            textView.append(text)

                            // Set function
                            function.start = textView.text.length - 2
                            function.end = textView.text.length - 1
                            function.deep = ++specialFunctionDeep
                            specialFunctions.add(function)
                            commaUsed = false
                        }
                    }
                    else {
                        // Find deeper embedded function
                        var function: Function<Int> = specialFunctions.last()
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

                        val newFunction: Function<Int> =
                            Function<Int>(0, 0, 0, 0)
                        if (textView.text[limit] != ',') {
                            var counter = 0
                            while (counter < bracketCounter) {
                                textView.text = textView.text.dropLast(1)
                                counter++
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

                            val rangeFunctions = specialFunctions.size-1 downTo 0
                            var buffor = specialFunctionDeep - 1
                            for (i in rangeFunctions) {
                                if (specialFunctions[i].deep < specialFunctionDeep) {
                                    if (buffor <= 0) {
                                        break
                                    }
                                    if (buffor == specialFunctions[i].deep) {
                                        specialFunctions[i].end += text.length
                                        buffor--
                                    }
                                }
                            }
                            specialFunctions.add(newFunction)
                            commaUsed = false
                        }
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
            }

            Handler(Looper.getMainLooper()).postDelayed({
                degreeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun changeFunctionsButtonClick() {
        changeFunctionsButton.setOnClickListener {
            changeFunctionsButton.setBackgroundResource(clickedButtonStyle)

            if (changeFunctionsButton.text == "2nd") {
                changeFunctionsButton.text = "1st"
                sinButton.text = "sin"
                cosButton.text = "cos"
                tgButton.text = "tan"

                sinButton.textSize = 16f
                cosButton.textSize = 16f
                tgButton.textSize = 16f

                secondFunctions = false
            }
            else {
                changeFunctionsButton.text = "2nd"
                sinButton.text = "arcsin"
                cosButton.text = "arccos"
                tgButton.text = "arctan"

                sinButton.textSize = 10f
                cosButton.textSize = 10f
                tgButton.textSize = 10f

                secondFunctions = true
                degreeButton.text = "rad"
                radians = true
            }

            Handler(Looper.getMainLooper()).postDelayed({
                changeFunctionsButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }
}