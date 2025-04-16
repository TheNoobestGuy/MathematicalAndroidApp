package com.example.mathmaster.customviews

import kotlin.math.min
import kotlin.math.round

data class Fraction(var numerator: MutableList<Any>, var denominator: MutableList<Any>? = null, var powerTo: Double = 1.0, var count: Double = 1.0, var multiplicative: Fraction? = null) {
    private val numeratorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val numeratorOperators = mutableListOf<Char>()
    private val numeratorElements = mutableListOf<MutableList<Any>>()
    private val commonForNumerator = hashMapOf<MutableList<Any>, Double>()

    private val denominatorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val denominatorOperators = mutableListOf<Char>()
    private val denominatorElements = mutableListOf<MutableList<Any>>()
    private val commonForDenominator = hashMapOf<MutableList<Any>, Double>()

    private fun clear() {
        numerator.clear()
        denominator = null
        multiplicative = null
        numeratorMaps.clear()
        numeratorElements.clear()
        commonForNumerator.clear()
        denominatorMaps.clear()
        denominatorOperators.clear()
        denominatorElements.clear()
        commonForDenominator.clear()
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
                            is Function -> if (element.getFunction().isNotEmpty()) return false
                        }
                    }
                }
            }
        }
        return false
    }

    private fun isFraction(input: MutableList<Any>): Boolean {
        if (input.size != 1) return false

        if (input.last() is Fraction) {
            return true
        }

        return false
    }

    init {
        setFraction()
    }

    fun setFraction() {
        updateFractionNumerator()
        if (denominator != null) updateFractionDenominator()

        setMultiplicative(getMultiplicativeNumerator(),  getMultiplicativeDenominator())

        shortenEveryFractionNumerator()
        if (denominator != null) shortenEveryFractionDenominator()

        rebuildFractionNumerator()
        if (denominator != null) rebuildFractionDenominator()

        shortenNumeratorWithDenominator()

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
                else -> {
                    if (brackets == 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun equationHasOperators(equation: MutableList<Any>): Boolean {
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

    private fun sortFraction(input: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<Any>()
        val fragment = mutableListOf<Any>()
        var sorted: MutableList<Any>

        for (element in input) {
            if (element is Fraction) {
                fragment.addAll(sortFraction(element.getFraction()))
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

    private fun getInsideOfFraction(input: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<Any>()

        for (element in input) {
            if (element is Fraction) {
                output.addAll(element.getFraction())
            }
            else {
                when (element) {
                    is UnknownEntity -> output.addAll(element.getOriginal())
                    is Function -> output.addAll(element.getFunction())
                    else -> {
                        output.add(element)
                    }
                }
            }
        }

        return output
    }

    fun getFraction(onlyMultiplication: Boolean = false): MutableList<Any> {
        if (onlyMultiplication) {
            return getInsideOfFraction(multiplicative!!.getFraction())
        }

        val fraction = mutableListOf<Any>()

        if (count != 0.0) {
            if (powerTo == 0.0) {
                fraction.add(1.0)
            }
            else {
                val content = numerator

                if (multiplicative != null) {
                    fraction.addAll(sortFraction(multiplicative!!.numerator))
                }

                if (content.isNotEmpty() || multiplicative != null || denominator != null) {
                    val addBrackets = equationNotInBrackets(content) && equationHasOperators(content)

                    if (addBrackets) {
                        fraction.add('(')
                    }
                    val denominatorExists = denominator != null && denominator!!.isNotEmpty()

                    if (denominatorExists && content.isNotEmpty()) {
                        fraction.add('(')
                    }

                    fraction.addAll(sortFraction(content))

                    if (denominatorExists) {
                        if (content.isNotEmpty()) {
                            fraction.add(')')
                        }
                        fraction.add('/')
                        fraction.add('(')

                        if (multiplicative != null && multiplicative!!.denominator != null) {
                            fraction.addAll(sortFraction(multiplicative!!.denominator!!))
                        }

                        fraction.addAll(sortFraction(denominator!!))

                        fraction.add(')')
                    }

                    if (addBrackets) {
                        fraction.add(')')
                    }
                }

                if (powerTo != 1.0) {
                    fraction.add('^')
                    fraction.add(powerTo)
                }
            }
        }

        return getInsideOfFraction(fraction)
    }

    private fun getKey(onlyMultiplication: Boolean = false): MutableList<Any> {
        return getInsideOfFraction(getFraction(onlyMultiplication)).sortedWith(
            compareBy<Any> {
                if (it is Char) it.code else 0
            }.thenBy {
                if (it is Double) it else 1.0
            }.thenBy {
                it::class.simpleName
            }).toMutableList()
    }

    private fun withoutMultiplication(): Fraction {
        multiplicative = null
        return this
    }

    private fun hasUnknownEntity(input: MutableList<Any>): UnknownEntity? {
        if (input.size > 1) return null

        for (element in input) {
            if (element is UnknownEntity) {
                return element
            }
        }
        return null
    }

    private fun hasFraction(input: MutableList<Any>): Fraction? {
        if (input.size > 1) return null

        for (element in input) {
            if (element is Fraction) {
                if (hasUnknownEntity(element.numerator) != null) {
                    if (denominator != null) {
                        if (hasUnknownEntity(element.denominator!!) != null) {
                            return element
                        }
                    }
                    else {
                        return element
                    }
                }
            }
        }
        return null
    }

    private fun updateFractionNumerator() {
        var numeratorMap = hashMapOf<MutableList<Any>, Double>()
        var numeratorElement = mutableListOf<Any>()
        var fraction = Fraction(mutableListOf())
        var entity = UnknownEntity()

        var sign = '0'
        for (element in numerator) {
            when (element) {
                is UnknownEntity -> {
                    if (sign == '/') {
                        entity /= element
                    }
                    else {
                        entity *= element
                    }
                }
                is Function -> {
                    if (element.getFunction().isNotEmpty()) {
                        if (numeratorMap[element.getFunction()] == null) {
                            numeratorElement.add(element)
                        }

                        if (sign == '/') {
                            numeratorMap[element.getFunction()] = numeratorMap.getOrDefault(element.getFunction(), 0.0) - 1
                        }
                        else {
                            numeratorMap[element.getFunction()] = numeratorMap.getOrDefault(element.getFunction(), 0.0) + 1
                        }
                    }
                }
                is Fraction -> {
                    if (element.multiplicative != null) {
                        val multiplicativeNumeratorUnknown = hasUnknownEntity(element.multiplicative!!.numerator)
                        val multiplicativeNumeratorFraction = hasFraction(element.multiplicative!!.numerator)

                        var denominatorIsComplex = false
                        if (element.multiplicative!!.denominator != null) {
                            val multiplicativeDenominatorUnknown = hasUnknownEntity(element.multiplicative!!.denominator!!)
                            val multiplicativeDenominatorFraction = hasFraction(element.multiplicative!!.denominator!!)

                            if (multiplicativeDenominatorUnknown != null) {
                                entity /= multiplicativeDenominatorUnknown
                            }
                            else if (multiplicativeDenominatorFraction != null) {
                                fraction *= multiplicativeDenominatorFraction
                            }
                            else {
                                denominatorIsComplex = true
                            }
                        }

                        if (multiplicativeNumeratorUnknown != null && !denominatorIsComplex) {
                            entity *= multiplicativeNumeratorUnknown
                        }
                        else if (multiplicativeNumeratorFraction != null) {
                            fraction *= multiplicativeNumeratorFraction
                        }
                        else{
                            if (sign == '/') {
                                fraction /= element.multiplicative!!
                            }
                            else {
                                fraction *= element.multiplicative!!
                            }
                        }
                    }

                    element.withoutMultiplication()

                    val multiplicativeNumeratorUnknown = hasUnknownEntity(element.numerator)
                    val multiplicativeNumeratorFraction = hasFraction(element.numerator)

                    var denominatorIsComplex = false
                    if (element.denominator != null) {
                        val multiplicativeDenominatorUnknown = hasUnknownEntity(element.denominator!!)
                        val multiplicativeDenominatorFraction = hasFraction(element.denominator!!)

                        if (multiplicativeDenominatorUnknown != null) {
                            entity *= multiplicativeDenominatorUnknown
                        }
                        else if (multiplicativeDenominatorFraction != null) {
                           fraction *= multiplicativeDenominatorFraction
                        }
                        else {
                            denominatorIsComplex = true
                        }
                    }

                    if (multiplicativeNumeratorUnknown != null && !denominatorIsComplex) {
                        entity *= multiplicativeNumeratorUnknown
                    }
                    else if (multiplicativeNumeratorFraction != null) {
                        fraction *= multiplicativeNumeratorFraction
                    }
                    else{
                        if (sign == '/') {
                            fraction /= element
                        }
                        else {
                            fraction *= element
                        }
                    }
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    numeratorMap[entity.getOriginal(value = true)] = entity.multiplier!!
                                }
                                else {
                                    numeratorMap[entity.getOriginal(withoutMultiplier = true)] = entity.multiplier!!
                                }

                                numeratorElement.add(entity.copy())
                                entity = UnknownEntity()
                            }
                            if (!fraction.isEmpty()) {
                                numeratorMap[fraction.getKey()] = numeratorMap.getOrDefault(fraction.getKey(), 0.0) + 1

                                numeratorElement.add(fraction.copy())
                                fraction = Fraction(mutableListOf())
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
                numeratorMap[entity.getOriginal(value = true)] = entity.multiplier!!
            }
            else {
                numeratorMap[entity.getOriginal(withoutMultiplier = true)] = entity.multiplier!!
            }
            numeratorElement.add(entity)
        }
        if (!fraction.isEmpty()) {
            numeratorMap[fraction.getKey()] = numeratorMap.getOrDefault(fraction.getKey(), 0.0) + 1

            numeratorElement.add(fraction)
        }

        numeratorMaps.add(numeratorMap)
        numeratorElements.add(numeratorElement)

        println("numeratorElements")
        println(numeratorElements)
    }

    private fun updateFractionDenominator() {
        var denominatorMap = hashMapOf<MutableList<Any>, Double>()
        var denominatorElement = mutableListOf<Any>()
        var fraction = Fraction(mutableListOf())
        var entity = UnknownEntity()

        var sign = '0'
        for (element in denominator!!) {
            when (element) {
                is UnknownEntity -> {
                    if (sign == '/') {
                        entity /= element
                    }
                    else {
                        entity *= element
                    }
                }
                is Function -> {
                    if (element.getFunction().isNotEmpty()) {
                        if (denominatorMap[element.getFunction()] == null) {
                            denominatorElement.add(element)
                        }

                        if (sign == '/') {
                            denominatorMap[element.getFunction()] = denominatorMap.getOrDefault(element.getFunction(), 0.0) - 1
                        }
                        else {
                            denominatorMap[element.getFunction()] = denominatorMap.getOrDefault(element.getFunction(), 0.0) + 1
                        }
                    }
                }
                is Fraction -> {
                    if (element.multiplicative != null) {
                        val multiplicativeNumeratorUnknown = hasUnknownEntity(element.multiplicative!!.numerator)
                        val multiplicativeNumeratorFraction = hasFraction(element.multiplicative!!.numerator)

                        var denominatorIsComplex = false
                        if (element.multiplicative!!.denominator != null) {
                            val multiplicativeDenominatorUnknown = hasUnknownEntity(element.multiplicative!!.denominator!!)
                            val multiplicativeDenominatorFraction = hasFraction(element.multiplicative!!.denominator!!)

                            if (multiplicativeDenominatorUnknown != null) {
                                entity *= multiplicativeDenominatorUnknown
                            }
                            else if (multiplicativeDenominatorFraction != null) {
                                fraction *= multiplicativeDenominatorFraction
                            }
                            else {
                                denominatorIsComplex = true
                            }
                        }

                        if (multiplicativeNumeratorUnknown != null && !denominatorIsComplex) {
                            entity *= multiplicativeNumeratorUnknown
                        }
                        else if (multiplicativeNumeratorFraction != null) {
                            fraction *= multiplicativeNumeratorFraction
                        }
                        else{
                            if (sign == '/') {
                                fraction /= element.multiplicative!!
                            }
                            else {
                                fraction *= element.multiplicative!!
                            }
                        }
                    }

                    element.withoutMultiplication()

                    val multiplicativeNumeratorUnknown = hasUnknownEntity(element.numerator)
                    val multiplicativeNumeratorFraction = hasFraction(element.numerator)

                    var denominatorIsComplex = false
                    if (element.denominator != null) {
                        val multiplicativeDenominatorUnknown = hasUnknownEntity(element.denominator!!)
                        val multiplicativeDenominatorFraction = hasFraction(element.denominator!!)

                        if (multiplicativeDenominatorUnknown != null) {
                            entity /= multiplicativeDenominatorUnknown
                        }
                        else if (multiplicativeDenominatorFraction != null) {
                            fraction *= multiplicativeDenominatorFraction
                        }
                        else {
                            denominatorIsComplex = true
                        }
                    }

                    if (multiplicativeNumeratorUnknown != null && !denominatorIsComplex) {
                        entity *= multiplicativeNumeratorUnknown
                    }
                    else if (multiplicativeNumeratorFraction != null) {
                        fraction *= multiplicativeNumeratorFraction
                    }
                    else{
                        if (sign == '/') {
                            fraction /= element
                        }
                        else {
                            fraction *= element
                        }
                    }
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    denominatorMap[entity.getOriginal(value = true)] = entity.multiplier!!
                                }
                                else {
                                    denominatorMap[entity.getOriginal(withoutMultiplier = true)] = entity.multiplier!!
                                }

                                denominatorElement.add(entity.copy())
                                entity = UnknownEntity()
                            }
                            if (!fraction.isEmpty()) {
                                denominatorMap[fraction.getKey()] = denominatorMap.getOrDefault(fraction.getKey(), 0.0) + 1

                                denominatorElement.add(fraction.copy())
                                fraction = Fraction(mutableListOf())
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
                denominatorMap[entity.getOriginal(value = true)] = entity.multiplier!!
            }
            else {
                denominatorMap[entity.getOriginal(withoutMultiplier = true)] = entity.multiplier!!
            }
            denominatorElement.add(entity)
        }
        if (!fraction.isEmpty()) {
            denominatorMap[fraction.getKey()] = denominatorMap.getOrDefault(fraction.getKey(), 0.0) + 1

            denominatorElement.add(fraction)
        }

        denominatorMaps.add(denominatorMap)
        denominatorElements.add(denominatorElement)
    }

    private fun itIsUnknown(input: MutableList<Any>): Boolean {
        if (input.size == 3) {
            if (input[0] == 'x' || input[0] == 'y' || input[0] == 'z') {
                if (input[1] == '^') {
                    if (input[2] is Double) {
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
                                val newPower = min(k.last() as Double, key.last() as Double)
                                val newKey = mutableListOf(key.first(), key[1], newPower)
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
                    else if (map[key] == null) {
                        continue
                    }
                    else {
                        if (startCommonForNumerator[key] != null && map[key]!! > value) {
                            commonForNumerator[key] = map[key]!!
                        }
                    }
                }
            }

            for (i in toRemove) {
                commonForNumerator.remove(i)
            }
        }

        // Remove commons from fraction numerator and denominator
        for (map in numeratorMaps) {
            for ((original, _) in map) {
                for ((common, count) in commonForNumerator) {
                    if (original == common) {
                        map[original] = map[original]!! - count
                    }
                }
            }
        }

        // Make a fraction out of found commons REPAIR DENOMINATOR
        val numeratorOutput = mutableListOf<Any>()
        val commonsToFind = hashMapOf<MutableList<Any>, Double>()
        var entity: UnknownEntity? = null

        for ((key, v) in commonForNumerator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entity = UnknownEntity()
                entity.multiplier = commonsToFind[key]
                entity.variable = key.first() as Char
                entity.powerTo = key.last() as Double
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
                                        key.last() as Double
                                    )

                                if ((numeratorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    (numeratorElements[index][element] as UnknownEntity).variable = null
                                    (numeratorElements[index][element] as UnknownEntity).powerTo = null
                                }
                            }
                        }

                        is Function -> {
                            if (key == (numeratorElements[index][element] as Function).getFunction()) {
                                (numeratorElements[index][element] as Function).powerTo = v
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
                                (numeratorElements[index][element] as Fraction).powerTo = v
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

            numeratorOutput.add(Fraction(mutableListOf(UnknownEntity(gcd.toDouble(), entity.variable, entity.powerTo)), mutableListOf(UnknownEntity(decimalPoint.toDouble()))))

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
                                val newPower = min(k.last() as Double, key.last() as Double)
                                val newKey = mutableListOf(key.first(), key[1], newPower)
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
                    else if (map[key] == null) {
                        continue
                    }
                    else {
                        if (startCommonForDenominator[key] != null && map[key]!! > value) {
                            commonForDenominator[key] = map[key]!!
                        }
                    }
                }
            }

            for (i in toRemove) {
                commonForDenominator.remove(i)
            }
        }

        // Remove commons from fraction numerator and denominator
        for (map in denominatorMaps) {
            for ((original, _) in map) {
                for ((common, count) in commonForDenominator) {
                    if (original == common) {
                        map[original] = map[original]!! - count
                    }
                }
            }
        }

        // Make a fraction out of found commons REPAIR DENOMINATOR
        val denominatorOutput = mutableListOf<Any>()
        val commonsToFind = hashMapOf<MutableList<Any>, Double>()
        var entity: UnknownEntity? = null

        for ((key, v) in commonForDenominator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entity = UnknownEntity()
                entity.multiplier = commonsToFind[key]
                entity.variable = key.first() as Char
                entity.powerTo = key.last() as Double
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
                                        key.last() as Double
                                    )

                                if ((denominatorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    (denominatorElements[index][element] as UnknownEntity).variable = null
                                    (denominatorElements[index][element] as UnknownEntity).powerTo = null
                                }
                            }
                        }

                        is Function -> {
                            if (key == (denominatorElements[index][element] as Function).getFunction()) {
                                (denominatorElements[index][element] as Function).powerTo = v
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
                                (denominatorElements[index][element] as Fraction).powerTo = v
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

            denominatorOutput.add(Fraction(mutableListOf(UnknownEntity(gcd.toDouble(), entity.variable, entity.powerTo)), mutableListOf(UnknownEntity(decimalPoint.toDouble()))))

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

        if (multiplicative != null && numerator.isEmpty()) {
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

    private fun rebuildFractionNumerator() {
        val outputNumerator = mutableListOf<Any>()
        var operatorIndex = 0
        for (index in 0..< numeratorMaps.size) {
            for ((key, v) in numeratorMaps[index]) {
                for (entity in numeratorElements[index]) {
                    when (entity) {
                        is UnknownEntity -> {
                            if (numeratorOperators.isEmpty() && entity.onlyNumber() && entity.multiplier == 1.0) {
                                entity.clear()
                            }
                        }
                        is Function -> {
                            if (key == entity.getFunction()) {
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
            val append = numeratorElements[index]

            if (append.isEmpty() && numeratorOperators.isNotEmpty()) {
                outputNumerator.add(UnknownEntity(1.0))
            }
            else {
                outputNumerator.addAll(append)
            }

            if (operatorIndex < numeratorOperators.size) {
                outputNumerator.add(numeratorOperators[operatorIndex])
                operatorIndex++
            }
        }
        numerator = outputNumerator
    }

    private fun rebuildFractionDenominator() {
        val outputDenominator = mutableListOf<Any>()
        var operatorIndex = 0
        for (index in 0..< denominatorMaps.size) {
            for ((key, v) in denominatorMaps[index]) {
                for (entity in denominatorElements[index]) {
                    when (entity) {
                        is UnknownEntity -> {
                            if (denominatorOperators.isEmpty() && entity.onlyNumber() && entity.multiplier == 1.0) {
                                entity.clear()
                            }
                        }
                        is Function -> {
                            if (key == entity.getFunction()) {
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
            val append = denominatorElements[index]

            if (append.isEmpty() && denominatorOperators.isNotEmpty()) {
                outputDenominator.add(UnknownEntity(1.0))
            }
            else {
                outputDenominator.addAll(append)
            }

            if (operatorIndex < denominatorOperators.size) {
                outputDenominator.add(denominatorOperators[operatorIndex])
                operatorIndex++
            }
        }
        denominator = outputDenominator
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

    operator fun times(other: Fraction): Fraction {
        if (this.numerator.isEmpty()) {
            return other
        }

        val newNumerator = mutableListOf<Any>(Fraction(this.numerator))
        newNumerator.add(Fraction(other.numerator))

        var newDenominator: MutableList<Any>? = null
        if (this.denominator != null && other.denominator != null) {
            newDenominator = mutableListOf(Fraction(this.denominator!!))
            newDenominator.add(Fraction(other.denominator!!))
        }
        else if (this.denominator != null) {
            newNumerator.add(Fraction(this.denominator!!))
            newDenominator = mutableListOf(Fraction(this.denominator!!))
        }
        else if (other.denominator != null) {
            newNumerator.add(Fraction(other.denominator!!))
            newDenominator = mutableListOf(Fraction(other.denominator!!))
        }

        val result = Fraction(newNumerator, newDenominator)
        println("times")
        println(result)
        return result
    }

    operator fun div(other: Fraction): Fraction {
        if (this.numerator.isEmpty()) {
            return other
        }

        val newNumerator = mutableListOf<Any>(Fraction(this.numerator))
        val newDenominator: MutableList<Any>?

        if (this.denominator != null && other.denominator != null) {
            newNumerator.add(Fraction(other.denominator!!))

            newDenominator = mutableListOf(Fraction(other.denominator!!))
            newDenominator.add(Fraction(this.numerator))
        }
        else if (this.denominator != null) {
            newDenominator = mutableListOf(Fraction(this.denominator!!))
            newDenominator.add(Fraction(other.numerator))
        }
        else if (other.denominator != null) {
            newNumerator.add(Fraction(other.denominator!!))

            newDenominator = mutableListOf(Fraction(other.numerator))
        }
        else {
            newDenominator = mutableListOf(Fraction(other.numerator))
        }

        val result = Fraction(newNumerator, newDenominator)
        println("division")
        println(result)
        return result
    }
}