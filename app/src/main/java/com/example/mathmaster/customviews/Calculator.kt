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

data class Equations(var original: MutableList<Any>? = null, var derivative: MutableList<Any>? = null)

data class UnknownEntity(var multiplier: Double? = null, var variable: Char? = null, var powerTo: Double? = null) {
    private var function: Char? = null
    private var functionContent: MutableList<Any>? = null

    fun isFunction(): Boolean {
        return function != null && functionContent != null
    }

    fun setFunction(function: Char, functionList: MutableList<Any>) {
        this.function = function
        this.functionContent = functionList
    }

    fun getFunction(): Char? {
        return function
    }

    fun getFunctionContent(): MutableList<Any>?  {
        return functionContent
    }

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
            else if (variable != null && powerTo != null) {
                original.add(1.0)
                original.add(variable!!)
                original.add('^')
                original.add(powerTo!!)
            }
        }

        return original
    }

    fun getDerivative(): MutableList<Any> {
        val derivative = mutableListOf<Any>()

        if (variable == null) {
            return derivative
        }

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

    fun clear() {
        multiplier = null
        variable = null
        powerTo = null
    }

    fun onlyNumber(): Boolean {
        return multiplier != null && variable == null && powerTo == null
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownEntity) return false

        if (this.isFunction() && other.isFunction()) {
            if (this.functionContent!!.size == other.functionContent!!.size) {
                for (i in this.functionContent!!.indices) {
                    if (this.functionContent!![i] != other.functionContent!![i]) {
                        return false
                    }
                }
            }
            else {
                return false
            }
        }
        else {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = multiplier?.hashCode() ?: 0
        result = 31 * result + (variable?.hashCode() ?: 0)
        result = 31 * result + (powerTo?.hashCode() ?: 0)
        result = 31 * result + (function?.hashCode() ?: 0)
        result = 31 * result + (functionContent?.hashCode() ?: 0)
        return result
    }
}

class Calculator {
    private val matrixCalculator = MatrixCalculator()
    private val fractions = Fractions()

    // Advance calculator
    private fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
    }

    private fun findNewBracketIndex(transformedEquation: MutableList<Any>, variable: Boolean = false, multiplyDivide: Boolean = false): Int {
        var openBrackets = 0
        var closeBrackets = 0
        var bracket = false

        val range = transformedEquation.size - 1 downTo 0
        for (i in range) {
            if (transformedEquation[i] == ')') {
                closeBrackets++
            }
            else if (transformedEquation[i] == '(') {
                bracket = true

                if (closeBrackets == 0) {
                    return i
                }
                openBrackets++
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
                    if (multiplyDivide && bracket && closeBrackets == 0) {
                        return i+1
                    }
                    else if (!multiplyDivide && closeBrackets == openBrackets) {
                        return i
                    }
                }
            }

            if (closeBrackets == openBrackets && !multiplyDivide) {
                if ((i > 0 && transformedEquation[i-1] is Char && (transformedEquation[i-1] as Char).isLetter()) || (i > 0 && transformedEquation[i-1] == '√')) {
                    if (transformedEquation[i-1] != 'x' && transformedEquation[i-1] != 'y' && transformedEquation[i-1] != 'z') {
                        return i-1
                    }
                }
                return i
            }
        }

        return 0
    }

    fun transformEquation(input: String): MutableList<Any> {
        val transformedEquation: MutableList<Any> = mutableListOf()

        // Equation variables
        var whatFunction = '0'
        var lastChar = '?'

        // Number variables
        var commaInUse = false
        var numberBase = 0
        var numBuffer = ""

        // Equation validation
        val multiplyOrDivide = mutableListOf(false)
        var inDegree = false
        var addDegree = false
        val inRoot  = mutableListOf(false)
        var negativeNumber = false

        // Brackets
        val bracketsInput: MutableList<Char> = mutableListOf()

        var addBracketIndex: Int
        val powerToOpenedBrackets: MutableList<Int> = mutableListOf()
        val additionalOpenedBrackets: MutableList<MutableList<Char>> = mutableListOf(mutableListOf())

        // Transform equation for calculations
        input.forEach { element ->
            if (element.isDigit()) {
                // Add digit to buffer
                numBuffer += element

                // Decimal number
                if (lastChar == '.') {
                    val outputNumber: Double = ("$numberBase.$numBuffer").toDouble()

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
                        val outputNumber: Double = numBuffer.toDouble()

                        if (element == '.') {
                            numberBase = outputNumber.toInt()
                            numBuffer = ""
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
                            'a' -> if (whatFunction != 't') whatFunction = element
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
                        multiplyOrDivide.add(false)

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
                            if (inRoot.last()) {
                                while (additionalOpenedBrackets.last().isNotEmpty()) {
                                    transformedEquation.add(
                                        additionalOpenedBrackets.last().removeLast()
                                    )
                                }
                                inRoot.removeLast()
                            }

                            if (powerToOpenedBrackets.isNotEmpty()) {
                                if (powerToOpenedBrackets.last() >= additionalOpenedBrackets.size - 1) {
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
                                addBracketIndex = findNewBracketIndex(transformedEquation, multiplyDivide = true)
                                transformedEquation.add(addBracketIndex, '(')
                                additionalOpenedBrackets.last().add(')')
                            }

                            transformedEquation.add('×')
                            multiplyOrDivide[multiplyOrDivide.size - 1] = true
                        }

                        if (element == '√') {
                            transformedEquation.add(element)
                            transformedEquation.add('(')
                            additionalOpenedBrackets.last().add(')')
                            inRoot.add(true)
                        }
                        else {
                            if (whatFunction != '0') {
                                transformedEquation.add(whatFunction)
                                negativeNumber = false
                                whatFunction = '0'
                            }

                            additionalOpenedBrackets.add(mutableListOf())
                            bracketsInput.add(')')
                            transformedEquation.add(element)
                            inRoot.add(false)
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
                        inRoot.removeLast()
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
                        inRoot[inRoot.size-1] = false
                        inDegree = false
                    }
                    '×', '/' -> {
                        if (inRoot.last()) {
                            if (additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                            inRoot.removeLast()
                        }

                        if (powerToOpenedBrackets.isNotEmpty()) {
                            if (powerToOpenedBrackets.last() >= additionalOpenedBrackets.size-1) {
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

                        addBracketIndex = findNewBracketIndex(transformedEquation, multiplyDivide = true)

                        if (!multiplyOrDivide.last()) {
                            transformedEquation.add(addBracketIndex, '(')
                            additionalOpenedBrackets.last().add(')')
                        }

                        transformedEquation.add(element)

                        multiplyOrDivide[multiplyOrDivide.size-1] = true
                        inRoot[inRoot.size-1] = false
                        inDegree = false
                        negativeNumber = false
                    }
                    '^' -> {
                        if (inRoot.last()) {
                            while (additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                            inRoot.removeLast()
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
                        transformedEquation.add('(')
                        additionalOpenedBrackets.last().add(')')
                        additionalOpenedBrackets.last().add(')')

                        powerToOpenedBrackets.add(additionalOpenedBrackets.size-1)
                        inRoot[inRoot.size-1] = false
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
                        if (transformedEquation.isNotEmpty() && transformedEquation.last() != '(') {
                            if (inRoot.last()) {
                                if (additionalOpenedBrackets.last().isNotEmpty()) {
                                    transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                                }
                                inRoot.removeLast()
                            }
                        }

                        if (transformedEquation.isNotEmpty()) {
                            if (transformedEquation.last() is Double || transformedEquation.last() == ')') {
                                if (powerToOpenedBrackets.isNotEmpty() &&
                                    powerToOpenedBrackets.last() >= additionalOpenedBrackets.size-1) {
                                    while (additionalOpenedBrackets.last().isNotEmpty()) {
                                        transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                                    }
                                    powerToOpenedBrackets.removeLast()
                                }

                            }
                        }

                        if (!multiplyOrDivide.last() && transformedEquation.isNotEmpty()
                            && transformedEquation.last() != '√' && transformedEquation.last() != '^') {
                            addBracketIndex = findNewBracketIndex(transformedEquation)
                            transformedEquation.add(addBracketIndex, '(')
                            additionalOpenedBrackets.last().add(')')
                            multiplyOrDivide[multiplyOrDivide.size-1] = true
                        }

                        if (transformedEquation.isNotEmpty() && transformedEquation.last() != '('
                            && transformedEquation.last() != '×'  && transformedEquation.last() != '/'
                            && transformedEquation.last() != '√'
                            && transformedEquation.last() != '^') {
                            transformedEquation.add('×')
                        }

                        transformedEquation.add(element)
                        inDegree = false
                    }
                    '!', '%', '°' -> {
                        if (inRoot.last()) {
                            if (additionalOpenedBrackets.last().isNotEmpty()) {
                                transformedEquation.add(additionalOpenedBrackets.last().removeLast())
                            }
                            inRoot.removeLast()
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

                        inRoot[inRoot.size-1] = false
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
                        whatFunction = '0'
                        lastChar = '?'

                        commaInUse = false
                        numberBase = 0
                        numBuffer = ""

                        // Equation validation
                        multiplyOrDivide.clear()
                        multiplyOrDivide.add(false)
                        inDegree = false
                        addDegree = false
                        inRoot[inRoot.size-1] = false
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

                lastChar = element
                numBuffer = ""
            }
        }
        // Add number that lasts in buffer
        if (numBuffer.isNotEmpty() && lastChar != '.') {
            val outputNumber: Double = numBuffer.toDouble()

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

        // Add all the brackets that lasts in buffers
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

    fun calculateEquation(equation: MutableList<Any>, index: Int = 0, baseOfLogarithm: Double = 2.0): PairEquation<Double, Int> {
        var equationSign = 'E'
        val result: PairEquation<Double, Int> = PairEquation(0.0, index)
        var iterator: Int = index
        val threshold = 1E-10
        var lastChar = '0'

        while (iterator < equation.size) {
            when (equation[iterator]) {
                is Char -> {
                    if (equation[iterator] == '(') {
                        val equationBuffer = calculateEquation(equation,iterator + 1, baseOfLogarithm)
                        when (equationSign) {
                            '+' -> result.first +=  equationBuffer.first
                            '-' -> result.first -= equationBuffer.first
                            '×' -> result.first *= equationBuffer.first
                            '/' -> result.first /= equationBuffer.first
                            '^' -> result.first = (result.first).pow(equationBuffer.first)
                            'E' -> result.first = equationBuffer.first
                        }
                        iterator = equationBuffer.second
                    }
                    else if (equation[iterator] == '√') {
                        val equationBuffer = calculateEquation(equation, iterator + 2, baseOfLogarithm)
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
                        val equationBuffer = calculateEquation(equation, iterator + 2, baseOfLogarithm)

                        if (lastChar == '-') {
                            equationSign = '-'
                        }

                        when (equationSign) {
                            '+' -> {
                                when (equation[iterator]) {
                                    's' -> result.first += sin(equationBuffer.first)
                                    'c' -> result.first += cos(equationBuffer.first)
                                    't' -> result.first += tan(equationBuffer.first)
                                    'g' -> result.first += log(equationBuffer.first, baseOfLogarithm)
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
                                    'g' -> result.first -= log(equationBuffer.first, baseOfLogarithm)
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
                                    'g' -> result.first *= log(equationBuffer.first, baseOfLogarithm)
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
                                    'g' -> result.first /= log(equationBuffer.first, baseOfLogarithm)
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
                                        (result.first).pow(log(equationBuffer.first, baseOfLogarithm))

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
                                    'g' -> result.first = log(equationBuffer.first, baseOfLogarithm)
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
                    if(element.isLetter()) {
                        entity.variable = element

                        if (entity.multiplier == null) {
                            entity.multiplier = 1.0
                        }
                    }
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

        return result
    }

    private fun checkIsEquationCalculable(equation: MutableList<Any>, iterator: Int) : Pair<MutableList<Any>, Int?> {
        val result = mutableListOf<Any>()
        var i = iterator
        var brackets = 0
        while (i < equation.size) {
            when (equation[i]) {
                '(' -> brackets++
                ')' -> {
                    brackets--
                    if (brackets <= 0) {
                        result.add(equation[i])
                        return Pair(result, i+1)
                    }
                }
                is Char -> {
                    if (equation[i] == 'x' || equation[i] == 'y' || equation[i] == 'z') {
                        return Pair(result, null)
                    }
                }
            }

            result.add(equation[i])
            i++
        }
        return Pair(result, --i)
    }

    private fun getMultiplier(entity: UnknownEntity, multipliers: MutableList<UnknownEntity>, dividers: MutableList<UnknownEntity>): MutableList<Any> {
        var additionalEntity = entity.copy()

        // Calculate multipliers and dividers
        var divider = UnknownEntity(null, null, null)
        var multiplier = UnknownEntity(null, null, null)

        if (multipliers.isNotEmpty() || dividers.isNotEmpty()) {
            for (div in dividers) {
                divider *= div
            }
            for (mul in multipliers) {
                multiplier *= mul
            }

            if (!multiplier.isEmpty()) {
                additionalEntity *= multiplier
            }
            if (!divider.isEmpty()) {
                additionalEntity /= divider
            }
        }

        if(additionalEntity.multiplier == null && additionalEntity.variable == null && additionalEntity.powerTo != null) {
            additionalEntity.multiplier = entity.powerTo
            additionalEntity.powerTo = null
        }

        return additionalEntity.getOriginal()
    }

    private fun appendCalculationsToEquation(input: MutableList<Any>, noComputeIndex: Int?,
                                             entity: UnknownEntity, multipliers: MutableList<UnknownEntity>, dividers: MutableList<UnknownEntity>,
                                             multiply: Boolean, divide: Boolean, divideEquation: Boolean, function: Char?, clear: Boolean = true, special: Boolean = false): MutableList<Any> {
        if (noComputeIndex == null) {
            return input
        }
        else if (function == '!' || function == '°') {
            val operation = getMultiplier(entity, multipliers, dividers)
            operation.add(0, '(')
            operation.add(0,'(')
            if (function == '°') {
                operation.add('°')
            }
            else {
                operation.add('!')
            }
            operation.add(')')
            operation.add(')')

            if (clear) {
                entity.clear()
                multipliers.clear()
                dividers.clear()
            }

            return operation
        }

        // Append multiplication of entity with sub string of result that is computable
        var result = input
        if (result.isNotEmpty() && noComputeIndex != result.size && noComputeIndex != 0 && function == null) {
            val buffer = multiplyTwoEquations(result.subList(noComputeIndex, result.size), getMultiplier(entity, multipliers, dividers))
            result = result.subList(0, noComputeIndex)
            if (result.last() != '+' && result.last() != '-'
                && result.last() != '×' && result.last() != '/') {
                var add = false
                if (multiply) {
                    result.add(0, '×')
                    add = true
                }
                else if (divideEquation || divide) {
                    result.add('×')
                }

                buffer.add(0, '(')
                buffer.add(')')

                if (add) {
                    result.addAll(0, buffer)
                }
                else {
                    result.addAll(buffer)
                }
            }
            else {
                result.addAll(buffer)
            }
        }
        else if (result.isNotEmpty() && noComputeIndex == 0 && function == null) {
            val buffer = multiplyTwoEquations(result.subList(noComputeIndex, result.size), getMultiplier(entity, multipliers, dividers))

            if (clear) {
                entity.clear()
                multipliers.clear()
                dividers.clear()
            }

            return buffer
        }
        // If result is not computable just append calculated before entity
        else {
            val entityBuffer = getMultiplier(entity, multipliers, dividers)

            if (entityBuffer.isNotEmpty()) {
                if (!special) {
                    if (result.isNotEmpty() && result.last() == '=') {
                        result.addAll(entityBuffer)
                    }
                    else {
                        if (entityBuffer.isNotEmpty()) {
                            var add = false
                            if (result.isNotEmpty() && result.last() != '+' && result.last() != '-') {
                                if ((result.first() != '(' && result.last() != ')') && result.last() != '×' && result.last() != '/') {
                                    result.add(0, '(')
                                    result.add(')')
                                }

                                if (multiply) {
                                    result.add(0, '×')
                                    add = true
                                }
                                else if (divideEquation || divide) {
                                    result.add('×')
                                }
                            }

                            entityBuffer.add(0, '(')
                            entityBuffer.add(')')

                            if (add) {
                                result.addAll(0, entityBuffer)
                            }
                            else {
                                result.addAll(entityBuffer)
                            }
                        }
                    }
                }
                else {
                    val multiplier = mutableListOf(entityBuffer.removeFirst())
                    val entities = mutableListOf(entityBuffer, multiplier)

                    for (e in entities) {
                        if (e.isEmpty()) {
                            continue
                        }

                        var add = false
                        if (result.isNotEmpty() && result.last() != '+' && result.last() != '-') {
                            if ((result.first() != '(' && result.last() != ')') && result.last() != '×' && result.last() != '/') {
                                result.add(0, '(')
                                result.add(')')
                            }

                            if (multiply) {
                                result.add(0, '×')
                                add = true
                            }
                            else if (divideEquation || divide) {
                                result.add('×')
                            }
                        }

                        e.add(0, '(')
                        e.add(')')

                        if (add) {
                            result.addAll(0, e)
                        }
                        else {
                            result.addAll(e)
                        }
                    }
                }
            }
        }

        if (clear) {
            entity.clear()
            multipliers.clear()
            dividers.clear()
        }

        return result
    }

    private fun transformEquationForSolvingUnknowns(equation: MutableList<Any>,
                                                    multipliersInput: MutableList<UnknownEntity> = mutableListOf(),
                                                    dividersInput: MutableList<UnknownEntity> = mutableListOf(),
                                                    index: Int = 0, keepMultiplyInput: Boolean = false): TripleSolve<MutableList<Any>, Int, Int?> {
        // Variables
        var result = mutableListOf<Any>()
        var noComputeIndex: Int? = 0
        var computedIndex = 0
        var compute = true

        var divide = false
        var divideEquation = false
        var multiply = false
        var function: Char? = null
        var keepMultiply: Boolean

        val multipliers: MutableList<UnknownEntity> = mutableListOf()
        val dividers: MutableList<UnknownEntity> = mutableListOf()

        // Append keep going multiplication
        if (keepMultiplyInput) {
            multipliers.addAll(multipliersInput)
            dividers.addAll(dividersInput)
        }

        val entity = UnknownEntity(null, null, null)

        var iterator = index
        while (iterator < equation.size) {
            when (equation[iterator]) {
                '(' -> {
                    // If it is division and the divided is only number append it to result
                    if (!entity.isEmpty() || multipliers.isNotEmpty() || dividers.isNotEmpty()) {
                        result = appendCalculationsToEquation(
                                result, noComputeIndex, entity, multipliers, dividers,
                                multiply, divide, divideEquation, function, clear = true, special = false
                            )
                    }

                    // Check for keep going multiplication
                    keepMultiply = false
                    if (multipliers.isNotEmpty() || dividers.isNotEmpty()) {
                        keepMultiply = true
                    }

                    if (!keepMultiply) {
                        multipliers.clear()
                        dividers.clear()
                    }

                    // Check are brackets calculable
                    val checkIsItCalculable = checkIsEquationCalculable(equation, iterator)

                    // If sub-equation is calculable just append result of calculations
                    if (checkIsItCalculable.second != null && function != '^') {
                        val value = calculateEquation(checkIsItCalculable.first, baseOfLogarithm = 10.0).first

                        if (function == null || function == '√') {
                            if (divide) {
                                if (dividers.isNotEmpty() && dividers.last().isEmpty()) {
                                    dividers.add(dividers.removeLast())
                                }
                                else {
                                    dividers.add(UnknownEntity(null, null, null))
                                }
                                dividers.last().multiplier = value
                            }
                            else if (multiply) {
                                if (multipliers.isNotEmpty() && multipliers.last().isEmpty()) {
                                    multipliers.add(multipliers.removeLast())
                                }
                                else {
                                    multipliers.add(UnknownEntity(null, null, null))
                                }
                                multipliers.last().multiplier = value
                            }
                            else {
                                entity.multiplier = value
                            }

                            if(entity.isEmpty()) {
                                entity.multiplier = 1.0
                            }
                        }
                        else {
                            val functionEquation = mutableListOf<Any>()
                            functionEquation.add(function)
                            functionEquation.add('(')
                            functionEquation.add(value)
                            functionEquation.add(')')

                            entity.multiplier = calculateEquation(functionEquation, baseOfLogarithm = 10.0).first
                        }
                        iterator = checkIsItCalculable.second!!
                    }
                    else {
                        // Get equation
                        val subEquation = if (function == null) {
                            transformEquationForSolvingUnknowns(equation, multipliers, dividers, iterator + 1, keepMultiply)
                        }
                        else {
                            transformEquationForSolvingUnknowns(equation, mutableListOf(), mutableListOf(), iterator + 1, keepMultiply)
                        }

                        iterator = subEquation.iterator

                        // Check is there minus before equation
                        if (result.isNotEmpty() && result.last() == '-') {
                            var brackets = 0
                            var step = 0
                            while (step < subEquation.list.size) {
                                when (subEquation.list[step]) {
                                    '+' -> if (brackets == 0) subEquation.list[step] = '-'
                                    '-' -> if (brackets == 0) subEquation.list[step] = '+'
                                    '(' -> brackets++
                                    ')' -> brackets--
                                }
                                step++
                            }
                        }

                        // Handle compute
                        if (subEquation.compute == null) {
                            noComputeIndex = result.size
                            compute = false
                        }
                        else if (subEquation.list.size > 4 && divideEquation) {
                            noComputeIndex = result.size
                        }

                        // Append multiplication or division of found equations
                        var buffer: MutableList<Any>
                        if (result.isNotEmpty() && noComputeIndex != result.size && function == null) {
                            buffer = multiplyTwoEquations(result.subList(noComputeIndex!!, result.size), subEquation.list, divideEquation)
                            result = result.subList(0, noComputeIndex)
                        }
                        else {
                            buffer = subEquation.list

                            if (function != null) {
                                noComputeIndex = result.size
                            }
                        }

                        // Append result
                        var add = false
                        var inBrackets = false
                        if (noComputeIndex == result.size && noComputeIndex != 0 && result.isNotEmpty()) {
                            if (result.last() != '+' && result.last() != '-') {
                                if ((result.first() != '(' && result.last() != ')') && result.last() != '×' && result.last() != '/') {
                                    result.add(0, '(')
                                    result.add(')')
                                    inBrackets = true
                                }

                                if (result.last() != '×' && result.last() != '/') {
                                    if (divide) {
                                        if (!inBrackets) {
                                            result.add(0, '(')
                                            result.add(')')
                                        }
                                        result.add('/')
                                        compute = false
                                        add = true
                                    }
                                    else if (multiply) {
                                        result.add('×')
                                        noComputeIndex = result.size
                                        compute = true
                                        add = true
                                    }
                                }

                                if (add) {
                                    buffer.add(0, '(')
                                    buffer.add(')')
                                }

                                if (function == '^') {
                                    buffer.add(')')
                                }
                            }
                        }

                        // If it is function append brackets of function
                        if (function != null) {
                            if (!add) {
                                buffer.add(0, '(')
                                buffer.add(')')
                            }

                            buffer.add(0, function)
                        }
                        result.addAll(buffer)

                        // Move compute index if further compute is off
                        if (!compute || function != null) {
                            noComputeIndex = result.size
                        }

                        if (function == null) {
                            entity.clear()
                            multipliers.clear()
                            dividers.clear()
                        }

                        // Handle compute
                        if (subEquation.compute == null) {
                            noComputeIndex = result.size
                            compute = false
                        }
                    }

                    if (function == null) {
                        multiply = false
                        divide = false
                    }
                    else {
                        noComputeIndex = result.size
                        compute = false
                    }

                    function = null
                    computedIndex = result.size
                    continue
                }
                ')' -> {
                    if (computedIndex != result.size || !entity.isEmpty() || multipliers.isNotEmpty() || dividers.isNotEmpty()) {
                        result = if (function == null) {
                            appendCalculationsToEquation(
                                result, noComputeIndex, entity, multipliers, dividers,
                                multiply, divide, divideEquation, function = null, clear = false
                            )
                        } else {
                            appendCalculationsToEquation(
                                result, noComputeIndex, entity, multipliers, dividers,
                                multiply, divide, divideEquation, function, clear = false, special = true
                            )
                        }
                    }

                    if (!compute) {
                        noComputeIndex = null
                    }
                    return TripleSolve(result, iterator+1, noComputeIndex)
                }
                '=' -> {
                    result = if (function == null) {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function = null, clear = true
                        )
                    } else {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function, clear = true, special = true
                        )
                    }
                    result.add(equation[iterator])
                    noComputeIndex = result.size
                    computedIndex = result.size

                    multiply = false
                    divide = false
                    divideEquation = false
                    function = null
                    dividers.clear()
                    multipliers.clear()
                    entity.clear()
                    multipliersInput.clear()
                    dividersInput.clear()
                }
                '+', '-' -> {
                    result = if (function == null) {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function = null, clear = true
                        )
                    } else {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function, clear = true, special = true
                        )
                    }

                    if (keepMultiplyInput) {
                        multipliers.addAll(multipliersInput)
                        dividers.addAll(dividersInput)
                    }

                    result.add(equation[iterator])
                    noComputeIndex = result.size
                    computedIndex = result.size
                    multiply = false
                    divide = false
                    divideEquation = false
                    function = null
                }
                '×' -> {
                    if (multipliers.isNotEmpty() && multipliers.last().isEmpty()) {
                        multipliers.add(multipliers.removeLast())
                    }
                    else {
                        multipliers.add(UnknownEntity(null, null, null))
                    }
                    multiply = true
                    divide = false
                    function = null
                }
                '/' -> {
                    if (dividers.isNotEmpty() && dividers.last().isEmpty()) {
                        dividers.add(dividers.removeLast())
                    }
                    else {
                        dividers.add(UnknownEntity(null, null, null))
                    }
                    divide = true
                    multiply = false
                    divideEquation = true
                    function = null
                }
                '^' -> {
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

                    // Check are brackets calculable
                    val checkIsItCalculable = checkIsEquationCalculable(equation, iterator+1)
                    var add = true
                    var calculable = false
                    if (checkIsItCalculable.second != null) {
                        equation.removeAt(newBracketIndex)
                        iterator = checkIsItCalculable.second!!

                        if (function == null && !entity.isEmpty()) {
                            val powerTo = calculateEquation(checkIsItCalculable.first, baseOfLogarithm = 10.0).first

                            if (divide) {
                                dividers.last().powerTo = powerTo
                            }
                            else if (multiply) {
                                multipliers.last().powerTo = powerTo
                            }
                            else {
                                entity.powerTo = powerTo
                            }
                            add = false
                        }
                        calculable = true
                    }

                    if (add) {
                        // Variables
                        var powerBase = mutableListOf<Any>()
                        powerBase.addAll(result)
                        result.clear()
                        if (powerBase.isEmpty()) {
                            powerBase = entity.getOriginal()
                            entity.clear()
                        }

                        val multiplier = getMultiplier(entity, multipliers, dividers)
                        entity.clear()
                        multipliers.clear()
                        dividers.clear()

                        // Get equation
                        val powerTo: MutableList<Any>
                        if (calculable) {
                            powerTo = mutableListOf(calculateEquation(checkIsItCalculable.first, baseOfLogarithm = 10.0).first)
                        }
                        else {
                            val subEquation = transformEquationForSolvingUnknowns(
                                equation,
                                mutableListOf(),
                                mutableListOf(),
                                iterator + 1,
                                keepMultiplyInput = false
                            )
                            iterator = subEquation.iterator

                            powerTo = subEquation.list
                        }

                        val power = mutableListOf<Any>('(')
                        if (multiplier.isNotEmpty()) {
                            power.add('(')
                            power.addAll(multiplier)
                            power.add(')')
                            power.add('×')
                        }
                        power.add('(')
                        power.addAll(powerBase)
                        power.add(')')
                        power.add('^')
                        power.add('(')
                        power.addAll(powerTo)
                        power.add(')')
                        power.add(')')

                        result.addAll(power)
                        computedIndex = result.size
                        noComputeIndex = result.size
                        compute = false
                    }

                    function = null
                    continue
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

                    if (entity.isEmpty()) {
                        entity.multiplier = 1.0
                    }
                }
                '!' -> {
                    result = if (function == null) {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function = null, clear = true
                        )
                    } else {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function, clear = true, special = true
                        )
                    }

                    if (keepMultiplyInput) {
                        multipliers.addAll(multipliersInput)
                        dividers.addAll(dividersInput)
                    }
                }
                '°' -> {
                    result = if (function == null) {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function = null, clear = true
                        )
                    } else {
                        appendCalculationsToEquation(
                            result, noComputeIndex, entity, multipliers, dividers,
                            multiply, divide, divideEquation, function, clear = true, special = true
                        )
                    }

                    if (keepMultiplyInput) {
                        multipliers.addAll(multipliersInput)
                        dividers.addAll(dividersInput)
                    }
                }
                '√' -> {
                     function = equation[iterator] as Char
                }
                is Char -> {
                    if ((equation[iterator] as Char).isLetter()) {
                        // Handle unknowns
                        if (equation[iterator] == 'x' || equation[iterator] == 'y' || equation[iterator] == 'z') {
                            if (divide) {
                                if (dividers.isNotEmpty() && dividers.last().isEmpty()) {
                                    dividers.add(dividers.removeLast())
                                }
                                else {
                                    dividers.add(UnknownEntity(null, null, null))
                                }

                                dividers.last().variable = equation[iterator] as Char
                                if (dividers.last().multiplier == null) {
                                    dividers.last().multiplier = 1.0
                                }
                                dividers.last().powerTo = 1.0

                                if(entity.isEmpty()) {
                                    entity.multiplier = 1.0
                                }
                            }
                            else if (multiply) {
                                if (multipliers.isNotEmpty() && multipliers.last().isEmpty()) {
                                    multipliers.add(multipliers.removeLast())
                                }
                                else {
                                    multipliers.add(UnknownEntity(null, null, null))
                                }

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

                            if(entity.isEmpty()) {
                                entity.multiplier = 1.0
                            }
                        }
                        // Handle functions
                        else {
                            function = equation[iterator] as Char
                        }
                    }
                }
            }

            iterator++
        }
        result = if (function == null) {
            appendCalculationsToEquation(
                result, noComputeIndex, entity, multipliers, dividers,
                multiply, divide, divideEquation, function = null, clear = true
            )
        } else {
            appendCalculationsToEquation(
                result, noComputeIndex, entity, multipliers, dividers,
                multiply, divide, divideEquation, function, clear = true, special = true
            )
        }

        println("transformEquationForSolvingUnknowns:")
        println(result)
        return TripleSolve(result, iterator+1, noComputeIndex)
    }

    private fun groupUnknowns(equation: MutableList<Any>, eqSign: Boolean = false, negative: Boolean = false, firstOperator: Boolean = false) : MutableList<Any> {
        val result = mutableListOf<Any>()
        val map = HashMap<MutableList<Any>, Double>()

        val entities = getEntitiesOfEquation(equation)

        // Get entities
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

        // Sort map by power of entity
        val sortedMap = map.toList().sortedWith(
            compareByDescending<Pair<MutableList<Any>, Double>> {
                if (it.first.last() is Double) it.first.last() as Double else 0.0
            }.thenBy {
                if (it.first.first() is Char) it.first.first() as Char else 0
            }
        ).toMap()

        // Build output
        for ((k, v) in sortedMap) {
            if (v != 0.0) {
                // Deduce operator before number
                if (v < 0) {
                    if (negative) result.add('+') else result.add('-')
                    result.add(-v)
                }
                else {
                    if (negative) {
                        result.add('-')
                    } else  {
                        if (result.isNotEmpty() || firstOperator) {
                            result.add('+')
                        }
                    }
                    result.add(v)
                }

                // Append variable
                if (k.first() !is String) {
                    result.addAll(k)
                }

                // If equality sign then value without variable must be moved to left side of equation
                else {
                    if (eqSign) {
                        result.removeLast()
                        if (result.isNotEmpty() && result.last() is Char && result.last() != ')') {
                            result.removeLast()
                        }
                        calcResult = -v
                    }
                }
            }
        }
        // Remove redundant operator
        if (result.isNotEmpty() && result.last() is Char) {
            if (result.last() == '+' || result.last() == '-'
                || result.last() == '×' || result.last() == '/') {
                result.removeLast()
            }
        }

        if (eqSign) {
            result.add('=')
            result.add(calcResult)
        }

        return result
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
            val eq = groupEquation(transformEquationForSolvingUnknowns(transformEquation(equation)).list, eqSign = true)
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
        var powerTo = false

        for (element in equation) {
            when (element) {
                is Char -> {
                    if (element == 'x') {
                        if (result.last() is Double) {
                            result.add('×')
                        }
                        val newBracket = findNewBracketIndex(result)
                        result.add('(')
                        result.add(newBracket-1, '(')
                        result.add(variable)
                    }
                    else if (element == 'e') {
                        result.add(Math.E)
                    }
                    else if (element == '^') {
                        if (result.last() != ')') {
                            powerTo = true
                        }
                        result.add(element)
                    }
                    else if (element == '×') {
                        if (result.last() == 1.0) {
                            result.removeLast()
                        }
                        else {
                            result.add(element)
                        }
                    }
                    else {
                        result.add(element)
                    }
                }
                is Double -> {
                    result.add(element)

                    if (powerTo) {
                        result.add(')')
                        result.add(')')
                        powerTo = false
                    }
                }
                else -> {
                    result.add(element)
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
    private fun calculateSpecialDerivative(expressions: MutableList<Pair<Equations, Boolean>>): Equations {
        if (expressions.isEmpty()) {
            return Equations(null, null)
        }
        if (expressions.size == 1) {
            return expressions[0].first
        }

        val equations = Equations(null, null)

        // Determine is it division
        var divide = false
        for (expression in expressions) {
            if (!expression.second) {
                divide = true
                break
            }
        }

        if (divide) {
            val functionF = expressions.removeFirst().first
            val functionG = Equations(null, null)

            // Check does functionF is a function or not
            var functionFIsFunction = false
            var lastElement:Any = 0
            for (element in functionF.original!!) {
                if (element is Char) {
                    if (element.isLetter() || element == '√' || element == '^') {
                        if (element != 'x' && element != 'y' && element != 'z') {
                            functionFIsFunction = true
                            break
                        }
                    }
                    else  if (lastElement == '^' && element == '(') {
                        functionFIsFunction = true
                        break
                    }
                    lastElement = element
                }
            }

            // Calculate divided and divider
            var divided = mutableListOf<Any>()
            var divider = mutableListOf<Any>()

            // Extract functions from expressions
            val functionsDivided = mutableListOf<Any>()
            val functionsDivider = mutableListOf<Any>()

            // Make calculations an extract functions
            if (expressions.isNotEmpty()) {
                for (expression in expressions) {
                    // Recognize function
                    var function = false
                    lastElement = 0
                    for (element in expression.first.original!!) {
                        if (element is Char) {
                            if (element.isLetter() || element == '√') {
                                if (element != 'x' && element != 'y' && element != 'z') {
                                    function = true
                                    break
                                }
                            }
                            else  if (lastElement == '^' && element == '(') {
                                functionFIsFunction = true
                                break
                            }
                            lastElement = element
                        }
                    }

                    if (function) {
                        if (!expression.second) {
                            functionsDivider.addAll(expression.first.original!!)
                            functionsDivider.add('×')
                        }
                        else {
                            functionsDivided.addAll(expression.first.original!!)
                            functionsDivided.add('×')
                        }
                    }
                    else {
                        if (!expression.second) {
                            if (divider.isEmpty()) {
                                divider.addAll(expression.first.original!!)
                            }
                            else {
                                divider = multiplyTwoEquations(divider, expression.first.original!!)
                            }
                        }
                        else {
                            if (divided.isEmpty()) {
                                divided.addAll(expression.first.original!!)
                            }
                            else {
                                divided = multiplyTwoEquations(divided, expression.first.original!!)
                            }
                        }
                    }
                }

                // Function F
                if (divided.isNotEmpty()) {
                    functionF.original = multiplyTwoEquations(divided, functionF.original!!)

                    if (functionsDivided.isNotEmpty()) {
                        functionF.original!!.addAll(0, functionsDivided)
                    }

                    functionF.derivative = findDerivative(functionF.original!!).second
                }
                else if (functionsDivided.isNotEmpty()) {
                    functionF.original!!.addAll(0, functionsDivided)
                    functionF.derivative = findDerivative(functionF.original!!).second
                }

                // Function G
                functionG.original = divider
                if (functionsDivider.isNotEmpty()) {
                    functionG.original!!.addAll(0, functionsDivider)
                }

                functionG.derivative = findDerivative(functionG.original!!).second

                // Remove redundant multiplication
                if (functionF.original!!.last() == '×') {
                    functionF.original!!.removeLast()
                }
                if (functionG.original!!.last() == '×') {
                    functionG.original!!.removeLast()
                }
            }

            // If divided is empty return nothing
            if (divided.isEmpty() && functionF.original!!.isEmpty()) {
                return equations
            }

            // If derivatives equations are empty then it add an additional zeros for proper calculations
            if (functionF.derivative!!.isEmpty()) {
                functionF.derivative!!.add(0.0)
            }

            if (functionG.derivative!!.isEmpty()) {
                functionG.derivative!!.add(0.0)
            }

            // Get original an expression
            val originalResult = mutableListOf<Any>()
            originalResult.add('(')
            originalResult.addAll(divided)
            originalResult.add(')')
            originalResult.add('/')
            originalResult.add('(')
            originalResult.addAll(divider)
            originalResult.add(')')

            equations.original = originalResult

            // Calculate derivative
            val dFxG: MutableList<Any>
            val dGxF: MutableList<Any>
            val gx2: MutableList<Any>

            if (functionsDivided.isEmpty() && functionsDivider.isEmpty() && !functionFIsFunction) {
                dFxG = groupUnknowns(multiplyTwoEquations(functionF.derivative!!, functionG.original!!))
                dGxF = groupUnknowns(multiplyTwoEquations(functionG.derivative!!, functionF.original!!), negative = true)
                gx2 = groupUnknowns(multiplyTwoEquations(functionG.original!!, functionG.original!!))
            }
            else {
                dFxG = mutableListOf()
                dFxG.addAll(listOf('(', '('))
                dFxG.addAll(functionF.derivative!!)
                dFxG.add(')')
                dFxG.add('×')
                dFxG.add('(')
                dFxG.addAll(functionG.original!!)
                dFxG.addAll(listOf(')', ')'))

                dGxF = mutableListOf()
                dGxF.addAll(listOf('(', '('))
                dGxF.addAll(functionG.derivative!!)
                dGxF.add(')')
                dGxF.add('×')
                dGxF.add('(')
                dGxF.addAll(functionF.original!!)
                dGxF.addAll(listOf(')', ')'))

                gx2 = mutableListOf()
                gx2.addAll(listOf('(', '('))
                gx2.addAll(functionG.original!!)
                gx2.add(')')
                gx2.add('×')
                gx2.add('(')
                gx2.addAll(functionG.original!!)
                gx2.addAll(listOf(')', ')'))
            }

            var dividedDerivative = mutableListOf<Any>()

            dividedDerivative.addAll(listOf('(', '('))
            dividedDerivative.addAll(dFxG)
            dividedDerivative.add(')')
            dividedDerivative.add('-')
            dividedDerivative.add('(')
            dividedDerivative.addAll(dGxF)
            dividedDerivative.addAll(listOf(')', ')'))

            if (functionsDivided.isEmpty() && functionsDivider.isEmpty() && !functionFIsFunction) {
                dividedDerivative = groupUnknowns(dividedDerivative)
            }

            val derivativeResult = mutableListOf<Any>()

            derivativeResult.add('(')
            derivativeResult.addAll(dividedDerivative)
            derivativeResult.add(')')
            derivativeResult.add('/')
            derivativeResult.add('(')
            derivativeResult.addAll(gx2)
            derivativeResult.add(')')

            equations.derivative = derivativeResult
        }
        else {
            val resultOriginal = mutableListOf<Any>()
            val resultDerivative = mutableListOf<Any>()

            // Build original and derivative expressions
            for (index in expressions.indices) {
                if (expressions[index].first.original!!.isNotEmpty()) {
                    resultOriginal.addAll(expressions[index].first.original!!)
                    resultOriginal.add(0, '(')
                    resultOriginal.add(')')
                    resultOriginal.add('×')
                }
                else {
                    continue
                }

                val derivativeExpression = mutableListOf<Any>()

                // Get derivative of proper element of equation
                var multiplicationByZero = false
                for ((getDerivative, equation) in expressions.withIndex()) {
                    if (index == getDerivative) {
                        val derivative = equation.first.derivative!!

                        if (derivative.size == 1 && derivative.last() == 1.0) {
                            continue
                        }
                        else if (derivative.isNotEmpty()) {
                            derivative.add(0, '(')
                            derivative.add(')')
                            derivativeExpression.addAll(derivative)
                        }
                        else {
                            multiplicationByZero = true
                            derivativeExpression.clear()
                            break
                        }
                    }
                    else {
                        val original = equation.first.original!!

                        if (original.size == 1 && original.last() == 1.0) {
                            continue
                        }
                        else if (original.isNotEmpty()) {
                            original.add(0, '(')
                            original.add(')')
                            derivativeExpression.addAll(original)
                        }
                    }
                    if (derivativeExpression.isNotEmpty() && derivativeExpression.last() != '×') {
                        derivativeExpression.add('×')
                    }
                }
                if (multiplicationByZero) {
                    continue
                }

                // Remove redundant operator
                if (derivativeExpression.isNotEmpty() && derivativeExpression.last() == '×') {
                    derivativeExpression.removeLast()
                }

                // Append addition and expressions
                derivativeExpression.add(0, '(')
                derivativeExpression.add(')')
                resultDerivative.addAll(derivativeExpression)
                resultDerivative.add('+')
            }
            // Remove redundant operator
            if (resultOriginal.isNotEmpty()) {
                resultOriginal.removeLast()
            }
            if (resultDerivative.isNotEmpty()) {
                resultDerivative.removeLast()
            }

            equations.derivative = resultDerivative
            equations.original = resultOriginal
        }

        return equations
    }

    private fun calculateFunctionDerivative(expressions: MutableList<Pair<Equations, Boolean>>, equation: MutableList<Any>, function: Char): Equations {
        val result = Equations(mutableListOf(), mutableListOf())

        // Original equation
        if (function != '^') {
            result.original!!.add(function)
            result.original!!.add('(')
            result.original!!.addAll(equation)
            result.original!!.add(')')
        }

        // Derivative of nested function
        val nestedDerivative = findDerivative(equation).second
        var getNested = true

        if (nestedDerivative.isEmpty()) {
            result.original = null
            result.derivative = null
            return result
        }

        // Recognize function and apply proper derivative transformation
        when (function) {
            '^' -> {
                val powerBase = expressions.removeLast().first.original

                // Find multiplier
                var multiplier = 1.0
                for (expression in expressions) {
                    if (expression.first.original!!.size <= 4 && expression.first.original!!.first() is Double) {
                        multiplier *= expression.first.original!!.removeFirst() as Double
                        expression.first.original!!.add(0, 1.0)
                    }
                }

                // Build original equation
                result.original!!.addAll(listOf('(', '('))
                result.original!!.addAll(powerBase!!)
                result.original!!.add(')')
                result.original!!.add(function)
                result.original!!.add('(')
                result.original!!.addAll(equation)
                result.original!!.addAll(listOf(')', ')'))

                // Build derivative
                var derivative: MutableList<Any>
                if (powerBase.size == 1 && powerBase.last() is Double) {
                    derivative = mutableListOf('n', '(')
                    derivative.addAll(powerBase)
                    derivative.addAll(listOf(')', '×', '(', '('))
                    derivative.addAll(powerBase)
                    derivative.addAll(listOf(')', '^', '('))
                    derivative.addAll(equation)
                    derivative.addAll(listOf(')', ')'))

                }
                else {
                    derivative = mutableListOf('n', '(')
                    derivative.addAll(powerBase)
                    derivative.addAll(listOf(')', '×', '('))
                    derivative.addAll(equation)
                    derivative.add(')')

                    derivative = findDerivative(derivative).second

                    val buffer = mutableListOf<Any>()

                    buffer.addAll(listOf('(', '('))
                    buffer.addAll(powerBase)
                    buffer.add(')')
                    buffer.add('^')
                    buffer.add('(')
                    buffer.addAll(equation)
                    buffer.addAll(listOf(')', ')'))
                    buffer.add('×')
                    buffer.add('(')
                    derivative.addAll(0, buffer)
                    derivative.add(')')

                    if (multiplier != 1.0) {
                        derivative.add(0, '×')
                        derivative.add(0, multiplier)
                    }

                    getNested = false
                }

                result.derivative = derivative
            }
            's' -> {
                val buffer = mutableListOf<Any>('c', '(')
                buffer.addAll(equation)
                buffer.add(')')
                result.derivative = buffer
            }
            'c' -> {
                val buffer = mutableListOf<Any>('(', '-', 's', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')'))
                result.derivative = buffer
            }
            't' -> {
                val buffer = mutableListOf<Any>('(', '(', 's', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')', '/', '(', 'c', '('))
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')'))

                getNested = false
                result.derivative = findDerivative(buffer).second
            }
            'i' -> {
                val buffer = mutableListOf<Any>('(', 1.0, '/', '(', '√', '(', 1.0, '-', '(', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', '^', 2.0, ')', ')', ')', ')'))
                result.derivative = buffer
            }
            'o' -> {
                val buffer = mutableListOf<Any>('(', -1.0, '/', '(', '√', '(', 1.0, '-', '(', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', '^', 2.0, ')', ')', ')', ')'))
                result.derivative = buffer
            }
            'a' -> {
                val buffer = mutableListOf<Any>('(', 1.0, '/', '(',  1.0, '+','(', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', '^', 2.0, ')', ')', ')'))
                result.derivative = buffer
            }
            'g' -> {
                val buffer = mutableListOf<Any>('(', 'g', '(', 'e', ')', '/', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')'))
                result.derivative = buffer
            }
            'n' -> {
                val buffer = mutableListOf<Any>('(', 1.0, '/', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')'))
                result.derivative = buffer
            }
            '√' -> {
                val buffer = mutableListOf<Any>('(', 1.0, '/', '(', 2.0, '×', '√', '(')
                buffer.addAll(equation)
                buffer.addAll(listOf(')', ')', ')'))
                result.derivative = buffer
            }
        }

        // Remove redundant operator
        if (result.derivative!!.isNotEmpty()) {
            if (result.derivative!!.last() == '×') {
                result.derivative!!.removeLast()
            }
        }

        // Append derivative of nested function
        if (getNested) {
            if (nestedDerivative.size == 1 && nestedDerivative.last() == 1.0) {
                return result
            }
            else {
                if (result.derivative!!.isNotEmpty()) {
                    if (result.derivative!!.last() is Double || result.derivative!!.last() == ')') {
                        result.derivative!!.add('×')
                    }
                }
                result.derivative!!.add('(')
                result.derivative!!.addAll(nestedDerivative)
                result.derivative!!.add(')')
            }
        }

        return result
    }

    private fun findDerivative(equation: MutableList<Any>, iterator: Int = 0, expressions: MutableList<Pair<Equations, Boolean>> = mutableListOf()): Triple<MutableList<Any>, MutableList<Any>, Int> {
        val derivative = mutableListOf<Any>()
        val original = mutableListOf<Any>()
        var power = false

        val entity = UnknownEntity(null, null, null)
        var specialOperator: Char? = null
        var function: Char? = null

        var i = iterator
        while (i < equation.size) {
            when(equation[i]){
                '=' -> break
                '+', '-' -> {
                    if (expressions.isNotEmpty()) {
                        if (entity.isNotEmpty()) {
                            if (specialOperator == '/')  {
                                expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), false))
                            }
                            else {
                                expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), true))
                            }
                        }

                        val buffer = calculateSpecialDerivative(expressions)
                        if (buffer.original != null && buffer.derivative != null) {
                            derivative.addAll(buffer.derivative!!)
                            original.addAll(buffer.original!!)
                        }
                    }
                    else {
                        derivative.addAll(entity.getDerivative())
                        original.addAll(entity.getOriginal())
                    }

                    if (original.isNotEmpty()) {
                        if (original.last() == '+' || original.last() == '-' ) original.removeLast()
                    }
                    if (derivative.isNotEmpty()) {
                        if (derivative.last() == '+' || derivative.last() == '-' ) derivative.removeLast()
                    }

                    power = false

                    entity.clear()
                    expressions.clear()

                    function = null
                    specialOperator = null

                    derivative.add(equation[i] as Char)
                    original.add(equation[i] as Char)
                }
                '^' ->  {
                    function = equation[i] as Char
                    power = true
                }
                '(' -> {
                    val subEquation = findDerivative(equation, iterator = i+1, mutableListOf())
                    i = subEquation.third

                    val buffer: Equations = if (function == null) {
                        Equations(subEquation.first, subEquation.second)
                    }
                    else {
                        calculateFunctionDerivative(expressions, subEquation.first, function)
                    }

                    if (buffer.original != null && buffer.derivative != null) {
                        if (specialOperator == '/') {
                            expressions.add(Pair(buffer, false))
                        } else {
                            expressions.add(Pair(buffer, true))
                        }
                    }

                    function = null
                    continue
                }
                ')' -> {
                    if (expressions.isNotEmpty()) {
                        if (entity.isNotEmpty()) {
                            if (specialOperator == '/')  {
                                expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), false))
                            }
                            else {
                                expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), true))
                            }
                        }

                        val buffer = calculateSpecialDerivative(expressions)
                        if (buffer.original != null && buffer.derivative != null) {
                            derivative.addAll(buffer.derivative!!)
                            original.addAll(buffer.original!!)
                        }
                    }
                    else {
                        derivative.addAll(entity.getDerivative())
                        original.addAll(entity.getOriginal())
                    }

                    if (original.isNotEmpty()) {
                        if (original.last() == '+' || original.last() == '-' ) original.removeLast()
                    }
                    if (derivative.isNotEmpty()) {
                        if (derivative.last() == '+' || derivative.last() == '-' ) derivative.removeLast()
                    }

                    return Triple(original, derivative, i+1)
                }
                '×', '/' -> {
                    specialOperator = equation[i] as Char
                    function = null
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
                    if (equation[i] == 'x' || equation[i] == 'y' || equation[i] == 'z') {
                        entity.variable = equation[i] as Char
                    }
                    else {
                        function = equation[i] as Char
                    }
                }
            }
            i++
        }
        if (expressions.isNotEmpty()) {
            if (entity.isNotEmpty()) {
                if (specialOperator == '/')  {
                    expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), false))
                }
                else {
                    expressions.add(Pair(Equations(entity.getOriginal(), entity.getDerivative()), true))
                }
            }

            val buffer = calculateSpecialDerivative(expressions)
            if (buffer.original != null && buffer.derivative != null) {
                derivative.addAll(buffer.derivative!!)
                original.addAll(buffer.original!!)
            }
        }
        else {
            derivative.addAll(entity.getDerivative())
            original.addAll(entity.getOriginal())
        }

        if (original.isNotEmpty()) {
            if (original.last() == '+' || original.last() == '-' ) original.removeLast()
        }
        if (derivative.isNotEmpty()) {
            if (derivative.last() == '+' || derivative.last() == '-' ) derivative.removeLast()
        }

        return Triple(original, derivative, i+1)
    }

    private fun connectEquation(entities: MutableList<Any>, clear: Boolean = true): MutableList<Any> {
        val connectedEquation = mutableListOf<Any>()

        for (entity in entities) {
            if (entity is UnknownEntity) {
                connectedEquation.addAll(entity.getOriginal())
            }
            else {
                connectedEquation.add(entity)
            }
        }

        if (clear) {
            entities.clear()
        }
        return connectedEquation
    }

    private fun equationNotInBrackets(equation: MutableList<Any>): Boolean {
        var brackets = 0
        for (element in equation) {
            when(element) {
                '(' -> brackets++
                ')' -> brackets--
                else -> {
                    if (brackets == 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun equationHasOperatorsBrackets(equation: MutableList<Any>): Boolean {
        val operators = listOf('+', '-', '×', '/')

        for (element in equation) {
            if (element is Char) {
                for (operator in operators)  {
                    if (element == operator) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun groupEquation(equation: MutableList<Any>, iterator: Int = 0, eqSign: Boolean = false): Pair<MutableList<Any>, Int> {
        val resultEquation = mutableListOf<Any>()
        val stackForEquation = mutableListOf<Any>()

        val entity = UnknownEntity(null, null, null)

        var power = false
        var equalSign = false
        var addBrackets = false
        var multiplyDivide = false

        var i = iterator
        while (i < equation.size) {
            when(equation[i]) {
                '^' ->  {
                    power = true
                    if (resultEquation.isNotEmpty() && resultEquation.last() == ')') {
                        if (entity.isEmpty()) {
                            resultEquation.add(equation[i])
                        }
                    }
                }
                '=' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    stackForEquation.add('-')
                    equalSign = true
                }
                '(' -> {
                    if (stackForEquation.isNotEmpty() && stackForEquation.last() is Char) {
                        resultEquation.add(stackForEquation.removeLast())
                        addBrackets = true
                    }

                    val subEquation = groupEquation(equation, i+1)
                    i = subEquation.second

                    // Check does function already has brackets and if not append them
                    if (!addBrackets && resultEquation.isNotEmpty() && resultEquation.last() is Char) {
                        if ((resultEquation.last() as Char).isLetter() || resultEquation.last() == '√') {
                            if (equationNotInBrackets(subEquation.first)) {
                                subEquation.first.add(0, '(')
                                subEquation.first.add(')')
                            }
                        }
                    }

                    resultEquation.addAll(subEquation.first)
                    continue
                }
                ')' ->  {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    var operatorFirst = false
                    if (resultEquation.isNotEmpty() && resultEquation.last() == ')') {
                        operatorFirst = true
                    }
                    val buffer = groupUnknowns(connectEquation(stackForEquation), firstOperator = operatorFirst)

                    // Append brackets for equations
                    if (buffer.isNotEmpty() && equationHasOperatorsBrackets(buffer)) {
                        buffer.add(0, '(')
                        buffer.add(')')
                        addBrackets = false
                    }

                    resultEquation.addAll(buffer)

                    if (addBrackets) {
                        resultEquation.add(0, '(')
                        resultEquation.add(')')
                    }

                    return Pair(resultEquation, i+1)
                }
                '×' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    stackForEquation.add(equation[i])
                    power = false
                    multiplyDivide = true
                }
                '/' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    var operatorFirst = false
                    if (resultEquation.isNotEmpty() && resultEquation.last() == ')') {
                        operatorFirst = true
                    }
                    val buffer = groupUnknowns(connectEquation(stackForEquation), firstOperator = operatorFirst)

                    resultEquation.addAll(buffer)

                    resultEquation.add(equation[i])
                    power = false
                    multiplyDivide = true
                }
                '+' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    if (equalSign) {
                        stackForEquation.add('-')
                    }
                    else {
                        stackForEquation.add(equation[i])
                    }
                    power = false
                    multiplyDivide = false
                }
                '-' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    if (equalSign) {
                        stackForEquation.add('+')
                    }
                    else {
                        stackForEquation.add(equation[i])
                    }
                    power = false
                }
                is Char -> {
                    if ((equation[i] as Char).isLetter() || equation[i] == '√') {
                        if (equation[i] != 'x' && equation[i] != 'y' && equation[i] != 'z') {
                            if (!entity.isEmpty()) {
                                stackForEquation.add(entity.copy())
                                entity.clear()
                            }

                            if (stackForEquation.isNotEmpty() && stackForEquation.last() is Char) {
                                resultEquation.add(stackForEquation.removeLast())
                            }

                            resultEquation.add(equation[i])
                            i++
                            continue
                        }
                        else {
                            entity.variable = equation[i] as Char
                        }
                    }
                }
                is Double -> {
                    if (power) {
                        entity.powerTo = equation[i] as Double
                    }
                    else {
                        entity.multiplier = equation[i] as Double
                    }
                }
            }
            i++
        }
        if (!entity.isEmpty()) {
            stackForEquation.add(entity.copy())
            entity.clear()
        }

        var operatorFirst = false
        if (resultEquation.isNotEmpty() && resultEquation.last() == ')') {
            operatorFirst = true
        }
        val buffer = groupUnknowns(connectEquation(stackForEquation), eqSign = eqSign, firstOperator = operatorFirst)
        resultEquation.addAll(buffer)

        return Pair(resultEquation, 0)
    }

    private fun getElementsOfEquation(stackForEquation: MutableList<Any>): MutableList<MutableList<Any>> {
        val elements = mutableListOf<MutableList<Any>>()
        var element = mutableListOf<Any>()

        for (entity in stackForEquation) {
            when(entity) {
                '+', '-'-> {
                    if (element.isNotEmpty()) {
                        elements.add(element)
                        element = mutableListOf()
                    }

                    elements.add(mutableListOf(entity))
                }
                is UnknownEntity -> {
                    element.add(entity)
                }
            }
        }
        if (element.isNotEmpty()) {
            elements.add(element)
        }

        return elements
    }

    private fun getBiggestMultiplicationAndConvertConstantsIntoFractions(stackForEquation: MutableList<MutableList<Any>>): Pair<MutableList<Any>, MutableList<UnknownEntity>> {
        val allEntities = mutableListOf<UnknownEntity>()
        val allFunctions = mutableListOf<MutableList<UnknownEntity>>()

        // Get all constant multipliers in stack
        for (element in stackForEquation) {
            // If it is operator
            if (element.size == 1 && element.last() is Char) {
                continue
            }
            // If it is element of equation
            else {
                var unknownEntity = UnknownEntity(1.0,null,null)
                val functions = mutableListOf<UnknownEntity>()

                for (entity in element) {
                    if (entity is UnknownEntity) {
                        if (entity.multiplier != null) {
                            unknownEntity *= entity
                        }
                        else if (entity.isFunction()) {
                            functions.add(entity)
                        }
                    }
                }

                allEntities.add(unknownEntity)
                allFunctions.add(functions)
            }
        }

        // Check does every entity contains unknown
        var unknownInEveryEntity = false
        for (entity in allEntities) {
            if (entity.variable == null && !entity.isFunction()) {
                unknownInEveryEntity = false
                break
            }
            else if (entity.variable != null) {
                unknownInEveryEntity = true
            }
        }

        // Find the lowest degree unknown
        var lowestMultiplier: Double? = null
        var lowestUnknownPower: Double? = null
        for (entity in allEntities) {
            if (entity.multiplier != null) {
                if (lowestMultiplier == null) {
                    lowestMultiplier = entity.multiplier!!
                }
                else if (entity.multiplier!! < lowestMultiplier) {
                    lowestMultiplier = entity.multiplier!!
                }
            }
            if (unknownInEveryEntity) {
                if (entity.variable != null && entity.powerTo != null) {
                    if (lowestUnknownPower == null) {
                        lowestUnknownPower = entity.powerTo!!
                    }
                    else if (entity.powerTo!! < lowestUnknownPower) {
                        lowestUnknownPower = entity.powerTo!!
                    }
                }
            }
        }

        // Convert constants into fractions
        val fractionsList = fractions.convertListIntoFractionsWithSameBase(allEntities)
        val allConstants = fractions.getAllNumeratorsOfList(fractionsList)
        var multiplier: Fraction

        // If there is lowest possible multiplier find all divisors of it and find common multiplier for list
        if (lowestMultiplier != null) {
            multiplier = fractions.convertIntoFraction(lowestMultiplier, fractionsList.last().denominator)
            val multiplicative = mutableListOf(1)

            var multiplicator = 1
            while (multiplicator <= multiplier.numerator/2) {
                if (multiplier.numerator % multiplicator == 0) {
                    multiplicative.add(multiplier.numerator/multiplicator)
                }
                multiplicator++
            }

            val commonDivisors = mutableListOf<Int>()
            for (divisor in multiplicative) {
                var isDivisor = true
                for (constant in allConstants) {
                    if (constant % divisor != 0) {
                        isDivisor = false
                        break
                    }
                }

                if (isDivisor) {
                    commonDivisors.add(divisor)
                }
            }

            var biggestDivisor = 1
            for (divisor in commonDivisors) {
                if (divisor > biggestDivisor) {
                    biggestDivisor = divisor
                }
            }

            multiplier.numerator = biggestDivisor
        }
        else {
            multiplier = Fraction(1, 1)
        }

        // Divide every fraction by found multiplier
        for (index in fractionsList.indices) {
            fractionsList[index] = fractions.shortenFraction(fractionsList[index]/multiplier)
        }

        // Shorten fractions and multiplier
        for (i in fractionsList.indices) {
            fractionsList[i] = fractions.shortenFraction(fractionsList[i])
        }
        multiplier = fractions.shortenFraction(multiplier)

        // Check does every entity contains at least one function
        val multiplierFunctions = mutableListOf<Pair<UnknownEntity, Int>>()
        var functionInEveryEntity = true
        for (list in allFunctions) {
            if (list.isEmpty()) {
                functionInEveryEntity = false
                break
            }
        }

        // Check does every entity contains same functions, if they are add them to multiplier
        if (functionInEveryEntity && allFunctions.size > 1) {
            val commonFunctionResult = mutableMapOf<UnknownEntity, Pair<Int, Int>>()
            val commonFunctions = mutableMapOf<UnknownEntity, Int>()

            val firstList = allFunctions.first()
            for (function in firstList) {
                commonFunctions[function] = commonFunctions.getOrDefault(function, 0) + 1
            }
            println("allFunctions")
            println(allFunctions)

            var ommit = true
            for (list in allFunctions) {
                if (ommit) {
                    ommit = false
                    continue
                }

                val common = mutableMapOf<UnknownEntity, Int>()

                for (functionA in list) {
                    for ((functionB, v) in commonFunctions) {
                        if (functionA == functionB) {
                            common[functionA] = commonFunctions.getOrDefault(functionA, 0) + 1
                        }
                    }
                }

                if (common.isNotEmpty()) {
                    for ((function, v) in commonFunctions) {
                        if (common[function] != null && commonFunctions[function] != null) {
                            if (common[function]!! <= commonFunctions[function]!!) {
                                if (common[function]!! < commonFunctions[function]!!) {
                                    val value = common[function]!!

                                    commonFunctions[function] = value

                                    if (commonFunctionResult[function] == null) {
                                        commonFunctionResult[function] = Pair(value, 1)
                                    }
                                    else {
                                        commonFunctionResult[function] = Pair(value, commonFunctionResult[function]!!.second + 1)
                                    }
                                }
                                else {
                                    if (commonFunctionResult[function] == null) {
                                        commonFunctionResult[function] = Pair(v, 1)
                                    }
                                    else {
                                        commonFunctionResult[function] = Pair(v, commonFunctionResult[function]!!.second + 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add functions that are common to multiplier
            for ((function, v) in commonFunctionResult) {
                if (v.second == allFunctions.size) {
                    multiplierFunctions.add(Pair(function, v.first))
                }
            }

            // Remove multiplier functions from functions
            for (list in allFunctions) {
                var i = 0
                for (function in multiplierFunctions) {
                    var counter = function.second

                    while (i < list.size) {
                        if (list[i] == function.first) {
                            if (counter > 0) {
                                list.removeAt(i)
                                counter--
                                i--
                            }
                        }
                        i++
                    }
                }
            }
        }
        else if (functionInEveryEntity && allFunctions.size == 1) {
            val commonFunctionResult = mutableMapOf<MutableList<Any>, Pair<UnknownEntity, Int>>()

            for (function in allFunctions[0]) {
                commonFunctionResult[function.getFunctionContent()!!] = Pair(function, commonFunctionResult.getOrDefault(function.getFunctionContent(), Pair(function, 0)).second + 1)
            }

            // Add functions that are common to multiplier
            for ((key, v) in commonFunctionResult) {
                multiplierFunctions.add(Pair(v.first, v.second))
            }

            // Remove multiplier functions from functions
            for (list in allFunctions) {
                var i = 0
                for (function in multiplierFunctions) {
                    var counter = function.second

                    while (i < list.size) {
                        if (list[i] == function.first) {
                            if (counter > 0) {
                                list.removeAt(i)
                                counter--
                                i--
                            }
                        }
                        i++
                    }
                }
            }
        }

        // Replace multipliers and get out of equation the lowest possible multiplication of unknown
        val resultEquation = mutableListOf<Any>()
        var entityIndex = 0
        var functionIndex = 0
        for (element in stackForEquation) {
            // If it is operator
            if (element.size == 1 && element.last() is Char) {
                resultEquation.add(element.last())
            }
            // If it is element of equation
            else {
                for (entity in element) {
                    if (entity is UnknownEntity) {
                        // Append function
                        if (entity.isFunction()) {
                            if (functionIndex < allFunctions.size) {
                                if (allFunctions[functionIndex].isNotEmpty()) {
                                    resultEquation.addAll(entity.getFunctionContent()!!)
                                }
                                functionIndex++
                            }
                        }
                        // Append entity
                        else {
                            entity.multiplier = fractionsList[entityIndex].numerator.toDouble()/fractionsList[entityIndex].denominator.toDouble()
                            if (entity.variable != null && entity.powerTo != null && lowestUnknownPower != null) {
                                entity.powerTo = entity.powerTo!! - lowestUnknownPower
                            }
                            resultEquation.addAll(entity.getOriginal())
                            entityIndex++
                        }
                    }
                }
            }
        }

        // Get final multiplier
        val resultMultiplier = mutableListOf<UnknownEntity>()

        if (lowestUnknownPower != null) {
            resultMultiplier.add(UnknownEntity(multiplier.numerator.toDouble()/multiplier.denominator.toDouble(), 'x', lowestUnknownPower))
        }
        else {
            resultMultiplier.add(UnknownEntity(multiplier.numerator.toDouble()/multiplier.denominator.toDouble(), null, null))
        }

        for (function in multiplierFunctions) {
            resultMultiplier.add(function.first)
            resultMultiplier.last().powerTo = function.second.toDouble()
        }

        return Pair(resultEquation, resultMultiplier)
    }

    private fun getNestedMultiplication(equation: MutableList<Any>, iterator: Int = 0): Pair<MutableList<Any>, Int> {
        val resultWithMultiplication = mutableListOf<Any>()
        val stackForEquation = mutableListOf<Any>()
        val entity = UnknownEntity()

        var power = false

        var i = iterator
        while (i < equation.size) {
            when(equation[i]) {
                '^' ->  {
                    power = true
                }
                '=' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    stackForEquation.add('-')
                }
                '(' -> {
                    if (stackForEquation.isNotEmpty() && stackForEquation.last() is Char) {
                        resultWithMultiplication.add(stackForEquation.removeLast())
                    }

                    val subEquation = getNestedMultiplication(equation, i+1)
                    i = subEquation.second

                    resultWithMultiplication.addAll(subEquation.first)
                    continue
                }
                ')' ->  {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }

                    val result = getBiggestMultiplicationAndConvertConstantsIntoFractions(getElementsOfEquation(stackForEquation))
                    resultWithMultiplication.addAll(result.first)

                    if (result.second.isNotEmpty()) {
                        val multiplication = mutableListOf<Any>()
                        for (element in result.second) {
                            if (element.isFunction()) {
                                val function = element.getFunctionContent()
                                if (element.powerTo != null) {
                                    if (element.powerTo != 1.0) {
                                        function!!.add('^')
                                        function.add(element.powerTo!!)
                                    }
                                }
                                multiplication.addAll(function!!)
                            }
                            else if (element.onlyNumber() && element.multiplier == 1.0){
                               continue
                            }
                            else {
                                multiplication.addAll(element.getOriginal())
                            }
                        }
                        if (multiplication.isNotEmpty()) {
                            if (resultWithMultiplication.isEmpty()) {
                                resultWithMultiplication.addAll(multiplication)
                            }
                            else {
                                if (resultWithMultiplication.size == 1 && resultWithMultiplication.last() == 1.0) {
                                    resultWithMultiplication.clear()
                                    resultWithMultiplication.addAll(multiplication)
                                }
                                else {
                                    resultWithMultiplication.add(0, '(')
                                    resultWithMultiplication.addAll(0, multiplication)
                                    resultWithMultiplication.add(')')
                                }
                            }
                        }
                    }
                    stackForEquation.clear()
                    return Pair(resultWithMultiplication, i+1)
                }
                '×' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }
                    stackForEquation.add(equation[i])
                    power = false
                }
                '/' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }
                    power = false
                }
                '+' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }
                    stackForEquation.add(equation[i])
                    power = false
                }
                '-' -> {
                    if (!entity.isEmpty()) {
                        stackForEquation.add(entity.copy())
                        entity.clear()
                    }
                    stackForEquation.add(equation[i])
                    power = false
                }
                is Char -> {
                    // Check is it function and get it as UnknownEntity
                    if ((equation[i] as Char).isLetter() || equation[i] == '√') {
                        if (equation[i] != 'x' && equation[i] != 'y' && equation[i] != 'z') {
                            if (!entity.isEmpty()) {
                                stackForEquation.add(entity.copy())
                                entity.clear()
                            }

                            if (stackForEquation.isNotEmpty() && stackForEquation.last() is Char) {
                                if (stackForEquation.last() == '×') {
                                    stackForEquation.removeLast()
                                }
                            }

                            val function = getNestedMultiplication(equation, i+2)

                            val functionContent = mutableListOf<Any>()
                            functionContent.add(equation[i])
                            functionContent.add('(')
                            functionContent.addAll(function.first)
                            functionContent.add(')')

                            val functionEntity = UnknownEntity()
                            functionEntity.setFunction(equation[i] as Char, functionContent)
                            stackForEquation.add(functionEntity)

                            i = function.second
                            continue
                        }
                        else {
                            entity.variable = equation[i] as Char
                        }
                    }
                }
                is Double -> {
                    if (power) {
                        entity.powerTo = equation[i] as Double
                    }
                    else {
                        entity.multiplier = equation[i] as Double
                    }
                }
            }
            i++
        }
        if (!entity.isEmpty()) {
            stackForEquation.add(entity.copy())
            entity.clear()
        }

        val result = getBiggestMultiplicationAndConvertConstantsIntoFractions(getElementsOfEquation(stackForEquation))
        resultWithMultiplication.addAll(result.first)

        if (result.second.isNotEmpty()) {
            val multiplication = mutableListOf<Any>()
            for (element in result.second) {
                if (element.isFunction()) {
                    val function = element.getFunctionContent()
                    if (element.powerTo != null) {
                        if (element.powerTo != 1.0) {
                            function!!.add('^')
                            function.add(element.powerTo!!)
                        }
                    }
                    multiplication.addAll(function!!)
                }
                else if (element.onlyNumber() && element.multiplier == 1.0){
                    continue
                }
                else {
                    multiplication.addAll(element.getOriginal())
                }
            }
            if (multiplication.isNotEmpty()) {
                if (resultWithMultiplication.isEmpty()) {
                    resultWithMultiplication.addAll(multiplication)
                }
                else {
                    if (resultWithMultiplication.size == 1 && resultWithMultiplication.last() == 1.0) {
                        resultWithMultiplication.clear()
                        resultWithMultiplication.addAll(multiplication)
                    }
                    else {
                        resultWithMultiplication.add(0, '(')
                        resultWithMultiplication.addAll(0, multiplication)
                        resultWithMultiplication.add(')')
                    }
                }
            }
        }
        stackForEquation.clear()
        println("END")
        return Pair(resultWithMultiplication, i+1)
    }

    private fun convertDerivativeForOutput(equation: MutableList<Any>): String {
        var output = ""

        var passOne = false
        var lastNum = 0.0

        var append: Any = 0
        for (element in equation) {
            when (element) {
                is Double -> {
                    if (passOne && element == 1.0) {
                        output = output.dropLast(1)
                        continue
                    }

                    append = if (hasDecimal(element)) {
                        element
                    } else {
                        element.toInt()
                    }

                    lastNum = element
                    passOne = false
                }
                else -> {
                    passOne = false

                    when(element) {
                        's' -> append = "sin"
                        'c' -> append = "cos"
                        't' -> append = "tan"
                        'a' -> append = "arcsin"
                        'i' -> append = "arccos"
                        'o' -> append = "arctan"
                        'n' -> append = "ln"
                        'g' -> append = "lg"
                    }

                    when (element) {
                        '^' -> passOne = true
                        is Char -> {
                            if (element.isLetter() || element == '×') {
                                if (lastNum == 1.0) {
                                    if (append == 0) {
                                        output = output.dropLast(1)
                                    }
                                    lastNum = 0.0

                                    if (element != '×') {
                                        output += element
                                    }
                                    continue
                                }
                            }
                        }
                    }

                    if (append == 0) {
                        append = element
                    }
                    lastNum = 0.0
                }
            }

            output += append
            append = 0
        }

        return output
    }

    fun solveDerivative(equation: String): String {
        val transformedEquation = transformEquationForSolvingUnknowns(transformEquation(equation)).list

        val derivative = findDerivative(transformedEquation).second
        println("Derivative:")
        println(derivative)

        println("Grouped derivative:")
        println(groupEquation(findDerivative(transformedEquation).second).first)

        println("With gcd out:")
        println(getNestedMultiplication(groupEquation(findDerivative(transformedEquation).second).first).first)

        val substitute = substituteVariableForDerivative(derivative, 1.0)
        val calc = calculateEquation(substitute, baseOfLogarithm = 10.0)
        println("For 1.0: $calc")

        val substitute1 = substituteVariableForDerivative(derivative, 2.0)
        val calc1 = calculateEquation(substitute1, baseOfLogarithm = 10.0)
        println("For 2.0: $calc1")

        val substitute2 = substituteVariableForDerivative(derivative, 3.0)
        val calc2 = calculateEquation(substitute2, baseOfLogarithm = 10.0)
        println("For 3.0: $calc2")

        val substitute3 = substituteVariableForDerivative(derivative, 0.5)
        val calc3 = calculateEquation(substitute3, baseOfLogarithm = 10.0)
        println("For 0.5: $calc3")

        return convertDerivativeForOutput(getNestedMultiplication(groupEquation(findDerivative(transformedEquation).second).first).first)
    }
}