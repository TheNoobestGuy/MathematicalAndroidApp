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

    private fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
    }

    private fun convertToFraction(input: Double): Fraction {
        var divided = input
        var divider = 1.0
        while (hasDecimal(divided)) {
            divided *= 10.0
            divider *= 10.0
        }

        return Fraction(mutableListOf(UnknownEntity(divided)), mutableListOf(UnknownEntity(divider)))
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

//        if (multiplicative != null) {
//            if (multiplicative!!.numerator.isNotEmpty()) {
//                i = 0
//                brackets = 0
//                list.add('×')
//                list.add('(')
//                while (i < multiplicative!!.numerator.size) {
//                    if (multiplicative!!.numerator[i] is UnknownEntity && (multiplicative!!.numerator[i] as UnknownEntity).onlyNumber()) {
//                        if (list.last() !is Char) {
//                            list.add('×')
//                        }
//                        list.addAll((multiplicative!!.numerator[i] as UnknownEntity).getOriginal())
//                    }
//                    else if (multiplicative!!.numerator[i] is Char) {
//                        list.add(multiplicative!!.numerator[i])
//                        list.add('(')
//                        brackets++
//                    }
//                    else {
//                        return mutableListOf()
//                    }
//                    i++
//                }
//                i = 0
//                while (i < brackets) {
//                    list.add(')')
//                    i++
//                }
//                list.add(')')
//            }
//
//            if (multiplicative!!.denominator != null && multiplicative!!.denominator!!.isNotEmpty()) {
//                i = 0
//                brackets = 0
//                list.add('/')
//                list.add('(')
//                while (i < multiplicative!!.denominator!!.size) {
//                    if (multiplicative!!.denominator!![i] is UnknownEntity && (multiplicative!!.denominator!![i] as UnknownEntity).onlyNumber()) {
//                        if (list.last() !is Char) {
//                            list.add('/')
//                        }
//                        list.addAll((multiplicative!!.denominator!![i] as UnknownEntity).getOriginal())
//                    }
//                    else if (multiplicative!!.denominator!![i] is Char) {
//                        list.add(multiplicative!!.denominator!![i])
//                        list.add('(')
//                        brackets++
//                    }
//                    else {
//                        return mutableListOf()
//                    }
//                    i++
//                }
//                i = 0
//                while (i < brackets) {
//                    list.add(')')
//                    i++
//                }
//                list.add(')')
//            }
//        }

        if (list.size == 3) {
            return mutableListOf()
        }
        return list
    }

    private fun convertToSingleElementIfCalculable() {
        val equation = this.isCalculable()

        if (equation.isNotEmpty() && this.powerTo == 1.0 && this.count == 1.0) {
            val calculator = Calculator()
            val result = calculator.calculateEquation(equation).first

            if (result != 1.0) {
                this.numerator.clear()
                this.denominator = null

                if (multiplicative == null) {
                    multiplicative = Fraction()
                }

                if (hasDecimal(result)) {
                    val fraction = convertToFraction(result)
                    multiplicative!!.numerator.add(fraction.numerator)
                    if (multiplicative!!.denominator == null) {
                        multiplicative!!.denominator = fraction.denominator
                    }
                    else {
                        multiplicative!!.denominator!!.add(fraction.denominator!!)
                    }
                }
                else {
                    multiplicative!!.numerator.add(UnknownEntity(result))
                }
            }
        }
    }

    init {
        if (denominator != null && denominator!!.isEmpty()) {
            denominator = null
        }
        if (multiplicative != null && multiplicative!!.isEmpty()) {
            multiplicative = null
        }

        cleanFraction()
        shortenEverything()
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

        if (equationHasOperators(this.numerator) || (this.denominator != null && this.denominator!!.isNotEmpty() && equationHasOperators(this.denominator!!))) {
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
                            if (switch) {
                                if (element.onlyNumber()) {
                                    list.add(Triple(element.getKey(value = true), -element.multiplier!!, element))
                                }
                                else {
                                    list.add(Triple(element.getKey(), -element.multiplier!!, element))
                                }
                            }
                            else {
                                if (element.onlyNumber()) {
                                    list.add(Triple(element.getKey(value = true), element.multiplier!!, element))
                                }
                                else {
                                    list.add(Triple(element.getKey(), element.multiplier!!, element))
                                }
                            }
                        }
                        else {
                            if (switch) {
                                list.add(Triple(element.getKey(value = true), -1.0, element))
                            }
                            else {
                                list.add(Triple(element.getKey(value = true), 1.0, element))
                            }
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
                        element.getOutPowerTo()
                        list.addAll(element.getKeys(switch))
                    }
                }
            }

            if (denominator != null) {
                if (equationHasOperators(denominator!!)) {
                    if (switch) {
                        list.add(Triple(this.getKey(), this.powerTo, this))
                    }
                    else {
                        list.add(Triple(this.getKey(), -this.powerTo, this))
                    }
                }
                for (element in denominator!!) {
                    when (element) {
                        is UnknownEntity -> {
                            if (element.multiplier != null) {
                                if (switch) {
                                    if (element.onlyNumber()) {
                                        list.add(Triple(element.getKey(value = true), element.multiplier!!, element))
                                    }
                                    else {
                                        list.add(Triple(element.getKey(), element.multiplier!!, element))
                                    }
                                }
                                else {
                                    if (element.onlyNumber()) {
                                        list.add(Triple(element.getKey(value = true), -element.multiplier!!, element))
                                    }
                                    else {
                                        list.add(Triple(element.getKey(), -element.multiplier!!, element))
                                    }
                                }
                            }
                            else {
                                if (switch) {
                                    list.add(Triple(element.getKey(value = true), 1.0, element))
                                }
                                else {
                                    list.add(Triple(element.getKey(value = true), -1.0, element))
                                }
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
                            element.getOutPowerTo()
                            list.addAll(element.getKeys(!switch))
                        }
                    }
                }
            }
        }

        return list
    }

    private fun getOutMultiplicative() {
        val toRemoveFromNumerator = mutableListOf<Any>()
        val toRemoveFromDenominator = mutableListOf<Any>()
        var i = 0
        while (i < numerator.size) {
            var index = i
            if (numerator[i] is Fraction) {
                if ((numerator[i] as Fraction).multiplicative != null) {
                    for (j in (numerator[i] as Fraction).multiplicative!!.numerator) {
                        when (j) {
                            is UnknownEntity -> {
                                if (j.powerTo != null) {
                                    if (j.powerTo!! < 0) {
                                        numerator.add(++index, '/')
                                        j.powerTo = -j.powerTo!!
                                    }
                                }
                            }
                            is Fraction -> {
                                if (j.powerTo < 0) {
                                    numerator.add(++index, '/')
                                    j.powerTo = -j.powerTo
                                }
                            }
                            is Function -> {
                                if (j.powerTo < 0) {
                                    numerator.add(++index, '/')
                                    j.powerTo = -j.powerTo
                                }
                            }
                        }
                        if (withoutGCD) {
                            numerator.add(++index, '(')
                            numerator.add(++index, j)
                            numerator.add(++index, ')')
                        } else {
                            numerator.add(++index, j)
                        }
                    }
                    if ((numerator[i] as Fraction).multiplicative!!.denominator != null) {
                        for (j in (numerator[i] as Fraction).multiplicative!!.denominator!!) {
                            when (j) {
                                is UnknownEntity -> {
                                    if (j.powerTo != null) {
                                        if (j.powerTo!! > 0) {
                                            numerator.add(++index, '/')
                                        }
                                        else {
                                            j.powerTo = -j.powerTo!!
                                        }
                                    }
                                    else {
                                        numerator.add(++index, '/')
                                    }
                                }
                                is Fraction -> {
                                    if (j.powerTo > 0) {
                                        numerator.add(++index, '/')
                                    }
                                    else {
                                        j.powerTo = -j.powerTo
                                    }
                                }
                                is Function -> {
                                    if (j.powerTo > 0) {
                                        numerator.add(++index, '/')
                                    }
                                    else {
                                        j.powerTo = -j.powerTo
                                    }
                                }
                            }
                            if (withoutGCD) {
                                numerator.add(++index, '(')
                                numerator.add(++index, j)
                                numerator.add(++index, ')')
                            } else {
                                numerator.add(++index, j)
                            }
                        }
                    }
                    (numerator[i] as Fraction).multiplicative = null

                    if ((numerator[i] as Fraction).isEmpty()) {
                        toRemoveFromNumerator.add(numerator[i])
                    }
                }
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
                            when (j) {
                                is UnknownEntity -> {
                                    if (j.powerTo != null) {
                                        if (j.powerTo!! < 0) {
                                            denominator!!.add(++index, '/')
                                            j.powerTo = -j.powerTo!!
                                        }
                                    }
                                }
                                is Fraction -> {
                                    if (j.powerTo < 0) {
                                        denominator!!.add(++index, '/')
                                        j.powerTo = -j.powerTo
                                    }
                                }
                                is Function -> {
                                    if (j.powerTo < 0) {
                                        denominator!!.add(++index, '/')
                                        j.powerTo = -j.powerTo
                                    }
                                }
                            }
                            if (withoutGCD) {
                                denominator!!.add(++index, '(')
                                denominator!!.add(++index, j)
                                denominator!!.add(++index, ')')
                            } else {
                                denominator!!.add(++index, j)
                            }
                        }
                        if ((denominator!![i] as Fraction).multiplicative!!.denominator != null) {
                            for (j in (denominator!![i] as Fraction).multiplicative!!.denominator!!) {
                                when (j) {
                                    is UnknownEntity -> {
                                        if (j.powerTo != null) {
                                            if (j.powerTo!! > 0) {
                                                denominator!!.add(++index, '/')
                                            }
                                            else {
                                                j.powerTo = -j.powerTo!!
                                            }
                                        }
                                        else {
                                            denominator!!.add(++index, '/')
                                        }
                                    }
                                    is Fraction -> {
                                        if (j.powerTo > 0) {
                                            denominator!!.add(++index, '/')
                                        }
                                        else {
                                            j.powerTo = -j.powerTo
                                        }
                                    }
                                    is Function -> {
                                        if (j.powerTo > 0) {
                                            denominator!!.add(++index, '/')
                                        }
                                        else {
                                            j.powerTo = -j.powerTo
                                        }
                                    }
                                }
                                if (withoutGCD) {
                                    denominator!!.add(++index, '(')
                                    denominator!!.add(++index, j)
                                    denominator!!.add(++index, ')')
                                } else {
                                    denominator!!.add(++index, j)
                                }
                            }
                        }
                        (denominator!![i] as Fraction).multiplicative = null

                        if ((denominator!![i] as Fraction).isEmpty()) {
                            toRemoveFromDenominator.add(denominator!![i])
                        }
                    }
                }
                i++
            }
        }

        for (k in toRemoveFromNumerator) {
            numerator.remove(k)
        }

        for (k in toRemoveFromDenominator) {
            denominator!!.remove(k)
        }
    }

    private fun getOutMultiplication() {
        if (multiplicative != null) {
            if (numerator.isEmpty() && (denominator == null || denominator!!.isEmpty())) {
                numerator = multiplicative!!.numerator
                denominator = multiplicative!!.denominator
            }
            else if (denominator == null || denominator!!.isEmpty()) {
                if (multiplicative!!.numerator.isNotEmpty()) {
                    if (itIsNotFraction(numerator) && equationHasOperators(numerator)) {
                        numerator = mutableListOf(Fraction(numerator))
                    }
                    numerator.addAll(multiplicative!!.numerator)
                }
                if (multiplicative!!.denominator != null) {
                    denominator = multiplicative!!.denominator
                }
            }
            else {
                if (multiplicative!!.numerator.isNotEmpty()) {
                    if (itIsNotFraction(numerator) && equationHasOperators(numerator)) {
                        numerator = mutableListOf(Fraction(numerator))
                    }
                    numerator.addAll(multiplicative!!.numerator)
                }
                if (multiplicative!!.denominator != null) {
                    if (denominator!!.isNotEmpty()) {
                        if (itIsNotFraction(denominator!!) && equationHasOperators(denominator!!)) {
                            denominator = mutableListOf(Fraction(denominator!!))
                        }
                        denominator!!.addAll(multiplicative!!.denominator!!)
                    }
                    else {
                        denominator = multiplicative!!.denominator
                    }
                }
            }
        }

        multiplicative = null
    }

    private fun getOutPowerTo() {
        if (this.powerTo > 1.0) {
            var newFraction = Fraction()
            var i = 0.0
            val limit = this.powerTo
            this.powerTo = 1.0
            while  (i < limit) {
                newFraction *= this
                i += 1.0
            }

            newFraction.cleanFraction()
            newFraction.getOutMultiplicative()
            newFraction.getOutMultiplication()
            newFraction.shortenFraction()
            newFraction.numerator =  shortenEquation(newFraction.numerator)
            if (newFraction.denominator != null) {
                newFraction.denominator =  shortenEquation(newFraction.denominator!!)
            }
            this.numerator = newFraction.numerator
            this.denominator = newFraction.denominator
        }
        else if (this.powerTo < 0.0){
            var newFraction = Fraction()
            var i = this.powerTo
            this.powerTo = 1.0
            while  (i < 0) {
                newFraction /= this
                i += 1.0
            }
            newFraction.cleanFraction()
            newFraction.getOutMultiplicative()
            newFraction.getOutMultiplication()
            newFraction.shortenFraction()
            newFraction.numerator =  shortenEquation(newFraction.numerator)
            if (newFraction.denominator != null) {
                newFraction.denominator =  shortenEquation(newFraction.denominator!!)
            }
            this.numerator = newFraction.numerator
            this.denominator = newFraction.denominator
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

    private fun shortenEverything() {
        getOutMultiplicative()
        getOutMultiplication()

        numerator = shortenEquation(numerator)
        if (denominator != null) denominator = shortenEquation(denominator!!)

        if (denominator != null) {
            val list = mutableListOf<MutableList<Any>>()

            for (divided in numerator) {
                val entry = mutableListOf<Any>()
                for (divider in denominator!!) {
                    if (divided is UnknownEntity) {
                        if (divider is UnknownEntity) {
                            entry.add(divider)
                            break
                        }
                    }
                    if (divided == divider) {
                        entry.add(divider)
                        break
                    }
                }

                list.add(entry)
            }

            // Find common unknown
            val commonEntity = UnknownEntity()
            for (array in list) {
                for (i in array) {
                    if (i is UnknownEntity) {
                        if (commonEntity.multiplier == null) {
                            commonEntity.multiplier = i.multiplier
                        }
                        else {
                            if (commonEntity.multiplier!! > i.multiplier!!) {
                                commonEntity.multiplier = i.multiplier
                            }
                        }

                        if (commonEntity.powerTo != null) {
                            if (i.powerTo != null) {
                                if (commonEntity.powerTo!! > i.powerTo!!) {
                                    commonEntity.powerTo = i.powerTo
                                }
                            }
                            else {
                                commonEntity.powerTo = null
                            }
                        }
                        else {
                            commonEntity.powerTo = i.powerTo
                        }
                    }
                }
            }
            if ((commonEntity.multiplier == 1.0 && commonEntity.onlyNumber()) || commonEntity.multiplier == 0.0) commonEntity.clear()

            val map = hashMapOf<Any, Int>()
            for (array in list) {
                for (i in array) {
                    map[i] = map.getOrDefault(i, 0) + 1
                }
            }

            val commonEntities = map.filter { it.value == list.size }.keys
            val toRemoveNumerator = mutableListOf<Any>()
            val toRemoveDenominator = mutableListOf<Any>()

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
                allMultipliers.add(Fraction(mutableListOf(UnknownEntity(round(element * 1000) / 1000))))
                val decimalPoint = countDecimalPlaces(round(element * 1000) / 1000)
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

            val allCommonGCD = commonGCD.filter { (_, v) -> v == allGCD.size }.keys
            var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
            for (value in allCommonGCD) {
                if (gcd < value) {
                    gcd = value
                }
            }

            if (!(gcd == 1 && decimalPoint == 1)) {
                for (entity in commonEntities) {
                    if (entity is UnknownEntity && !commonEntity.isEmpty()) {
                        for (element in numerator) {
                            if (element is UnknownEntity) {
                                element.multiplier = element.multiplier?.div(commonEntity.multiplier!!)
                                if (element.powerTo != null) {
                                    if (commonEntity.powerTo != null) {
                                        element.powerTo = element.powerTo?.minus(commonEntity.powerTo!!)
                                    }
                                }
                            }
                        }
                        for (element in denominator!!) {
                            if (element is UnknownEntity) {
                                element.multiplier = element.multiplier?.div(commonEntity.multiplier!!)
                                if (element.powerTo != null) {
                                    if (commonEntity.powerTo != null) {
                                        element.powerTo = element.powerTo?.minus(commonEntity.powerTo!!)
                                    }
                                }
                            }
                        }
                    }
                    else {
                        for (element in numerator) {
                            if (entity == element) {
                                toRemoveNumerator.add(element)
                            }
                        }
                        for (element in denominator!!) {
                            if (entity == element) {
                                toRemoveDenominator.add(element)
                            }
                        }
                    }
                }

                for (i in toRemoveNumerator) {
                    numerator.remove(i)
                }
                for (i in toRemoveDenominator) {
                    denominator!!.remove(i)
                }
            }
        }
    }

    fun setFraction() {
        println("INPUT")
        println(this)

        clearMaps()
        shortenEverything()

        println("shortenEverything")
        println(this)

        updateFractionNumerator()
        if (denominator != null) updateFractionDenominator()

        if (!withoutGCD) {
            setMultiplicative(getMultiplicativeNumerator(), getMultiplicativeDenominator())
        }

        if (multiplicative != null) multiplicative!!.shortenFraction()

        shortenEveryFractionNumerator()
        if (denominator != null) shortenEveryFractionDenominator()
        if (denominator != null) shortenNumeratorWithDenominator()

        cleanNumeratorMaps()
        cleanDenominatorMaps()

        rebuildFractionNumerator()
        if (denominator != null) rebuildFractionDenominator()
        if (denominator != null && denominator!!.isEmpty()) denominator = null

        println("AFTER REBUILD")
        println(this)

        cleanFraction()
        convertToSingleElementIfCalculable()

        shortenEverything()

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
        var lastDouble = false
        while (i < equation.size) {
            when (equation[i]) {
                '(' -> {
                    brackets.add(i)
                    lastDouble = false
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
                    lastDouble = false
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
                    lastDouble = false
                }
                is Double -> {
                    if (i+1 < equation.size && equation[i+1] == '(') {
                        if (i-1 > 0 && equation[i-1] == '(') {
                            operators.add(i-1)
                        }
                        else {
                            operators.add(i+1)
                        }
                    }

                    if (lastDouble) {
                        equation.removeAt(i)
                        i--
                    }
                    else {
                        lastDouble = true
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
        return key.size == 1 && key.last() == 1.0 && this.count == 1.0
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
                content.add(0, '(')

                if (multiplicative != null && !multiplicative!!.isOne()) {
                    fraction.add('(')
                    fraction.addAll(sortFraction(multiplicative!!.numerator, flatFraction = flatFraction))
                    fraction.add('(')
                    fraction.add('(')
                }
                else {
                    multiplicative = null
                }

                if (content.isNotEmpty() || multiplicative != null || (denominator != null && denominator!!.isNotEmpty())) {
                    val denominatorExists = (denominator != null && denominator!!.isNotEmpty()) || (multiplicative != null && multiplicative!!.denominator != null)

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

                        if (denominator != null) {
                            val buffer = sortFraction(denominator!!, flatFraction = flatFraction)
                            fraction.addAll(buffer)
                        }
                        fraction.add(')')
                    }
                }

                if (multiplicative != null && !multiplicative!!.isOne() && content.isNotEmpty()) {
                    fraction.add(')')
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

                if (count != 1.0 && !flatFraction && !withoutCount) {
                    fraction.add(')')
                }

                fraction.add(')')
            }
        }

        if (!withMultiplication) {
            return cleanEquation(getInsideOfFraction(fraction, flatFraction = flatFraction, withMultiplication = false))
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
        var entity = UnknownEntity(1.0)

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
                    element.getOutPowerTo()
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (numeratorMap[key.first] == null && key.third !is UnknownEntity) {
                                numeratorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                if (sign == '/') {
                                    if (key.second < 0) {
                                        entity *= key.third as UnknownEntity
                                    }
                                    else {
                                        entity /= key.third as UnknownEntity
                                    }
                                }
                                else {
                                    if (key.second < 0) {
                                        entity /= key.third as UnknownEntity
                                    }
                                    else {
                                        entity *= key.third as UnknownEntity
                                    }
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
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    numeratorMap[entity.getKey(value = true)] = entity.multiplier!!
                                }
                                else {
                                    numeratorMap[entity.getKey()] = entity.multiplier!!
                                }

                                numeratorElement.add(entity)
                                entity = UnknownEntity(1.0)
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
        var entity = UnknownEntity(1.0)

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
                    element.getOutPowerTo()
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (denominatorMap[key.first] == null && key.third !is UnknownEntity) {
                                denominatorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                if (sign == '/') {
                                    if (key.second < 0) {
                                        entity *= key.third as UnknownEntity
                                    }
                                    else {
                                        entity /= key.third as UnknownEntity
                                    }
                                }
                                else {
                                    if (key.second < 0) {
                                        entity /= key.third as UnknownEntity
                                    }
                                    else {
                                        entity *= key.third as UnknownEntity
                                    }
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
                            if (!entity.isEmpty()) {
                                if (entity.onlyNumber()) {
                                    denominatorMap[entity.getKey(value = true)] = entity.multiplier!!
                                }
                                else {
                                    denominatorMap[entity.getKey()] = entity.multiplier!!
                                }

                                denominatorElement.add(entity)
                                entity = UnknownEntity(1.0)
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
                                if (!(commonForNumerator[newKey] == 1.0 || commonForNumerator[newKey] == 0.0)) {
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
                                if ((numeratorElements[index][element] as UnknownEntity).powerTo == null) {
                                    (numeratorElements[index][element] as UnknownEntity).powerTo = 0.0
                                }
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
                                    if ((numeratorElements[index][element] as Function).powerTo == 0.0) {
                                        numeratorOutput.add(numeratorElements[index].removeAt(element))
                                        (numeratorOutput.last() as Function).powerTo = 1.0
                                        element--
                                    }
                                    else {
                                        numeratorOutput.add((numeratorElements[index][element] as Function))
                                    }
                                    commonsToFind[key] = commonsToFind[key]!! - 1
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
                                (numeratorElements[index][element] as Fraction).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    if ((numeratorElements[index][element] as Fraction).powerTo == 0.0) {
                                        numeratorOutput.add(numeratorElements[index].removeAt(element))
                                        (numeratorOutput.last() as Fraction).powerTo = 1.0
                                        element--
                                    }
                                    else {
                                        numeratorOutput.add((numeratorElements[index][element] as Fraction))
                                    }
                                    commonsToFind[key] = commonsToFind[key]!! - 1
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
            else {
                if (entity.powerTo != 0.0) {
                    numeratorOutput.add(UnknownEntity(1.0, entity.variable, entity.powerTo))
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
                            if (itIsUnknown(k)) {
                                val newPower = min(k.first() as Double, key.first() as Double)
                                val newKey = mutableListOf(newPower, key[1], key.last())
                                commonForDenominator[newKey] = if (value < v) value else v
                                if (key != newKey) {
                                    toRemove.add(key)
                                }
                                if (!(commonForDenominator[newKey] == 1.0 || commonForDenominator[newKey] == 0.0)) {
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
                                if ((denominatorElements[index][element] as UnknownEntity).powerTo == null) {
                                    (denominatorElements[index][element] as UnknownEntity).powerTo = 0.0
                                }

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
                                    if ((denominatorElements[index][element] as Function).powerTo == 0.0) {
                                        denominatorOutput.add(denominatorElements[index].removeAt(element))
                                        (denominatorOutput.last() as Function).powerTo = 1.0
                                        element--
                                    }
                                    else {
                                        denominatorOutput.add((denominatorElements[index][element] as Function))
                                    }
                                    commonsToFind[key] = commonsToFind[key]!! - 1
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
                                (denominatorElements[index][element] as Fraction).powerTo -= v
                                if (commonsToFind[key]!! > 0) {
                                    if ((denominatorElements[index][element] as Fraction).powerTo == 0.0) {
                                        denominatorOutput.add(denominatorElements[index].removeAt(element))
                                        (denominatorOutput.last() as Fraction).powerTo = 1.0
                                        element--
                                    }
                                    else {
                                        denominatorOutput.add((denominatorElements[index][element] as Fraction))
                                    }
                                    commonsToFind[key] = commonsToFind[key]!! - 1
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

        if (entity != null) {
            // Get all multipliers of unknown entities
            val allDenominatorMultipliers = mutableListOf<Fraction>()
            var biggestDecimalPoint = 0
            for (list in denominatorElements) {
                for (element in list) {
                    if (element is UnknownEntity) {
                        allDenominatorMultipliers.add(
                            Fraction(
                                mutableListOf(
                                    UnknownEntity(
                                        round(
                                            element.multiplier!! * 1000
                                        ) / 1000
                                    )
                                )
                            )
                        )
                        val decimalPoint =
                            countDecimalPlaces(round(element.multiplier!! * 1000) / 1000)
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
            } else {
                if (entity.powerTo != 0.0) {
                    denominatorOutput.add(
                        UnknownEntity(
                            1.0,
                            entity.variable,
                            entity.powerTo
                        )
                    )
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
        this.getOutMultiplicative()
        this.getOutMultiplication()
        this.numerator = shortenEquation(this.numerator)
        if (this.denominator != null) {
            this.denominator = shortenEquation(this.denominator!!)
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
        val valueKey = mutableListOf<Any>(0.0, '^', 'x')

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
        val valueKey = mutableListOf<Any>(0.0, '^', 'x')

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
            else {
                outputNumerator.add(UnknownEntity(1.0))
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
            else {
                outputDenominator.add(UnknownEntity(1.0))
            }

            if (operatorIndex < denominatorOperators.size && outputDenominator.isNotEmpty()) {
                outputDenominator.add(denominatorOperators[operatorIndex])
            }
            else {
                operatorIndex++
            }
        }

        val result = shortenEquation(outputDenominator)

        denominator = if (equationHasOperators(result)) mutableListOf(Fraction(result)) else result
    }

    private fun cleanFraction() {
        val toRemove = mutableListOf<Any>()


        var noOperators = !equationHasOperators(numerator) && numerator.size != 1
        for (i in numerator) {
            when (i) {
                is Fraction -> if (i.isEmpty()) toRemove.add(i)
                is Function -> {
                    if (!i.isNotEmpty()) toRemove.add(i)
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
                        is Fraction -> if (i.isEmpty()) toRemove.add(i)
                        is Function -> {
                            if (!i.isNotEmpty()) toRemove.add(i)
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
                            is Fraction -> if (i.isEmpty()) toRemove.add(i)
                            is Function -> {
                                if (!i.isNotEmpty()) toRemove.add(i)
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
                                    is Fraction -> if (i.isEmpty()) toRemove.add(i)
                                    is Function -> {
                                        if (!i.isNotEmpty()) toRemove.add(i)
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
        return this.numerator.size == 1 && numerator.last() is UnknownEntity && (denominator == null || denominator!!.isEmpty()) && (multiplicative == null || multiplicative!!.isEmpty()) && this.powerTo == 1.0 && this.count == 1.0
    }

    private fun isNumberFraction(): Boolean {
        return this.numerator.size == 1 && numerator.last() is UnknownEntity
                && (denominator != null && this.denominator!!.isNotEmpty() && this.denominator!!.size == 1 && denominator!!.last() is UnknownEntity )
                && (multiplicative == null || multiplicative!!.isEmpty())
                && this.powerTo == 1.0 && this.count == 1.0
    }

    private fun getUnknownEntity(): UnknownEntity {
        return numerator.last() as UnknownEntity
    }

    private fun isFunction(): Boolean {
        return this.numerator.size == 1 && numerator.last() is Function && (denominator == null || denominator!!.isEmpty()) && (multiplicative == null || multiplicative!!.isEmpty()) && this.powerTo == 1.0 && this.count == 1.0
    }

    private fun getFunction(): Function {
        return numerator.last() as Function
    }

    private fun shortenEquation(equation: MutableList<Any>): MutableList<Any> {
        // Make multiplications and division
        val grouped = mutableListOf<Any>()
        val pieceOfEquation = mutableListOf<Any>()

        var operator = '×'
        for (element in equation) {
            when (element) {
                is UnknownEntity -> {
                    if (element.onlyNumber()) {
                        element.variable = null
                        element.powerTo = null
                    }

                    var found = false
                    var i = 0
                    while (i < pieceOfEquation.size) {
                        if (pieceOfEquation[i] is UnknownEntity) {
                            if (operator == '×') {
                                pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) * element
                            }
                            else {
                                pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) / element
                            }
                            found = true
                            break
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
                        i++
                    }

                    if (!found) {
                        if (operator == '/') {
                            element.powerTo = -element.powerTo
                        }
                        pieceOfEquation.add(element)
                    }

                    operator = '×'
                }
                is Fraction -> {
                    if (element.isUnknownEntity()) {
                        val entity = element.getUnknownEntity()

                        if (entity.onlyNumber()) {
                            entity.variable = null
                            entity.powerTo = null
                        }

                        var found = false
                        var i = 0
                        while (i < pieceOfEquation.size) {
                            if (pieceOfEquation[i] is UnknownEntity) {
                                if (operator == '×') {
                                    pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) * entity
                                }
                                else {
                                    pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) / entity
                                }
                                found = true
                                break
                            }
                            else if (pieceOfEquation[i] is Fraction) {
                                if ((pieceOfEquation[i] as Fraction).isNumberFraction()) {
                                    if (operator == '×') {
                                        pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) * entity
                                    }
                                    else {
                                        pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) / entity
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
                                        (pieceOfEquation[i] as Function).powerTo += element.powerTo
                                    }
                                    else {
                                        (pieceOfEquation[i] as Function).powerTo -= element.powerTo
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
                    else {
                        if (element.isNumberFraction()) {
                            var found = false
                            var i = 0
                            while (i < pieceOfEquation.size) {
                                if (pieceOfEquation[i] is UnknownEntity) {
                                    if (operator == '×') {
                                        pieceOfEquation[i] = element * (pieceOfEquation[i] as UnknownEntity)
                                    }
                                    else {
                                        pieceOfEquation[i] = (pieceOfEquation[i] as UnknownEntity) / element
                                    }
                                    found = true
                                    break
                                }
                                else if (pieceOfEquation[i] is Fraction) {
                                    if ((pieceOfEquation[i] as Fraction).isNumberFraction()) {
                                        if (operator == '×') {
                                            pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) * element
                                        }
                                        else {
                                            pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) / element
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
                            var found = false
                            var i = 0
                            while (i < pieceOfEquation.size) {
                                if (pieceOfEquation[i] is Fraction) {
                                    if (operator == '×') {
                                        pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) * element
                                    }
                                    else {
                                        pieceOfEquation[i] = (pieceOfEquation[i] as Fraction) / element
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
                    operator = '×'
                }
                is Char -> {
                    if (element == '+' || element == '-') {
                        grouped.addAll(pieceOfEquation)
                        pieceOfEquation.clear()
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

        // Make subtraction and addition if possible println
        for (element in grouped) {

        }

        return grouped
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

        this.getOutPowerTo()
        other.getOutPowerTo()

        if (this.getKey() == other.getKey() && this !== other) {
            if (this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()) {
                this.powerTo += other.powerTo
                return this
            }
        }

        if (!equationHasOperators(this.numerator) && !equationHasOperators(other.numerator)) {
            this.numerator.addAll(other.numerator)

            if (this.denominator != null && other.denominator != null && this.denominator!!.isNotEmpty() && other.denominator!!.isNotEmpty()) {
                if (denominator!! == other.denominator) {
                    if (!equationHasOperators(this.denominator!!) && !equationHasOperators(other.denominator!!)) {
                        this.denominator!!.addAll(other.denominator!!)
                    }
                    else {
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                }
            }
            else if (other.denominator != null && other.denominator!!.isNotEmpty()) {
                this.denominator = other.denominator
            }

            return this
        }
        else {
            if (this.denominator != null && other.denominator != null && this.denominator!!.isNotEmpty() && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        if (itIsNotFraction(this.numerator)) {
                            this.numerator = mutableListOf(Fraction(this.numerator))
                        }
                        if (itIsNotFraction(other.numerator)) {
                            this.numerator.add(Fraction(other.numerator))
                        }
                        else {
                            this.numerator.addAll(other.numerator)
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                    else {
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                }
                else {
                    if (other.numerator.isNotEmpty()) {
                        if (itIsNotFraction(other.denominator!!)) {
                            other.denominator = mutableListOf(Fraction(other.denominator!!))
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            other.denominator!!.add(Fraction(this.denominator!!))
                        }
                        else {
                            other.denominator!!.addAll(this.denominator!!)
                        }
                        return other
                    }
                    else {
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                        }
                        if (itIsNotFraction(this.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                }
            }
            else if (this.denominator != null && this.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        if (itIsNotFraction(this.numerator)) {
                            this.numerator = mutableListOf(Fraction(this.numerator))
                        }
                        if (itIsNotFraction(other.numerator)) {
                            this.numerator.add(Fraction(other.numerator))
                        }
                        else {
                            this.numerator.addAll(other.numerator)
                        }
                    }
                }
            }
            else if (other.denominator != null && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        if (itIsNotFraction(this.numerator)) {
                            this.numerator = mutableListOf(Fraction(this.numerator))
                        }
                        if (itIsNotFraction(other.numerator)) {
                            this.numerator.add(Fraction(other.numerator))
                        }
                        else {
                            this.numerator.addAll(other.numerator)
                        }
                        this.denominator = other.denominator!!
                    }
                    else {
                        return other
                    }
                }
                else {
                    return other
                }
            }
            else {
                if (this.numerator.isNotEmpty()) {
                    if (other.numerator.isNotEmpty()) {
                        if (itIsNotFraction(this.numerator)) {
                            this.numerator = mutableListOf(Fraction(this.numerator))
                        }
                        if (itIsNotFraction(other.numerator)) {
                            this.numerator.add(Fraction(other.numerator))
                        }
                        else {
                            this.numerator.addAll(other.numerator)
                        }
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
        if (this.isZero()) {
            return this
        }
        if (isOne(other.content)) {
            return this
        }

        this.getOutPowerTo()

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

        this.numerator.add(other)

        return this
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

        this.getOutPowerTo()
        other.getOutPowerTo()

        if (this.getKey() == other.getKey() && this !== other) {
            if (this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()) {
                this.powerTo -= other.powerTo
                return this
            }
        }
        else {
            if (this.denominator != null && other.denominator != null && this.denominator!!.isNotEmpty() && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (itIsNotFraction(this.numerator)) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                    }
                }
                if (itIsNotFraction(other.denominator!!)) {
                    this.numerator.add(Fraction(other.denominator!!))
                }
                else {
                    this.numerator.addAll(other.denominator!!)
                }
                if (itIsNotFraction(this.denominator!!)) {
                    this.denominator = mutableListOf(Fraction(this.denominator!!))
                }

                if (itIsNotFraction(other.numerator)) {
                    this.denominator!!.add(Fraction(other.numerator))
                }
                else {
                    this.denominator!!.addAll((other.numerator))
                }
            }
            else if (this.denominator != null && this.denominator!!.isNotEmpty()) {
                if (itIsNotFraction(other.numerator)) {
                    this.denominator = mutableListOf(Fraction(this.denominator!!))
                }

                if (itIsNotFraction(other.numerator)) {
                    this.denominator!!.add(Fraction(other.numerator))
                }
                else {
                    this.denominator!!.addAll((other.numerator))
                }
            }
            else if (other.denominator != null && other.denominator!!.isNotEmpty()) {
                if (this.numerator.isNotEmpty()) {
                    if (itIsNotFraction(this.numerator)) {
                        this.numerator = mutableListOf(Fraction(this.numerator))
                    }
                }
                if (itIsNotFraction(other.denominator!!)) {
                    this.numerator.add(Fraction(other.denominator!!))
                }
                else {
                    this.numerator.addAll(other.denominator!!)
                }

                this.denominator = other.numerator
            }
            else {
                this.denominator =  other.numerator
            }
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

        this.getOutPowerTo()

        if (this.denominator == null) denominator = mutableListOf()
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

        if (this.denominator == null) denominator = mutableListOf()
        this.denominator!!.add(other)

        return this
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is Fraction) {
            return this.getKey() == other.getKey() && this.powerTo == other.powerTo
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