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

    fun getOriginal(): MutableList<Any> {
        val original = mutableListOf<Any>()

        if (isNotEmpty()) {
            original.add(multiplier!!)
            original.add(variable!!)
            original.add('^')
            original.add(powerTo!!)
        }
        else {
            if (multiplier != null) {
                original.add(multiplier!!)
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
}

class Calculator {
    private val matrixCalculator = MatrixCalculator()

    // Advance calculator
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

    private fun findNewBracketIndex(transformedEquation: MutableList<Any>, flag: Boolean = false): Int {
        var openBrackets = 0
        var closeBrackets = 0
        var brackets = flag

        val range = transformedEquation.size - 1 downTo 0

        for (i in range) {
            if (transformedEquation[i] == ')') {
                if (i != transformedEquation.size - 1 && !brackets) {
                    return i+2
                }

                closeBrackets++
                brackets = true
            }
            else if (transformedEquation[i] == '(') {
                if (!brackets) {
                    break
                }
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

            if (closeBrackets == openBrackets && brackets) {
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
        val multiplyOrDivide  = mutableListOf(false)
        var inDegree = false
        var addDegree = false
        var inRoot = false
        var negativeNumber = false

        // Brackets
        val bracketsInput: MutableList<Char> = mutableListOf()

        var addBracketIndex: Int?
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

                            if (transformedEquation.last() is Char) {
                                if ((transformedEquation.last() as Char).isLetter()) {
                                    addBracketIndex = findNewBracketIndex(transformedEquation)
                                    transformedEquation.add(addBracketIndex!!, '(')
                                    transformedEquation.add(')')
                                }
                            }
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
                            transformedEquation.add(addBracketIndex!!, '(')
                            additionalOpenedBrackets.last().add(')')
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

                        addBracketIndex = findNewBracketIndex(transformedEquation, flag = true)
                        transformedEquation.add(addBracketIndex!!, '(')

                        if (negativeNumber) {
                            if (transformedEquation.last() is Double) {
                                transformedEquation[transformedEquation.size-1] = -(transformedEquation[transformedEquation.size-1] as Double)
                                transformedEquation.add(addBracketIndex!!, '-')
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
                                    transformedEquation.add(addBracketIndex!!, '(')
                                    additionalOpenedBrackets.last().add(')')

                                    multiplyOrDivide[multiplyOrDivide.size-1] = true
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
                        if (!multiplyOrDivide.last()) {
                            addBracketIndex = findNewBracketIndex(transformedEquation, true)
                            transformedEquation.add(addBracketIndex!!, '(')
                            additionalOpenedBrackets.last().add(')')
                            multiplyOrDivide[multiplyOrDivide.size-1] = true
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
                            transformedEquation.add(addBracketIndex!!, '(')
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
        println("tearIntoPiecesEquation")
        println(equation)
        for (i in equation) {
            when (i) {
                '+', '-' -> {
                    val copy = mutableListOf<Any>()
                    copy.addAll(buffer)
                    result.add(copy)
                    buffer.clear()
                }
                else -> {
                    if (i != ')' && i != '(') {
                        buffer.add(i)
                    }
                }
            }
        }
        if (buffer.isNotEmpty()) {
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
        println(firstPieces)
        println(secondPieces)
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

    private fun addOnesForSpecialOperations(equation: MutableList<Any>): MutableList<Any>{
        val transformedEquation = mutableListOf<Any>()
        transformedEquation.addAll(equation)
        /*
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
        }*/

        return transformedEquation
    }

    private fun transformEquationForSolvingUnknowns(equation: MutableList<Any>, index: Int, flag: Boolean, subtract: Boolean = false): TripleSolve<MutableList<Any>, Int, Int?> {
        // Variables
        var result = mutableListOf<Any>()
        val unknowns = mutableListOf<Char>()
        val multipliers = mutableListOf<Double>()

        var lastElement: Any = '0'
        var multiply = false
        var divide = false
        val specialOperatorsStack = mutableListOf<Char>()
        var keepMultiply = false
        var compute = flag
        var noComputeIndex: Int? = null

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

                    if (result.isNotEmpty()) {
                        if (result.last() == '+' || result.last() == '-') {
                            keepMultiply = false
                        }
                    }

                    // Turn off compute if brackets are non computable
                    if (compute) {
                        compute = subEquation.compute == null
                    }

                    // Check for keep going divide
                    if (divide) {
                        if (subEquation.list.size <= 4){
                            if (subEquation.compute == null) {
                                if (!compute){
                                    val buffer = if (noComputeIndex != null) {
                                        transformEquationForSolvingUnknowns(result.subList(noComputeIndex, result.size), 0, flag = true, subtract = false)
                                    }
                                    else {
                                        transformEquationForSolvingUnknowns(result, 0, flag = true, subtract = false)
                                    }

                                    compute = buffer.compute == null
                                }
                            }
                            else {
                                compute = false
                            }
                        }
                        else {
                            compute = false
                        }
                    }

                    // Check for brackets multiplication or division
                    if (compute) {
                        if (!keepMultiply) {
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (divide) {
                                    val buffer = calculateTwoEquations(calculateUnknownsAndMultipliers(unknowns, multipliers, true), subEquation.list, false)
                                    result.addAll(buffer)

                                    unknowns.clear()
                                    multipliers.clear()
                                }
                                else if (multiply){
                                    val buffer = calculateTwoEquations(calculateUnknownsAndMultipliers(unknowns, multipliers, true), subEquation.list, true)
                                    result.addAll(buffer)
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
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (result.isNotEmpty() && specialOperatorsStack.isNotEmpty()) {
                                    result.add(specialOperatorsStack.removeFirst())
                                }

                                result.addAll(
                                    calculateUnknownsAndMultipliers(
                                        unknowns,
                                        multipliers,
                                        true
                                    )
                                )
                                unknowns.clear()
                                multipliers.clear()
                            }

                            val buffer: MutableList<Any>
                            if (divide) {
                                buffer = if (noComputeIndex == null) {
                                    calculateTwoEquations(result, subEquation.list, false)
                                }
                                else {
                                    if (noComputeIndex+1 < result.size) {
                                        calculateTwoEquations(result.subList(noComputeIndex+1, result.size), subEquation.list, false)
                                    }
                                    else {
                                        subEquation.list
                                    }
                                }

                                if (noComputeIndex != null)  {
                                    if (noComputeIndex != result.size) {
                                        result = result.subList(0, noComputeIndex)
                                        buffer.add(0, '(')
                                        buffer.add(0, '/')
                                        buffer.add(')')
                                    }
                                    else {
                                        buffer.add(0, '/')
                                    }
                                }
                                else {
                                    result.clear()
                                }
                            }
                            else {
                                buffer = if (noComputeIndex == null) {
                                    calculateTwoEquations(result, subEquation.list, true)
                                } else {
                                    if (noComputeIndex+1 < result.size) {
                                        calculateTwoEquations(result.subList(noComputeIndex+1, result.size), subEquation.list, true)
                                    }
                                    else {
                                        subEquation.list
                                    }
                                }

                                if (noComputeIndex != null)  {
                                    if (noComputeIndex != result.size) {
                                        result = result.subList(0, noComputeIndex)
                                        buffer.add(0, '(')
                                        buffer.add(0, '×')
                                        buffer.add(')')
                                    }
                                    else {
                                        buffer.add(0, '×')
                                    }
                                }
                                else {
                                    result.clear()
                                }
                            }

                            result.addAll(buffer)
                            buffer.clear()
                        }

                        if (iterator < equation.size) {
                            if (equation[iterator] == '×') {
                                multiply = true
                                keepMultiply = true
                                divide = false
                            }
                            else if (equation[iterator] == '/') {
                                divide = true
                                keepMultiply = true
                                multiply = false
                                compute = false
                            }
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }
                    else {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            var add = false
                            if (result.isNotEmpty() && specialOperatorsStack.isNotEmpty()) {
                                result.add(specialOperatorsStack.removeFirst())
                                result.add('(')
                                add = true
                            }
                            result.addAll(
                                calculateUnknownsAndMultipliers(
                                    unknowns,
                                    multipliers,
                                    true
                                )
                            )
                            unknowns.clear()
                            multipliers.clear()
                            if (add) {
                                result.add(')')
                            }
                        }

                        if (result.isNotEmpty()) {
                            if (result.last() is Double) {
                                result.add(0, '(')
                                result.add(')')
                            }

                            if (result.last() == ')') {
                                if (divide) {
                                    result.add('/')
                                }
                                else {
                                    result.add('×')
                                }
                            }
                        }

                        if (result.isNotEmpty()) {
                            subEquation.list.add(0, '(')
                            subEquation.list.add(')')
                        }

                        result.addAll(subEquation.list)

                        if (iterator < equation.size) {
                            if (equation[iterator] == '×') {
                                multiply = true
                                divide = false
                            }
                            else if (equation[iterator] == '/') {
                                divide = true
                                multiply = false
                            }
                        }
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
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            if (divide) {
                                result.add('/')
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                            }
                            else if (multiply){
                                result.add('×')
                                result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                            }
                        }
                    }

                    if (!compute) {
                        noComputeIndex = result.size
                    }

                    return TripleSolve(result, iterator+1, noComputeIndex)
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
                    compute = true
                }
                '×' -> {
                    if ((divide || !multiply) && !keepMultiply) {
                        unknowns.clear()
                        multipliers.clear()

                        if (result.isNotEmpty()) {
                            if (result.last() != ')') {
                                result.removeLast()
                            }
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

                    if (!compute) {
                        noComputeIndex = result.size
                        compute = true
                    }

                    if (specialOperatorsStack.size == 2) {
                        specialOperatorsStack.removeFirst()
                    }
                    specialOperatorsStack.add(equation[iterator] as Char)

                    multiply = true
                    number = false
                }
                '/' -> {
                    if ((multiply || !divide) && !keepMultiply) {
                        unknowns.clear()
                        multipliers.clear()

                        if (result.isNotEmpty()) {
                            if (result.last() != ')') {
                                result.removeLast()
                            }
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
                    if (iterator+1 < equation.size && equation[iterator+1] != '(') {
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
                            if (i >= equation.size-1) {
                                equation.add(')')
                            }
                            else {
                                equation.add(i, ')')
                            }
                        }
                    }

                    if (specialOperatorsStack.size == 2) {
                        specialOperatorsStack.removeFirst()
                    }
                    specialOperatorsStack.add(equation[iterator] as Char)

                    divide = true
                    number = false
                }
                '^', '√' -> {
                    var subtraction = false
                    if (lastElement == '-') {
                        subtraction = true
                    }

                    // Add brackets for powerTo
                    println(equation)
                    if (iterator+1 < equation.size && equation[iterator+1] != '(') {
                        var i = iterator+1
                        var brackets = 0
                        var bracket = false
                        while (i < equation.size) {
                            when (equation[i]) {
                                '+', '-' -> {
                                    if (brackets == 0) {
                                        break
                                    }
                                }
                                '/', '×' -> {
                                    if (brackets == 0) {
                                        break
                                    }
                                }
                                '(' -> {
                                    bracket = true
                                    brackets++
                                }
                                ')' -> brackets--
                            }
                            if ((brackets == 0 && bracket) || brackets < 0) {
                                break
                            }

                            i++
                        }
                        equation.add(i, ')')
                    }
                    println(equation)
                    val subEquation = transformEquationForSolvingUnknowns(equation, iterator+1, flag)
                    iterator = subEquation.iterator
                    println(subEquation)
                    val additionalEquation = mutableListOf<Any>()

                    // If power to is equal 0
                    var appended = false
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
                    }

                    // Append unknowns with multipliers
                    if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                        additionalEquation.addAll(
                            calculateUnknownsAndMultipliers(
                                unknowns,
                                multipliers,
                                true
                            )
                        )
                        unknowns.clear()
                        multipliers.clear()
                    }

                    // Handle root and power
                    if (subEquation.list.size == 1 && !appended) {
                        if (subEquation.list.last() is Double) {
                            if (additionalEquation.isNotEmpty() && additionalEquation.last() is Double) {
                                if (divide) {
                                    additionalEquation[additionalEquation.size-1] = additionalEquation[additionalEquation.size-1] as Double - subEquation.list.last() as Double + 1
                                    appended = true
                                }
                                else {
                                    additionalEquation[additionalEquation.size-1] = additionalEquation[additionalEquation.size-1] as Double + subEquation.list.last() as Double - 1
                                    appended = true
                                }
                            }
                        }
                    }
                    println("ADDITIONAL")
                    println(additionalEquation)
                    println(result)

                    if (!appended && additionalEquation.isNotEmpty()) {
                        if (subtraction) {
                            additionalEquation.add(0, '-')
                        }
                        additionalEquation.add(0, '(')
                        additionalEquation.add(0, '(')
                        additionalEquation.add(')')
                        additionalEquation.add('^')
                        additionalEquation.add('(')
                        additionalEquation.addAll(subEquation.list)
                        additionalEquation.add(')')
                        additionalEquation.add(')')
                        result.addAll(additionalEquation)

                        noComputeIndex = result.size
                        compute = false
                    }

                    // Check for brackets multiplication or division
                    if (compute) {
                        if (!keepMultiply) {
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (divide) {
                                    val buffer = calculateTwoEquations(calculateUnknownsAndMultipliers(unknowns, multipliers, true), additionalEquation, false)
                                    result.addAll(buffer)

                                    unknowns.clear()
                                    multipliers.clear()
                                }
                                else if (multiply){
                                    val buffer = calculateTwoEquations(calculateUnknownsAndMultipliers(unknowns, multipliers, true), additionalEquation, true)
                                    result.addAll(buffer)
                                    unknowns.clear()
                                    multipliers.clear()
                                }
                                else {
                                    result.addAll(additionalEquation)
                                }
                            }
                            else {
                                result.addAll(additionalEquation)
                            }
                        }
                        else {
                            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                                if (result.isNotEmpty() && specialOperatorsStack.isNotEmpty()) {
                                    result.add(specialOperatorsStack.removeFirst())
                                }

                                result.addAll(
                                    calculateUnknownsAndMultipliers(
                                        unknowns,
                                        multipliers,
                                        true
                                    )
                                )
                                unknowns.clear()
                                multipliers.clear()
                            }

                            val buffer: MutableList<Any>
                            if (divide) {
                                buffer = if (noComputeIndex == null) {
                                    calculateTwoEquations(result, additionalEquation, false)
                                }
                                else {
                                    if (noComputeIndex+1 < result.size) {
                                        calculateTwoEquations(result.subList(noComputeIndex+1, result.size), additionalEquation, false)
                                    }
                                    else {
                                        subEquation.list
                                    }
                                }

                                if (noComputeIndex != null)  {
                                    if (noComputeIndex != result.size) {
                                        result = result.subList(0, noComputeIndex)
                                        buffer.add(0, '(')
                                        buffer.add(0, '/')
                                        buffer.add(')')
                                    }
                                    else {
                                        buffer.add(0, '/')
                                    }
                                }
                                else {
                                    result.clear()
                                }
                            }
                            else {
                                buffer = if (noComputeIndex == null) {
                                    calculateTwoEquations(result, additionalEquation, true)
                                } else {
                                    if (noComputeIndex+1 < result.size) {
                                        calculateTwoEquations(result.subList(noComputeIndex+1, result.size), additionalEquation, true)
                                    }
                                    else {
                                        subEquation.list
                                    }
                                }

                                if (noComputeIndex != null)  {
                                    if (noComputeIndex != result.size) {
                                        result = result.subList(0, noComputeIndex)
                                        buffer.add(0, '(')
                                        buffer.add(0, '×')
                                        buffer.add(')')
                                    }
                                    else {
                                        buffer.add(0, '×')
                                    }
                                }
                                else {
                                    result.clear()
                                }
                            }

                            result.addAll(buffer)
                            buffer.clear()
                        }

                        if (iterator < equation.size) {
                            if (equation[iterator] == '×') {
                                multiply = true
                                keepMultiply = true
                                divide = false
                            }
                            else if (equation[iterator] == '/') {
                                divide = true
                                keepMultiply = true
                                multiply = false
                                compute = false
                            }
                        }

                        unknowns.clear()
                        multipliers.clear()
                    }
                    else {
                        if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                            var add = false
                            if (result.isNotEmpty() && specialOperatorsStack.isNotEmpty()) {
                                result.add(specialOperatorsStack.removeFirst())
                                result.add('(')
                                add = true
                            }
                            result.addAll(
                                calculateUnknownsAndMultipliers(
                                    unknowns,
                                    multipliers,
                                    true
                                )
                            )
                            unknowns.clear()
                            multipliers.clear()
                            if (add) {
                                result.add(')')
                            }
                        }

                        if (result.isNotEmpty()) {
                            if (result.last() is Double) {
                                result.add(0, '(')
                                result.add(')')
                            }

                            if (result.last() == ')') {
                                if (divide) {
                                    result.add('/')
                                }
                                else {
                                    result.add('×')
                                }
                            }
                        }

                        if (result.isNotEmpty()) {
                            additionalEquation.add(0, '(')
                            additionalEquation.add(')')
                        }

                        result.addAll(additionalEquation)

                        if (iterator < equation.size) {
                            if (equation[iterator] == '×') {
                                multiply = true
                                divide = false
                            }
                            else if (equation[iterator] == '/') {
                                divide = true
                                multiply = false
                            }
                        }
                    }
                    println(result)
                    continue
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
            if (unknowns.isNotEmpty() || multipliers.isNotEmpty()) {
                if (divide) {
                    result.add('/')
                    result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, false))
                }
                else if (multiply){
                    result.add('×')
                    result.addAll(calculateUnknownsAndMultipliers(unknowns, multipliers, true))
                }
            }
        }

        println("transformEquationForSolvingUnknowns:")
        println(result)
        return TripleSolve(result, iterator+1, noComputeIndex)
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
                                       closeBracket: Boolean = false, equalSign: Boolean,
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
            else if (v == 0.0) {
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
                    if (result.isNotEmpty()) {
                        if (result.last() != ')') {
                            equationWithOperators.removeFirst()
                        }
                    }
                    else {
                        equationWithOperators.removeFirst()
                    }
                }
            }
        }

        if (lastAddition) {
            equationWithOperators.add('=')
            equationWithOperators.add(map.getOrDefault(mutableListOf("value"), 0.0))
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
                                result.add(operator)
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
                            value = 0.0
                            operator = equation[i] as Char
                        }
                        '/' -> {
                            operatorsList.add(equation[i] as Char)
                            operatorsIndexes.add(operatorsList.size-1)
                            value = 0.0

                            operator = equation[i] as Char
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
        appendEquationToResult(result, map, value, key, operator, operatorsList, operatorsIndexes, closeBracket, equalSign, lastAddition = true, insideRec = insideRec)

        println("groupUnknowns:")
        println(result)
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
            val eq = groupUnknowns(transformEquationForSolvingUnknowns(addOnesForSpecialOperations(transformEquation(equation)), 0 ,true).list, eqSign = false, insideRec = false,0).first
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

    fun substituteVariableForDerivative(equation: MutableList<Any>, variable: Double): MutableList<Any> {
        val result = mutableListOf<Any>()

        var powerTo = false
        for (element in equation) {
            when (element) {
                '^' -> {
                    result.add(element)
                    powerTo = true
                }
                '+', '-' -> {
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
                    result.add(element)
                }
                '×' -> {
                    result.add(element)
                }
                is Char -> {
                    if (element == 'x') {
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
        println(result)
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

        val substitute = substituteVariableForDerivative(derivative.second, 1.0)
        val calc = calculateEquation(substitute, 0)
        println(calc)

        val substitute1 = substituteVariableForDerivative(derivative.second, 2.0)
        val calc1 = calculateEquation(substitute1, 0)
        println(calc1)

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

    private fun multiplyTwoEquations(f: MutableList<Any>, g: MutableList<Any>): MutableList<Any> {
        val result = mutableListOf<Any>()

        val entitiesF = getEntitiesOfEquation(f)
        val entitiesG = getEntitiesOfEquation(g)

        println("ENTITY")
        println(f)
        println(g)
        println(entitiesF)
        println(entitiesG)
        for (entityF in entitiesF) {
            for (entityG in entitiesG) {
                val entity = UnknownEntity(null, null, null)

                // Multiplier
                if (entityF.multiplier != null && entityG.multiplier != null) {
                    entity.multiplier = entityF.multiplier!! * entityG.multiplier!!
                }
                else if (entityF.multiplier != null) {
                    entity.multiplier = entityF.multiplier!!
                }
                else if (entityG.multiplier != null) {
                    entity.multiplier = entityG.multiplier!!
                }

                // Variable
                if (entityF.variable != null) {
                    entity.variable = entityF.variable!!
                }
                else if (entityG.variable != null) {
                    entity.variable = entityG.variable!!
                }

                // Power
                if (entityF.powerTo != null && entityG.powerTo != null) {
                    entity.powerTo = entityF.powerTo!! + entityG.powerTo!!
                }
                else if (entityF.powerTo != null) {
                    entity.powerTo = entityF.powerTo!!
                }
                else if (entityG.powerTo != null) {
                    entity.powerTo = entityG.powerTo!!
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
                    result.addAll(entity.getOriginal())
                }
                else {
                    if (result.isNotEmpty()) {
                        result.add('+')
                    }
                    result.addAll(entity.getOriginal())
                }
            }
        }
        println(result)
        return result
    }

    private fun groupUnknownsForDerivatives(equation: MutableList<Any>) : MutableList<Any> {
        val result = mutableListOf<Any>()
        val map = LinkedHashMap<MutableList<Any>, Double>()

        val entities = getEntitiesOfEquation(equation)

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
            if (result.isNotEmpty()) {
                result.add('+')
            }
            result.add(v)
            if (k.first() !is String) {
                result.addAll(k)
            }
        }
        return result
    }

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
                println(functionF)
            }
            functionG = divider
            derivativeG = findDerivative(divider, 0).second

            wholeExpression.clear()
        }
        val sspecialOperator = '/'
        when(sspecialOperator) {
            '×' -> {

            }
            '/' -> {
                val dFxG = groupUnknownsForDerivatives(multiplyTwoEquations(derivativeF, functionG))
                val dGxF = groupUnknownsForDerivatives(multiplyTwoEquations(derivativeG, functionF))
                val gx2 = groupUnknownsForDerivatives(multiplyTwoEquations(functionG, functionG))

                val result = mutableListOf<Any>()
                result.add('(')
                result.add('(')
                result.addAll(dFxG)
                result.add(')')
                result.add('-')
                result.add('(')
                result.addAll(dGxF)
                result.add(')')
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

    fun findDerivative(equation: MutableList<Any>, iterator: Int, expressions: MutableList<Pair<MutableList<Any>, Boolean>> = mutableListOf()): Triple<MutableList<Any>, MutableList<Any>, Int> {
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
                    if (previousEquation != null) {
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

            println(expressions)

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
}