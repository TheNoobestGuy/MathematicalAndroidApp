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

    fun setFraction() {
        updateFraction()
        getMultiplicative()
        shortenEveryFraction()
        rebuildFraction()
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
                    fraction.addAll(multiplicative!!.getFraction())
                }

                if (content.isNotEmpty()) {
                    val addBrackets = equationNotInBrackets(content) && equationHasOperators(content)

                    if (addBrackets) {
                        fraction.add('(')
                    }

                    fraction.addAll(sortFraction(content))
                    if (denominator != null) {
                        fraction.add('/')
                        fraction.addAll(denominator!!)
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

    private fun updateFraction() {
        var numeratorMap = hashMapOf<MutableList<Any>, Double>()
        var numeratorElement = mutableListOf<Any>()
        var entity = UnknownEntity()

        var sign = '0'
        for (element in numerator) {
            when (element) {
                is UnknownEntity -> {
                    entity *= element
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
                        if (element.multiplicative!!.numerator.size == 1 && element.multiplicative!!.numerator.last() is UnknownEntity) {
                            entity *= (element.multiplicative!!.numerator.last() as UnknownEntity)
                        }
                        else{
                            numeratorElement.add(element.multiplicative!!)

                            if (sign == '/') {
                                numeratorMap[element.multiplicative!!.getKey()] = numeratorMap.getOrDefault(element.multiplicative!!.getKey(), 0.0) - 1
                            }
                            else {
                                numeratorMap[element.multiplicative!!.getKey()] = numeratorMap.getOrDefault(element.multiplicative!!.getKey(), 0.0) + 1
                            }
                        }
                    }

                    element.withoutMultiplication()

                    if (element.numerator.size == 1 && element.numerator.last() is UnknownEntity) {
                        entity *= (element.numerator.last() as UnknownEntity)
                    }
                    else{
                        if (numeratorMap[element.getKey()] == null) {
                            numeratorElement.add(element)
                        }

                        if (sign == '/') {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) - 1
                        }
                        else {
                            numeratorMap[element.getKey()] = numeratorMap.getOrDefault(element.getKey(), 0.0) + 1
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
            numeratorElement.add(entity.copy())
        }
        numeratorMaps.add(numeratorMap)
        numeratorElements.add(numeratorElement)
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

    private fun getMultiplicative() {
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

        if (numeratorOutput.isNotEmpty()) {
            multiplicative = if (numeratorOutput.size == 1 && numeratorOutput.last() is Fraction) {
                numeratorOutput.last() as Fraction
            } else {
                Fraction(numeratorOutput)
            }
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

    private fun shortenEveryFraction() {
        // Shorten fractions
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

    private fun rebuildFraction() {
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

    operator fun times(other: Fraction): Fraction {
        val newNumerator = mutableListOf<Any>(Fraction(this.numerator))
        newNumerator.add('×')
        newNumerator.add(Fraction(other.numerator))

        var newDenominator: MutableList<Any>? = null
        if (this.denominator != null && other.denominator != null) {
            newDenominator = mutableListOf(Fraction(this.denominator!!))
            newDenominator.add('×')
            newDenominator.add(Fraction(other.denominator!!))
        }
        else if (this.denominator != null) {
            newNumerator.add('×')
            newNumerator.add(Fraction(this.denominator!!))
            newDenominator = mutableListOf(Fraction(this.denominator!!))
        }
        else if (other.denominator != null) {
            newNumerator.add('×')
            newNumerator.add(Fraction(other.denominator!!))
            newDenominator = mutableListOf(Fraction(other.denominator!!))
        }

        val result = if (newDenominator != null) {
            Fraction(newNumerator, newDenominator)
        } else {
            Fraction(newNumerator)
        }
        result.setFraction()

        if (result.isEmpty()) {
            return Fraction(mutableListOf(UnknownEntity(1.0)))
        }
        return result
    }

    operator fun div(other: Fraction): Fraction {
        val newNumerator = mutableListOf<Any>(Fraction(this.numerator))
        var newDenominator: MutableList<Any>? = null

        if (this.denominator != null && other.denominator != null) {
            newNumerator.add('×')
            newNumerator.add(Fraction(other.denominator!!))

            newDenominator = mutableListOf(Fraction(other.denominator!!))
            newDenominator.add('×')
            newDenominator.add(Fraction(this.numerator))
        }
        else if (this.denominator != null) {
            newDenominator = mutableListOf(Fraction(this.denominator!!))
            newDenominator.add('×')
            newDenominator.add(Fraction(other.numerator))
        }
        else if (other.denominator != null) {
            newNumerator.add('×')
            newNumerator.add(Fraction(other.denominator!!))

            newDenominator = mutableListOf(Fraction(other.numerator))
        }

        val result = if (newDenominator != null) {
            Fraction(newNumerator, newDenominator)
        } else {
            Fraction(newNumerator)
        }
        result.setFraction()

        if (result.isEmpty()) {
            return Fraction(mutableListOf(UnknownEntity(1.0)))
        }
        return result
    }
}