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
    private val specialFunctions: MutableList<Function<Int>> = mutableListOf()
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
        commaButton = findViewById(R.id.Comma)
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
        var tempLength= length
        var outputNumber = 0.0

        if (divide) {
            val range = length - 1 downTo 0
            tempLength++
            for (i in range) {
                outputNumber += convertNumber(list[i], tempLength, true)
                tempLength--
            }
        } else {
            list.forEach { num ->
                outputNumber += convertNumber(num, tempLength, false)
                tempLength--
            }
        }

        return outputNumber
    }

    private fun transformEquation(equation: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()

        // Equation variables
        var intConverter = 0
        var lastChar = '?'
        var numberBase = 0.0
        val numBuffer: MutableList<Double> = mutableListOf()

        // Equation validation
        var multiplyDivide = false
        var powerTo = false
        var powerToLevel = 0
        var inRoot = false
        var addBracketIndex = -1

        val openedBracketsInput = ArrayDeque<Char>()
        val bracketsInsideFunctionInput: MutableList<MutableList<Char>> = mutableListOf()

        val openedBrackets: MutableList<Char> = mutableListOf()
        val bracketsInsideFunction: MutableList<MutableList<Char>> = mutableListOf()

        // Functions
        var iterator = 0
        var functionIndex = -1
        var commaInUse = true
        var whatFunction = '0'

        equation.forEach { element ->
            if (element.isDigit()) {
                // Add digit to buffer
                numBuffer.add((element.code - 48).toDouble())
                intConverter++

                // Decimal number
                if (lastChar == ',') {
                    val outputNumber: Double = calculateNumber(numBuffer, intConverter, true)
                    transformedEquation.removeLast()

                    val decimalNumber: Double = numberBase + outputNumber

                    if (!commaInUse) {
                        commaInUse = true
                    }
                    transformedEquation.add(decimalNumber)
                }
            }
            else {
                // Clear buffers of constant numbers that has been added in number handler
                if (lastChar == ',') {
                    numBuffer.clear()
                }

                // Append number that is in buffer
                if (numBuffer.isNotEmpty() && whatFunction == '0') {
                    if (lastChar != ',') {
                        val outputNumber: Double = calculateNumber(numBuffer, intConverter, false)
                        transformedEquation.add(outputNumber)

                        if (element == ',') {
                            numberBase = outputNumber
                        }
                    }

                    if (powerTo && powerToLevel == 0) {
                        powerTo = false
                    }
                }

                // Root
                if (element == '√') {
                    if (functionIndex >= 0) {
                        if (inRoot && transformedEquation.last().toString()[0].isDigit()) {
                            if (bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunction[functionIndex].removeLast())
                            }
                            transformedEquation.add('×')
                            multiplyDivide = true
                        }
                        else if (transformedEquation.last().toString()[0].isDigit()) {
                            transformedEquation.add('×')
                            multiplyDivide = true
                        }
                    } else {
                        if (inRoot) {
                            if (openedBrackets.isNotEmpty()) {
                                transformedEquation.add(openedBrackets.removeLast())
                            }
                            transformedEquation.add('×')
                            multiplyDivide = true
                        }
                        else if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last().toString()[0].isDigit()) {
                                transformedEquation.add('×')
                                multiplyDivide = true
                            }
                        }
                    }

                    transformedEquation.add(element)

                    if (functionIndex >= 0) {
                        bracketsInsideFunction[functionIndex].add(')')
                    }
                    else {
                        openedBrackets.add(')')
                    }

                    if (!multiplyDivide) {
                        var openBrackets = 0
                        var closeBrackets = 0
                        var indexBuffer = 0
                        var index = 0
                        val range = transformedEquation.size - 1 downTo 0

                        for (i in range) {
                            if (transformedEquation[i] == ')') {
                                closeBrackets++
                            } else if (transformedEquation[i] == '(') {
                                openBrackets++
                            } else if (indexBuffer == 0 &&
                                (transformedEquation[i] == '+' || transformedEquation[i] == '-')) {
                                indexBuffer = i
                            }

                            if (closeBrackets == openBrackets) {
                                index = i
                                break
                            }
                        }
                        if (closeBrackets != openBrackets) {
                            addBracketIndex = if (indexBuffer == 0) 0 else ++indexBuffer
                        } else if (index >= 1) {
                            if (transformedEquation[index-1] == '(') {
                                addBracketIndex = ++index
                                multiplyDivide = true
                            }
                            else if (transformedEquation[index-1].toString()[0].isLetter()) {
                                addBracketIndex = --index
                            }
                            else {
                                addBracketIndex = index
                            }
                        } else {
                            addBracketIndex = 0
                        }

                        transformedEquation.add(addBracketIndex, '(')

                        if (functionIndex >= 0) {
                            bracketsInsideFunction[functionIndex].add(')')
                        }
                        else {
                            openedBrackets.add(')')
                        }
                    }

                    inRoot = true
                    multiplyDivide = true
                }

                // Handle subtract and add
                if (element == '+' || element == '-') {
                    if (functionIndex >= 0) {
                        if (lastChar != '(' && lastChar != '-' && lastChar != '+') {
                            while (bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunction[functionIndex].removeLast())
                            }
                        }
                    } else {
                        if (lastChar != '(' && lastChar != '-' && lastChar != '+') {
                            while (openedBrackets.isNotEmpty()) {
                                transformedEquation.add(openedBrackets.removeLast())
                            }
                        }
                    }

                    multiplyDivide = false
                }

                // Preparing brackets for multiplication and division
                if (element == '×' || element == '/') {
                    if (transformedEquation.isNotEmpty()) {
                        var openBrackets = 0
                        var closeBrackets = 0
                        var indexBuffer = 0
                        var index = 0
                        val range = transformedEquation.size - 1 downTo 0

                        for (i in range) {
                            if (transformedEquation[i] == ')') {
                                closeBrackets++
                            } else if (transformedEquation[i] == '(') {
                                openBrackets++
                            } else if (indexBuffer == 0 &&
                                (transformedEquation[i] == '+' || transformedEquation[i] == '-')) {
                                indexBuffer = i
                            }

                            if (closeBrackets == openBrackets) {
                                index = i
                                break
                            }
                        }
                        if (closeBrackets != openBrackets) {
                            addBracketIndex = if (indexBuffer == 0) 0 else ++indexBuffer
                        } else if (index >= 1) {
                            if (transformedEquation[index-1] == '(') {
                                addBracketIndex = ++index
                                multiplyDivide = true
                            }
                            else if (transformedEquation[index-1].toString()[0].isLetter()) {
                                addBracketIndex = --index
                            }
                            else {
                                addBracketIndex = index
                            }
                        } else {
                            addBracketIndex = 0
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
                    var addMultiplication = false

                    // Append multiplication if before number is other function or constants
                    if (lastChar == 'π' || lastChar == 'e') {
                        addMultiplication = true
                        lastChar = '0'
                    }
                    else if (transformedEquation.isNotEmpty() && numBuffer.isEmpty()) {
                        if (transformedEquation.last() != '×' && transformedEquation.last() != '/'
                            && transformedEquation.last() != '+' && transformedEquation.last() != '-'
                            && transformedEquation.last() != '(') {
                            addMultiplication = true
                        }
                    }

                    // Append number that is before function as multiplication
                    if (numBuffer.isNotEmpty()) {
                        val outputNumber: Double = transformedEquation.removeLast() as Double
                        transformedEquation.add(outputNumber)
                        addMultiplication = true
                    }

                    functionIndex++
                    bracketsInsideFunction.add(mutableListOf())
                    bracketsInsideFunctionInput.add(mutableListOf())

                    // Act as it is multiplication when some number is before function
                    if (lastChar == ',') {
                        addMultiplication = true
                    }

                    if (addMultiplication) {
                        if(!multiplyDivide) {
                            if (transformedEquation.isNotEmpty()) {
                                var openBrackets = 0
                                var closeBrackets = 0
                                var indexBuffer = 0
                                var index = 0
                                val range = transformedEquation.size - 1 downTo 0

                                for (i in range) {
                                    if (transformedEquation[i] == ')') {
                                        closeBrackets++
                                    } else if (transformedEquation[i] == '(') {
                                        openBrackets++
                                    } else if (indexBuffer == 0 &&
                                        (transformedEquation[i] == '+' || transformedEquation[i] == '-')) {
                                        indexBuffer = i
                                    }

                                    if (closeBrackets == openBrackets) {
                                        index = i
                                        break
                                    }
                                }
                                if (closeBrackets != openBrackets) {
                                    addBracketIndex = if (indexBuffer == 0) 0 else ++indexBuffer
                                } else if (index >= 1) {
                                    if (transformedEquation[index-1] == '(') {
                                        addBracketIndex = ++index
                                        multiplyDivide = true
                                    }
                                    else if (transformedEquation[index-1].toString()[0].isLetter()) {
                                        addBracketIndex = --index
                                    }
                                    else {
                                        addBracketIndex = index
                                    }
                                } else {
                                    addBracketIndex = 0
                                }
                            }

                            transformedEquation.add(addBracketIndex, '(')

                            if (functionIndex >= 0) {
                                bracketsInsideFunction[functionIndex].add(')')
                            }
                            else {
                                openedBrackets.add(')')
                            }
                        }

                        transformedEquation.add('×')

                        multiplyDivide = true
                    }

                    transformedEquation.add(whatFunction)
                    whatFunction = '0'
                }

                // Brackets handling
                else if (element == '(') {
                    if (functionIndex >= 0) {
                        bracketsInsideFunctionInput[functionIndex].add(')')
                    } else {
                        openedBracketsInput.addLast(')')
                    }

                    if (powerTo) {
                        powerToLevel++
                    }

                    multiplyDivide = false
                }
                else if (element == ')') {
                    if (functionIndex >= 0) {
                        if (iterator == specialFunctions[functionIndex].end) {
                            while(bracketsInsideFunctionInput[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunctionInput[functionIndex].removeLast())
                            }

                            while(bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunction[functionIndex].removeLast())
                            }

                            bracketsInsideFunction.removeLast()
                            functionIndex--
                        }
                        else {
                            if (bracketsInsideFunctionInput[functionIndex].isNotEmpty()) {
                                bracketsInsideFunctionInput[functionIndex].removeLast()
                            }

                            while(bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunction[functionIndex].removeLast())
                            }
                        }
                    } else {
                        if (openedBracketsInput.isNotEmpty()) {
                            openedBracketsInput.removeLast()
                        }

                        while (openedBrackets.isNotEmpty()) {
                            transformedEquation.add(openedBrackets.removeLast())
                        }
                    }

                    if (powerTo) {
                        powerToLevel--

                        if (powerToLevel == 0) {
                            transformedEquation.add(')')
                            powerTo = false
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
                        if (numBuffer.isEmpty()) {
                            transformedEquation.add('(')
                            transformedEquation.add(num)
                            if (functionIndex >= 0) {
                                bracketsInsideFunction[functionIndex].add(')')
                            }
                            else {
                                openedBrackets.add(')')
                            }
                        }
                        else {
                            transformedEquation.add('×')
                            transformedEquation.add(num)
                        }
                    }
                    else {
                        if (numBuffer.isEmpty() && !transformedEquation.last().toString()[0].isDigit()) {
                            transformedEquation.add(num)
                        }
                        else {
                            val bufferNum: Double
                            if (transformedEquation.last().toString()[0].isDigit()) {
                                bufferNum = transformedEquation.last() as Double
                                transformedEquation.removeLast()

                                transformedEquation.add('(')
                                if (functionIndex >= 0) {
                                    bracketsInsideFunction[functionIndex].add(')')
                                }
                                else {
                                    openedBrackets.add(')')
                                }
                                transformedEquation.add(bufferNum)
                            }
                            transformedEquation.add('×')
                            transformedEquation.add(num)
                            multiplyDivide = true
                        }
                    }
                }

                // Handle power to
                if (element == '^') {
                    if (inRoot) {
                        if (functionIndex >= 0) {
                            while (bracketsInsideFunction[functionIndex].isNotEmpty()) {
                                transformedEquation.add(bracketsInsideFunction[functionIndex].removeLast())
                            }
                        }
                        else {
                            while (openedBrackets.isNotEmpty()) {
                                transformedEquation.add(openedBrackets.removeLast())
                            }
                        }
                    }

                    if (transformedEquation.isNotEmpty()) {
                        var openBrackets = 0
                        var closeBrackets = 0
                        var indexBuffer = 0
                        var index = 0
                        val range = transformedEquation.size - 1 downTo 0

                        for (i in range) {
                            if (transformedEquation[i] == ')') {
                                closeBrackets++
                            } else if (transformedEquation[i] == '(') {
                                openBrackets++
                            } else if (indexBuffer == 0 &&
                                (transformedEquation[i] == '+' || transformedEquation[i] == '-')) {
                                indexBuffer = i
                            }

                            if (closeBrackets == openBrackets) {
                                index = i
                                break
                            }
                        }
                        if (closeBrackets != openBrackets) {
                            addBracketIndex = if (indexBuffer == 0) 0 else ++indexBuffer
                        } else if (index >= 1) {
                            if (transformedEquation[index-1] == '(') {
                                addBracketIndex = ++index
                                multiplyDivide = true
                            }
                            else if (transformedEquation[index-1].toString()[0].isLetter()) {
                                addBracketIndex = --index
                            }
                            else {
                                addBracketIndex = index
                            }
                        } else {
                            addBracketIndex = 0
                        }
                    }

                    transformedEquation.add(addBracketIndex, '(')
                    transformedEquation.add('^')

                    powerTo = true
                    multiplyDivide = true
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
                        when (element) {
                            '!' -> transformedEquation.add('!')
                            '%' -> transformedEquation.add('%')
                            '°' -> transformedEquation.add('°')
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
                        when (element) {
                            '!' -> transformedEquation.add('!')
                            '%' -> transformedEquation.add('%')
                            '°' -> transformedEquation.add('°')
                        }
                        if (lastChar != '^') {
                            if (lastChar == '√' && element == '°') {
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
                when (element) {
                    '+', '-', '(', ')' -> {
                        transformedEquation.add(element)
                        lastChar = element
                        inRoot = false
                    }
                    '×', '/' -> {
                        if (!multiplyDivide) {
                            transformedEquation.add(addBracketIndex, '(')
                            if (functionIndex >= 0) {
                                bracketsInsideFunction[functionIndex].add(')')
                            }
                            else {
                                openedBrackets.add(')')
                            }
                        }
                        if (inRoot) {
                            bracketsInsideFunction[functionIndex].add(')')
                        }
                        transformedEquation.add(element)

                        multiplyDivide = true
                        lastChar = element
                        inRoot = false
                    }
                    '^' -> {
                        lastChar = element
                        inRoot = false
                    }
                    ',', '√', 'π', 'e' -> lastChar = element
                    '!', '%', '°' -> inRoot = false
                }

                commaInUse = false
                addBracketIndex = -1
                intConverter = 0
                if (whatFunction == '0') {
                    numBuffer.clear()
                }
            }
            iterator++
        }
        // Add everything that lasts in buffers
        if (numBuffer.isNotEmpty() && lastChar != ',') {
            val outputNumber: Double = calculateNumber(numBuffer, intConverter, false)
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

        if (powerTo) {
            transformedEquation.add(')')
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
        var equationSign = 'E'
        val result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index
        val threshold = 1E-10
        
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
                        when (equationSign) {
                            '+' -> {
                                when (equation[iterator]) {
                                    's' -> result.first += sin(equationBuffer.first)
                                    'c' -> result.first += cos(equationBuffer.first)
                                    't' -> result.first += tan(equationBuffer.first)
                                    'g' -> result.first += log(equationBuffer.first, 10.0)
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
                                    'g' -> result.first -= log(equationBuffer.first, 10.0)
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
                                    'g' -> result.first *= log(equationBuffer.first, 10.0)
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
                                    'g' -> result.first /= log(equationBuffer.first, 10.0)
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
                                        (result.first).pow(log(equationBuffer.first, 10.0))

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
                                    'g' -> result.first = log(equationBuffer.first, 10.0)
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

    private fun getCurrentFunctionEnd(textView: TextView): Int {
        // Find current function
        var function: Function<Int> = Function(0,0,0,0)
        val range = specialFunctions.size - 1 downTo 0
        for (j in range) {
            if (specialFunctions[j].deep == specialFunctionDeep) {
                function = specialFunctions[j]
                break
            }
        }

        // Check how manu brackets are needed to be deleted
        var limit = textView.text.length - 1
        var counter = 0

        while (function.end <= limit && textView.text[limit] == ')') {
            counter++
            limit--
        }

        return counter
    }

    private fun getCurrentFunctionAndItsEnd(textView: TextView): Pair<Function<Int>, Int> {
        // Find current function
        var function: Function<Int> = Function(0,0,0,0)
        val range = specialFunctions.size - 1 downTo 0
        for (j in range) {
            if (specialFunctions[j].deep == specialFunctionDeep) {
                function = specialFunctions[j]
                break
            }
        }

        // Check how manu brackets are needed to be deleted
        var limit = textView.text.length - 1
        var counter = 0
        while (function.end <= limit && textView.text[limit] == ')') {
            counter++
            limit--
        }

        return Pair(function, counter)
    }

    private fun updateFunctionsLength(length: Int) {
        val rangeFunctions = specialFunctions.size-1 downTo 0
        var buffer = specialFunctionDeep
        for (i in rangeFunctions) {
            if (specialFunctions[i].deep <= specialFunctionDeep) {
                if (buffer <= 0) {
                    break
                }
                if (buffer == specialFunctions[i].deep) {
                    specialFunctions[i].end += length
                    buffer--
                }
            }
        }
    }

    fun numberButtonClick(textView: TextView, resultTextView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                var addedNumber = false

                if (textView.text.isNotEmpty()) {
                    if (specialFunctionDeep > 0) {
                        var functionEnd = getCurrentFunctionEnd(textView)

                        val bufferText = textView.text.dropLast(functionEnd)
                        if (bufferText.last() != ')' && bufferText.last() != 'π'
                            && bufferText.last() != 'e') {
                            textView.text = bufferText

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

                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            // Update functions that are in use
                            updateFunctionsLength(text.length)
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
                        var functionEnd = getCurrentFunctionEnd(textView)

                        val bufferText = textView.text.dropLast(functionEnd)

                        if (bufferText.last().isDigit() || bufferText.last() == '!'
                            || bufferText.last() == ')' || bufferText.last() == 'π'
                            || bufferText.last() == 'e' || bufferText.last() == '('
                            || bufferText.last() == '°') {

                            var run = true
                            if (bufferText.last() == '(') {
                                run = basicCalcButtons[i].text == "-"
                            }

                            if (run){
                                textView.text = bufferText

                                val text = basicCalcButtons[i].text
                                textView.append(text)

                                while (functionEnd > 0) {
                                    textView.append(")")
                                    functionEnd--
                                }

                                // Update functions that are in use
                                updateFunctionsLength(text.length)
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
                    var functionEnd = getCurrentFunctionEnd(textView)

                    var bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last().isDigit() || bufferText.last() == ')' ||
                        bufferText.last() == 'π' || bufferText.last() == 'e'
                        || bufferText.last() == '°') {
                        if (bufferText.last() == '°') {
                            bufferText = bufferText.dropLast(1)
                        }

                        textView.text = bufferText
                        val text = powerButton.text
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(text.length)
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
                        var functionEnd = getCurrentFunctionEnd(textView)

                        var bufferText = textView.text.dropLast(functionEnd)

                        if (bufferText.last().isDigit() || bufferText.last() == '°') {
                            if (bufferText.last() == '°') {
                                bufferText = bufferText.dropLast(1)
                            }

                            textView.text = bufferText

                            val text = commaButton.text
                            textView.append(text)

                            if (!radians) {
                                textView.append("°")
                            }

                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            updateFunctionsLength(text.length)
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
                    var (function, functionEnd) = getCurrentFunctionAndItsEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    var pass = false
                    if (function.start == textView.text.dropLast(functionEnd).length-1) {
                        pass = true
                    }

                    if (bufferText.last().isDigit() || bufferText.last() == '+' ||
                        bufferText.last() == '-' || bufferText.last() == '×' ||
                        bufferText.last() == '/' || bufferText.last() == 'e' ||
                        bufferText.last() == 'π' || bufferText.last() == '(' || pass
                        ) {
                        textView.text = bufferText

                        val text = "√"
                        textView.append(text)

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        // Update functions that are in use
                        updateFunctionsLength(text.length)
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
                    var functionEnd = getCurrentFunctionEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last().isDigit() || bufferText.last() == ')') {
                        var run = true

                        // Check does some function was encountered or not
                        if (bufferText.last() == ')') {
                            var openBrackets = 0
                            var closeBrackets = 0
                            var index = textView.text.length - (functionEnd + 1)
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
                            textView.text = bufferText

                            val text = "!"
                            textView.append(text)

                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            updateFunctionsLength(text.length)
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
                    var functionEnd = getCurrentFunctionEnd(textView)

                    var bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last().isDigit() || bufferText.last() == ')' ||
                        bufferText.last() == 'π' || bufferText.last() == 'e'
                        || bufferText.last() == '°') {
                        if (bufferText.last() == '°') {
                            bufferText = bufferText.dropLast(1)
                        }

                        textView.text = bufferText
                        val text = "^(-"
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        bracketsCounter++

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(text.length)
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

            var appendedPercent = false
            if (textView.text.isNotEmpty()) {
                if (specialFunctionDeep > 0) {
                    var functionEnd = getCurrentFunctionEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last().isDigit() || bufferText.last() == ')') {
                        var run = true

                        // Check does some function was encountered or not
                        if (bufferText.last() == ')') {
                            var openBrackets = 0
                            var closeBrackets = 0
                            var index = textView.text.length - (functionEnd + 1)
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
                            textView.text = bufferText

                            val text = "%"
                            textView.append(text)

                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            updateFunctionsLength(text.length)
                            appendedPercent = true
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
                        appendedPercent = true
                    }
                }
            }

            if (appendedPercent) {
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
                    var functionEnd = getCurrentFunctionEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last() != ')' && bufferText.last() != ',') {
                        textView.text = bufferText
                        val text = numberPIButton.text.toString()
                        textView.append(text)

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(text.length)
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
                    var functionEnd = getCurrentFunctionEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last() != ')' && bufferText.last() != ',') {
                        textView.text = bufferText
                        val text = numberEulerButton.text.toString()
                        textView.append(text)

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(text.length)
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
                    var (function, functionEnd) = getCurrentFunctionAndItsEnd(textView)

                    val bufferText = textView.text.dropLast(functionEnd)

                    if (bufferText.last() != ')' && bufferText.last() != '!'
                        && !bufferText.last().isDigit()) {

                        textView.text = bufferText

                        val text = openBracketButton.text.toString()
                        textView.append(text)
                        bracketsCounter++
                        function.brackets++

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(text.length)
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
                    var (function, functionEnd) = getCurrentFunctionAndItsEnd(textView)

                    if (function.brackets > 0) {
                        var bufferText = textView.text.dropLast(functionEnd)

                        if (bufferText.last().isDigit() || bufferText.last() == ')'
                            || bufferText.last() == '!' || bufferText.last() == 'π'
                            || bufferText.last() == 'e' || bufferText.last() == '°') {

                            if(bufferText.last() == '°') {
                                bufferText = bufferText.dropLast(1)
                            }

                            textView.text = bufferText

                            val text = closeBracketButton.text.toString()
                            textView.append(text)
                            bracketsCounter--
                            function.brackets--

                            if (!radians) {
                                textView.append("°")
                            }

                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            updateFunctionsLength(text.length)
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
                    var (function, functionEnd) = getCurrentFunctionAndItsEnd(textView)
                    val range = specialFunctions.size-1 downTo 0

                    textView.text = textView.text.dropLast(functionEnd)

                    if (textView.text.last() == ')') {
                        // Check is it second function
                        var secondFunction =
                            Function(0, 0, 0, 0)
                        for (i in range) {
                            if (specialFunctions[i].deep == specialFunctionDeep+1) {
                                secondFunction = specialFunctions[i]
                                break
                            }
                        }

                        if (secondFunction.deep != 0) {
                            specialFunctionDeep = secondFunction.deep
                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }
                        }
                        else {
                            if (textView.text.last() == ',') {
                                commaUsed = false
                            }

                            textView.text = textView.text.dropLast(1)
                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }

                            updateFunctionsLength(-1)
                            bracketsCounter++
                            function.brackets++
                        }
                    }
                    else if (textView.text.last() == '(') {
                        // Check is it start of function
                        var secondFunction = Function(0, 0, 0, 0)
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
                            while (functionEnd > 1) {
                                textView.append(")")
                                functionEnd--
                            }

                            // Update functions
                            specialFunctions.removeLast()
                            specialFunctionDeep--
                            if (bracketsCounter > 0) {
                                bracketsCounter--
                            }
                            if (function.brackets > 0) {
                                function.brackets--
                            }

                            updateFunctionsLength(-(counter+1))
                        }
                        else {
                            if (textView.text.last() == ',') {
                                commaUsed = false
                            }

                            textView.text = textView.text.dropLast(1)
                            while (functionEnd > 0) {
                                textView.append(")")
                                functionEnd--
                            }
                            if (bracketsCounter > 0) {
                                bracketsCounter--
                            }
                            if (function.brackets > 0) {
                                function.brackets--
                            }

                            updateFunctionsLength(-1)
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

                        while (functionEnd > 0) {
                            textView.append(")")
                            functionEnd--
                        }

                        updateFunctionsLength(-1)
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
                    if (textView.text.isEmpty()
                        || (textView.text.last() != ')' && textView.text.last() != ',')) {

                        val function: Function<Int> =
                            Function(0, 0, 0, 0)

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
                    if (specialFunctionDeep == 0) {
                        if (textView.text.last() != ',') {
                            val function: Function<Int> =
                                Function(0, 0, 0, 0)

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
                        val functionEnd = getCurrentFunctionEnd(textView)

                        if (textView.text.dropLast(functionEnd) != ",") {
                            val newFunction: Function<Int> =
                                Function(0, 0, 0, 0)

                            textView.text = textView.text.dropLast(functionEnd)

                            val text = button.text.toString() + "()"
                            textView.append(text)

                            var counter = 0
                            while (counter < functionEnd) {
                                textView.append(")")
                                counter++
                            }

                            updateFunctionsLength(text.length)

                            newFunction.start = textView.text.length - functionEnd - 2
                            newFunction.end = textView.text.length - functionEnd - 1
                            newFunction.deep = ++specialFunctionDeep

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
}