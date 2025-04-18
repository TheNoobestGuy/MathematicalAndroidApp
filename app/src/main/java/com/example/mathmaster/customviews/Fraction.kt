package com.example.mathmaster.customviews

import kotlin.math.min
import kotlin.math.round

data class Fraction(var numerator: MutableList<Any> = mutableListOf(), var denominator: MutableList<Any>? = null, var powerTo: Double = 1.0, var count: Double = 1.0, var multiplicative: Fraction? = null) {
    private var withoutGCD = false

    private val numeratorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val numeratorOperators = mutableListOf<Char>()
    private val numeratorElements = mutableListOf<MutableList<Any>>()
    private val commonForNumerator = hashMapOf<MutableList<Any>, Double>()

    private val denominatorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val denominatorOperators = mutableListOf<Char>()
    private val denominatorElements = mutableListOf<MutableList<Any>>()
    private val commonForDenominator = hashMapOf<MutableList<Any>, Double>()

    fun setWithoutGCDMode() {
        withoutGCD = true
    }

    fun setGCDMode() {
        withoutGCD = false
    }

    private fun getOutFractions() {
        var i = 0
        while (i < numerator.size) {
            var index = i
            if (numerator[i] is Fraction) {
                if ((numerator[i] as Fraction).multiplicative != null) {
                    for (j in (numerator[i] as Fraction).multiplicative!!.numerator) {
                        if (withoutGCD) {
                            if ((numerator[i] as Fraction).powerTo < 0) {
                                numerator.add(++index, '/')
                                (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                            }
                            numerator.add(++index, '(')
                            numerator.add(++index, j)
                            numerator.add(++index, ')')
                        }
                        else {
                            if ((numerator[i] as Fraction).powerTo < 0) {
                                numerator.add(++index, '/')
                                (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                            }
                            numerator.add(++index, j)
                        }
                    }
                    if ((numerator[i] as Fraction).multiplicative!!.denominator != null) {
                        for (j in (numerator[i] as Fraction).multiplicative!!.denominator!!) {
                            if (withoutGCD) {
                                if ((numerator[i] as Fraction).powerTo > 0) {
                                    numerator.add(++index, '/')
                                    (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                                }
                                numerator.add(++index, '(')
                                numerator.add(++index, j)
                                numerator.add(++index, ')')
                            }
                            else {
                                if ((numerator[i] as Fraction).powerTo > 0) {
                                    numerator.add(++index, '/')
                                    (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                                }
                                numerator.add(++index, j)
                            }
                        }
                    }

                    (numerator[i] as Fraction).multiplicative = null
                }

                for (j in (numerator[i] as Fraction).numerator) {
                    if (withoutGCD) {
                        if ((numerator[i] as Fraction).powerTo < 0) {
                            numerator.add(++index, '/')
                            (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                        }
                        numerator.add(++index, '(')
                        numerator.add(++index, j)
                        numerator.add(++index, ')')
                    }
                    else {
                        if ((numerator[i] as Fraction).powerTo < 0) {
                            numerator.add(++index, '/')
                            (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                        }
                        numerator.add(++index, j)
                    }
                }
                (numerator[i] as Fraction).numerator.clear()

                if ((numerator[i] as Fraction).denominator != null) {
                    for (j in (numerator[i] as Fraction).denominator!!) {
                        if (withoutGCD) {
                            if ((numerator[i] as Fraction).powerTo > 0) {
                                numerator.add(++index, '/')
                                (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                            }
                            numerator.add(++index, '(')
                            numerator.add(++index, j)
                            numerator.add(++index, ')')
                        }
                        else {
                            if ((numerator[i] as Fraction).powerTo > 0) {
                                numerator.add(++index, '/')
                                (numerator[i] as Fraction).powerTo = -(numerator[i] as Fraction).powerTo
                            }
                            numerator.add(++index, j)
                        }
                    }
                }
                (numerator[i] as Fraction).denominator = null

                numerator.removeAt(i)
                i = --index
            }
            i++
        }
        if (denominator != null) {
            i = 0
            while (i < denominator!!.size) {
                var index = i
                if (denominator!![i] is Fraction) {
                    if ((denominator!![i] as Fraction).multiplicative != null) {
                        for (j in (denominator!![i] as Fraction).multiplicative!!.numerator) {
                            if (withoutGCD) {
                                if ((denominator!![i] as Fraction).powerTo < 0) {
                                    denominator!!.add(++index, '/')
                                    (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                }
                                denominator!!.add(++index, '(')
                                denominator!!.add(++index, j)
                                denominator!!.add(++index, ')')
                            }
                            else {
                                if ((denominator!![i] as Fraction).powerTo < 0) {
                                    denominator!!.add(++index, '/')
                                    (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                }
                                denominator!!.add(++index, j)
                            }
                        }
                        if ((denominator!![i] as Fraction).multiplicative!!.denominator != null) {
                            for (j in (denominator!![i] as Fraction).multiplicative!!.denominator!!) {
                                if (withoutGCD) {
                                    if ((denominator!![i] as Fraction).powerTo > 0) {
                                        denominator!!.add(++index, '/')
                                        (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                    }
                                    denominator!!.add(++index, '(')
                                    denominator!!.add(++index, j)
                                    denominator!!.add(++index, ')')
                                }
                                else {
                                    if ((denominator!![i] as Fraction).powerTo > 0) {
                                        denominator!!.add(++index, '/')
                                        (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                    }
                                    denominator!!.add(++index, j)
                                }
                            }
                        }
                        (denominator!![i] as Fraction).multiplicative = null
                    }

                    for (j in (denominator!![i] as Fraction).numerator) {
                        if (withoutGCD) {
                            if ((denominator!![i] as Fraction).powerTo < 0) {
                                denominator!!.add(++index, '/')
                                (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                            }
                            denominator!!.add(++index, '(')
                            denominator!!.add(++index, j)
                            denominator!!.add(++index, ')')
                        }
                        else {
                            if ((denominator!![i] as Fraction).powerTo < 0) {
                                denominator!!.add(++index, '/')
                                (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                            }
                            denominator!!.add(++index, j)
                        }
                    }
                    (denominator!![i] as Fraction).numerator.clear()

                    if ((denominator!![i] as Fraction).denominator != null) {
                        for (j in (denominator!![i] as Fraction).denominator!!) {
                            if (withoutGCD) {
                                if ((denominator!![i] as Fraction).powerTo > 0) {
                                    denominator!!.add(++index, '/')
                                    (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                }
                                denominator!!.add(++index, '(')
                                denominator!!.add(++index, j)
                                denominator!!.add(++index, ')')
                            }
                            else {
                                if ((denominator!![i] as Fraction).powerTo > 0) {
                                    denominator!!.add(++index, '/')
                                    (denominator!![i] as Fraction).powerTo = -(denominator!![i] as Fraction).powerTo
                                }
                                denominator!!.add(++index, j)
                            }
                        }
                    }
                    (denominator!![i] as Fraction).denominator = null

                    denominator!!.removeAt(i)
                    i = --index
                }
                i++
            }
        }
    }

    private fun isNotEmpty(): Boolean {
        return numerator.isNotEmpty()
    }

    private fun clear() {
        numerator.clear()
    }

    init {
        getOutFractions()
        cleanFraction()
    }

    fun isCalculable(): MutableList<Any> {
        val list = mutableListOf<Any>(1.0)
        var i = 0
        while (i < numerator.size) {
            if (numerator[i] is UnknownEntity && (numerator[i] as UnknownEntity).onlyNumber()) {
                list.add('×')
                list.addAll((numerator[i] as UnknownEntity).getOriginal())
            }
            else if (numerator[i] !is Char){
                return mutableListOf()
            }
            i++
        }

        if (multiplicative != null) {
            i = 0
            while (i < multiplicative!!.numerator.size) {
                if (multiplicative!!.numerator[i] is UnknownEntity && (multiplicative!!.numerator[i] as UnknownEntity).onlyNumber()) {
                    list.add('×')
                    list.addAll((multiplicative!!.numerator[i] as UnknownEntity).getOriginal())
                }
                else if (multiplicative!!.numerator[i] !is Char){
                    return mutableListOf()
                }
                i++
            }

            if (multiplicative!!.denominator != null) {
                i = 0
                while (i < multiplicative!!.denominator!!.size) {
                    if (multiplicative!!.denominator!![i] is UnknownEntity && (multiplicative!!.denominator!![i] as UnknownEntity).onlyNumber()) {
                        list.add('/')
                        list.addAll((multiplicative!!.denominator!![i] as UnknownEntity).getOriginal())
                    }
                    else if (multiplicative!!.denominator!![i] !is Char){
                        return mutableListOf()
                    }
                    i++
                }
            }
        }

        if (list.size == 1) {
            return mutableListOf()
        }
        return list
    }

    private fun isEmpty(): Boolean {
        if (numerator.isEmpty()) {
            if (multiplicative == null) {
                return true
            }
            else {
                if (multiplicative!!.numerator.isEmpty()) {
                    return true
                }
                else {
                    for (element in multiplicative!!.numerator) {
                        when (element) {
                            is Fraction -> if (!element.isEmpty()) return false
                            is UnknownEntity -> if (!element.isEmpty()) return false
                            is Function -> if (element.isNotEmpty()) return false
                        }
                    }
                }
            }
        }
        return false
    }

    fun setFraction() {
        getOutFractions()

        updateFractionNumerator()
        if (denominator != null) updateFractionDenominator()

        if (!withoutGCD) {
            setMultiplicative(getMultiplicativeNumerator(),  getMultiplicativeDenominator())
        }

        shortenEveryFractionNumerator()
        if (denominator != null) shortenEveryFractionDenominator()
        if (denominator != null) shortenNumeratorWithDenominator()

        cleanNumeratorMaps()
        cleanDenominatorMaps()

        rebuildFractionNumerator()
        if (denominator != null) rebuildFractionDenominator()

        if (denominator != null && denominator!!.isEmpty()) denominator = null

        cleanFraction()
    }

    private fun getValue(): Double {
        if (numerator.size == 1 && numerator.last() is UnknownEntity) {
            if (denominator != null) {
                if (denominator!!.size == 1 && denominator!!.last() is UnknownEntity) {
                    if ((numerator.last() as UnknownEntity).onlyNumber() && (denominator!!.last() as UnknownEntity).onlyNumber()) {
                        return (numerator.last() as UnknownEntity).multiplier!! / (denominator!!.last() as UnknownEntity).multiplier!!
                    }
                }
            }
            else {
                return (numerator.last() as UnknownEntity).multiplier!!
            }
        }
        return 1.0
    }

    private fun equationNotInBrackets(equation: MutableList<Any>): Boolean {
        var brackets = 0
        for (element in equation) {
            when(element) {
                '(' -> brackets++
                ')' -> brackets--
                else -> if (brackets == 0) return true
            }
        }
        return false
    }

    private fun equationHasOperators(equation: MutableList<Any>): Boolean {
        val operators = listOf('+', '-')

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

    private fun setEquationInBrackets(equation: MutableList<Any>) {
        for (element in equation) {
            if (element is Function) {
                if (equationHasOperators(element.content) && equationNotInBrackets(element.content)) {
                    setEquationInBrackets(element.content)

                    if (equationNotInBrackets(element.content)) {
                        if (equationNotInBrackets(equation) && equation.size != 1) {
                            element.content.add(0, '(')
                            element.content.add(')')
                        }
                    }
                }
            }
        }

        return
    }

    private fun cleanEquation(equation: MutableList<Any>): MutableList<Any> {
        val brackets = mutableListOf<Int>()
        var omit = false
        var i = 0
        while (i < equation.size) {
            when(equation[i]) {
                '(' -> {
                    brackets.add(i)
                }
                ')' -> {
                    if (i+1 < equation.size && equation[i+1] == ')' && omit) {
                        equation.removeAt(brackets.removeLast())
                        i--
                        equation.removeAt(i)
                        i--
                    }
                    else {
                        brackets.removeLast()
                    }
                    omit = false
                }
                '×' -> {
                    equation.removeAt(i)
                    i--
                }
                is Char -> {
                    if ((equation[i] as Char).isLetter() || equation[i] == '√') {
                        if (equation[i] != 'x' && equation[i] != 'y' && equation[i] != 'z') {
                            omit = true
                        }
                    }
                }
            }
            i++
        }

        brackets.clear()
        i = 0
        val operators = mutableListOf<Int>()
        while (i < equation.size) {
            when (equation[i]) {
                '(' -> {
                    brackets.add(i)
                }
                ')' -> {
                    if ((operators.isNotEmpty() && operators.last() != brackets.last()) || operators.isEmpty()) {
                        equation.removeAt(brackets.removeLast())
                        i--
                        equation.removeAt(i)
                        i--
                    }
                    else {
                        brackets.removeLast()
                        if (operators.isNotEmpty()) {
                            operators.removeLast()
                        }
                    }
                }
                is Char -> {
                    if ((equation[i] as Char).isLetter() || equation[i] == '√') {
                        if (equation[i] != 'x' && equation[i] != 'y' && equation[i] != 'z') {
                            operators.add(i+1)
                        }
                    } else if (equation[i] == '+' || equation[i] == '-' || equation[i] == '/') {
                        if (brackets.isNotEmpty()) {
                            operators.add(brackets.last())
                        }
                    }
                }
                is Double -> {
                    if (i+1 < equation.size && equation[i+1] == '(') {
                        if (i-1 > 0 && equation[i-1] == '(') {
                            operators.add(i-1)
                        }
                        operators.add(i+1)
                    }
                }
            }
            i++
        }

        if (!equationNotInBrackets(equation) && equation.size > 2) {
            equation.removeFirst()
            equation.removeLast()
        }

        return equation
    }

    private fun sortFragment(input: MutableList<Any>): MutableList<Any> {
        return input.sortedBy {
            when (it) {
                is UnknownEntity -> 1
                is Function -> 2
                is Fraction -> 3
                else -> 4
            }
        }.toMutableList()
    }

    private fun sortFraction(input: MutableList<Any>, key: Boolean = false, flatFraction: Boolean = false): MutableList<Any> {
        val output = mutableListOf<Any>()
        val fragment = mutableListOf<Any>()
        var sorted: MutableList<Any>

        for (element in input) {
            if (element is Fraction) {
                if (key) {
                    fragment.addAll(element.getKey())
                }
                else {
                    fragment.addAll(sortFraction(element.getFraction(flatFraction = flatFraction), flatFraction = flatFraction))
                }
            }
            else {
                when (element) {
                    is UnknownEntity -> fragment.add(element)
                    is Function -> fragment.add(element)
                    else -> {
                        sorted = sortFragment(fragment)
                        for (i in sorted) {
                            output.add(i)
                        }

                        output.add(element)
                        fragment.clear()
                    }
                }
            }
        }
        sorted = sortFragment(fragment)

        for (i in sorted) {
            output.add(i)
        }

        return output
    }

    private fun getInsideOfFraction(input: MutableList<Any>, key: Boolean = false, flatFraction: Boolean = false, withMultiplication: Boolean = false): MutableList<Any> {
        val output = mutableListOf<Any>()

        for (element in input) {
            if (element is Fraction) {
                if (key) {
                    output.addAll(element.getKey())
                }
                else {
                    output.addAll(element.getFraction(flatFraction = flatFraction, withMultiplication = withMultiplication))
                }
            }
            else {
                when (element) {
                    is UnknownEntity -> {
                        if (withMultiplication) {
                            output.add('(')
                            if (key) {
                                output.addAll(element.getKey())
                            }
                            else {
                                output.addAll(element.getOriginal())
                            }
                            output.add(')')
                            output.add('×')
                        }
                        else {
                            if (key) {
                                output.addAll(element.getKey())
                            }
                            else {
                                output.addAll(element.getOriginal())
                            }
                        }
                    }
                    is Function -> {
                        if (withMultiplication) {
                            output.add('(')
                            if (key) {
                                output.addAll(element.getKey())
                            }
                            else {
                                output.addAll(element.getFunction(flatFunction = flatFraction, withMultiplication = true))
                            }
                            output.add(')')
                            output.add('×')
                        }
                        else {
                            if (key) {
                                output.addAll(element.getKey())
                            }
                            else {
                                output.addAll(element.getFunction(flatFunction = flatFraction, withMultiplication = true))
                            }
                        }
                    }
                    else -> {
                        if (output.isNotEmpty() && output.last() == '×') {
                            output.removeLast()
                        }
                        output.add(element)
                    }

                }
            }
        }
        if (output.isNotEmpty() && output.last() == '×') {
            output.removeLast()
        }

        return output
    }

    private fun isOne(): Boolean {
        if (this.numerator.size == 1 && this.denominator == null) {
            if (this.numerator.last() is UnknownEntity) {
                if ((this.numerator.last() as UnknownEntity).onlyNumber()) {
                    if ((this.numerator.last() as UnknownEntity).multiplier == 1.0) {
                        multiplicative = null
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getFraction(flatFraction: Boolean = false, withoutCount: Boolean = false, withMultiplication: Boolean = true): MutableList<Any> {
        val fraction = mutableListOf<Any>()
        var index = 0

        if (count != 0.0) {
            if (powerTo == 0.0 && !flatFraction) {
                return getInsideOfFraction(mutableListOf(UnknownEntity(1.0)), withMultiplication = withMultiplication)
            }
            else {
                if (count != 1.0 && !flatFraction && !withoutCount) {
                    fraction.add('(')
                    fraction.add(count)
                    fraction.add(')')
                    fraction.add('×')
                    fraction.add('(')
                    index = fraction.size
                }

                val content = sortFraction(numerator, flatFraction = flatFraction)

                if (multiplicative != null && !multiplicative!!.isOne()) {
                    fraction.add('(')
                    fraction.addAll(sortFraction(multiplicative!!.numerator, flatFraction = flatFraction))
                    fraction.add('(')
                }
                else {
                    multiplicative = null
                }

                if (content.isNotEmpty() || multiplicative != null || denominator != null) {
                    val denominatorExists = denominator != null && denominator!!.isNotEmpty()

                    fraction.addAll(sortFraction(content, flatFraction = flatFraction))

                    if (denominatorExists) {
                        fraction.add(0, '(')
                        fraction.add(')')
                        fraction.add('/')

                        fraction.add('(')
                        if (multiplicative != null && multiplicative!!.denominator != null) {
                            val buffer = sortFraction(multiplicative!!.denominator!!, flatFraction = flatFraction)
                            fraction.addAll(buffer)
                        }

                        val buffer = sortFraction(denominator!!, flatFraction = flatFraction)
                        fraction.addAll(buffer)
                        fraction.add(')')
                    }
                }

                if (!flatFraction && withMultiplication) {
                    setEquationInBrackets(fraction)
                }

                if (multiplicative != null && !multiplicative!!.isOne()) {
                    fraction.add(')')
                    fraction.add(')')
                }

                if (!flatFraction && multiplicative != null) {
                    if (powerTo != 1.0) {
                        fraction.add(index, '(')
                        fraction.add(')')
                        fraction.add('^')
                        fraction.add('(')
                        fraction.add(UnknownEntity(powerTo))
                        fraction.add(')')
                    }
                }

                if (count != 1.0 && !flatFraction && !withoutCount) {
                    fraction.add(')')
                }
            }
        }

        if (!withMultiplication) {
            return cleanEquation(getInsideOfFraction(fraction, flatFraction = flatFraction))
        }

        return getInsideOfFraction(fraction, flatFraction = flatFraction, withMultiplication = true)
    }

    fun getKey(): MutableList<Any> {
        return getInsideOfFraction(getFraction(flatFraction = true, withMultiplication = false), key = true, withMultiplication = false)
            .filter { it != '(' && it != ')' }.sortedWith(
            compareBy<Any> {
                if (it is Char) it.code else 0
            }.thenBy {
                if (it is Double) it else 1.0
            }.thenBy {
                it::class.simpleName
            }).toMutableList()
    }

    private fun updateFractionNumerator() {
        var numeratorMap = hashMapOf<MutableList<Any>, Double>()
        var numeratorElement = mutableListOf<Any>()
        var entity = UnknownEntity()

        var sign = '0'
        for (element in numerator) {
            when (element) {
                is UnknownEntity -> {
                    if (sign == '/') {
                        entity /= element
                        sign = '0'
                    }
                    else {
                        entity *= element
                    }
                }
                is Function -> {
                    if (element.isNotEmpty()) {
                        if (numeratorMap[element.getKey()] == null) {
                            numeratorElement.add(element)
                        }

                        if (sign == '/') {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) - element.powerTo
                            sign = '0'
                        }
                        else {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) + element.powerTo
                        }
                    }
                }
                is Fraction -> {
                    if (element.getKey().isNotEmpty()) {
                        if (numeratorMap[element.getKey()] == null) {
                            numeratorElement.add(element)
                        }

                        if (sign == '/') {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) - element.powerTo
                            sign = '0'
                        }
                        else {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) + element.powerTo
                        }
                    }
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    numeratorMap[entity.getKey(value = true)] = entity.multiplier!!
                                }
                                else {
                                    numeratorMap[entity.getKey()] = entity.multiplier!!
                                }

                                numeratorElement.add(entity)
                                entity = UnknownEntity()
                            }

                            numeratorMaps.add(numeratorMap)
                            numeratorMap = hashMapOf()

                            numeratorElements.add(numeratorElement)
                            numeratorElement = mutableListOf()

                            numeratorOperators.add(element)
                        }
                    }
                    sign = element
                }
            }
        }
        if (!entity.isEmpty()) {
            if (entity.onlyNumber()) {
                numeratorMap[entity.getKey(value = true)] = entity.multiplier!!
            }
            else {
                numeratorMap[entity.getKey()] = entity.multiplier!!
            }
            numeratorElement.add(entity)
        }

        numeratorMaps.add(numeratorMap)
        numeratorElements.add(numeratorElement)
    }

    private fun updateFractionDenominator() {
        var denominatorMap = hashMapOf<MutableList<Any>, Double>()
        var denominatorElement = mutableListOf<Any>()
        var entity = UnknownEntity()

        var sign = '0'
        for (element in denominator!!) {
            when (element) {
                is UnknownEntity -> {
                    if (sign == '/') {
                        entity /= element
                        sign = '0'
                    }
                    else {
                        entity *= element
                    }
                }
                is Function -> {
                    if (element.isNotEmpty()) {
                        if (denominatorMap[element.getKey()] == null) {
                            denominatorElement.add(element)
                        }

                        if (sign == '/') {
                            denominatorMap[element.getKey()] = denominatorMap.getOrDefault(element.getKey(), 0.0) - element.powerTo
                            sign = '0'
                        }
                        else {
                            denominatorMap[element.getKey()] = denominatorMap.getOrDefault(element.getKey(), 0.0) + element.powerTo
                        }
                    }
                }
                is Fraction -> {
                    if (element.isNotEmpty()) {
                        if (denominatorMap[element.getKey()] == null) {
                            denominatorElement.add(element)
                        }

                        if (sign == '/') {
                            denominatorMap[element.getKey()] = denominatorMap.getOrDefault(element.getKey(), 0.0) - element.powerTo
                            sign = '0'
                        }
                        else {
                            denominatorMap[element.getKey()] = denominatorMap.getOrDefault(element.getKey(), 0.0) + element.powerTo
                        }
                    }
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    denominatorMap[entity.getKey(value = true)] = entity.multiplier!!
                                }
                                else {
                                    denominatorMap[entity.getKey()] = entity.multiplier!!
                                }

                                denominatorElement.add(entity)
                                entity = UnknownEntity()
                            }

                            denominatorMaps.add(denominatorMap)
                            denominatorMap = hashMapOf()

                            denominatorElements.add(denominatorElement)
                            denominatorElement = mutableListOf()

                            denominatorOperators.add(element)
                        }
                    }
                    sign = element
                }
            }
        }
        if (!entity.isEmpty()) {
            if (entity.onlyNumber()) {
                denominatorMap[entity.getKey(value = true)] = entity.multiplier!!
            }
            else {
                denominatorMap[entity.getKey()] = entity.multiplier!!
            }
            denominatorElement.add(entity)
        }

        denominatorMaps.add(denominatorMap)
        denominatorElements.add(denominatorElement)
    }

    private fun itIsUnknown(input: MutableList<Any>): Boolean {
        if (input.size == 3) {
            if (input[0] is Double) {
                if (input[1] == '^') {
                    if (input[2] == 'x' || input[2] == 'y' || input[2] == 'z') {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }

    private fun countDecimalPlaces(value: Double): Int {
        val text = value.toString()
        return if (text.contains(".")) {
            text.substringAfter(".").trimEnd('0').length
        } else {
            0
        }
    }

    private fun getMultiplicativeNumerator(): MutableList<Any> {
        // Find commons for numerator
        if (numeratorMaps.isNotEmpty()) {
            val startCommonForNumerator =  hashMapOf<MutableList<Any>, Double>()
            for ((key, v) in numeratorMaps.last()) {
                startCommonForNumerator[key] = v
            }

            var noCommonEntity = false
            val toRemove = mutableListOf<MutableList<Any>>()
            for (map in numeratorMaps) {
                for ((key, value) in startCommonForNumerator) {
                    if (itIsUnknown(key) && !noCommonEntity) {
                        var found = false
                        for((k, v) in map) {
                            if (itIsUnknown(k)) {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForNumerator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                found = true
                            }
                        }

                        if (!found) {
                            for ((common, _) in commonForNumerator) {
                                if (itIsUnknown(common)) {
                                    toRemove.add(common)
                                }
                            }
                            noCommonEntity = true
                        }
                    }
                    else if (map[key] == null && startCommonForNumerator[key] != null) {
                        toRemove.add(key)
                    }
                    else if (map[key] == null) {
                        continue
                    }
                    else {
                        if (startCommonForNumerator[key] != null && map[key]!! < value) {
                            commonForNumerator[key] = map[key]!!
                        }
                        else if (startCommonForNumerator[key] != null && map[key]!! == value) {
                            if (map !== numeratorMaps.last()) {
                                commonForNumerator[key] = value
                            }
                        }
                        else if (startCommonForNumerator[key] != null && map[key]!! > value) {
                            commonForNumerator[key] = value
                        }
                    }
                }
            }

            for (i in toRemove) {
                commonForNumerator.remove(i)
            }
        }

        // Remove commons from fraction numerator
        for (map in numeratorMaps) {
            for ((original, _) in map) {
                for ((common, count) in commonForNumerator) {
                    if (original == common) {
                        map[original] = map[original]!! - count
                    }
                }
            }
        }

        // Make a fraction out of found commons
        val numeratorOutput = mutableListOf<Any>()
        val commonsToFind = hashMapOf<MutableList<Any>, Double>()
        var entity: UnknownEntity? = null

        for ((key, v) in commonForNumerator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entity = UnknownEntity()
                entity.multiplier = commonsToFind[key]
                entity.variable = key.last() as Char
                entity.powerTo = key.first() as Double
            }
        }

        var index = 0
        while (index < numeratorElements.size) {
            var element = 0
            while (element < numeratorElements[index].size) {
                for ((key, v) in commonForNumerator) {
                    if (element > numeratorElements[index].size || element < 0) {
                        break
                    }
                    when (numeratorElements[index][element]) {
                        is UnknownEntity -> {
                            if (itIsUnknown(key)){
                                (numeratorElements[index][element] as UnknownEntity).powerTo =
                                    (numeratorElements[index][element] as UnknownEntity).powerTo?.minus(
                                        key.first() as Double
                                    )

                                if ((numeratorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    (numeratorElements[index][element] as UnknownEntity).variable = null
                                    (numeratorElements[index][element] as UnknownEntity).powerTo = 1.0
                                }
                            }
                        }

                        is Function -> {
                            if (key == (numeratorElements[index][element] as Function).getKey()) {
                                (numeratorElements[index][element] as Function).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    numeratorOutput.add(numeratorElements[index].removeAt(element))
                                    commonsToFind[key] = commonsToFind[key]!! - 1
                                }
                                else {
                                    numeratorElements[index].removeAt(element)
                                }
                                element--
                            }
                        }

                        is Fraction -> {
                            if (key == (numeratorElements[index][element] as Fraction).getKey()) {
                                (numeratorElements[index][element] as Fraction).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    numeratorOutput.add(numeratorElements[index].removeAt(element))
                                    commonsToFind[key] = commonsToFind[key]!! - 1
                                }
                                else {
                                    numeratorElements[index].removeAt(element)
                                }
                                element--
                            }
                        }
                    }
                }
                element++
            }
            index++
        }

        if (entity != null) {
            // Get all multipliers of unknown entities
            val allNumeratorMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (list in numeratorElements) {
                for (element in list) {
                    if (element is UnknownEntity) {
                        allNumeratorMultipliers.add(Fraction(mutableListOf(UnknownEntity(round(element.multiplier!! * 1000) / 1000))))
                        val decimalPoint = countDecimalPlaces(round(element.multiplier!! * 1000) / 1000)
                        if (decimalPoint > biggestDecimalPoint) {
                            biggestDecimalPoint = decimalPoint
                        }
                    }
                }
            }

            // Convert multipliers to same base
            var decimalPoint = 1
            for (i in 0..<biggestDecimalPoint) {
                decimalPoint *= 10
            }
            for (fraction in allNumeratorMultipliers) {
                (fraction.numerator.last() as UnknownEntity).multiplier = (fraction.numerator.last() as UnknownEntity).multiplier?.times(
                    decimalPoint
                )
                fraction.denominator = mutableListOf()
                fraction.denominator!!.add(UnknownEntity(decimalPoint.toDouble()))
            }

            // Find gcd
            val allGCD = mutableListOf<MutableList<Int>>()
            var currentGCD = mutableListOf<Int>()
            for (multiplierA in allNumeratorMultipliers) {
                for (multiplierB in allNumeratorMultipliers) {
                    val value = gcd((multiplierA.numerator.last() as UnknownEntity).multiplier!!.toInt(), (multiplierB.numerator.last() as UnknownEntity).multiplier!!.toInt())
                    currentGCD.add(value)
                }
                allGCD.add(currentGCD)
                currentGCD = mutableListOf()
            }

            // Get common gcd
            val commonGCD = hashMapOf<Int, Int>()
            for (list in allGCD) {
                var biggestValue = 0
                for (gcd in list) {
                    if (gcd > biggestValue) {
                        biggestValue = gcd
                    }
                }

                val allDivisors = mutableListOf<Int>()
                for (i in 1..biggestValue) {
                    if (biggestValue % i == 0) {
                        allDivisors.add(i)
                    }
                }

                val checkUnique = mutableListOf<Int>()
                for (divisor in allDivisors) {
                    var found = false
                    for (u in checkUnique) {
                        if (u == divisor) {
                            found = true
                        }
                    }

                    if (!found) {
                        commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
                        checkUnique.add(divisor)
                    }
                }
            }

            var gcd = 1
            for ((k, v) in commonGCD) {
                if (v == allGCD.size) {
                    if (gcd < k) {
                        gcd = k
                    }
                }
            }

            // Apply GCD to all multipliers
            for (fraction in allNumeratorMultipliers) {
                (fraction.numerator.last() as UnknownEntity).multiplier = (fraction.numerator.last() as UnknownEntity).multiplier?.div(
                    gcd
                )
            }

            var i = 0
            var multiplier = 0
            while (i < numeratorElements.size) {
                var j = 0
                while (j < numeratorElements[i].size) {
                    if (numeratorElements[i][j] is UnknownEntity) {
                        val value = allNumeratorMultipliers[multiplier].getValue()
                        if (value == 1.0) {
                            if ((numeratorElements[i][j] as UnknownEntity).onlyNumber()) {
                                numeratorElements[i].removeAt(j)
                                j--
                            }
                            else {
                                (numeratorElements[i][j] as UnknownEntity).multiplier = value
                            }
                        }
                        else {
                            (numeratorElements[i][j] as UnknownEntity).multiplier = value
                        }
                        multiplier++
                        j++
                    }
                    j++
                }
                i++
            }

            numeratorOutput.add(
                Fraction(
                    mutableListOf(
                        UnknownEntity(
                            gcd.toDouble(),
                            entity.variable,
                            entity.powerTo
                        )
                    ), mutableListOf(UnknownEntity(decimalPoint.toDouble()))
                )
            )

            for (element in numeratorOutput) {
                if (element is Fraction) {
                    element.shortenFraction()
                }
            }
        }

        return numeratorOutput
    }

    private fun getMultiplicativeDenominator(): MutableList<Any>? {
        if (denominator == null) return null

        // Find commons for numerator
        if (denominatorMaps.isNotEmpty()) {
            val startCommonForDenominator = hashMapOf<MutableList<Any>, Double>()
            for ((key, v) in denominatorMaps.last()) {
                startCommonForDenominator[key] = v
            }
            var noCommonEntity = false
            val toRemove = mutableListOf<MutableList<Any>>()
            for (map in denominatorMaps) {
                for ((key, value) in startCommonForDenominator) {
                    if (itIsUnknown(key) && !noCommonEntity) {
                        var found = false
                        for((k, v) in map) {
                            if (itIsUnknown(k)) {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForDenominator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                found = true
                            }
                        }

                        if (!found) {
                            for ((common, _) in commonForDenominator) {
                                if (itIsUnknown(common)) {
                                    toRemove.add(common)
                                }
                            }
                            noCommonEntity = true
                        }
                    }
                    else if (map[key] == null && startCommonForDenominator[key] != null) {
                        toRemove.add(key)
                    }
                    else if (map[key] == null) {
                        continue
                    }
                    else {
                        if (startCommonForDenominator[key] != null && map[key]!! < value) {
                            commonForDenominator[key] = map[key]!!
                        }
                        else if (startCommonForDenominator[key] != null && map[key]!! == value) {
                            if (map !== denominatorMaps.last()) {
                                commonForDenominator[key] = value
                            }
                        }
                        else if (startCommonForDenominator[key] != null && map[key]!! > value) {
                            commonForDenominator[key] = value
                        }
                    }
                }
            }

            for (i in toRemove) {
                commonForDenominator.remove(i)
            }
        }

        // Remove commons from fraction denominator
        for (map in denominatorMaps) {
            for ((original, _) in map) {
                for ((common, count) in commonForDenominator) {
                    if (original == common) {
                        map[original] = map[original]!! - count
                    }
                }
            }
        }

        // Make a fraction out of found commons
        val denominatorOutput = mutableListOf<Any>()
        val commonsToFind = hashMapOf<MutableList<Any>, Double>()
        var entity: UnknownEntity? = null

        for ((key, v) in commonForDenominator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entity = UnknownEntity()
                entity.multiplier = commonsToFind[key]
                entity.variable = key.last() as Char
                entity.powerTo = key.first() as Double
            }
        }

        var index = 0
        while (index < denominatorElements.size) {
            var element = 0
            while (element < denominatorElements[index].size) {
                for ((key, v) in commonForDenominator) {
                    if (element > denominatorElements[index].size || element < 0) {
                        break
                    }
                    when (denominatorElements[index][element]) {
                        is UnknownEntity -> {
                            if (itIsUnknown(key)){
                                (denominatorElements[index][element] as UnknownEntity).powerTo =
                                    (denominatorElements[index][element] as UnknownEntity).powerTo?.minus(
                                        key.first() as Double
                                    )

                                if ((denominatorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    (denominatorElements[index][element] as UnknownEntity).variable = null
                                    (denominatorElements[index][element] as UnknownEntity).powerTo = 1.0
                                }
                            }
                        }

                        is Function -> {
                            if (key == (denominatorElements[index][element] as Function).getKey()) {
                                (denominatorElements[index][element] as Function).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    denominatorOutput.add(denominatorElements[index].removeAt(element))
                                    commonsToFind[key] = commonsToFind[key]!! - 1
                                }
                                else {
                                    denominatorElements[index].removeAt(element)
                                }
                                element--
                            }
                        }
                        is Fraction -> {
                            if (key == (denominatorElements[index][element] as Fraction).getKey()) {
                                (denominatorElements[index][element] as Fraction).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    denominatorOutput.add(denominatorElements[index].removeAt(element))
                                    commonsToFind[key] = commonsToFind[key]!! - 1
                                }
                                else {
                                    denominatorElements[index].removeAt(element)
                                }
                                element--
                            }
                        }
                    }
                }
                element++
            }
            index++
        }

        if (entity != null) {
            // Get all multipliers of unknown entities
            val allDenominatorMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (list in denominatorElements) {
                for (element in list) {
                    if (element is UnknownEntity) {
                        allDenominatorMultipliers.add(Fraction(mutableListOf(UnknownEntity(round(element.multiplier!! * 1000) / 1000))))
                        val decimalPoint = countDecimalPlaces(round(element.multiplier!! * 1000) / 1000)
                        if (decimalPoint > biggestDecimalPoint) {
                            biggestDecimalPoint = decimalPoint
                        }
                    }
                }
            }

            // Convert multipliers to same base
            var decimalPoint = 1
            for (i in 0..<biggestDecimalPoint) {
                decimalPoint *= 10
            }
            for (fraction in allDenominatorMultipliers) {
                (fraction.numerator.last() as UnknownEntity).multiplier = (fraction.numerator.last() as UnknownEntity).multiplier?.times(
                    decimalPoint
                )
                fraction.denominator = mutableListOf()
                fraction.denominator!!.add(UnknownEntity(decimalPoint.toDouble()))
            }

            // Find gcd
            val allGCD = mutableListOf<MutableList<Int>>()
            var currentGCD = mutableListOf<Int>()
            for (multiplierA in allDenominatorMultipliers) {
                for (multiplierB in allDenominatorMultipliers) {
                    val value = gcd((multiplierA.numerator.last() as UnknownEntity).multiplier!!.toInt(), (multiplierB.numerator.last() as UnknownEntity).multiplier!!.toInt())
                    currentGCD.add(value)
                }
                allGCD.add(currentGCD)
                currentGCD = mutableListOf()
            }

            // Get common gcd
            val commonGCD = hashMapOf<Int, Int>()
            for (list in allGCD) {
                var biggestValue = 0
                for (gcd in list) {
                    if (gcd > biggestValue) {
                        biggestValue = gcd
                    }
                }

                val allDivisors = mutableListOf<Int>()
                for (i in 1..biggestValue) {
                    if (biggestValue % i == 0) {
                        allDivisors.add(i)
                    }
                }

                val checkUnique = mutableListOf<Int>()
                for (divisor in allDivisors) {
                    var found = false
                    for (u in checkUnique) {
                        if (u == divisor) {
                            found = true
                        }
                    }

                    if (!found) {
                        commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
                        checkUnique.add(divisor)
                    }
                }
            }

            var gcd = 1
            for ((k, v) in commonGCD) {
                if (v == allGCD.size) {
                    if (gcd < k) {
                        gcd = k
                    }
                }
            }

            // Apply GCD to all multipliers
            for (fraction in allDenominatorMultipliers) {
                (fraction.numerator.last() as UnknownEntity).multiplier = (fraction.numerator.last() as UnknownEntity).multiplier?.div(
                    gcd
                )
            }

            var i = 0
            var multiplier = 0
            while (i < denominatorElements.size) {
                var j = 0
                while (j < denominatorElements[i].size) {
                    if (denominatorElements[i][j] is UnknownEntity) {
                        val value = allDenominatorMultipliers[multiplier].getValue()
                        if (value == 1.0) {
                            if ((denominatorElements[i][j] as UnknownEntity).onlyNumber()) {
                                denominatorElements[i].removeAt(j)
                                j--
                            }
                            else {
                                (denominatorElements[i][j] as UnknownEntity).multiplier = value
                            }
                        }
                        else {
                            (denominatorElements[i][j] as UnknownEntity).multiplier = value
                        }
                        multiplier++
                        j++
                    }
                    j++
                }
                i++
            }

            denominatorOutput.add(
                    Fraction(
                        mutableListOf(
                            UnknownEntity(
                                gcd.toDouble(),
                                entity.variable,
                                entity.powerTo
                            )
                        ), mutableListOf(UnknownEntity(decimalPoint.toDouble()))
                    )
                )

            for (element in denominatorOutput) {
                if (element is Fraction) {
                    element.shortenFraction()
                }
            }
        }

        return denominatorOutput
    }

    private fun setMultiplicative(numerator: MutableList<Any>, denominator: MutableList<Any>?) {
        multiplicative = if (denominator != null) {
            if (numerator.isNotEmpty() || denominator.isNotEmpty()) {
                Fraction(numerator, denominator)
            }
            else {
                null
            }
        } else {
            if (numerator.isNotEmpty()) {
                if (numerator.size == 1 && numerator.last() is Fraction) {
                    numerator.last() as Fraction
                } else {
                    Fraction(numerator)
                }
            }
            else {
                null
            }
        }

        if (multiplicative != null) {
            if (multiplicative!!.multiplicative != null)  {
                if (multiplicative!!.multiplicative!!.denominator == null) {
                    multiplicative!!.numerator.addAll(multiplicative!!.multiplicative!!.numerator)
                }
                else {
                    multiplicative!!.numerator.addAll(multiplicative!!.multiplicative!!.numerator)

                    if ( multiplicative!!.denominator == null) {
                        multiplicative!!.denominator = multiplicative!!.multiplicative!!.denominator
                    }
                    else {
                        if (multiplicative!!.multiplicative!!.denominator != null) {
                            multiplicative!!.denominator!!.addAll(multiplicative!!.multiplicative!!.denominator!!)
                        }
                    }
                }
            }
            multiplicative!!.multiplicative = null
        }

        if (multiplicative != null && numerator.isEmpty() && multiplicative!!.denominator == null) {
            numerator.add(multiplicative!!)
            multiplicative = null
        }
    }

    private fun shortenFraction() {
        if (this.denominator != null) {
            if (this.denominator!!.size == 1 && this.denominator!!.last() is UnknownEntity) {
                if ((this.denominator!!.last() as UnknownEntity).onlyNumber() && (this.denominator!!.last() as UnknownEntity).multiplier == 1.0) {
                    this.denominator!!.clear()
                    this.denominator = null
                }
            }
        }

        if (this.denominator != null) {
            if (!equationHasOperators(this.numerator) && !equationHasOperators(this.denominator!!)) {
                for (elementA in this.denominator!!) {
                    var i = 0
                    for (elementB in this.numerator) {
                        when (elementA) {
                            is Function -> {

                            }
                            is Fraction -> {
                                when (elementB) {
                                    is Function -> {
                                        if (elementA.getKey() == elementB.getKey()) {
                                            elementB.powerTo -= elementA.powerTo
                                            elementA.powerTo = 0.0
                                            break
                                        }
                                    }
                                    is Fraction -> {
                                        if (elementA == elementB) {
                                            elementB.powerTo -= elementA.powerTo
                                            elementA.powerTo = 0.0
                                            break
                                        }
                                    }
                                }
                            }
                            is UnknownEntity -> {

                            }
                        }
                        i++
                    }
                }
            }
        }
        else {
            if (!equationHasOperators(this.numerator)) {
                val toRemove = mutableListOf<Any>()
                for (elementA in this.numerator) {
                    var i = 0
                    var run = false
                    for (elementB in this.numerator) {
                        if (elementA === elementB) {
                            run = true
                            continue
                        }
                        if (!run) continue

                        when (elementA) {
                            is Function -> {
                                when (elementB) {
                                    is Function -> {
                                        if (elementA.getKey() == elementB.getKey()) {
                                            elementB.powerTo += elementA.powerTo
                                            toRemove.add(elementA)
                                            break
                                        }
                                    }

                                    is Fraction -> {
                                        if (elementA == elementB) {
                                            elementB.powerTo += elementA.powerTo
                                            toRemove.add(elementA)
                                            break
                                        }
                                    }
                                }
                            }

                            is Fraction -> {
                                when (elementB) {
                                    is Function -> {
                                        if (elementA.getKey() == elementB.getKey()) {
                                            elementB.powerTo += elementA.powerTo
                                            toRemove.add(elementA)
                                            break
                                        }
                                    }

                                    is Fraction -> {
                                        if (elementA == elementB) {
                                            elementB.powerTo += elementA.powerTo
                                            toRemove.add(elementA)
                                            break
                                        }
                                    }
                                }
                            }

                            is UnknownEntity -> {

                            }
                        }
                        i++
                    }
                }

                for (i in toRemove) {
                    numerator.remove(i)
                }
            }
        }
    }

    private fun shortenEveryFractionNumerator() {
        for (list in numeratorElements) {
            for (element in list) {
                when (element) {
                    is Fraction -> {
                        element.shortenFraction()
                    }
                }
            }
        }
    }

    private fun shortenEveryFractionDenominator() {
        for (list in denominatorElements) {
            for (element in list) {
                when (element) {
                    is Fraction -> {
                        element.shortenFraction()
                    }
                }
            }
        }
    }

    private fun shortenNumeratorWithDenominator() {
        for (numeratorMap in numeratorMaps) {
            for (denominatorMap in denominatorMaps) {
                for ((key, value) in numeratorMap) {
                    for ((k, v) in denominatorMap) {
                        if (key == k) {
                            if (v > value) {
                                numeratorMap[key] = numeratorMap[key]!! - value
                                denominatorMap[key] = denominatorMap[key]!! - value
                            }
                            else {
                                numeratorMap[key] = numeratorMap[key]!! - v
                                denominatorMap[key] = denominatorMap[key]!! - v
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    private fun cleanNumeratorMaps() {
        val toRemove = mutableListOf<MutableList<Any>>()
        for (map in numeratorMaps) {
            val remove = mutableListOf<Any>()
            for((k, v) in map) {
                if (v == 0.0) {
                    remove.add(k)
                }
            }
            toRemove.add(remove)
        }

        for ((index, _) in toRemove.withIndex()) {
            for (element in toRemove[index]) {
                numeratorMaps[index].remove(element)
            }
        }
    }

    private fun cleanDenominatorMaps() {
        val toRemove = mutableListOf<MutableList<Any>>()
        for (map in denominatorMaps) {
            val remove = mutableListOf<Any>()
            for((k, v) in map) {
                if (v == 0.0) {
                    remove.add(k)
                }
            }
            toRemove.add(remove)
        }

        for ((index, _) in toRemove.withIndex()) {
            for (element in toRemove[index]) {
                denominatorMaps[index].remove(element)
            }
        }
    }

    private fun rebuildFractionNumerator() {
        val outputNumerator = mutableListOf<Any>()
        var operatorIndex = 0

        for (index in 0..< numeratorMaps.size) {
            for (entity in numeratorElements[index]) {
                for ((key, v) in numeratorMaps[index]) {
                    when (entity) {
                        is Function -> {
                            if (key == entity.getKey()) {
                                entity.powerTo = v
                            }
                        }

                        is Fraction -> {
                            if (key == entity.getKey()) {
                                entity.powerTo = v
                            }
                        }
                    }
                }
            }

            if (numeratorElements[index].isNotEmpty()) {
                outputNumerator.addAll(sortFragment(numeratorElements[index]))
            }
            else {
                if (outputNumerator.isNotEmpty() && outputNumerator.last() is Char || outputNumerator.isEmpty()) {
                    outputNumerator.add(UnknownEntity(1.0))
                }
            }

            if (operatorIndex < numeratorOperators.size && outputNumerator.isNotEmpty()) {
                outputNumerator.add(numeratorOperators[operatorIndex])
                operatorIndex++
            }
            else {
                operatorIndex++
            }
        }

        val result = shortenEquation(outputNumerator)

        numerator = if (equationHasOperators(result) && result.size != 1) {
            mutableListOf(Function(result))
        } else {
            result
        }
    }

    private fun rebuildFractionDenominator() {
        val outputDenominator = mutableListOf<Any>()
        var operatorIndex = 0

        for (index in 0..< denominatorMaps.size) {
            for (entity in denominatorElements[index]) {
                for ((key, v) in denominatorMaps[index]) {
                    when (entity) {
                        is Function -> {
                            if (key == entity.getKey()) {
                                entity.powerTo = v
                            }
                        }
                        is Fraction -> {
                            if (key == entity.getKey()) {
                                entity.powerTo = v
                            }
                        }
                    }
                }
            }

            if (denominatorElements[index].isNotEmpty()) {
                outputDenominator.addAll(sortFragment(denominatorElements[index]))
            }
            else {
                if (outputDenominator.isNotEmpty() && outputDenominator.last() is Char || outputDenominator.isEmpty()) {
                    outputDenominator.add(UnknownEntity(1.0))
                }
            }

            if (operatorIndex < denominatorOperators.size && outputDenominator.isNotEmpty()) {
                outputDenominator.add(denominatorOperators[operatorIndex])
            }
            else {
                operatorIndex++
            }
        }

        val result = shortenEquation(outputDenominator)

        denominator = if (equationHasOperators(result) && result.size != 1) {
            mutableListOf(Function(result))
        } else {
            result
        }
    }

    private fun cleanFraction() {
        val toRemove = mutableListOf<Any>()

        for (i in numerator) {
            when (i) {
                is Fraction -> if (i.isEmpty()) toRemove.add(i)
                is UnknownEntity -> if (i.isEmpty()) toRemove.add(i)
            }
        }

        for (i in toRemove){
            numerator.remove(i)
        }

        if (denominator != null) {
            toRemove.clear()

            for (i in denominator!!) {
                when (i) {
                    is Fraction -> if (i.isEmpty()) toRemove.add(i)
                    is UnknownEntity -> if (i.isEmpty()) toRemove.add(i)
                }
            }

            for (i in toRemove){
                denominator!!.remove(i)
            }
        }

        if (multiplicative != null) {
            if (multiplicative!!.isEmpty()) {
                multiplicative = null
            }
            else {
                toRemove.clear()

                for (i in multiplicative!!.numerator) {
                    when (i) {
                        is Fraction -> if (i.isEmpty()) toRemove.add(i)
                        is UnknownEntity -> if (i.isEmpty()) toRemove.add(i)
                    }
                }

                for (i in toRemove){
                    multiplicative!!.numerator.remove(i)
                }

                if (multiplicative!!.denominator != null) {
                    toRemove.clear()

                    for (i in multiplicative!!.denominator!!) {
                        when (i) {
                            is Fraction -> if (i.isEmpty()) toRemove.add(i)
                            is UnknownEntity -> if (i.isEmpty()) toRemove.add(i)
                        }
                    }

                    for (i in toRemove){
                        multiplicative!!.denominator!!.remove(i)
                    }
                }
            }
        }
    }

    private fun shortenEquation(equation: MutableList<Any>): MutableList<Any> {
        val grouped = mutableListOf<Any>()

        var fraction = Fraction()
        for (element in equation) {
            when (element) {
                is Char -> {
                    if (element == '+' || element == '-') {
                        if (fraction.isNotEmpty()) {
                            grouped.add(fraction)
                            fraction.shortenFraction()
                            grouped.add(element)
                            fraction = Fraction()
                        }
                    }
                }
                is UnknownEntity -> {
                    if (!element.isEmpty()) {
                        fraction *= element
                    }
                }
                is Function -> {
                    if (element.isNotEmpty()) {
                        fraction *= element
                    }
                }
                is Fraction -> {
                    if (element.isNotEmpty()) {
                        fraction *= element
                    }
                }
            }
        }
        if (fraction.isNotEmpty()) {
            grouped.add(fraction)
            fraction.shortenFraction()
        }

        val output = mutableListOf<Any>()
        var lastChar = '+'
        for (elementA in grouped) {
            if (elementA is Char) continue
            if (elementA is Fraction) fraction = elementA

            for (elementB in grouped) {
                if (elementA === elementB) continue

                when (elementB) {
                    is Char -> lastChar = elementB
                    is Fraction -> {
                        if (elementA == elementB) {
                            if (elementB.isNotEmpty()) {
                                if (lastChar == '-') {
                                    fraction -= elementB
                                }
                                else {
                                    fraction += elementB
                                }
                                elementB.clear()
                            }
                        }
                    }
                }
            }

            if (fraction.isNotEmpty()) {
                output.add(fraction)
                output.add('+')
            }
        }
        if (output.isNotEmpty() && output.last() is Char) {
            output.removeLast()
        }

        return output
    }

    operator fun plus(other: Fraction): Fraction {
        if (this == other) {
            this.count += other.count
        }
        else {
            val newFraction = Fraction(denominator = mutableListOf())
            if (this.denominator != null && other.denominator != null) {
                if (this.denominator != other.denominator) {
                    newFraction.numerator = mutableListOf(listOf(Fraction(this.numerator), Fraction(other.denominator!!)))
                    newFraction.numerator.add('+')
                    newFraction.numerator.addAll(listOf(Fraction(other.numerator), Fraction(this.denominator!!)))

                    newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
                    newFraction.denominator!!.add(Fraction(other.denominator!!))
                }
                else {
                    newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
                }
            }
            else if (this.denominator != null) {
                newFraction.numerator = mutableListOf(Fraction(this.numerator))
                newFraction.numerator.add('+')
                newFraction.numerator.addAll(listOf(Fraction(other.numerator), Fraction(this.denominator!!)))

                newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
            }
            else if (other.denominator != null) {
                newFraction.numerator = mutableListOf(listOf(Fraction(this.numerator), Fraction(other.denominator!!)))
                newFraction.numerator.add('+')
                newFraction.numerator.add(Fraction(other.numerator))

                newFraction.denominator = mutableListOf(Fraction(other.denominator!!))
            }
            else {
                newFraction.numerator = mutableListOf(Fraction(this.numerator))
                newFraction.numerator.add('+')
                newFraction.numerator.add(Fraction(other.numerator))
            }

            return newFraction
        }
        return this
    }

    operator fun minus(other: Fraction): Fraction {
        if (this == other) {
            this.count -= other.count
        }
        else {
            val newFraction = Fraction(denominator = mutableListOf())

            if (this.denominator != null && other.denominator != null) {
                if (this.denominator != other.denominator) {
                    newFraction.numerator = mutableListOf(listOf(Fraction(this.numerator), Fraction(other.denominator!!)))
                    newFraction.numerator.add('-')
                    newFraction.numerator.addAll(listOf(Fraction(other.numerator), Fraction(this.denominator!!)))

                    newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
                    newFraction.denominator!!.add(Fraction(other.denominator!!))
                }
                else {
                    newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
                }
            }
            else if (this.denominator != null) {
                newFraction.numerator = mutableListOf(Fraction(this.numerator))
                newFraction.numerator.add('-')
                newFraction.numerator.addAll(listOf(Fraction(other.numerator), Fraction(this.denominator!!)))

                newFraction.denominator = mutableListOf(Fraction(this.denominator!!))
            }
            else if (other.denominator != null) {
                newFraction.numerator = mutableListOf(listOf(Fraction(this.numerator), Fraction(other.denominator!!)))
                newFraction.numerator.add('-')
                newFraction.numerator.add(Fraction(other.numerator))

                newFraction.denominator = mutableListOf(Fraction(other.denominator!!))
            }
            else {
                newFraction.numerator = mutableListOf(Fraction(this.numerator))
                newFraction.numerator.add('-')
                newFraction.numerator.add(Fraction(other.numerator))
            }

            return newFraction
        }
        return this
    }

    operator fun times(other: Fraction): Fraction {
        if (this.getKey() == other.getKey()) {
            this.powerTo += other.powerTo
        }
        else {
            if (this.denominator != null && other.denominator != null && this.denominator!!.isNotEmpty() && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                        this.numerator.add(Fraction(other.numerator))
                        this.denominator = mutableListOf(Fraction(this.denominator!!))
                        this.denominator!!.add(Fraction(other.denominator!!))
                    }
                }
                else {
                    return other
                }
            }
            else if (this.denominator != null && this.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                        this.numerator.add(Fraction(other.numerator))
                    }
                }
                else {
                    return other
                }
            }
            else if (other.denominator != null && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                        this.numerator.add(Fraction(other.numerator))
                        this.denominator = mutableListOf(Fraction(other.denominator!!))
                    }
                }
                else {
                    return other
                }
            }
            else {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                        this.numerator.add(Fraction(other.numerator))
                    }
                }
                else {
                    return other
                }
            }
        }
        return this
    }

    operator fun times(other: Function): Fraction {
        if (this.numerator.isNotEmpty()) {
            if (other.isNotEmpty()) {
                this.numerator.add(other)
            }
        }
        else {
            if (other.isNotEmpty()) {
                return Fraction(mutableListOf(other))
            }
        }
        return this
    }

    operator fun times(other: UnknownEntity): Fraction {
        if (this.numerator.isNotEmpty()) {
            if (!other.isEmpty()) {
                this.numerator.add(other)
            }
        }
        else {
            if (!other.isEmpty()) {
                return Fraction(mutableListOf(other))
            }
        }
        return this
    }

    operator fun div(other: Fraction): Fraction {
        if (this.getFraction(withoutCount = true, withMultiplication = false) == other.getFraction(withoutCount = true, withMultiplication = false)) {
            this.powerTo -= other.powerTo
        }
        else {
            if (this.denominator != null && other.denominator != null && this.denominator!!.isNotEmpty() && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    this.numerator = mutableListOf(Fraction(this.numerator))
                }
                this.numerator.add(Fraction(other.denominator!!))
                this.denominator = mutableListOf(Function(this.denominator!!))
                this.denominator!!.add(Fraction(other.numerator))
            }
            else if (this.denominator != null && this.denominator!!.isNotEmpty()) {
                this.denominator = mutableListOf(Fraction(this.denominator!!))
                this.denominator!!.add(Fraction(other.numerator))
            }
            else if (other.denominator != null && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    this.numerator = mutableListOf(Fraction(this.numerator))
                }
                this.numerator.add(Fraction(other.denominator!!))
                this.denominator = mutableListOf(Fraction(other.numerator))
            }
            else {
                this.denominator =  mutableListOf(Fraction(other.numerator))
            }
        }
        return this
    }

    operator fun div(other: Function): Fraction {
        if (this.denominator != null && this.denominator!!.isNotEmpty()) {
            this.denominator = mutableListOf(Fraction(this.denominator!!))
            this.denominator!!.add(Fraction(mutableListOf(other)))
        }
        else {
            this.denominator = mutableListOf(Fraction(mutableListOf(other)))
        }
        return this
    }

    operator fun div(other: UnknownEntity): Fraction {
        if (this.denominator != null && this.denominator!!.isNotEmpty()) {
            this.denominator = mutableListOf(Fraction(this.denominator!!))
            this.denominator!!.add(Fraction(mutableListOf(other)))
        }
        else {
            this.denominator = mutableListOf(Fraction(mutableListOf(other)))
        }
        return this
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is Fraction) {
            return this.getFraction(withoutCount = true, withMultiplication = false) == other.getFraction(withoutCount = true, withMultiplication = false) && this.powerTo == other.powerTo
        }
        return false
    }

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result = 31 * result + (denominator?.hashCode() ?: 0)
        result = 31 * result + powerTo.hashCode()
        result = 31 * result + count.hashCode()
        result = 31 * result + (multiplicative?.hashCode() ?: 0)
        result = 31 * result + numeratorMaps.hashCode()
        result = 31 * result + numeratorOperators.hashCode()
        result = 31 * result + numeratorElements.hashCode()
        result = 31 * result + commonForNumerator.hashCode()
        result = 31 * result + denominatorMaps.hashCode()
        result = 31 * result + denominatorOperators.hashCode()
        result = 31 * result + denominatorElements.hashCode()
        result = 31 * result + commonForDenominator.hashCode()
        return result
    }
}