package com.example.mathmaster.customviews

import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

data class Fraction(var numerator: MutableList<Any> = mutableListOf(), var denominator: MutableList<Any>? = null, var powerTo: Double = 1.0, var count: UnknownEntity? = null, var multiplicative: Fraction? = null) {
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

    private fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
    }

    private fun convertToFraction(input: Double): Any {
        val fraction = Fraction()

        if (input == Math.PI) {
            return UnknownEntity(1.0, 'π', 1.0)
        }
        else if (input == Math.E) {
            return UnknownEntity(1.0, 'e', 1.0)
        }

        var divided = round( input * 1000 ) / 1000
        var divider = 1.0
        while (hasDecimal(divided)) {
            divided *= 10.0
            divider *= 10.0
        }

        val gcd = gcd(divided.toInt(), divider.toInt())

        fraction.numerator = mutableListOf(UnknownEntity(divided / gcd))
        fraction.denominator = mutableListOf(UnknownEntity(divider / gcd))

        return fraction
    }

    fun isCalculable(): MutableList<Any> {
        val list = mutableListOf<Any>('(', 1.0)
        var i = 0
        var brackets = 0
        while (i < numerator.size) {
            if (numerator[i] is Fraction) {
                val buffer = (numerator[i] as Fraction).isCalculable()
                if (buffer.isNotEmpty()) {
                    if (list.last() !is Char) {
                        list.add('×')
                    }
                    list.addAll(buffer)
                }
                else {
                    return mutableListOf()
                }
            }
            else if (numerator[i] is UnknownEntity && (numerator[i] as UnknownEntity).onlyNumber()) {
                if (list.last() !is Char) {
                    list.add('×')
                }
                list.addAll((numerator[i] as UnknownEntity).getOriginal())
            }
            else if (numerator[i] is Char) {
                list.add(numerator[i])
                list.add('(')
                brackets++
            }
            else {
                return mutableListOf()
            }
            i++
        }
        i = 0
        while (i < brackets) {
            list.add(')')
            i++
        }
        list.add(')')

        if (denominator != null && denominator!!.isNotEmpty()) {
            i = 0
            brackets = 0
            list.add('/')
            list.add('(')
            while (i < denominator!!.size) {
                if (denominator!![i] is Fraction) {
                    val buffer = (denominator!![i] as Fraction).isCalculable()
                    if (buffer.isNotEmpty()) {
                        if (list.last() !is Char) {
                            list.add('×')
                        }
                        list.addAll(buffer)
                    }
                    else {
                        return mutableListOf()
                    }
                }
                else if (denominator!![i] is UnknownEntity && (denominator!![i] as UnknownEntity).onlyNumber()) {
                    if (list.last() !is Char) {
                        list.add('×')
                    }
                    list.addAll((denominator!![i] as UnknownEntity).getOriginal())
                }
                else if (denominator!![i] is Char) {
                    list.add(denominator!![i])
                    list.add('(')
                    brackets++
                }
                else {
                    return mutableListOf()
                }
                i++
            }
            i = 0
            while (i < brackets) {
                list.add(')')
                i++
            }
            list.add(')')
        }

        if (list.size == 3) {
            return mutableListOf()
        }
        return list
    }

    private fun makeCopy() {
        numerator = numerator.map {
            when (it) {
                is UnknownEntity -> it.copy()
                is Function -> it.copy()
                is Fraction -> it.copy()
                else -> it
            }
        }.toMutableList()

        if (denominator != null) {
            denominator = denominator!!.map {
                when (it) {
                    is UnknownEntity -> it.copy()
                    is Function -> it.copy()
                    is Fraction -> it.copy()
                    else -> it
                }
            }.toMutableList()
        }
    }

    init {
        if (denominator != null && denominator!!.isEmpty()) {
            denominator = null
        }

        makeCopy()

        shortenEverything()
        sortEverything()
        cleanFraction()
    }

    private fun isNotEmpty(): Boolean {
        if (numerator.isEmpty()) {
            return if (denominator == null) {
                multiplicative != null
            } else {
                true
            }
        }
        return true
    }

    private fun clearMaps() {
        numeratorMaps.clear()
        numeratorOperators.clear()
        numeratorElements.clear()
        commonForNumerator.clear()
        denominatorMaps.clear()
        denominatorOperators.clear()
        denominatorElements.clear()
        commonForDenominator.clear()
    }

    private fun getKeys(switch: Boolean = false): MutableList<Triple<MutableList<Any>, Double, Any>> {
        val list = mutableListOf<Triple<MutableList<Any>, Double, Any>>()

        if (equationHasOperators(this.numerator) || (this.denominator != null && equationHasOperators(this.denominator!!)) || this.powerTo != 1.0) {
            if (switch) {
                list.add(Triple(this.getKey(), -this.powerTo, this))
            }
            else {
                list.add(Triple(this.getKey(), this.powerTo, this))
            }
        }
        else {
            for (element in numerator) {
                when (element) {
                    is UnknownEntity -> {
                        if (element.multiplier != null) {
                            if (element.onlyNumber()) {
                                list.add(Triple(element.getKey(value = true), element.multiplier!!, element))
                            }
                            else {
                                list.add(Triple(element.getKey(), element.multiplier!!, element))
                            }
                        }
                        else {
                            list.add(Triple(element.getKey(value = true), 1.0, element))
                        }
                    }
                    is Function -> {
                        if (switch) {
                            list.add(Triple(element.getKey(), -element.powerTo, element))
                        }
                        else {
                            list.add(Triple(element.getKey(), element.powerTo, element))
                        }
                    }
                    is Fraction -> {
                        if (equationHasOperators(element.numerator) || (element.denominator != null && equationHasOperators(element.denominator!!))) {
                            if (switch) {
                                list.add(Triple(element.getKey(), -element.powerTo, element))
                            }
                            else {
                                list.add(Triple(element.getKey(), element.powerTo, element))
                            }
                        }
                        else {
                            list.addAll(element.getKeys(switch))
                        }
                    }
                }
            }

            if (denominator != null) {
                for (element in denominator!!) {
                    when (element) {
                        is UnknownEntity -> {
                            if (element.multiplier != null) {
                                if (element.onlyNumber()) {
                                    list.add(Triple(element.getKey(value = true), element.multiplier!!, element))
                                }
                                else {
                                    list.add(Triple(element.getKey(), element.multiplier!!, element))
                                }
                            }
                            else {
                                list.add(Triple(element.getKey(value = true), 1.0, element))
                            }
                        }
                        is Function -> {
                            if (switch) {
                                list.add(Triple(element.getKey(), element.powerTo, element))
                            }
                            else {
                                list.add(Triple(element.getKey(), -element.powerTo, element))
                            }
                        }
                        is Fraction -> {
                            list.addAll(element.getKeys(!switch))
                        }
                    }
                }
            }
        }

        return list
    }

    private fun getOutMultiplication() {
        if (multiplicative != null) {
            if (multiplicative!!.isCount() != null) {
                this.count = multiplicative!!.isCount()!!
            }
            else if (!multiplicative!!.isOne()) {
                if (isOne(numerator) && (denominator == null || denominator!!.isEmpty())) numerator.clear()

                if (numerator.isNotEmpty()) {
                    if (equationHasOperators(numerator)) {
                        if (equationHasOperators(multiplicative!!.numerator)) {
                            numerator = mutableListOf(Fraction(numerator), Fraction(multiplicative!!.numerator))
                        }
                        else {
                            numerator = mutableListOf(Fraction(numerator))
                            numerator.addAll(multiplicative!!.numerator)
                        }
                    }
                    else {
                        if (equationHasOperators(multiplicative!!.numerator)) {
                            numerator.add(Fraction(multiplicative!!.numerator))
                        }
                        else {
                            numerator.addAll(multiplicative!!.numerator)
                        }
                    }
                }
                else {
                    numerator = multiplicative!!.numerator
                }

                if (denominator != null) {
                    if (multiplicative!!.denominator != null) {
                        if (equationHasOperators(denominator!!)) {
                            if (equationHasOperators(multiplicative!!.denominator!!)) {
                                denominator = mutableListOf(Fraction(denominator!!), Fraction(multiplicative!!.denominator!!))
                            }
                            else {
                                denominator = mutableListOf(Fraction(denominator!!))
                                denominator!!.addAll(multiplicative!!.denominator!!)
                            }
                        }
                        else {
                            if (equationHasOperators(multiplicative!!.denominator!!)) {
                                denominator!!.add(Fraction(multiplicative!!.denominator!!))
                            }
                            else {
                                denominator!!.addAll(multiplicative!!.denominator!!)
                            }
                        }
                    }
                }
                else {
                    denominator = multiplicative!!.denominator
                }
            }
        }

        multiplicative = null
    }

    private fun getOutPowerTo() {
        if (this.powerTo > 1.0) {
            this.getOutCountRecursive()

            val numeratorExp = this.numerator.toMutableList()
            var denominatorExp: MutableList<Any>? = null
            if(this.denominator != null) {
                denominatorExp = this.denominator!!.toMutableList()
            }
            var i = 1.0
            val limit = this.powerTo
            this.powerTo = 1.0
            while  (i < limit) {
                this.multiplyByItself(numeratorExp, denominatorExp)
                i += 1.0
            }
        }
        else if (this.powerTo < 0.0){
            this.getOutCountRecursive()

            val numeratorExp = this.numerator.toMutableList()
            var denominatorExp: MutableList<Any>? = null
            if(this.denominator != null) {
                denominatorExp = this.denominator!!.toMutableList()
            }

            if (denominatorExp == null) {
                this.numerator.clear()
            }
            else {
                this.numerator = denominatorExp
            }
            this.denominator = numeratorExp

            this.powerTo += 1.0

            var i = this.powerTo
            this.powerTo = 1.0
            while  (i < 0) {
                this.divideByItself(numeratorExp, denominatorExp)
                i += 1.0
            }
        }
    }

    private fun getOutCount() {
        if (count != null && !count!!.isOne()) {
            numerator = mutableListOf(Fraction(numerator))
            numerator.add(count!!)
        }
        count = null
    }

    private fun getOutCountRecursive() {
        this.getOutCount()

        for (element in numerator) {
            if (element is Fraction) {
                element.getOutCount()
                element.getOutCountRecursive()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    element.getOutCount()
                    element.getOutCountRecursive()
                }
            }
        }
    }

    fun isEmpty(): Boolean {
        if (numerator.isEmpty()) {
            if (denominator != null && denominator!!.isNotEmpty()) {
                return false
            }
            else if (multiplicative == null) {
                return true
            }
            else {
                if (multiplicative!!.numerator.isEmpty() && (multiplicative!!.denominator == null || multiplicative!!.denominator!!.isEmpty())) {
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

                    if (multiplicative!!.denominator != null) {
                        for (element in multiplicative!!.denominator!!) {
                            when (element) {
                                is Fraction -> if (!element.isEmpty()) return false
                                is UnknownEntity -> if (!element.isEmpty()) return false
                                is Function -> if (element.isNotEmpty()) return false
                            }
                        }
                    }
                }
            }
        }
        else {
            return false
        }
        return true
    }

    private fun reduceFraction() {
        if (denominator != null && denominator!!.isNotEmpty() && !equationHasOperators(numerator) && !equationHasOperators(denominator!!)) {
            val list = mutableListOf<MutableList<Any>>()
            val functionsMap = hashMapOf<Any, Double>()

            var dividedOperator = '×'
            for (divided in numerator) {
                if (divided is Char) {
                    dividedOperator = divided
                    continue
                }
                if (dividedOperator == '/') {
                    dividedOperator = '×'
                    continue
                }

                val entry = mutableListOf<Any>()
                for (divider in denominator!!) {
                    if (divider is Function) {
                        if (divided is Function) {
                            if (divider.getKey() == divided.getKey()) {
                                if (divider.powerTo > divided.powerTo) {
                                    if (functionsMap[divided] != null && functionsMap[divided]!! > divided.powerTo) {
                                        functionsMap[divided] = divided.powerTo
                                    }
                                    else if (functionsMap[divided] == null) {
                                        functionsMap[divided] = divided.powerTo
                                    }
                                }
                                else {
                                    if (functionsMap[divided] != null && functionsMap[divided]!! > divider.powerTo) {
                                        functionsMap[divided] = divider.powerTo
                                    }
                                    else if (functionsMap[divided] == null) {
                                        functionsMap[divided] = divider.powerTo
                                    }
                                }
                            }
                        }
                    }
                    else if (divider is Fraction) {
                        if (divided is Fraction) {
                            val dividedKeys = divided.getKeys()
                            val dividerKeys = divider.getKeys()

                            for (dividedKey in dividedKeys) {
                                for (dividerKey in dividerKeys) {
                                    when (dividedKey.third) {
                                        is UnknownEntity -> {
                                            if (dividerKey.third is UnknownEntity) {
                                                if (dividedKey.third == dividerKey.third) {
                                                    entry.add(divider)
                                                    break
                                                }
                                            }
                                        }
                                        is Function -> {
                                            when (dividerKey.third) {
                                                is Function -> {
                                                    if (dividedKey.third == dividerKey.third) {
                                                        if ((dividedKey.third as Function).powerTo > (dividerKey.third as Function).powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividedKey.third as Function).powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividerKey.third as Function).powerTo) {
                                                                functionsMap[divided] = (dividerKey.third as Function).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividerKey.third as Function).powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                                is Fraction -> {
                                                    if (dividedKey.third == dividerKey.third) {
                                                        if ((dividedKey.third as Function).powerTo > (dividerKey.third as Fraction).powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividedKey.third as Function).powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividerKey.third as Fraction).powerTo) {
                                                                functionsMap[divided] = (dividerKey.third as Fraction).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividerKey.third as Fraction).powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                    else if (dividedKey.third == divider) {
                                                        if ((dividedKey.third as Function).powerTo > divider.powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > divider.powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Function).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > divider.powerTo) {
                                                                functionsMap[divided] = divider.powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = divider.powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                        is Fraction -> {
                                            when (dividerKey.third) {
                                                is Function -> {
                                                    if (dividedKey.third == dividerKey.third) {
                                                        if ((dividedKey.third as Fraction).powerTo > (dividerKey.third as Function).powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividedKey.third as Fraction).powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividerKey.third as Function).powerTo) {
                                                                functionsMap[divided] = (dividerKey.third as Function).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividerKey.third as Function).powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                                is Fraction -> {
                                                    if (dividedKey.third == dividerKey.third) {
                                                        if ((dividedKey.third as Fraction).powerTo > (dividerKey.third as Fraction).powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividedKey.third as Fraction).powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividerKey.third as Fraction).powerTo) {
                                                                functionsMap[divided] = (dividerKey.third as Fraction).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividerKey.third as Fraction).powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                    else if (dividedKey.third == divider) {
                                                        if ((dividedKey.third as Fraction).powerTo > divider.powerTo) {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > (dividedKey.third as Fraction).powerTo) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = (dividedKey.third as Fraction).powerTo
                                                            }
                                                        }
                                                        else {
                                                            if (functionsMap[divided] != null && functionsMap[divided]!! > divider.powerTo) {
                                                                functionsMap[divided] = divider.powerTo
                                                            }
                                                            else if (functionsMap[divided] == null) {
                                                                functionsMap[divided] = divider.powerTo
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (divided == divider) {
                                if (divider.powerTo > divided.powerTo) {
                                    if (functionsMap[divided] != null && functionsMap[divided]!! > divided.powerTo) {
                                        functionsMap[divided] = divided.powerTo
                                    }
                                    else if (functionsMap[divided] == null) {
                                        functionsMap[divided] = divided.powerTo
                                    }
                                }
                                else {
                                    if (functionsMap[divided] != null && functionsMap[divided]!! > divider.powerTo) {
                                        functionsMap[divided] = divider.powerTo
                                    }
                                    else if (functionsMap[divided] == null) {
                                        functionsMap[divided] = divider.powerTo
                                    }
                                }
                            }
                        }
                    }
                    else if (divider is UnknownEntity) {
                        if (divided is UnknownEntity) {
                            if (divider.onlyNumber() && divided.onlyNumber()) {
                                entry.add(divider)
                                break
                            }
                            else {
                                if (divided.getKey() == divider.getKey()) {
                                    entry.add(divider)
                                    break
                                }
                            }
                        }
                    }
                }

                list.add(entry)
            }

            // Find common unknowns
            var commonUnknownEntities = mutableListOf<UnknownEntity>()

            if (list.isNotEmpty()) {
                for (i in list[0]) {
                    if (i is UnknownEntity) {
                        commonUnknownEntities.add(i)
                    }
                }
            }

            for (array in list) {
                for (i in array) {
                    if (i is UnknownEntity) {
                        val found = mutableListOf<Char?>()

                        for (entity in commonUnknownEntities) {
                            if (entity.variable == i.variable) {
                                if (entity.multiplier!! > i.multiplier!!) {
                                    entity.multiplier = i.multiplier
                                }
                                if (i.powerTo != null) {
                                    if (entity.powerTo != null) {
                                        if (entity.powerTo!! > i.powerTo!!) {
                                            entity.powerTo = i.powerTo
                                        }
                                    } else {
                                        entity.powerTo = i.powerTo
                                    }
                                }

                                found.add(entity.variable)
                            }
                        }

                        val common = mutableListOf<UnknownEntity>()
                        for (variable in found) {
                            for (entity in commonUnknownEntities) {
                                if (variable == entity.variable) {
                                    common.add(entity)
                                }
                            }
                        }

                        commonUnknownEntities = common
                    }
                }
            }

            val map = hashMapOf<Any, Int>()
            for (array in list) {
                for (i in array) {
                    map[i] = map.getOrDefault(i, 0) + 1
                }
            }

            val commonEntities = map.filter { it.value == list.size }.keys

            // Check gcd for unknown entities
            val values = mutableListOf<Double>()
            for (element in commonEntities) {
                if (element is UnknownEntity) {
                    values.add(element.multiplier!!)
                }
            }

            // Get all multipliers of unknown entities
            val allMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (element in values) {
                allMultipliers.add(Fraction(mutableListOf(UnknownEntity(ceil(round(element * 1000) / 1000)))))
                val decimalPoint = countDecimalPlaces(ceil(round(element * 1000) / 1000))
                if (decimalPoint > biggestDecimalPoint) {
                    biggestDecimalPoint = decimalPoint
                }
            }

            // Convert multipliers to same base
            var decimalPoint = 1
            for (i in 0..<biggestDecimalPoint) {
                decimalPoint *= 10
            }
            for (fraction in allMultipliers) {
                (fraction.numerator.last() as UnknownEntity).multiplier = (fraction.numerator.last() as UnknownEntity).multiplier?.times(
                    decimalPoint
                )
                fraction.denominator = mutableListOf()
                fraction.denominator!!.add(UnknownEntity(decimalPoint.toDouble()))
            }

            // Find gcd
            val allGCD = mutableListOf<MutableList<Int>>()
            var currentGCD = mutableListOf<Int>()
            for (multiplierA in allMultipliers) {
                for (multiplierB in allMultipliers) {
                    val value = gcd((multiplierA.numerator.last() as UnknownEntity).multiplier!!.toInt(), (multiplierB.numerator.last() as UnknownEntity).multiplier!!.toInt())
                    currentGCD.add(value)
                }
                if (currentGCD.isNotEmpty()) {
                    allGCD.add(currentGCD)
                    currentGCD = mutableListOf()
                }
            }

            // Get common gcd
            val commonGCD = hashMapOf<Int, Int>()
            for (array in allGCD) {
                var biggestValue = 0
                for (gcd in array) {
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
                        if (divisor != 1) {
                            commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
                            checkUnique.add(divisor)
                        }
                    }
                }
            }

            val allCommonGCD = commonGCD.filter { (_, v) -> v >= allGCD.size }.keys
            var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
            for (value in allCommonGCD) {
                if (gcd < value) {
                    gcd = value
                }
            }

            if (commonUnknownEntities.isNotEmpty()) {
                for (commonEntity in commonUnknownEntities) {
                    for (element in numerator) {
                        if (element is UnknownEntity) {
                            if (element.variable == commonEntity.variable) {
                                element.multiplier = element.multiplier?.times(decimalPoint.toDouble())
                                element.multiplier = element.multiplier?.div(gcd.toDouble())
                                if (element.powerTo != null) {
                                    if (commonEntity.powerTo != null) {
                                        val onlyNumber = element.onlyNumber()

                                        element.powerTo = element.powerTo?.minus(commonEntity.powerTo!!)

                                        if (element.powerTo == 0.0 && !onlyNumber) {
                                            element.clear()
                                            element.multiplier = 1.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (element in denominator!!) {
                        if (element is UnknownEntity) {
                            if (element.variable == commonEntity.variable) {
                                element.multiplier = element.multiplier?.times(decimalPoint.toDouble())
                                element.multiplier = element.multiplier?.div(gcd.toDouble())
                                if (element.powerTo != null) {
                                    if (commonEntity.powerTo != null) {
                                        val onlyNumber = element.onlyNumber()

                                        element.powerTo = element.powerTo?.minus(commonEntity.powerTo!!)

                                        if (element.powerTo == 0.0 && !onlyNumber) {
                                            element.clear()
                                            element.multiplier = 1.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val allCommonFunctions = functionsMap.filter { (_, v) -> v > 0 }

            for ((k, v) in allCommonFunctions) {
                if (k is Function) {
                    val numeratorToFind = numerator.find { it is Function && it.getKey() == k.getKey() }
                    if (numeratorToFind != null) {
                        (numeratorToFind as Function).powerTo -= v

                        if ((numeratorToFind as Function).powerTo == 0.0) {
                            numerator.remove(numeratorToFind)
                        }
                    }
                    val denominatorToFind = denominator!!.find { it is Function && it.getKey() == k.getKey() }
                    if (denominatorToFind != null) {
                        (denominatorToFind as Function).powerTo -= v

                        if ((denominatorToFind as Function).powerTo == 0.0) {
                            denominator!!.remove(denominatorToFind)
                        }
                    }
                }
                else if (k is Fraction) {
                    val numeratorToFind = numerator.find { it is Fraction && it.getKey() == k.getKey() }
                    if (numeratorToFind != null) {
                        (numeratorToFind as Fraction).powerTo -= v

                        if ((numeratorToFind as Fraction).powerTo == 0.0) {
                            numerator.remove(numeratorToFind)
                        }
                    }
                    val denominatorToFind = denominator!!.find { it is Fraction && it.getKey() == k.getKey() }
                    if (denominatorToFind != null) {
                        (denominatorToFind as Fraction).powerTo -= v

                        if ((denominatorToFind as Fraction).powerTo == 0.0) {
                            denominator!!.remove(denominatorToFind)
                        }
                    }
                }
            }

            // Divide by only numbers
            val toRemove = mutableListOf<Any>()
            for (element in denominator!!) {
                if (element is UnknownEntity) {
                    if (element.onlyNumber()) {
                        for ((i, entity) in numerator.withIndex()) {
                            if (entity is UnknownEntity) {
                                (numerator[i] as UnknownEntity).multiplier = entity.multiplier!! / element.multiplier!!
                                toRemove.add(element)
                                break
                            }
                        }
                    }
                }
            }

            for (i in toRemove) {
                denominator!!.remove(i)
            }
        }
    }

    private fun shortenEquationRecursive(equation: MutableList<Any>): MutableList<Any> {
        // Make multiplications and division
        val grouped = mutableListOf<Any>()
        var pieceOfEquation = mutableListOf<Any>()

        var operator = '×'
        for (element in equation) {
            when (element) {
                is UnknownEntity -> {
                    pieceOfEquation.add(element)
                    operator = '×'
                }
                is Function -> {
                    if (element.powerTo != 0.0) {
                        var found = false
                        var i = 0
                        while (i < pieceOfEquation.size) {
                            if (pieceOfEquation[i] is Function) {
                                if ((pieceOfEquation[i] as Function) == element) {
                                    if (operator == '×') {
                                        (pieceOfEquation[i] as Function).powerTo += element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.times(
                                                element.count!!
                                            )
                                        }
                                    }
                                    else {
                                        (pieceOfEquation[i] as Function).powerTo -= element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.div(
                                                element.count!!
                                            )
                                        }
                                    }
                                    found = true
                                    break
                                }
                            }
                            else if (pieceOfEquation[i] is Fraction) {
                                if ((pieceOfEquation[i] as Fraction).getKey() == element.getKey()) {
                                    if (operator == '×') {
                                        (pieceOfEquation[i] as Fraction).powerTo += element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.times(
                                                element.count!!
                                            )
                                        }
                                    }
                                    else {
                                        (pieceOfEquation[i] as Fraction).powerTo -= element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.div(
                                                element.count!!
                                            )
                                        }
                                    }
                                    found = true
                                    break
                                }
                            }
                            i++
                        }

                        if (!found) {
                            if (operator == '/') {
                                element.powerTo = -element.powerTo
                            }
                            pieceOfEquation.add(element)
                        }
                    }
                    else {
                        pieceOfEquation.add(UnknownEntity(1.0))
                    }
                    operator = '×'
                }
                is Fraction -> {
                    if (element.powerTo != 0.0) {
                        var found = false
                        var i = 0
                        while (i < pieceOfEquation.size) {
                            if (pieceOfEquation[i] is Fraction) {
                                if ((pieceOfEquation[i] as Fraction) == element) {
                                    if (operator == '×') {
                                        (pieceOfEquation[i] as Fraction).powerTo += element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.times(
                                                element.count!!
                                            )
                                        }
                                    }
                                    else {
                                        (pieceOfEquation[i] as Fraction).powerTo -= element.powerTo
                                        if (element.count != null) {
                                            (pieceOfEquation[i] as Fraction).count = (pieceOfEquation[i] as Fraction).count?.div(
                                                element.count!!
                                            )
                                        }
                                    }
                                    found = true
                                    break
                                }
                            }
                            i++
                        }
                        if (!found) {
                            element.numerator = element.shortenEquationRecursive(element.numerator)
                            if (element.denominator != null) {
                                element.denominator = element.shortenEquationRecursive(element.denominator!!)
                                element.reduceFraction()
                            }
                            if (operator == '/') {
                                element.powerTo = -element.powerTo
                            }
                            pieceOfEquation.add(element)
                        }
                    }
                    else {
                        pieceOfEquation.add(UnknownEntity(1.0))
                    }
                    operator = '×'
                }
                is Char -> {
                    if (element == '+' || element == '-') {
                        grouped.addAll(pieceOfEquation)
                        pieceOfEquation = mutableListOf()
                        grouped.add(element)
                        operator = '×'
                    }
                    else {
                        operator = element
                    }
                }
            }
        }
        grouped.addAll(pieceOfEquation)

        return grouped
    }

    private fun fractionCalculationsRecursive() {
        this.numerator = finalCalculation(this.numerator)
        if (this.denominator != null) {
            this.denominator = finalCalculation(this.denominator!!)
        }

        for (element in numerator) {
            if (element is Fraction) {
                element.numerator = finalCalculation(element.numerator)
                if (element.denominator != null) {
                    element.denominator = finalCalculation(element.denominator!!)
                }

                element.fractionCalculationsRecursive()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    element.numerator = finalCalculation(element.numerator)
                    if (element.denominator != null) {
                        element.denominator = finalCalculation(element.denominator!!)
                    }

                    element.fractionCalculationsRecursive()
                }
            }
        }
    }

    private fun finalCalculation(equation: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<Any>()

        for (elementA in equation) {
            var found = false
            for ((index, elementB) in output.withIndex()) {
                if (elementA is Fraction && elementB is Fraction) {
                    if (elementA == elementB) {
                        if (elementB.count != null && elementA.count == elementB.count) {
                            output.add(index, elementB.count!! + elementA.count!!)
                            elementB.count = null
                            found = true
                            break
                        }
                    }
                    else {
                        val elementAKeys = elementA.getKeys()
                        val elementBKeys = elementB.getKeys()

                        if (elementAKeys == elementBKeys) {
                            if (elementB.count != null && elementA.count == elementB.count) {
                                output.add(index, elementB.count!! + elementA.count!!)
                                elementB.count = null
                                found = true
                                break
                            }
                        }
                    }
                }
                else if (elementA is Function && elementB is Function) {
                    if (elementA == elementB) {
                        if (elementB.count != null && elementA.count == elementB.count) {
                            output.add(index, elementB.count!! + elementA.count!!)
                            elementB.count = null
                            found = true
                            break
                        }
                    }
                }
            }

            if (!found) {
                if (elementA is Fraction) {
                    elementA.fractionCalculationsRecursive()
                }

                output.add(elementA)
            }
            else {
                if (output.last() is Char) {
                    output.removeLast()
                }
            }
        }

        return output
    }

    fun finalShort() {
        numerator = finalCalculation(shortenEquationRecursive(numerator))
        if (denominator != null) denominator = finalCalculation(shortenEquationRecursive(denominator!!))

        for (element in numerator) {
            if (element is Fraction) {
                element.setFraction()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    element.setFraction()
                }
            }
        }

        this.shortenEverything()
    }

    private fun shortenEverything() {
        this.shortenEveryFractionNumerator()
        if (denominator != null) this.shortenEveryFractionDenominator()

        numerator = makeCalculations(shortenEquation(numerator))
        if (denominator != null) denominator =  makeCalculations(shortenEquation(denominator!!))

        this.reduceFraction()
    }

    fun setFraction() {
        clearMaps()

        println("INPUT")
        println(this)

        updateFractionNumerator()
        if (denominator != null) updateFractionDenominator()

        if (!withoutGCD) {
            setMultiplicative(getMultiplicativeNumerator(), getMultiplicativeDenominator())
        }

        cleanNumeratorMaps()
        cleanDenominatorMaps()
        if (denominator != null) shortenNumeratorWithDenominator()

        rebuildFractionNumerator()
        if (denominator != null) rebuildFractionDenominator()
        if (denominator != null && denominator!!.isEmpty()) denominator = null

        println("AFTER BUILD")
        println(this)

        getOutMultiplication()

        makeCopy()
        shortenEverything()

        convertDoublesToFractionInNumerator()
        if (denominator != null) convertDoublesToFractionInDenominator()
        sortEverything()
        cleanFraction()

        getOutCount()
        println("OUTPUT")
        println(this)
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

    private fun equationNotInBrackets(equation: MutableList<Any>): Boolean {
        var brackets = 0
        for (element in equation) {
            when (element) {
                '(' -> brackets++
                ')' -> brackets--
            }
            if (brackets == 0 && element !== equation.last()) {
                return true
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

    private fun sortFraction(input: MutableList<Any>, key: Boolean = false, flatFraction: Boolean = false, withMultiplication: Boolean = false): MutableList<Any> {
        val output = mutableListOf<Any>()
        val fragment = mutableListOf<Any>()
        var sorted: MutableList<Any>

        for (element in input) {
            if (element is Fraction) {
                if (withMultiplication) {
                    fragment.add('(')
                    if (key) {
                        fragment.addAll(element.getKey())
                    }
                    else {
                        fragment.add('(')
                        fragment.addAll(sortFraction(element.getFraction(flatFraction = flatFraction, withMultiplication = true), flatFraction = flatFraction, withMultiplication = true))
                        fragment.add(')')
                    }
                    fragment.add(')')
                    fragment.add('×')
                }
                else {
                    if (key) {
                        fragment.addAll(element.getKey())
                    }
                    else {
                        val inside = sortFraction(element.getFraction(flatFraction = flatFraction, withMultiplication = false), flatFraction = flatFraction, withMultiplication = false)

                        if (inside.isNotEmpty()) {
                            val notInBrackets = equationNotInBrackets(inside) && equationHasOperators(inside)

                            if (notInBrackets) {
                                fragment.add('(')
                            }
                            fragment.addAll(inside)

                            if (notInBrackets) {
                                fragment.add(')')
                            }
                        }
                    }
                }
            }
            else {
                when (element) {
                    is UnknownEntity, is Function -> {
                        if (withMultiplication) {
                            fragment.add('(')
                            fragment.add(element)
                            fragment.add(')')
                            fragment.add('×')
                        }
                        else {
                            fragment.add(element)
                        }
                    }
                    else -> {
                        if (fragment.isNotEmpty() && fragment.last() == '×') {
                            fragment.removeLast()
                        }

                        if (!withMultiplication) {
                            sorted = sortFragment(fragment)
                            for (i in sorted) {
                                output.add(i)
                            }

                            output.add(element)
                            fragment.clear()
                        }
                        else {
                            output.addAll(fragment)
                            output.add(element)
                            fragment.clear()
                        }
                    }
                }
            }
        }
        if (fragment.isNotEmpty() && fragment.last() == '×') {
            fragment.removeLast()
        }
        if (!withMultiplication) {
            sorted = sortFragment(fragment)
            for (i in sorted) {
                output.add(i)
            }
        }
        else {
            output.addAll(fragment)
        }
        return output
    }

    private fun getInsideOfFraction(input: MutableList<Any>, key: Boolean = false, flatFraction: Boolean = false, withMultiplication: Boolean = false): MutableList<Any> {
        val output = mutableListOf<Any>()

        for (element in input) {
            if (element is Fraction) {
                if (withMultiplication) {
                    output.add('(')
                    if (key) {
                        output.addAll(element.getKey())
                    }
                    else {
                        output.addAll(element.getFraction(flatFraction = flatFraction, withMultiplication = true))
                    }
                    output.add(')')
                    output.add('×')
                }
                else {
                    if (key) {
                        output.addAll(element.getKey())
                    }
                    else {
                        output.addAll(element.getFraction(flatFraction = flatFraction, withMultiplication = false))
                    }
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
                                output.addAll(element.getFunction(flatFunction = flatFraction, withMultiplication = false))
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

    fun isOne(): Boolean {
        val key = this.getKey()
        return key.size == 1 && key.last() == 1.0 && (this.count == null || this.count!!.isOne())
    }

    fun isZero(): Boolean {
        val key = this.getKey()
        return key.size == 1 && key.last() == 0.0
    }

    private fun isOne(input: MutableList<Any>): Boolean {
        val output = getInsideOfFraction(input, key = true, withMultiplication = false)
            .filter { it != '(' && it != ')' }.sortedWith(
                compareBy<Any> {
                    if (it is Char) it.code else 0
                }.thenBy {
                    if (it is Double) it else 1.0
                }.thenBy {
                    it::class.simpleName
                }).toMutableList()

        return output.size == 1 && output.last() == 1.0
    }

    fun getFraction(flatFraction: Boolean = false, withoutCount: Boolean = false, withMultiplication: Boolean = true): MutableList<Any> {
        val fraction = mutableListOf<Any>()
        var index = 0

        if (powerTo == 0.0 && !flatFraction) {
            return mutableListOf()
        }
        else {
            if (count != null && !count!!.isOne() && !flatFraction && !withoutCount) {
                if (withMultiplication) {
                    fraction.add('(')
                    fraction.addAll(count!!.getOriginal())
                    fraction.add(')')
                    fraction.add('×')
                    fraction.add('(')
                }
                else {
                    fraction.addAll(count!!.getOriginal())
                    fraction.add('(')
                }
                index = fraction.size
            }

            val content = sortFraction(numerator, flatFraction = flatFraction, withMultiplication = withMultiplication)
            val denominatorExists = (denominator != null && denominator!!.isNotEmpty())

            if (content.isNotEmpty() || denominatorExists) {
                fraction.addAll(content)

                if (denominatorExists) {
                    fraction.add(0, '(')
                    fraction.add(')')
                    fraction.add('/')
                    fraction.add('(')
                    val buffer = sortFraction(denominator!!, flatFraction = flatFraction, withMultiplication = withMultiplication)
                    fraction.addAll(buffer)
                    fraction.add(')')
                }
            }

            if (count != null && !count!!.isOne() && !flatFraction && !withoutCount) {
                fraction.add(')')
            }

            if (!flatFraction) {
                if (powerTo != 1.0) {
                    fraction.add(index, '(')
                    fraction.add(')')
                    fraction.add('^')
                    fraction.add('(')
                    fraction.add(UnknownEntity(powerTo))
                    fraction.add(')')
                }
            }
        }

        return getInsideOfFraction(fraction, flatFraction = flatFraction, withMultiplication = withMultiplication)
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
        var entities = mutableListOf<UnknownEntity>()

        var sign = '0'
        for (element in numerator) {
            when (element) {
                is UnknownEntity -> {
                    var found = false
                    for ((i, entity) in entities.withIndex()) {
                        if (entity == element) {
                            if (sign == '/') {
                                entities[i] = entity / element
                                sign = '0'
                            }
                            else {
                                entities[i] = entity * element
                            }
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        entities.add(element)
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
                    element.getOutCount()
                    element.getOutPowerTo()
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (numeratorMap[key.first] == null && key.third !is UnknownEntity) {
                                numeratorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity == (key.third as UnknownEntity)) {
                                        if (sign == '/') {
                                            if (key.second < 0) {
                                                entities[i] = entity * key.third as UnknownEntity
                                            }
                                            else {
                                                entities[i] = entity / key.third as UnknownEntity
                                            }
                                        }
                                        else {
                                            if (key.second < 0) {
                                                entities[i] = entity / key.third as UnknownEntity
                                            }
                                            else {
                                                entities[i] = entity * key.third as UnknownEntity
                                            }
                                        }
                                        found = true
                                        break
                                    }
                                }

                                if (!found) {
                                    entities.add(key.third as UnknownEntity)
                                }
                            }
                            else {
                                numeratorMap[key.first] = numeratorMap.getOrDefault(key.first, 0.0) + key.second
                            }
                        }
                    }
                    sign = '0'
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (entities.isNotEmpty()) {
                                for (entity in entities) {
                                    if (entity.onlyNumber()) {
                                        numeratorMap[entity.getKey(value = true)] = entity.multiplier!!
                                    }
                                    else {
                                        numeratorMap[entity.getKey()] = entity.multiplier!!
                                    }
                                }

                                numeratorElement.addAll(entities)
                                entities = mutableListOf()
                            }

                            if (numeratorMap.isNotEmpty() && numeratorElement.isNotEmpty()) {
                                numeratorMaps.add(numeratorMap)
                                numeratorMap = hashMapOf()

                                numeratorElements.add(numeratorElement)
                                numeratorElement = mutableListOf()

                                numeratorOperators.add(element)
                            }
                        }
                    }
                    sign = element
                }
            }
        }
        if (entities.isNotEmpty()) {
            for (entity in entities) {
                if (entity.onlyNumber()) {
                    numeratorMap[entity.getKey(value = true)] = entity.multiplier!!
                }
                else {
                    numeratorMap[entity.getKey()] = entity.multiplier!!
                }
            }

            numeratorElement.addAll(entities)
        }

        if (numeratorMap.isNotEmpty() && numeratorElement.isNotEmpty()) {
            numeratorMaps.add(numeratorMap)
            numeratorElements.add(numeratorElement)
        }
    }

    private fun updateFractionDenominator() {
        var denominatorMap = hashMapOf<MutableList<Any>, Double>()
        var denominatorElement = mutableListOf<Any>()
        var entities = mutableListOf<UnknownEntity>()

        var sign = '0'
        for (element in denominator!!) {
            when (element) {
                is UnknownEntity -> {
                    var found = false
                    for ((i, entity) in entities.withIndex()) {
                        if (entity == element) {
                            if (sign == '/') {
                                entities[i] = entity / element
                                sign = '0'
                            }
                            else {
                                entities[i] = entity * element
                            }
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        entities.add(element)
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
                    element.getOutCount()
                    element.getOutPowerTo()
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (denominatorMap[key.first] == null && key.third !is UnknownEntity) {
                                denominatorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity == (key.third as UnknownEntity)) {
                                        if (sign == '/') {
                                            if (key.second < 0) {
                                                entities[i] = entity * key.third as UnknownEntity
                                            }
                                            else {
                                                entities[i] = entity / key.third as UnknownEntity
                                            }
                                        }
                                        else {
                                            if (key.second < 0) {
                                                entities[i] = entity / key.third as UnknownEntity
                                            }
                                            else {
                                                entities[i] = entity * key.third as UnknownEntity
                                            }
                                        }
                                        found = true
                                        break
                                    }
                                }

                                if (!found) {
                                    entities.add(key.third as UnknownEntity)
                                }
                            }
                            else {
                                denominatorMap[key.first] = denominatorMap.getOrDefault(key.first, 0.0) + key.second
                            }
                        }
                    }
                    sign = '0'
                }
                is Char -> {
                    when (element) {
                        '+', '-' -> {
                            if (entities.isNotEmpty()) {
                                for (entity in entities) {
                                    if (entity.onlyNumber()) {
                                        denominatorMap[entity.getKey(value = true)] = entity.multiplier!!
                                    }
                                    else {
                                        denominatorMap[entity.getKey()] = entity.multiplier!!
                                    }
                                }

                                denominatorElement.addAll(entities)
                                entities = mutableListOf()
                            }

                            if (denominatorMap.isNotEmpty() && denominatorElement.isNotEmpty()) {
                                numeratorMaps.add(denominatorMap)
                                denominatorMap = hashMapOf()

                                numeratorElements.add(denominatorElement)
                                denominatorElement = mutableListOf()

                                numeratorOperators.add(element)
                            }
                        }
                    }
                    sign = element
                }
            }
        }
        if (entities.isNotEmpty()) {
            for (entity in entities) {
                if (entity.onlyNumber()) {
                    denominatorMap[entity.getKey(value = true)] = entity.multiplier!!
                }
                else {
                    denominatorMap[entity.getKey()] = entity.multiplier!!
                }
            }

            denominatorElement.addAll(entities)
        }

        denominatorMaps.add(denominatorMap)
        denominatorElements.add(denominatorElement)
    }

    private fun itIsUnknown(input: MutableList<Any>): Boolean {
        if (input.size == 3) {
            if (input[0] is Double) {
                if (input[1] == '^') {
                    if (input[2] == 'x' || input[2] == 'y' || input[2] == 'z' || input[2] == 'π' || input[2] == 'e' || input[2] == 'f') {
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
            val startCommonForNumerator = hashMapOf<MutableList<Any>, Double>()
            for ((key, v) in numeratorMaps.last()) {
                startCommonForNumerator[key] = v
            }

            println("numeratorMaps")
            println(numeratorMaps)

            var noCommonEntity = false
            val toRemove = mutableListOf<MutableList<Any>>()
            for (map in numeratorMaps) {
                for ((key, value) in startCommonForNumerator) {
                    if (itIsUnknown(key) && !noCommonEntity) {
                        var found = false
                        for((k, v) in map) {
                            if (itIsUnknown(k) && key.last() == k.last()) {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForNumerator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                if (commonForNumerator[newKey] != 0.0) {
                                    found = true
                                }
                            }
                            else if (itIsUnknown(k) && key.last() != 'f' || k.last() != 'f') {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], 'f')
                                commonForNumerator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                if (commonForNumerator[newKey] != 0.0) {
                                    found = true
                                }
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
        val entities = mutableListOf<UnknownEntity>()

        for ((key, v) in commonForNumerator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entities.add(UnknownEntity())
                entities.last().multiplier = commonsToFind[key]
                entities.last().variable = key.last() as Char
                entities.last().powerTo = key.first() as Double
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
                            if (itIsUnknown(key) && ((numeratorElements[index][element] as UnknownEntity).variable == key.last() || key.last() == 'f')){
                                val onlyNumber = (numeratorElements[index][element] as UnknownEntity).onlyNumber()

                                (numeratorElements[index][element] as UnknownEntity).powerTo =
                                    (numeratorElements[index][element] as UnknownEntity).powerTo?.minus(
                                        key.first() as Double
                                    )

                                if ((numeratorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    if (!onlyNumber) {
                                        numeratorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }

                        is Function -> {
                            if (key == (numeratorElements[index][element] as Function).getKey()) {
                                if ((numeratorElements[index][element] as Function).powerTo != 0.0) {
                                    (numeratorElements[index][element] as Function).powerTo -= v
                                }

                                if (commonsToFind[key]!! > 0) {
                                    if ((numeratorElements[index][element] as Function).powerTo == 0.0) {
                                        numeratorOutput.add(numeratorElements[index].removeAt(element))
                                        element--
                                    }
                                    else {
                                        numeratorOutput.add((numeratorElements[index][element] as Function).copy())
                                    }
                                    (numeratorOutput.last() as Function).powerTo = v
                                    commonsToFind[key] = 0.0
                                }
                                else {
                                    if ((numeratorElements[index][element] as Function).powerTo == 0.0) {
                                        numeratorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }

                        is Fraction -> {
                            if (key == (numeratorElements[index][element] as Fraction).getKey()) {
                                if ((numeratorElements[index][element] as Fraction).powerTo != 0.0) {
                                    (numeratorElements[index][element] as Fraction).powerTo -= v
                                }
                                if (commonsToFind[key]!! > 0) {
                                    if ((numeratorElements[index][element] as Fraction).powerTo == 0.0) {
                                        numeratorOutput.add(numeratorElements[index].removeAt(element))
                                        element--
                                    }
                                    else {
                                        numeratorOutput.add((numeratorElements[index][element] as Fraction).copy())
                                    }
                                    (numeratorOutput.last() as Fraction).powerTo = v
                                    commonsToFind[key] = 0.0
                                }
                                else {
                                    if ((numeratorElements[index][element] as Fraction).powerTo == 0.0) {
                                        numeratorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }
                    }
                }
                element++
            }
            index++
        }

        if (entities.isNotEmpty()) {
            // Get all multipliers of unknown entities
            val allNumeratorMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (list in numeratorElements) {
                for (element in list) {
                    if (element is UnknownEntity) {
                        allNumeratorMultipliers.add(Fraction(mutableListOf(UnknownEntity(ceil(round(element.multiplier!! * 1000) / 1000)))))
                        val decimalPoint = countDecimalPlaces(ceil(round(element.multiplier!! * 1000) / 1000))
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
                if (currentGCD.isNotEmpty()) {
                    allGCD.add(currentGCD)
                    currentGCD = mutableListOf()
                }
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
                        if (divisor != 1) {
                            commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
                            checkUnique.add(divisor)
                        }
                    }
                }
            }

            val allCommonGCD = commonGCD.filter { (_, v) -> v == allGCD.size }.keys
            var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
            for (value in allCommonGCD) {
                if (gcd < value) {
                    gcd = value
                }
            }

            if (!(gcd == 1 && decimalPoint == 1)) {
                // Apply GCD to all multipliers
                for (fraction in allNumeratorMultipliers) {
                    val newValue =  (fraction.numerator.last() as UnknownEntity).multiplier?.times(decimalPoint.toDouble())
                    if (newValue != null) {
                        (fraction.numerator.last() as UnknownEntity).multiplier = newValue / gcd.toDouble()
                    }
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
                                    val buffer = numeratorElements[i].removeAt(j) as UnknownEntity
                                    if (numeratorElements[i].isEmpty()) {
                                        buffer.multiplier = 1.0
                                        numeratorElements[i].add(buffer)
                                    }
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

                for (entity in entities) {
                    if (entity.powerTo == 0.0) {
                        entity.variable = 'f'
                    }
                    numeratorOutput.add(Fraction(mutableListOf(UnknownEntity(gcd.toDouble(), entity.variable, entity.powerTo)), mutableListOf(UnknownEntity(decimalPoint.toDouble()))))
                }

                for (element in numeratorOutput) {
                    if (element is Fraction) {
                        element.shortenFraction()
                    }
                }
            }
            else {
                for (entity in entities) {
                    entity.multiplier = 1.0
                    numeratorOutput.add(entity)
                }
            }
        }

        for (element in numeratorOutput) {
            if (element is UnknownEntity) {
                if (element.powerTo == 0.0) {
                    element.variable = 'f'
                }
            }
        }

        return numeratorOutput
    }

    private fun getMultiplicativeDenominator(): MutableList<Any>? {
        if (denominator == null) return null

        // Find commons for denominator
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
                            if (itIsUnknown(k) && k.last() == key.last()) {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForDenominator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                if (commonForDenominator[newKey] != 0.0) {
                                    found = true
                                }
                            }
                            else if (itIsUnknown(k) && key.last() == 'f') {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForDenominator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                if (commonForDenominator[newKey] != 0.0) {
                                    found = true
                                }
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
        val entities = mutableListOf<UnknownEntity>()

        for ((key, v) in commonForDenominator) {
            commonsToFind[key] = v

            if (itIsUnknown(key)) {
                entities.add(UnknownEntity())
                entities.last().multiplier = commonsToFind[key]
                entities.last().variable = key.last() as Char
                entities.last().powerTo = key.first() as Double
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
                            if (itIsUnknown(key) && ((denominatorElements[index][element] as UnknownEntity).variable == key.last() || key.last() == 'f')){
                                val onlyNumber = (denominatorElements[index][element] as UnknownEntity).onlyNumber()

                                (denominatorElements[index][element] as UnknownEntity).powerTo =
                                    (denominatorElements[index][element] as UnknownEntity).powerTo?.minus(
                                        key.first() as Double
                                    )

                                if ((denominatorElements[index][element] as UnknownEntity).powerTo == 0.0) {
                                    if (!onlyNumber) {
                                        denominatorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }

                        is Function -> {
                            if (key == (denominatorElements[index][element] as Function).getKey()) {
                                if ((denominatorElements[index][element] as Function).powerTo != 0.0) {
                                    (denominatorElements[index][element] as Function).powerTo -= v
                                }
                                if (commonsToFind[key]!! > 0) {
                                    if ((denominatorElements[index][element] as Function).powerTo == 0.0) {
                                        denominatorOutput.add(denominatorElements[index].removeAt(element))
                                        element--
                                    }
                                    else {
                                        denominatorOutput.add((denominatorElements[index][element] as Function).copy())
                                    }
                                    (denominatorOutput.last() as Function).powerTo = v
                                    commonsToFind[key] = 0.0
                                }
                                else {
                                    if ((denominatorElements[index][element] as Function).powerTo == 0.0) {
                                        denominatorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }
                        is Fraction -> {
                            if (key == (denominatorElements[index][element] as Fraction).getKey()) {
                                if ((denominatorElements[index][element] as Fraction).powerTo != 0.0) {
                                    (denominatorElements[index][element] as Fraction).powerTo -= v
                                }
                                if (commonsToFind[key]!! > 0) {
                                    if ((denominatorElements[index][element] as Fraction).powerTo == 0.0) {
                                        denominatorOutput.add(denominatorElements[index].removeAt(element))
                                        element--
                                    }
                                    else {
                                        denominatorOutput.add((denominatorElements[index][element] as Fraction).copy())
                                    }
                                    (denominatorOutput.last() as Fraction).powerTo = v
                                    commonsToFind[key] = 0.0
                                }
                                else {
                                    if ((denominatorElements[index][element] as Fraction).powerTo == 0.0) {
                                        denominatorElements[index].removeAt(element)
                                        element--
                                    }
                                }
                            }
                        }
                    }
                }
                element++
            }
            index++
        }

        if (entities.isNotEmpty()) {
            // Get all multipliers of unknown entities
            val allDenominatorMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (list in denominatorElements) {
                for (element in list) {
                    if (element is UnknownEntity) {
                        allDenominatorMultipliers.add(Fraction(mutableListOf(UnknownEntity(ceil(round(element.multiplier!! * 1000) / 1000)))))
                        val decimalPoint = countDecimalPlaces(ceil(round(element.multiplier!! * 1000) / 1000))
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
                (fraction.numerator.last() as UnknownEntity).multiplier =
                    (fraction.numerator.last() as UnknownEntity).multiplier?.times(
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
                    val value = gcd(
                        (multiplierA.numerator.last() as UnknownEntity).multiplier!!.toInt(),
                        (multiplierB.numerator.last() as UnknownEntity).multiplier!!.toInt()
                    )
                    currentGCD.add(value)
                }
                if (currentGCD.isNotEmpty()) {
                    allGCD.add(currentGCD)
                    currentGCD = mutableListOf()
                }
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
                        if (divisor != 1) {
                            commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
                            checkUnique.add(divisor)
                        }
                    }
                }
            }

            val allCommonGCD = commonGCD.filter { (_, v) -> v == allGCD.size }.keys
            var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
            for (value in allCommonGCD) {
                if (gcd < value) {
                    gcd = value
                }
            }

            if (!(gcd == 1 && decimalPoint == 1)) {
                // Apply GCD to all multipliers
                for (fraction in allDenominatorMultipliers) {
                    val newValue =  (fraction.numerator.last() as UnknownEntity).multiplier?.times(decimalPoint.toDouble())
                    if (newValue != null) {
                        (fraction.numerator.last() as UnknownEntity).multiplier = newValue / gcd.toDouble()
                    }
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
                                    val buffer = denominatorElements[i].removeAt(j) as UnknownEntity
                                    if (denominatorElements[i].isEmpty()) {
                                        buffer.multiplier = 1.0
                                        denominatorElements[i].add(buffer)
                                    }
                                    j--
                                } else {
                                    (denominatorElements[i][j] as UnknownEntity).multiplier = value
                                }
                            } else {
                                (denominatorElements[i][j] as UnknownEntity).multiplier = value
                            }
                            multiplier++
                            j++
                        }
                        j++
                    }
                    i++
                }

                for (entity in entities) {
                    if (entity.powerTo == 0.0) {
                        entity.variable = 'f'
                    }
                    denominatorOutput.add(Fraction(mutableListOf(UnknownEntity(gcd.toDouble(), entity.variable, entity.powerTo)), mutableListOf(UnknownEntity(decimalPoint.toDouble()))))
                }

                for (element in denominatorOutput) {
                    if (element is Fraction) {
                        element.shortenFraction()
                    }
                }
            }
            else {
                for (entity in entities) {
                    entity.multiplier = 1.0
                    denominatorOutput.add(entity)
                }
            }
        }

        for (element in denominatorOutput) {
            if (element is UnknownEntity) {
                if (element.powerTo == 0.0) {
                    element.variable = 'f'
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
                if (itIsNotFraction(numerator)) {
                    Fraction(numerator)
                } else {
                    numerator.last() as Fraction
                }
            }
            else {
                null
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

        this.cleanFraction()
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
        val valueKey = mutableListOf<Any>(0.0, '^', 'f')

        val toRemove = mutableListOf<MutableList<Any>>()
        for ((index, map) in numeratorMaps.withIndex()) {
            val remove = mutableListOf<Any>()
            for((k, v) in map) {
                if (v == 0.0 && k != valueKey) {
                    remove.add(k)

                    var removeIt: Any = 0
                    for (element in numeratorElements[index]) {
                        when (element) {
                            is UnknownEntity -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                            is Function -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                            is Fraction -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                        }
                    }

                    numeratorElements[index].remove(removeIt)
                }
            }
            toRemove.add(remove)
        }

        for ((index, _) in toRemove.withIndex()) {
            for (element in toRemove[index]) {
                numeratorMaps[index].remove(element)
                if (numeratorMaps[index].isEmpty()) {
                    numeratorMaps[index][valueKey] = 1.0
                    if (numeratorElements[index].isEmpty()) {
                        numeratorElements[index].add(UnknownEntity(1.0))
                    }
                }
            }
        }

        val removeMap = mutableListOf<Any>()
        for (map in numeratorMaps) {
            if (map.isEmpty()) {
                removeMap.add(map)
            }
        }

        for (i in removeMap) {
            numeratorMaps.remove(i)
        }
    }

    private fun cleanDenominatorMaps() {
        val valueKey = mutableListOf<Any>(0.0, '^', 'f')

        val toRemove = mutableListOf<MutableList<Any>>()
        for ((index, map) in denominatorMaps.withIndex()) {
            val remove = mutableListOf<Any>()
            for((k, v) in map) {
                if (v == 0.0 && k != valueKey) {
                    remove.add(k)

                    var removeIt: Any = 0
                    for (element in denominatorElements[index]) {
                        when (element) {
                            is UnknownEntity -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                            is Function -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                            is Fraction -> {
                                if (k == element.getKey()) {
                                    removeIt = element
                                    break
                                }
                            }
                        }
                    }

                    denominatorElements[index].remove(removeIt)
                }
            }
            toRemove.add(remove)
        }

        for ((index, _) in toRemove.withIndex()) {
            for (element in toRemove[index]) {
                denominatorMaps[index].remove(element)
                if (denominatorMaps[index].isEmpty()) {
                    denominatorMaps[index][valueKey] = 1.0
                    if (denominatorElements[index].isEmpty()) {
                        denominatorElements[index].add(UnknownEntity(1.0))
                    }
                }
            }
        }

        val removeMap = mutableListOf<Any>()
        for (map in denominatorMaps) {
            if (map.isEmpty()) {
                removeMap.add(map)
            }
        }

        for (i in removeMap) {
            denominatorMaps.remove(i)
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

            if (operatorIndex < numeratorOperators.size && outputNumerator.isNotEmpty()) {
                outputNumerator.add(numeratorOperators[operatorIndex])
                operatorIndex++
            }
            else {
                operatorIndex++
            }
        }
        if (outputNumerator.isNotEmpty() && outputNumerator.last() is Char) {
            outputNumerator.removeLast()
        }

        val result = shortenEquation(outputNumerator)

        numerator = if (equationHasOperators(result)) mutableListOf(Fraction(result)) else result
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

            if (operatorIndex < denominatorOperators.size && outputDenominator.isNotEmpty()) {
                outputDenominator.add(denominatorOperators[operatorIndex])
            }
            else {
                operatorIndex++
            }
        }
        if (outputDenominator.isNotEmpty() && outputDenominator.last() is Char) {
            outputDenominator.removeLast()
        }

        val result = shortenEquation(outputDenominator)

        denominator = if (equationHasOperators(result)) mutableListOf(Fraction(result)) else result
    }

    private fun cleanFraction() {
        val toRemove = mutableListOf<Any>()

        var noOperators = !equationHasOperators(numerator) && numerator.size != 1
        for (i in numerator) {
            when (i) {
                is Fraction -> if (i.isEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                is Function -> {
                    if (!i.isNotEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                    i.cleanFunction()
                }
                is UnknownEntity -> {
                    if (i.isEmpty()) toRemove.add(i)
                    else if (noOperators && i.isOne()) toRemove.add(i)
                }
            }
        }
        for (i in toRemove){
            numerator.remove(i)
        }

        if (denominator != null) {
            if (denominator!!.isEmpty()) {
                denominator = null
            }
            else if (isOne(denominator!!)) {
                denominator = null
            }
            else {
                noOperators = !equationHasOperators(denominator!!) && denominator!!.size != 1
                toRemove.clear()

                for (i in denominator!!) {
                    when (i) {
                        is Fraction -> if (i.isEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                        is Function -> {
                            if (!i.isNotEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                            i.cleanFunction()
                        }
                        is UnknownEntity -> {
                            if (i.isEmpty()) toRemove.add(i)
                            else if (noOperators && i.isOne()) toRemove.add(i)
                        }
                    }
                }

                for (i in toRemove){
                    denominator!!.remove(i)
                }
            }
        }

        if (multiplicative != null) {
            if (multiplicative!!.isEmpty()) {
                multiplicative = null
            }
            else {
                if (isOne(multiplicative!!.numerator) && (multiplicative!!.denominator == null || (multiplicative!!.denominator!!.isEmpty() || isOne(multiplicative!!.denominator!!)))) {
                    multiplicative = null
                }
                else {
                    noOperators = !equationHasOperators(multiplicative!!.numerator) && multiplicative!!.numerator.size != 1
                    toRemove.clear()

                    for (i in multiplicative!!.numerator) {
                        when (i) {
                            is Fraction -> if (i.isEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                            is Function -> {
                                if (!i.isNotEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                                i.cleanFunction()
                            }
                            is UnknownEntity -> {
                                if (i.isEmpty()) toRemove.add(i)
                                else if (noOperators && i.isOne()) toRemove.add(i)
                            }
                        }
                    }

                    for (i in toRemove){
                        multiplicative!!.numerator.remove(i)
                    }

                    if (multiplicative!!.denominator != null) {
                        if (isOne(multiplicative!!.denominator!!)) {
                            multiplicative!!.denominator = null
                        }
                        else {
                            noOperators = !equationHasOperators(multiplicative!!.denominator!!) && multiplicative!!.denominator!!.size != 1
                            toRemove.clear()

                            for (i in multiplicative!!.denominator!!) {
                                when (i) {
                                    is Fraction -> if (i.isEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                                    is Function -> {
                                        if (!i.isNotEmpty() || (i.powerTo == 0.0 && noOperators)) toRemove.add(i)
                                        i.cleanFunction()
                                    }
                                    is UnknownEntity -> {
                                        if (i.isEmpty()) toRemove.add(i)
                                        else if (noOperators && i.isOne()) toRemove.add(i)
                                    }
                                }
                            }

                            for (i in toRemove){
                                multiplicative!!.denominator!!.remove(i)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isUnknownEntity(): Boolean {
        return this.numerator.size == 1 && numerator.last() is UnknownEntity && (denominator == null || denominator!!.isEmpty()) && this.powerTo == 1.0
    }

    private fun isCount(): UnknownEntity? {
        return if (this.numerator.size == 1 && numerator.last() is UnknownEntity && (numerator.last() as UnknownEntity).onlyNumber() && (denominator == null || denominator!!.isEmpty())) {
            (numerator.last() as UnknownEntity)
        } else {
            null
        }
    }

    private fun isNumberFraction(): Boolean {
        return this.numerator.size == 1 && numerator.last() is UnknownEntity
                && (denominator != null && this.denominator!!.isNotEmpty() && this.denominator!!.size == 1 && denominator!!.last() is UnknownEntity )
                && (multiplicative == null || multiplicative!!.isEmpty())
                && this.powerTo == 1.0 && (this.count == null || this.count!!.isOne())
    }

    private fun getUnknownEntity(): UnknownEntity {
        return numerator.last() as UnknownEntity
    }

    private fun isFunction(): Boolean {
        return this.numerator.size == 1 && numerator.last() is Function && (denominator == null || denominator!!.isEmpty()) && (multiplicative == null || multiplicative!!.isEmpty()) && this.powerTo == 1.0 && (this.count == null || this.count!!.isOne())
    }

    private fun getFunction(): Function {
        return numerator.last() as Function
    }

    private fun shortenEquation(equation: MutableList<Any>): MutableList<Any> {
        // Make multiplications and division
        val grouped = mutableListOf<Any>()
        var pieceOfEquation = mutableListOf<Any>()

        var operator = '×'
        for (element in equation) {
            when (element) {
                is UnknownEntity -> {
                    var found = false
                    var i = 0
                    while (i < pieceOfEquation.size) {
                        if (pieceOfEquation[i] is UnknownEntity) {
                            if ((pieceOfEquation[i] as UnknownEntity) == element) {
                                if (operator == '/') {
                                    pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) / element
                                }
                                else {
                                    pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) * element
                                }
                                found = true
                                break
                            }
                        }
                        i++
                    }

                    if (!found) {
                        if (operator == '/') {
                            if (element.powerTo != null) {
                                element.powerTo = -element.powerTo!!
                            }
                        }
                        pieceOfEquation.add(element)
                    }

                    operator = '×'
                }
                is Function -> {
                    if (element.powerTo != 0.0) {
                        var found = false
                        var i = 0
                        while (i < pieceOfEquation.size) {
                            if (pieceOfEquation[i] is Function) {
                                if ((pieceOfEquation[i] as Function) == element) {
                                    if (operator == '×') {
                                        (pieceOfEquation[i] as Function).powerTo += element.powerTo
                                    }
                                    else {
                                        (pieceOfEquation[i] as Function).powerTo -= element.powerTo
                                    }
                                    found = true
                                    break
                                }
                            }
                            else if (pieceOfEquation[i] is Fraction) {
                                if ((pieceOfEquation[i] as Fraction).getKey() == element.getKey()) {
                                    if (operator == '×') {
                                        (pieceOfEquation[i] as Fraction).powerTo += element.powerTo
                                    }
                                    else {
                                        (pieceOfEquation[i] as Fraction).powerTo -= element.powerTo
                                    }
                                    found = true
                                    break
                                }
                            }
                            i++
                        }

                        if (!found) {
                            if (operator == '/') {
                                element.powerTo = -element.powerTo
                            }
                            pieceOfEquation.add(element)
                        }
                    }
                    else {
                        pieceOfEquation.add(UnknownEntity(1.0))
                    }
                    operator = '×'
                }
                is Fraction -> {
                    if (element.powerTo != 0.0) {
                        if (element.isUnknownEntity()) {
                            val entity = element.getUnknownEntity()
                            var found = false
                            var i = 0
                            while (i < pieceOfEquation.size) {
                                if (pieceOfEquation[i] is UnknownEntity) {
                                    if ((pieceOfEquation[i] as UnknownEntity) == entity) {
                                        if (operator == '×') {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as UnknownEntity) * entity
                                        } else {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as UnknownEntity) / entity
                                        }
                                        found = true
                                        break
                                    }
                                } else if (pieceOfEquation[i] is Fraction) {
                                    if ((pieceOfEquation[i] as Fraction).isNumberFraction()) {
                                        if (operator == '×') {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as Fraction) * entity
                                        } else {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as Fraction) / entity
                                        }
                                        found = true
                                        break
                                    }
                                }
                                i++
                            }
                            if (!found) {
                                if (operator == '/') {
                                    if (entity.powerTo != null) {
                                        entity.powerTo = -entity.powerTo!!
                                    }
                                }
                                pieceOfEquation.add(entity)
                            }
                        }
                        else if (element.isFunction()) {
                            val function = element.getFunction()
                            var found = false
                            var i = 0
                            while (i < pieceOfEquation.size) {
                                if (pieceOfEquation[i] is Function) {
                                    if ((pieceOfEquation[i] as Function) == function) {
                                        if (operator == '×') {
                                            (pieceOfEquation[i] as Function).powerTo += element.getPower()
                                        } else {
                                            (pieceOfEquation[i] as Function).powerTo -= element.getPower()
                                        }
                                        found = true
                                        break
                                    }
                                }
                                i++
                            }
                            if (!found) {
                                if (operator == '/') {
                                    function.powerTo = -function.powerTo
                                }
                                pieceOfEquation.add(function)
                            }
                        }
                        else if (element.isNumberFraction()) {
                            var found = false
                            var i = 0
                            while (i < pieceOfEquation.size) {
                                if (pieceOfEquation[i] is UnknownEntity) {
                                    if (operator == '×') {
                                        pieceOfEquation[i] =
                                            element * (pieceOfEquation[i] as UnknownEntity)
                                    } else {
                                        pieceOfEquation[i] =
                                            (pieceOfEquation[i] as UnknownEntity) / element
                                    }
                                    found = true
                                    break
                                } else if (pieceOfEquation[i] is Fraction) {
                                    if ((pieceOfEquation[i] as Fraction).isNumberFraction()) {
                                        if (operator == '×') {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as Fraction) * element
                                        } else {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as Fraction) / element
                                        }
                                        found = true
                                        break
                                    }
                                }
                                i++
                            }
                            if (!found) {
                                if (operator == '/') {
                                    element.powerTo = -element.powerTo
                                }
                                pieceOfEquation.add(element)
                            }
                        }
                        else {
                            if (element.isNumberFraction()) {
                                var found = false
                                var i = 0
                                while (i < pieceOfEquation.size) {
                                    if (element === pieceOfEquation[i]) {
                                        i++
                                        continue
                                    }
                                    if (pieceOfEquation[i] is UnknownEntity) {
                                        if (operator == '×') {
                                            pieceOfEquation[i] =
                                                element * (pieceOfEquation[i] as UnknownEntity)
                                        } else {
                                            pieceOfEquation[i] =
                                                (pieceOfEquation[i] as UnknownEntity) / element
                                        }
                                        found = true
                                        break
                                    } else if (pieceOfEquation[i] is Fraction) {
                                        if ((pieceOfEquation[i] as Fraction).isNumberFraction()) {
                                            if (operator == '×') {
                                                pieceOfEquation[i] =
                                                    (pieceOfEquation[i] as Fraction) * element
                                            } else {
                                                pieceOfEquation[i] =
                                                    (pieceOfEquation[i] as Fraction) / element
                                            }
                                            found = true
                                            break
                                        }
                                    }
                                    i++
                                }
                                if (!found) {
                                    if (operator == '/') {
                                        element.powerTo = -element.powerTo
                                    }
                                    pieceOfEquation.add(element)
                                }
                            } else {
                                var found = false
                                var i = 0
                                while (i < pieceOfEquation.size) {
                                    if (element === pieceOfEquation[i]) {
                                        i++
                                        continue
                                    }
                                    if (pieceOfEquation[i] is Fraction) {
                                        if ((pieceOfEquation[i] as Fraction) == element) {
                                            if (operator == '×') {
                                                (pieceOfEquation[i] as Fraction).powerTo += element.powerTo
                                            }
                                            else {
                                                (pieceOfEquation[i] as Fraction).powerTo -= element.powerTo
                                            }
                                        }
                                        else {
                                            if (operator == '×') {
                                                pieceOfEquation[i] =
                                                    (pieceOfEquation[i] as Fraction) * element
                                            } else {
                                                pieceOfEquation[i] =
                                                    (pieceOfEquation[i] as Fraction) / element
                                            }
                                        }
                                        found = true
                                        break
                                    }
                                    i++
                                }
                                if (!found) {
                                    if (operator == '/') {
                                        element.powerTo = -element.powerTo
                                    }
                                    pieceOfEquation.add(element)
                                }
                            }
                        }
                    }
                    else {
                        pieceOfEquation.add(UnknownEntity(1.0))
                    }
                    operator = '×'
                }
                is Char -> {
                    if (element == '+' || element == '-') {
                        grouped.addAll(pieceOfEquation)
                        pieceOfEquation = mutableListOf()
                        grouped.add(element)
                        operator = '×'
                    }
                    else {
                        operator = element
                    }
                }
            }
        }
        if (pieceOfEquation.isNotEmpty()) {
            grouped.addAll(pieceOfEquation)
        }

        return grouped
    }

    private fun convertDoublesToFractionInNumerator() {
        val toAppend = mutableListOf<Pair<Any, Int>>()
        val toRemove = mutableListOf<UnknownEntity>()
        for ((index, element) in numerator.withIndex()) {
            if (element is UnknownEntity) {
                if (element.multiplier != null) {
                    if (hasDecimal(element.multiplier!!)) {
                        toAppend.add(Pair(convertToFraction(element.multiplier!!), index))
                        toRemove.add(element)
                    }
                }
            }
        }

        for (i in toAppend) {
            numerator.add(i.second, i.first)
        }
        for (i in toRemove) {
            numerator.remove(i)
        }
    }

    private fun convertDoublesToFractionInDenominator() {
        val toAppend = mutableListOf<Pair<Any, Int>>()
        val toRemove = mutableListOf<UnknownEntity>()
        for ((index, element) in denominator!!.withIndex()) {
            if (element is UnknownEntity) {
                if (element.multiplier != null) {
                    if (hasDecimal(element.multiplier!!)) {
                        toAppend.add(Pair(convertToFraction(element.multiplier!!), index))
                        toRemove.add(element)
                    }
                }
            }
        }

        for (i in toAppend) {
            denominator!!.add(i.second, i.first)
        }
        for (i in toRemove) {
            denominator!!.remove(i)
        }
    }

    private fun onlyCalculable(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var i = 0
        var brackets = 0
        list.add('(')
        while (i < numerator.size) {
            if (numerator[i] is Fraction) {
                val buffer = (numerator[i] as Fraction).onlyCalculable()
                if (buffer.isNotEmpty()) {
                    if (list.isNotEmpty() && (list.last() !is Char || list.last() == ')')) {
                        list.add('×')
                    }
                    list.add('(')
                    list.addAll(buffer)
                    list.add(')')
                }
                else {
                    return mutableListOf()
                }
            }
            else if (numerator[i] is UnknownEntity) {
                if (list.isNotEmpty() && (list.last() !is Char || list.last() == ')')) {
                    list.add('×')
                    list.add('(')
                    brackets++
                }
                list.add(numerator[i] as UnknownEntity)
            }
            else if (numerator[i] is Char) {
                list.add(numerator[i])
                list.add('(')
                brackets++
            }
            else {
                return mutableListOf()
            }
            i++
        }
        i = 0
        while (i < brackets) {
            list.add(')')
            i++
        }
        list.add(')')

        if (denominator != null && denominator!!.isNotEmpty()) {
            i = 0
            brackets = 0
            list.add('/')
            list.add('(')
            while (i < denominator!!.size) {
                if (denominator!![i] is Fraction) {
                    val buffer = (denominator!![i] as Fraction).onlyCalculable()
                    if (buffer.isNotEmpty()) {
                        if (list.isNotEmpty() && (list.last() !is Char || list.last() == ')')) {
                            list.add('×')
                        }
                        list.add('(')
                        list.addAll(buffer)
                        list.add(')')
                    }
                    else {
                        return mutableListOf()
                    }
                }
                else if (denominator!![i] is UnknownEntity) {
                    if (list.isNotEmpty() && (list.last() !is Char || list.last() == ')')) {
                        list.add('×')
                        list.add('(')
                        brackets++
                    }
                    list.add(denominator!![i] as UnknownEntity)
                }
                else if (denominator!![i] is Char) {
                    list.add(denominator!![i])
                    list.add('(')
                    brackets++
                }
                else {
                    return mutableListOf()
                }
                i++
            }
            i = 0
            while (i < brackets) {
                list.add(')')
                i++
            }
            list.add(')')
        }

        if (list.size == 2) return mutableListOf()
        return list
    }

    private fun getEntitiesOfEquation(equation: MutableList<Any>): MutableList<UnknownEntity> {
        val entities = mutableListOf<UnknownEntity>()

        var negative = false
        for (element in equation) {
            when(element) {
                '+' -> negative = false
                '-' -> negative = true
                is UnknownEntity -> {
                    if (negative) {
                        element.multiplier = -element.multiplier!!
                        negative = false
                    }
                    entities.add(element)
                }
            }
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
                if (entityF == entityG) {
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
                        result.add(entity)
                    } else {
                        if (result.isNotEmpty()) {
                            result.add('+')
                        }
                        result.add(entity)
                    }
                }
                else {
                    // Recreate operators
                    var negative = false
                    if ((entityF.multiplier!! < 0 || entityG.multiplier!! < 0) && !(entityF.multiplier!! < 0 && entityG.multiplier!! < 0)) {
                        negative = true
                    }

                    if (negative) {
                        if (result.isNotEmpty()) {
                            result.add('-')
                        }
                        result.add(entityF)
                        result.add(entityG)
                    } else {
                        if (result.isNotEmpty()) {
                            result.add('+')
                        }
                        result.add(entityF)
                        result.add(entityG)
                    }
                }
            }
        }

        return result
    }

    private fun calculateEquation(input: MutableList<Any>, iterator: Int = 0, negativeFlag: Boolean = false): Pair<MutableList<Any>, Int>{
        var equation = mutableListOf<Any>()
        var index = iterator
        var negative = negativeFlag

        while (index < input.size) {
            when (input[index]) {
                '(' -> {
                    val subEquation = if (negative) {
                        calculateEquation(input,index+1, !negativeFlag)
                    }
                    else {
                        calculateEquation(input,index+1, negativeFlag)
                    }
                    index = subEquation.second
                    equation.addAll(subEquation.first)
                    continue
                }
                ')' -> {
                    return Pair(equation, ++index)
                }
                '×' -> {
                    val subEquation = if (negative) {
                        calculateEquation(input,index+1, !negativeFlag)
                    }
                    else {
                        calculateEquation(input,index+1, negativeFlag)
                    }

                    equation = multiplyTwoEquations(equation, subEquation.first)
                    index = subEquation.second
                    continue
                }
                '/' -> {
                    val subEquation = if (negative) {
                        calculateEquation(input,index+1, !negativeFlag)
                    }
                    else {
                        calculateEquation(input,index+1, negativeFlag)
                    }
                    equation = multiplyTwoEquations(equation, subEquation.first, divide = true)
                    index = subEquation.second
                    continue
                }
                '+' -> {
                    if (negativeFlag) {
                        equation.add('-')
                    }
                    else {
                        equation.add(input[index] as Char)
                    }
                    negative = false
                }
                '-' -> {
                    if (negativeFlag) {
                        equation.add('+')
                    }
                    else {
                        equation.add(input[index] as Char)
                    }
                    negative = true
                }
                is UnknownEntity -> {
                    if (equation.isNotEmpty()) {
                        if (equation.last() == '-') {
                            var found = false
                            for ((i, element) in equation.withIndex()) {
                                if (element is UnknownEntity) {
                                    if (element == (input[index] as UnknownEntity)) {
                                        equation[i] = element - (input[index] as UnknownEntity)
                                        found = true
                                        break
                                    }
                                    else if (element.onlyNumber() || (input[index] as UnknownEntity).onlyNumber()) {
                                        equation[i] = element - (input[index] as UnknownEntity)
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (!found) {
                                equation.add(input[index] as UnknownEntity)
                            }
                            else {
                                if (equation.last() is Char) {
                                    equation.removeLast()
                                }
                            }
                        }
                        else {
                            var found = false
                            for ((i, element) in equation.withIndex()) {
                                if (element is UnknownEntity) {
                                    if (element == (input[index] as UnknownEntity)) {
                                        equation[i] = element + (input[index] as UnknownEntity)
                                        found = true
                                        break
                                    }
                                    else if (element.onlyNumber() || (input[index] as UnknownEntity).onlyNumber()) {
                                        equation[i] = element + (input[index] as UnknownEntity)
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (!found) {
                                equation.add(input[index] as UnknownEntity)
                            }
                            else {
                                if (equation.last() is Char) {
                                    equation.removeLast()
                                }
                            }
                        }
                    }
                    else {
                        equation.add(input[index] as UnknownEntity)
                    }
                }
            }
            index++
        }

        val toRemove = mutableListOf<Int>()
        for ((i, element) in equation.withIndex()) {
            if (element is UnknownEntity) {
                if (element.multiplier == 0.0) {
                    toRemove.add(i)
                    if (i+1 < equation.size) {
                        if (equation[i+1] is Char) {
                            if (equation[i+1] == '+') {
                                toRemove.add(i+1)
                            }
                            else if (equation[i+1] == '-') {
                                if (i+2 < equation.size && equation[i+2] is UnknownEntity) {
                                    (equation[i+2] as UnknownEntity).multiplier = -(equation[i+2] as UnknownEntity).multiplier!!
                                }
                                toRemove.add(i+1)
                            }
                        }
                    }
                }
            }
        }

        for (i in toRemove) {
            equation.removeAt(i)

            for (j in toRemove.indices) {
                toRemove[j]--
            }
        }

        return Pair(equation, ++index)
    }

    private fun makeCalculations(input: MutableList<Any>): MutableList<Any>  {
        if (equationHasOperators(input)) {
            val transformedInput = mutableListOf<MutableList<Any>>()
            var piece = mutableListOf<Any>()

            for (element in input) {
                when (element) {
                    is Char -> {
                        if (element == '+' || element == '-') {
                            if (piece.isNotEmpty()) {
                                transformedInput.add(piece)
                                piece = mutableListOf()
                                transformedInput.add(mutableListOf(element))
                            }
                        }
                        else {
                            piece.add(element)
                        }
                    }
                    else -> piece.add(element)
                }
            }
            transformedInput.add(piece)

            var operator = '+'
            val toRemove = mutableListOf<Int>()
            for ((index, listA) in transformedInput.withIndex()) {
                if (listA.size == 1 && listA.last() is Char) continue
                for ((i, listB) in transformedInput.drop(index + 1).withIndex()) {
                    if (listB.size == 1 && listB.last() is Char) operator = listB.last() as Char
                    for ((j, elementA) in listA.withIndex()) {
                        for (elementB in listB) {
                            when (elementA) {
                                is Fraction -> {
                                    when (elementB) {
                                        is Fraction -> {
                                            val calculableA = calculateEquation(elementA.onlyCalculable()).first
                                            val calculableB = calculateEquation(elementB.onlyCalculable()).first
                                            if (calculableA.isNotEmpty() && calculableB.isNotEmpty()) {
                                                if (operator == '-') {
                                                    for ((k, element) in calculableB.withIndex()) {
                                                        if (element == '-') {
                                                            calculableB[k] = '+'
                                                        }
                                                        else if (element == '+')  {
                                                            calculableB[k] = '-'
                                                        }
                                                    }
                                                }
                                                if (operator == '-') {
                                                    elementA.numerator = calculateEquation((calculableA + '-' + calculableB).toMutableList()).first
                                                }
                                                else {
                                                    elementA.numerator = calculateEquation((calculableA + '+' + calculableB).toMutableList()).first
                                                }
                                                elementA.denominator = null
                                                if (i-1 > 0 && transformedInput[i-1].size == 1 && transformedInput[i-1].last() is Char) {
                                                    toRemove.add(i-1)
                                                    transformedInput[i-1].clear()
                                                }
                                                toRemove.add(i)
                                                listB.clear()
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (i in toRemove.size-1 downTo 0) {
                transformedInput.removeAt(toRemove[i])
            }

            val output = mutableListOf<Any>()

            for (i in transformedInput) {
                output.addAll(i)
            }

            return output
        }

        return input
    }

    private fun sortEverything() {
        val addToDenominator = mutableListOf<MutableList<Any>>()
        if (!equationHasOperators(numerator)) {
            for (element in numerator) {
                if (element is Fraction) {
                    if (element.denominator != null) {
                        if (equationHasOperators(element.denominator!!)) {
                            addToDenominator.add(mutableListOf(Fraction(element.denominator!!)))
                        }
                        else {
                            addToDenominator.add(element.denominator!!)
                        }
                        element.denominator = null
                    }
                }
            }
        }

        val addToNumerator = mutableListOf<MutableList<Any>>()
        if (denominator != null && !equationHasOperators(denominator!!)) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    if (element.denominator != null) {
                        if (equationHasOperators(element.denominator!!)) {
                            addToNumerator.add(mutableListOf(Fraction(element.denominator!!)))
                        }
                        else {
                            addToNumerator.add(element.denominator!!)
                        }
                        element.denominator = null
                    }
                }
            }
        }

        if (addToNumerator.isNotEmpty()) {
            for (i in addToNumerator) {
                numerator.addAll(i)
            }
        }

        if (addToDenominator.isNotEmpty()) {
            if (denominator == null) denominator = mutableListOf()
            for (i in addToDenominator) {
                denominator!!.addAll(i)
            }
        }
    }

    private fun itIsNotFraction(input: MutableList<Any>): Boolean {
        return !(input.size == 1 && input.last() is Fraction)
    }

    private fun getSeriesOfPowersTo(): List<MutableList<Double>> {
        val list = List<MutableList<Double>>(2){ mutableListOf() }

        for (i in numerator) {
            when (i) {
                is UnknownEntity -> list[0].add(if (i.powerTo != null) i.powerTo!! else 1.0)
                is Function -> list[0].add(i.powerTo)
                is Fraction -> list[0].addAll(i.getSeriesOfPowersTo().flatten())
            }
        }

        if (denominator != null) {
            for (i in numerator) {
                when (i) {
                    is UnknownEntity -> list[1].add(if (i.powerTo != null) i.powerTo!! else 1.0)
                    is Function -> list[1].add(i.powerTo)
                    is Fraction -> list[1].addAll(i.getSeriesOfPowersTo().flatten())
                }
            }
        }

        return list
    }

    private fun getPower(): Double {
        var result = this.powerTo

        for (i in numerator) {
            when (i) {
                is Function -> result *= i.powerTo
                is Fraction -> {
                    result *= i.powerTo
                    result *= i.getPower()
                }
            }
        }

        return result
    }

    operator fun times(other: Fraction): Fraction {
        if (other.isZero()) {
            return other
        }
        if (this.isEmpty() || this.isOne()) {
            return other
        }
        if (this.isZero()) {
            return this
        }
        if (other.isOne()) {
            return this
        }

        if (this.getKey() == other.getKey()) {
            if (this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()) {
                if (this.denominator == null && other.denominator == null) {
                    this.powerTo = this.getPower()
                    other.powerTo = other.getPower()
                    this.powerTo += other.powerTo
                }
                else {
                    this.powerTo += other.powerTo
                }
                if (other.count != null) {
                    this.count = this.count?.times(other.count!!)
                }
                return this
            }
        }

        this.getOutCount()
        other.getOutCount()

        this.getOutPowerTo()
        other.getOutPowerTo()

        if (other.numerator.isNotEmpty()) {
            if (equationHasOperators(numerator)) {
                if (equationHasOperators(other.numerator)) {
                    numerator = mutableListOf(Fraction(numerator), Fraction(other.numerator))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.addAll(other.numerator)
                }
            }
            else {
                if (equationHasOperators(other.numerator)) {
                    numerator.add(Fraction(other.numerator))
                }
                else {
                    numerator.addAll(other.numerator)
                }
            }
        }

        if (other.denominator != null) {
            if (denominator != null) {
                if (equationHasOperators(denominator!!)) {
                    if (equationHasOperators(other.denominator!!)) {
                        denominator = mutableListOf(Fraction(denominator!!), Fraction(other.denominator!!))
                    }
                    else {
                        denominator = mutableListOf(Fraction(denominator!!))
                        denominator!!.addAll(other.denominator!!)
                    }
                }
                else {
                    if (equationHasOperators(other.denominator!!)) {
                        denominator!!.add(Fraction(other.denominator!!))
                    }
                    else {
                        denominator!!.addAll(other.denominator!!)
                    }
                }
            }
            else {
                denominator = other.denominator!!
            }
        }

        if (this.numerator.isEmpty() && other.numerator.isNotEmpty()) {
            this.numerator = other.numerator
        }

        return this
    }

    operator fun times(other: Function): Fraction {
        if (this.isZero()) {
            return this
        }
        if (isOne(other.content)) {
            return this
        }

        this.getOutCount()
        this.getOutPowerTo()

        if (itIsNotFraction(this.numerator)) {
            this.numerator = mutableListOf(Fraction(this.numerator))
        }
        this.numerator.add(other)

        return this
    }

    operator fun times(other: UnknownEntity): Fraction {
        if (other.isZero()) {
            return Fraction(mutableListOf(UnknownEntity(0.0)))
        }
        if (this.isZero()) {
            return this
        }
        if (other.isOne() && this.isNotEmpty()) {
            return this
        }
        if (this.isEmpty() || this.isOne()) {
            return Fraction(mutableListOf(other))
        }

        this.getOutCount()
        this.getOutPowerTo()

        if (itIsNotFraction(this.numerator)) {
            this.numerator = mutableListOf(Fraction(this.numerator))
        }
        this.numerator.add(other)

        return this
    }

    private fun multiplyByItself(numeratorExp: MutableList<Any>, denominatorExp: MutableList<Any>?) {
        if (numeratorExp.isNotEmpty()) {
            if (equationHasOperators(numerator)) {
                if (equationHasOperators(numeratorExp)) {
                    numerator = mutableListOf(Fraction(numerator), Fraction(numeratorExp))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.addAll(numeratorExp)
                }
            }
            else {
                if (equationHasOperators(numeratorExp)) {
                    numerator.add(Fraction(numeratorExp))
                }
                else {
                    numerator.addAll(numeratorExp)
                }
            }
        }

        if (denominatorExp != null) {
            if (denominator != null) {
                if (equationHasOperators(denominator!!)) {
                    if (equationHasOperators(denominatorExp)) {
                        denominator = mutableListOf(Fraction(denominator!!), Fraction(denominatorExp))
                    }
                    else {
                        denominator = mutableListOf(Fraction(denominator!!))
                        denominator!!.addAll(denominatorExp)
                    }
                }
                else {
                    if (equationHasOperators(denominatorExp)) {
                        denominator!!.add(Fraction(denominatorExp))
                    }
                    else {
                        denominator!!.addAll(denominatorExp)
                    }
                }
            }
            else {
                denominator = denominatorExp
            }
        }
    }

    operator fun div(other: Fraction): Fraction {
        if (this.isZero()) {
            return this
        }
        if (other.isZero()) {
            return other
        }

        if (other.isZero()) {
            return Fraction()
        }

        if (other.isOne()) {
            return this
        }

        if (this.getKey() == other.getKey()) {
            if (this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()) {
                if (this.denominator == null && other.denominator == null) {
                    this.powerTo = this.getPower()
                    other.powerTo = other.getPower()
                    this.powerTo -= other.powerTo
                }
                else {
                    this.powerTo -= other.powerTo
                }
                if (other.count != null) {
                    this.count = this.count?.div(other.count!!)
                }
                return this
            }
        }

        this.getOutCount()
        other.getOutCount()

        this.getOutPowerTo()
        other.getOutPowerTo()

        if (other.denominator != null) {
            if (equationHasOperators(numerator)) {
                if (equationHasOperators(other.denominator!!)) {
                    numerator = mutableListOf(Fraction(mutableListOf(Fraction(numerator), Fraction(other.denominator!!))))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.addAll(other.denominator!!)
                }
            }
            else {
                if (equationHasOperators(other.denominator!!)) {
                    numerator.add(Fraction(other.denominator!!))
                }
                else {
                    numerator.addAll(other.denominator!!)
                }
            }
        }

        if (denominator != null) {
            if (equationHasOperators(denominator!!)) {
                if (equationHasOperators(other.numerator)) {
                    denominator = mutableListOf(Fraction(mutableListOf(Fraction(denominator!!), Fraction(other.numerator))))
                }
                else {
                    denominator = mutableListOf(Fraction(denominator!!))
                    denominator!!.addAll(other.numerator)
                }
            }
            else {
                if (equationHasOperators(other.numerator)) {
                    denominator!!.add(Fraction(other.numerator))
                }
                else {
                    denominator!!.addAll(other.numerator)
                }
            }
        }
        else {
            denominator = other.numerator
        }

        if (this.numerator.isEmpty() && other.denominator != null) {
            this.numerator = other.denominator!!
        }

        return this
    }

    operator fun div(other: Function): Fraction {
        if (this.isZero()) {
            return this
        }
        if (isOne(other.content)) {
            return this
        }

        this.getOutCount()
        this.getOutPowerTo()

        if (this.denominator == null) denominator = mutableListOf()
        if (itIsNotFraction(this.denominator!!)) {
            this.denominator = mutableListOf(Fraction(this.denominator!!))
        }
        this.denominator!!.add(other)

        return this
    }

    operator fun div(other: UnknownEntity): Fraction {
        if (this.isZero()) {
            return this
        }
        if (other.isZero()) {
            return Fraction(mutableListOf(UnknownEntity(0.0)))
        }
        if (other.isOne() && this.isNotEmpty()) {
            return this
        }

        this.getOutCount()
        this.getOutPowerTo()

        if (this.denominator == null) denominator = mutableListOf()
        if (itIsNotFraction(this.denominator!!)) {
            this.denominator = mutableListOf(Fraction(this.denominator!!))
        }
        this.denominator!!.add(other)

        return this
    }

    private fun divideByItself(numeratorExp: MutableList<Any>, denominatorExp: MutableList<Any>?) {
        if (denominatorExp != null) {
            if (equationHasOperators(numerator)) {
                if (equationHasOperators(denominatorExp)) {
                    numerator = mutableListOf(Fraction(mutableListOf(Fraction(numerator), Fraction(denominatorExp))))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.addAll(denominatorExp)
                }
            }
            else {
                if (equationHasOperators(denominatorExp)) {
                    numerator.add(Fraction(denominatorExp))
                }
                else {
                    numerator.addAll(denominatorExp)
                }
            }
        }

        if (denominator != null) {
            if (equationHasOperators(denominator!!)) {
                if (equationHasOperators(numeratorExp)) {
                    denominator = mutableListOf(Fraction(mutableListOf(Fraction(denominator!!), Fraction(numeratorExp))))
                }
                else {
                    denominator = mutableListOf(Fraction(denominator!!))
                    denominator!!.addAll(numeratorExp)
                }
            }
            else {
                if (equationHasOperators(numeratorExp)) {
                    denominator!!.add(Fraction(numeratorExp))
                }
                else {
                    denominator!!.addAll(numeratorExp)
                }
            }
        }
        else {
            denominator = numeratorExp
        }
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is Fraction) {
            return this.getKey() == other.getKey() && this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()
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