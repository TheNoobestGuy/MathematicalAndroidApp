package com.example.mathmaster.customviews

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class Equations(var original: MutableList<Any>?, var derivative: MutableList<Any>?)

data class UnknownEntity(var multiplier: Double?, var variable: Char?, var powerTo: Double?) {
    fun isNotEmpty(): Boolean {
        return multiplier != null && variable != null && powerTo != null
    }

    fun isEmpty(): Boolean {
        return multiplier == null && variable == null && powerTo == null
    }

    fun getOriginal(negative: Boolean = false): MutableList<Any> {
        val original = mutableListOf<Any>()

        if (isNotEmpty()) {
            if (powerTo!! == 0.0) {
                if (negative) {
                    original.add(-multiplier!!)
                }
                else {
                    original.add(multiplier!!)
                }
            }
            else {
                if (negative) {
                    original.add(-multiplier!!)
                }
                else {
                    original.add(multiplier!!)
                }
                original.add(variable!!)
                original.add('^')
                original.add(powerTo!!)
            }
        }
        else {
            if (multiplier != null) {
                if (negative) {
                    original.add(-multiplier!!)
                }
                else {
                    original.add(multiplier!!)
                }

                if (powerTo != null) {
                    original.clear()
                    if (negative) {
                        original.add((-multiplier!!).pow(powerTo!!))
                    }
                    else {
                        original.add(multiplier!!.pow(powerTo!!))
                    }
                }
            }
        }

        return original
    }

    fun getDerivative(): MutableList<Any> {
        val derivative = mutableListOf<Any>()

        if (isNotEmpty()) {
            derivative.add(multiplier!!*powerTo!!)
            if (powerTo!!-1 != 0.0) {
                derivative.add(variable!!)
                derivative.add('^')
                derivative.add(powerTo!!-1)
            }
        }

        return derivative
    }

    fun onlyNumber(): Boolean {
        return multiplier != null && variable == null && powerTo == null
    }

    fun clear() {
        multiplier = null
        variable = null
        powerTo = null
    }

    operator fun times(other: UnknownEntity): UnknownEntity {
        return if (this.isNotEmpty() && other.isNotEmpty()) {
            UnknownEntity(this.multiplier!! * other.multiplier!!, this.variable, this.powerTo!! + other.powerTo!!)
        } else if (this.onlyNumber() && other.isNotEmpty()) {
            UnknownEntity(this.multiplier!! * other.multiplier!!, other.variable, other.powerTo)
        } else if (this.isNotEmpty() && other.onlyNumber()) {
            UnknownEntity(this.multiplier!! * other.multiplier!!, this.variable, this.powerTo)
        } else if (this.onlyNumber() && other.onlyNumber()) {
            UnknownEntity(this.multiplier!! * other.multiplier!!, null, null)
        } else if (this.isEmpty() && other.isNotEmpty()) {
            UnknownEntity(other.multiplier, other.variable, other.powerTo)
        } else if (this.isNotEmpty() && other.isEmpty()) {
            UnknownEntity(this.multiplier, this.variable, this.powerTo)
        } else if (this.isEmpty() && other.onlyNumber()) {
            UnknownEntity(other.multiplier, other.variable, other.powerTo)
        } else if (this.onlyNumber() && other.isEmpty()) {
            UnknownEntity(this.multiplier, this.variable, this.powerTo)
        } else {
            UnknownEntity(null, null, null)
        }
    }

    operator fun div(other: UnknownEntity): UnknownEntity {
        return if (this.isNotEmpty() && other.isNotEmpty()) {
            if (this.powerTo!! - other.powerTo!! == 0.0) {
                UnknownEntity(this.multiplier!! / other.multiplier!!, null, null)
            }
            else {
                UnknownEntity(this.multiplier!! / other.multiplier!!, this.variable, this.powerTo!! - other.powerTo!!)
            }
        } else if (this.onlyNumber() && other.isNotEmpty()) {
            UnknownEntity(this.multiplier!! / other.multiplier!!, other.variable, -(other.powerTo!!))
        } else if (this.isNotEmpty() && other.onlyNumber()) {
            UnknownEntity(this.multiplier!! / other.multiplier!!, this.variable, this.powerTo!!)
        } else if (this.onlyNumber() && other.onlyNumber()) {
            UnknownEntity(this.multiplier!! / other.multiplier!!, null, null)
        } else if (this.isEmpty() && other.isNotEmpty()) {
            UnknownEntity(other.multiplier, other.variable, other.powerTo)
        } else if (this.isNotEmpty() && other.isEmpty()) {
            UnknownEntity(this.multiplier, this.variable, this.powerTo)
        } else if (this.isEmpty() && other.onlyNumber()) {
            UnknownEntity(other.multiplier, other.variable, other.powerTo)
        } else if (this.onlyNumber() && other.isEmpty()) {
            UnknownEntity(this.multiplier, this.variable, this.powerTo)
        } else {
            UnknownEntity(null, null, null)
        }
    }
}

class Calculator {
    private val matrixCalculator = MatrixCalculator()

    // Advance calculator
    fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
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
        var brackets = false

        val range = transformedEquation.size - 1 downTo 0
        for (i in range) {
            if (transformedEquation[i] == ')') {
                closeBrackets++
                brackets = true
            }
            else if (transformedEquation[i] == '(') {
                if (closeBrackets == 0) {
                    return i
                }
                openBrackets++

                if (closeBrackets == openBrackets) {
                    return i
                }
            }
            else if (transformedEquation[i] == '√') {
                openBrackets++
                if (closeBrackets == openBrackets && brackets) {
                    if (i > 0) {
                        return i - 1
                    }
                }
            }
            else if (transformedEquation[i] == '+' || transformedEquation[i] == '-') {
                if (closeBrackets == openBrackets) {
                    return i+1
                }
            }
            else if (transformedEquation[i] == '=') {
                return i+1
            }
            else if (variable && transformedEquation[i] == '/') {
                if (closeBrackets == openBrackets) {
                    return i+1
                }
            }
            else if (transformedEquation[i] is Char && (transformedEquation[i] as Char).isLetter()) {
                if (transformedEquation[i] != 'x' && transformedEquation[i] != 'y' && transformedEquation[i] != 'z') {
                    if (closeBrackets == openBrackets) {
                        return i
                    }
                }
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
        val multiplyOrDivide  = mutableListOf(false)
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
                                    transformedEquation.add(
                                        additionalOpenedBrackets.last().removeLast()
                                    )
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

                            addBracketIndex = findNewBracketIndex(transformedEquation)
                            transformedEquation.add(addBracketIndex, '(')
                            additionalOpenedBrackets.last().add(')')
                            transformedEquation.add('×')
                            multiplyOrDivide[multiplyOrDivide.size - 1] = true
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

                        multiplyOrDivide.add(false)
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

                        multiplyOrDivide.removeLast()
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

                        transformedEquation.add(element)
                        multiplyOrDivide[multiplyOrDivide.size-1] = false
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

                        if (!multiplyOrDivide.last()) {
                            addBracketIndex = findNewBracketIndex(transformedEquation)
                            transformedEquation.add(addBracketIndex, '(')
                            additionalOpenedBrackets.last().add(')')
                        }

                        if (element == '/') {
                            addBracketIndex = findNewBracketIndex(transformedEquation)
                            transformedEquation.add(addBracketIndex, '(')
                            transformedEquation.add(')')
                        }
                        transformedEquation.add(element)

                        multiplyOrDivide[multiplyOrDivide.size-1] = true
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
                                if (!multiplyOrDivide.last()) {
                                    addBracketIndex = findNewBracketIndex(transformedEquation)
                                    transformedEquation.add(addBracketIndex, '(')
                                    additionalOpenedBrackets.last().add(')')

                                    multiplyOrDivide[multiplyOrDivide.size-1] = true
                                }
                            }
                        }

                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() is Double) {
                                transformedEquation.add('×')
                            }
                        }

                        val constant = if (element == 'π') PI else Math.E
                        transformedEquation.add(constant)
                        inDegree = false
                    }
                    'x', 'y', 'z' -> {
                        var add = false
                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() is Double) {
                                if (transformedEquation.isNotEmpty() && transformedEquation.last() != '(') {
                                    if (!multiplyOrDivide.last()) {
                                        addBracketIndex = findNewBracketIndex(transformedEquation)
                                        transformedEquation.add(addBracketIndex, '(')
                                        additionalOpenedBrackets.last().add(')')

                                        addBracketIndex = findNewBracketIndex(transformedEquation, variable = true)
                                        transformedEquation.add(addBracketIndex, '(')
                                        add = true
                                    }
                                }

                                if (add) {
                                    transformedEquation.add(')')
                                }

                                transformedEquation.add('×')
                                multiplyOrDivide[multiplyOrDivide.size-1] = true
                            }
                        }
                        else {
                            additionalOpenedBrackets.last().removeLast()
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
                        multiplyOrDivide.clear()
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

        println("ORIGINAL")
        println(transformedEquation)
        return transformedEquation
    }

    private fun factorial(number: Double): Double {
        if (number <= 1.0) {
            return 1.0
        }
        return number * factorial(number-1)
    }

    fun substituteVariable(equation: MutableList<Any>, variable: Double): MutableList<Any> {
        val result = mutableListOf<Any>()

        for (element in equation) {
            when (element) {
                is Char -> {
                    if (element == 'x') {
                        result.add(variable)
                    }
                    else {
                        result.add(element)
                    }
                }
                else -> {
                    result.add(element)
                }
            }
        }

        return result
    }

    fun calculateEquation(equation: MutableList<Any>, index: Int): PairEquation<Double, Int> {
        var equationSign = 'E'
        val result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index
        val threshold = 1E-10
        var lastChar = '0'

        while (iterator < equation.size) {
            when (equation[iterator]) {
                is Char -> {
                    if (equation[iterator] == '(') {
                        val equationBuffer = calculateEquation(equation, iterator + 1)
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
                        val equationBuffer = calculateEquation(equation, iterator + 1)
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
                        val equationBuffer = calculateEquation(equation, iterator + 2)

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

    // Solve equations with unknowns calculator
    private fun getEntitiesOfEquation(equation: MutableList<Any>): MutableList<UnknownEntity> {
        val entities = mutableListOf<UnknownEntity>()

        val entity = UnknownEntity(null, null, null)
        var power = false
        var operator = '0'
        for (element in equation) {
            when(element) {
                '=' -> {
                    if (entity.isNotEmpty()) {
                        entities.add(entity.copy())
                    }

                    return entities
                }
                '+', '-' -> {
                    if (entity.multiplier != null) {
                        entities.add(entity.copy())
                    }

                    power = false
                    entity.clear()
                    operator = element as Char
                }
                '^' -> power = true
                is Double -> {
                    if (power) {
                        entity.powerTo = element
                    }
                    else {
                        if (operator == '-') {
                            entity.multiplier = -element
                        }
                        else {
                            entity.multiplier = element
                        }
                    }
                }
                is Char -> {
                    entity.variable = element
                }
            }
        }
        if (entity.multiplier != null) {
            entities.add(entity.copy())
        }

        return entities
    }

    private fun multiplyTwoEquations(f: MutableList<Any>, g: MutableList<Any>, divide: Boolean = false): MutableList<Any> {
        if (f.isEmpty() && g.isNotEmpty()) {
            return g
        }
        else if (g.isEmpty() && f.isNotEmpty()) {
            return f
        }
        val result = mutableListOf<Any>()

        val entitiesF = getEntitiesOfEquation(f)
        val entitiesG = getEntitiesOfEquation(g)

        println("multiplyTwoEquations")
        println(f)
        println(g)
        println(entitiesF)
        println(entitiesG)

        for (entityF in entitiesF) {
            for (entityG in entitiesG) {
                val entity = if (divide) {
                    entityF / entityG
                } else {
                    entityF * entityG
                }

                // Recreate operators
                var negative = false
                if (entity.multiplier!! < 0) {
                    negative = true
                }

                if (negative) {
                    if (result.isNotEmpty()) {
                        result.add('-')
                    }
                    result.addAll(entity.getOriginal(negative = true))
                }
                else {
                    if (result.isNotEmpty()) {
                        result.add('+')
                    }
                    result.addAll(entity.getOriginal())
                }
            }
        }

        println("TwoEquations")
        println(result)
        return result
    }

    private fun checkIsEquationCalculable(equation: MutableList<Any>) : Boolean {
        for (element in equation) {
            if (element is Char) {
                if (element.isLetter()) {
                    return false
                }
            }
        }
        return true
    }

    private fun appendWhatLasts(resultI: MutableList<Any>, noComputeIndex: Int?,
                                entity: UnknownEntity, multipliers: MutableList<UnknownEntity>, dividers: MutableList<UnknownEntity>,
                                multiply: Boolean, divide: Boolean, divideEquation: Boolean): MutableList<Any> {
        if (noComputeIndex == null) {
            println("RETURN")
            return resultI
        }

        var result = resultI

        var empty = false
        var additionalEntity = if (!entity.isEmpty()) {
            entity
        } else {
            empty = true
            UnknownEntity(null, null, null)
        }

        var divider = UnknownEntity(null, null, null)
        var multiplier = UnknownEntity(null, null, null)

        if (multipliers.isNotEmpty() || dividers.isNotEmpty()) {
            for (div in dividers) {
                divider *= div
            }
            for (mul in multipliers) {
                multiplier *= mul
            }

            if (empty) {
                additionalEntity = multiplier / divider
            }
            else {
                additionalEntity *= multiplier
                additionalEntity /= divider
            }
        }

        val buffer: MutableList<Any>
        if (noComputeIndex != result.size && noComputeIndex != 0 && (multiply || divide)) {
            buffer = multiplyTwoEquations(result.subList(noComputeIndex+1, result.size), additionalEntity.getOriginal(), !empty && divideEquation)
            result = result.subList(0, noComputeIndex)
            result.addAll(buffer)
        }
        else if (noComputeIndex == 0) {
            println("BAD")
            buffer = multiplyTwoEquations(result, additionalEntity.getOriginal(), !empty && divideEquation)
            return buffer
        }
        else {
            if (divide) {
                result.add('/')
            }
            else if (multiply) {
                result.add('×')
            }

            result.addAll(additionalEntity.getOriginal())
        }

        return result
    }

    private fun transformEquationForSolvingUnknowns(equation: MutableList<Any>, index: Int): TripleSolve<MutableList<Any>, Int, Int?> {
        // Variables
        var result = mutableListOf<Any>()
        var noComputeIndex: Int? = 0
        var compute = true

        var divide = false
        var divideEquation = false
        var multiply = false

        val entity = UnknownEntity(null, null, null)

        val dividers = mutableListOf<UnknownEntity>()
        val multipliers = mutableListOf<UnknownEntity>()

        var iterator = index
        while (iterator < equation.size) {
            println(equation[iterator])
            when (equation[iterator]) {
                // Check special chars
                '(' -> {
                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1)
                    iterator = subEquation.iterator

                    var value = 0.0
                    if (checkIsEquationCalculable(subEquation.list)) {
                        value = calculateEquation(subEquation.list, 0).first
                        if (divide) {
                            dividers.last().multiplier = value
                        }
                        else if (multiply) {
                            multipliers.last().multiplier = value
                        }
                        else {
                            if (entity.multiplier != null) {
                                entity.multiplier = entity.multiplier!! + value - 1
                            }
                            else {
                                entity.multiplier = value
                            }
                        }
                    }
                    else {
                        if (subEquation.compute == null) {
                            noComputeIndex = result.size
                            compute = false
                        }
                        else if (subEquation.list.size > 4 && divideEquation) {
                            noComputeIndex = result.size
                        }

                        println("subEquation.list")
                        println(subEquation.list)

                        val buffer: MutableList<Any>
                        if (noComputeIndex != result.size) {
                            buffer = multiplyTwoEquations(result.subList(noComputeIndex!!, result.size), subEquation.list, divideEquation)
                            result = result.subList(0, noComputeIndex)
                        }
                        else {
                            buffer = subEquation.list
                        }

                        if (noComputeIndex == result.size && noComputeIndex != 0) {
                            if (result.last() != ')' && result.last() != '+' && result.last() != '-') {
                                result.add(0, '(')
                                result.add(')')
                            }

                            if (divide) {
                                result.add('/')
                                compute = false
                            }
                            else if (multiply) {
                                result.add('×')
                                noComputeIndex = result.size
                            }

                            if (result.last() != '+' && result.last() != '-') {
                                buffer.add(0, '(')
                                buffer.add(')')
                            }
                        }

                        result.addAll(buffer)

                        if (!compute) {
                            noComputeIndex = result.size
                        }
                    }

                    multiply = false
                    divide = false
                    continue
                }
                ')' -> {
                    result = appendWhatLasts(result, noComputeIndex, entity, multipliers, dividers,
                        multiply, divide, divideEquation)

                    if (!compute) {
                        noComputeIndex = null
                    }

                    return TripleSolve(result, iterator+1, noComputeIndex)
                }
                '+', '-' -> {
                    result = appendWhatLasts(result, noComputeIndex, entity, multipliers, dividers,
                        multiply, divide, divideEquation)

                    multipliers.clear()
                    dividers.clear()

                    result.add(equation[iterator])
                    noComputeIndex = result.size
                    multiply = false
                    divide = false
                    divideEquation = false
                }
                '×' -> {
                    multipliers.add(UnknownEntity(null, null, null))
                    multiply = true
                    divide = false
                }
                '/' -> {
                    dividers.add(UnknownEntity(null, null, null))
                    divide = true
                    multiply = false
                    divideEquation = true
                }
                '^', '√' -> {
                    // Add bracket for power
                    var newBracketIndex = equation.size-1
                    var brackets = 0
                    var i = iterator+1
                    while (i < equation.size) {
                        when (equation[i]) {
                            '(' -> brackets++
                            ')' -> {
                                brackets--

                                if (brackets <= 0) {
                                    newBracketIndex = i
                                    break
                                }
                            }
                        }
                        i++
                    }
                    equation.add(newBracketIndex, ')')

                    // Power recurrent call
                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1)
                    iterator = subEquation.iterator

                    var powerTo = 0.0
                    if (checkIsEquationCalculable(subEquation.list)) {
                        powerTo = calculateEquation(subEquation.list, 0).first
                        if (divide) {
                            dividers.last().powerTo = powerTo
                        }
                        else if (multiply) {
                            multipliers.last().powerTo = powerTo
                        }
                        else {
                            entity.powerTo = powerTo
                        }
                    }
                    else {
                        // BUILD STRING
                    }
                    continue
                }
                '=' -> {
                    result.add('=')
                }
                is Double -> {
                    if (divide) {
                        dividers.last().multiplier = equation[iterator] as Double
                    }
                    else if (multiply) {
                        multipliers.last().multiplier = equation[iterator] as Double
                    }
                    else {
                        entity.multiplier = equation[iterator] as Double
                    }
                }
                is Char -> {
                    if (divide) {
                        dividers.last().variable = equation[iterator] as Char
                        if (dividers.last().multiplier == null) {
                            dividers.last().multiplier = 1.0
                        }
                        dividers.last().powerTo = 1.0
                    }
                    else if (multiply) {
                        multipliers.last().variable = equation[iterator] as Char
                        if (multipliers.last().multiplier == null) {
                            multipliers.last().multiplier = 1.0
                        }
                        multipliers.last().powerTo = 1.0
                    }
                    else {
                        entity.variable = equation[iterator] as Char
                        if (entity.multiplier == null) {
                            entity.multiplier = 1.0
                        }
                        entity.powerTo = 1.0
                    }
                }
            }

            iterator++
        }
        result = appendWhatLasts(result, noComputeIndex, entity, multipliers, dividers,
            multiply, divide, divideEquation)

        println("transformEquationForSolvingUnknowns:")
        println(result)
        return TripleSolve(result, iterator+1, noComputeIndex)
    }

    private fun groupUnknowns(equation: MutableList<Any>, eqSign: Boolean = false) : MutableList<Any> {
        val result = mutableListOf<Any>()
        val map = LinkedHashMap<MutableList<Any>, Double>()

        val entities = getEntitiesOfEquation(equation)

        var calcResult = 0.0
        for (entity in entities) {
            var key = entity.getOriginal()
            val value = key.removeFirst() as Double
            if (key.isEmpty()) {
                key = mutableListOf("v")
                map[key] = map.getOrDefault(key, 0.0) + value
            }
            else {
                map[key] = map.getOrDefault(key, 0.0) + value
            }
        }

        for ((k, v) in map) {
            if (v != 0.0) {
                if (result.isNotEmpty()) {
                    result.add('+')
                }
                result.add(v)
                if (k.first() !is String) {
                    result.addAll(k)
                }
                else {
                    if (eqSign) {
                        result.removeLast()
                        calcResult = -v
                    }
                }
            }
        }

        if (result.isNotEmpty() && result.last() is Char) {
            if (result.last() == '+') {
                result.removeLast()
            }
        }

        if (eqSign) {
            result.add('=')
            result.add(calcResult)
        }

        return result
    }

    private fun groupEquation(equation: MutableList<Any>, iterator: Int) : Pair<MutableList<Any>, Int> {
        val resultEquation = mutableListOf<Any>()
        val stackForEntities = mutableListOf<Any>()

        var powerTo = false

        var i = iterator
        while (i < equation.size) {
            when(equation[i]) {
                '+', '-' -> {
                    powerTo = false
                }
                '(' -> {
                    val subEquation = groupEquation(equation, i+1)
                    i = subEquation.second+1

                    if (stackForEntities.isNotEmpty() && stackForEntities.last() == '-') {
                        resultEquation.add(stackForEntities.removeLast())
                    }

                    resultEquation.add('(')
                    resultEquation.addAll(subEquation.first)
                    resultEquation.add(')')
                    continue
                }
                ')' -> {
                    resultEquation.addAll(stackForEntities)

                    return if (powerTo) {
                        Pair(resultEquation, i)
                    } else {
                        Pair(groupUnknowns(resultEquation), i)
                    }
                }
                '^' ->  {
                    powerTo = true

                    if (stackForEntities.isEmpty()) {
                        resultEquation.add(equation[i])
                        i++
                        continue
                    }
                }
            }
            stackForEntities.add(equation[i])
            i++
        }
        resultEquation.add('+')
        resultEquation.addAll(groupUnknowns(stackForEntities, eqSign = true))

        return Pair(resultEquation, 0)
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
            val eq = groupEquation(transformEquationForSolvingUnknowns(transformEquation(equation), 0).list, iterator = 0)
            list.add(eq.first)
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
                            number = 0.0
                        }

                        powerKey = null
                        key = null
                    }
                    '×', '/'-> {
                        i++
                        continue
                    }
                    '(' -> {
                        if (powerKey == null && number == 0.0) {
                            val equationInBrackets = calcPowerForStandardEquations(
                                equation,
                                iterator = i + 1,
                                amountOfVariables = amountOfVariables
                            )
                            i = equationInBrackets.third
                            number = equationInBrackets.second
                            continue
                        }
                        else if (powerKey == null) {
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

            val determinant = matrixCalculator.determinant(baseMatrix)
            if (determinant == 0.0) {
                return null
            }

            for (i in 0 until unknowns.size) {
                val determinantUnknown = matrixCalculator.determinant(replaceColumnWithResults(equations, i))
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

    private fun solveQuadraticEquation(coefficients: MutableList<MutableList<Triple<String, Double, Double>>>): MutableList<Pair<Char, Double>>? {
        val result: MutableList<Pair<Char, Double>> = mutableListOf()



        return null
    }

    private fun substituteVariableForDerivative(equation: MutableList<Any>, variable: Double): MutableList<Any> {
        val result = mutableListOf<Any>()
        var additionalBrackets = 0
        var powerTo = false
        for (element in equation) {
            when (element) {
                '^' -> {
                    result.add(element)
                    powerTo = true
                }
                '+', '-' -> {
                    if (additionalBrackets != 0) {
                        additionalBrackets--
                        result.add(')')
                    }

                    if (powerTo) {
                        result.add(')')
                        powerTo = false
                    }
                    result.add(element)
                }
                ')' -> {
                    if (powerTo) {
                        result.add(')')
                        powerTo = false
                    }

                    if (additionalBrackets != 0) {
                        additionalBrackets--
                        result.add(element)
                    }

                    result.add(element)
                }
                '×' -> {
                    result.add(element)
                }
                is Char -> {
                    if (element == 'x') {
                        // Start bracket
                        val bracketIndex = findNewBracketIndex(result)
                        result.add(bracketIndex, '(')
                        additionalBrackets++

                        result.add('×')
                        result.add('(')
                        result.add(variable)
                    }
                    else {
                        result.add(element)
                    }
                }
                is Double -> {
                    result.add(element)

                    if (powerTo) {
                        result.add(')')
                        powerTo = false
                    }
                }
            }
        }

        return result
    }

    fun solveEquationsWithUnknowns(equationsList: MutableList<String>): MutableList<Pair<Char, Double>>? {
        val listOfEquations = transformEquationsForSolve(equationsList)

        println("------------------------")
        println("Transformed")
        println(listOfEquations[0])
        println("Derivative")
        val derivative = findDerivative(listOfEquations[0], iterator = 0)
        println(derivative.second)
        println("------------------------")

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

    // Derivatives calculator
    private fun calculateFunctionDerivative(wholeExpression: MutableList<Pair<MutableList<Any>, Boolean>>, f: MutableList<Any>): Equations {
        val equations = Equations(null, null)
        var functionF = f
        var derivativeF = findDerivative(f, 0).second
        var functionG = mutableListOf<Any>()
        var derivativeG = mutableListOf<Any>()

        // Get G function and it's derivative
        if (wholeExpression.size >= 1) {
            var divided = mutableListOf<Any>()
            var divider = mutableListOf<Any>()

            for (eq in wholeExpression) {
                if (!eq.second) {
                    if (divider.isEmpty()) {
                        divider.addAll(eq.first)
                    }
                    else {
                        divider = multiplyTwoEquations(findDerivative(divider, 0).first, eq.first)
                    }
                }
                else {
                    if (divided.isEmpty()) {
                        divided.addAll(eq.first)
                    }
                    else {
                        divided = multiplyTwoEquations(findDerivative(divided, 0).first, eq.first)
                    }
                }
            }
            if (divided.isNotEmpty()) {
                functionF = multiplyTwoEquations(findDerivative(divided, 0).first, f)
                derivativeF = findDerivative(functionF, 0).second
            }
            functionG = divider
            derivativeG = findDerivative(divider, 0).second

            wholeExpression.clear()
        }
        val sspecialOperator = '/'
        when(sspecialOperator) {
            '×' -> {
                val dFxG = groupUnknowns(multiplyTwoEquations(derivativeF, functionG))
                val dGxF = groupUnknowns(multiplyTwoEquations(derivativeG, functionF))

                val result = mutableListOf<Any>()
                result.add('(')
                result.add('(')
                result.addAll(dFxG)
                result.add(')')
                result.add('+')
                result.add('(')
                result.addAll(dGxF)
                result.add(')')
                result.add(')')


                equations.derivative = result
                equations.original = result
            }
            '/' -> {
                val dFxG = groupUnknowns(multiplyTwoEquations(derivativeF, functionG))
                val dGxF = groupUnknowns(multiplyTwoEquations(derivativeG, functionF))
                val gx2 = groupUnknowns(multiplyTwoEquations(functionG, functionG))

                val result = mutableListOf<Any>()

                val divided = mutableListOf<Any>()
                divided.add('(')
                divided.addAll(dFxG)
                divided.add(')')
                divided.add('-')
                divided.add('(')
                divided.addAll(dGxF)
                divided.add(')')

                result.add('(')
                result.addAll(divided)
                result.add(')')
                result.add('/')
                result.add('(')
                result.addAll(gx2)
                result.add(')')

                equations.derivative = result
                equations.original = result
            }
        }

        return equations
    }

    private fun findDerivative(equation: MutableList<Any>, iterator: Int, expressions: MutableList<Pair<MutableList<Any>, Boolean>> = mutableListOf()): Triple<MutableList<Any>, MutableList<Any>, Int> {
        val derivative = mutableListOf<Any>()
        val original = mutableListOf<Any>()
        var power = false

        val entity = UnknownEntity(null, null, null)
        var previousEquation: Equations? = null
        var specialOperator: Char? = null
        var i = iterator
        while (i < equation.size) {
            when(equation[i]){
                '=' -> break
                '+', '-' -> {
                    if (previousEquation != null && expressions.size >= 1 && specialOperator != null) {
                        if (entity.isNotEmpty()) {
                            if (specialOperator == '/')  {
                                expressions.add(Pair(entity.getOriginal(), false))
                            }
                            else if (specialOperator == '×'){
                                expressions.add(Pair(entity.getOriginal(), true))
                            }
                        }

                        val buffer = calculateFunctionDerivative(expressions, previousEquation.original!!)
                        derivative.addAll(buffer.derivative!!)
                        original.addAll(buffer.original!!)
                    }
                    else if (previousEquation != null) {
                        if (specialOperator == '/')  {
                            expressions.add(Pair(entity.getOriginal(), false))
                        }
                        else if (specialOperator == '×'){
                            expressions.add(Pair(entity.getOriginal(), true))
                        }

                        derivative.addAll(findDerivative(previousEquation.original!!, iterator = 0).second)
                        original.addAll(previousEquation.original!!)
                    }
                    else {
                        derivative.addAll(entity.getDerivative())
                        original.addAll(entity.getOriginal())
                    }

                    power = false
                    entity.clear()
                    specialOperator = null
                    derivative.add(equation[i] as Char)
                    original.add(equation[i] as Char)
                }
                '^' -> power = true
                '(' -> {
                    val nextStep = findDerivative(equation, iterator = i+1, expressions)
                    i = nextStep.third

                    if (previousEquation == null) {
                        previousEquation = Equations(null, null)
                        previousEquation.original = nextStep.first
                        previousEquation.derivative = nextStep.second
                    }
                    else {
                        if (specialOperator == '/')  {
                            expressions.add(Pair(nextStep.first, false))
                        }
                        else if (specialOperator == '×'){
                            expressions.add(Pair(nextStep.first, true))
                        }
                    }
                    continue
                }
                ')' -> {
                    if (entity.onlyNumber()) {
                        original.add(entity.multiplier as Double)
                    }
                    else {
                        derivative.addAll(entity.getDerivative())
                        original.addAll(entity.getOriginal())
                    }
                    return Triple(original, derivative, i+1)
                }
                '×', '/' -> {
                    specialOperator = equation[i] as Char
                }
                is Double -> {
                    if (power) {
                        entity.powerTo = equation[i] as Double
                    }
                    else {
                        entity.multiplier = equation[i] as Double
                    }
                }
                is Char -> {
                    entity.variable = equation[i] as Char
                }
            }
            i++
        }
        if (previousEquation != null && expressions.size >= 1 && specialOperator != null) {
            if (entity.isNotEmpty()) {
                if (specialOperator == '/')  {
                    expressions.add(Pair(entity.getOriginal(), false))
                }
                else if (specialOperator == '×'){
                    expressions.add(Pair(entity.getOriginal(), true))
                }
            }

            val buffer = calculateFunctionDerivative(expressions, previousEquation.original!!)
            derivative.addAll(buffer.derivative!!)
            original.addAll(buffer.original!!)
        }
        else {
            derivative.addAll(entity.getDerivative())
            original.addAll(entity.getOriginal())
        }

        return Triple(original, derivative, i+1)
    }

    private fun convertSignsForOutputDerivative(equation: MutableList<Any>): MutableList<Any> {
        val convertedList = mutableListOf<Any>()
        for (element in equation) {
            if (element is Double && convertedList.isNotEmpty()) {
                if (element < 0 && convertedList.last() == '+') {
                    convertedList[convertedList.size-1] = '-'
                    convertedList.add(-element)
                }
                else if (element < 0 && convertedList.last() == '-') {
                    convertedList.add(-element)
                }
                else {
                    convertedList.add(element)
                }
            }
            else {
                convertedList.add(element)
            }
        }

        if (convertedList.isNotEmpty() && convertedList.first() == '+') {
            convertedList.removeFirst()
        }

        return convertedList
    }

    fun solveDerivative(equation: String): MutableList<Any> {
        val transformedEquation = groupEquation(transformEquationForSolvingUnknowns(transformEquation(equation), 0).list, iterator = 0).first

        println("groupEquation")
        println(transformedEquation)

        println("Derivative")
        val derivative = findDerivative(transformedEquation, iterator = 0)
        println(derivative.second)

        val substitute = substituteVariableForDerivative(derivative.second, 1.0)
        val calc = calculateEquation(substitute, 0)
        println(calc)

        val substitute1 = substituteVariableForDerivative(derivative.second, 2.0)
        val calc1 = calculateEquation(substitute1, 0)
        println(calc1)

        val substitute2 = substituteVariableForDerivative(derivative.second, 3.0)
        val calc2 = calculateEquation(substitute2, 0)
        println(calc2)

        return convertSignsForOutputDerivative(derivative.second)
    }
}