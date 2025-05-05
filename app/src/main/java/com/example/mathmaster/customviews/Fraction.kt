package com.example.mathmaster.customviews

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

data class Fraction(var numerator: MutableList<Any> = mutableListOf(), var denominator: MutableList<Any>? = null, var powerTo: Double = 1.0, var count: Fraction? = null, var multiplicative: Fraction? = null) {
    private var withoutGCD = false

    private val numeratorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val numeratorOperators = mutableListOf<Char>()
    private val numeratorElements = mutableListOf<MutableList<Any>>()
    private var commonForNumerator = hashMapOf<MutableList<Any>, Double>()

    private val denominatorMaps = mutableListOf<HashMap<MutableList<Any>, Double>>()
    private val denominatorOperators = mutableListOf<Char>()
    private val denominatorElements = mutableListOf<MutableList<Any>>()
    private var commonForDenominator = hashMapOf<MutableList<Any>, Double>()

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

        // Avoid unnecessary conversion
        while (denominator == null && numerator.size == 1 && numerator.last() is Fraction) {
            denominator = (numerator.last() as Fraction).denominator
            numerator = (numerator.last() as Fraction).numerator
        }

        // Set only numbers
        for (element in numerator) {
            if (element is UnknownEntity) {
                if (element.variable == null) {
                    element.variable = 'f'
                    element.powerTo = 0.0
                }
            }
        }
        if (denominator != null) {
            for (element in denominator!!) {
                if (element is UnknownEntity) {
                    if (element.variable == null) {
                        element.variable = 'f'
                        element.powerTo = 0.0
                    }
                }
            }
        }

        makeCopy()
        sortEverything()
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

    private fun getKeys(switch: Boolean = false, exp: Double = this.powerTo): MutableList<Triple<MutableList<Any>, Double, Any>> {
        val list = mutableListOf<Triple<MutableList<Any>, Double, Any>>()

        if (equationHasOperators(this.numerator) || (this.denominator != null && equationHasOperators(this.denominator!!))) {
            if (switch) {
                list.add(Triple(this.getKey(), -this.powerTo.pow(exp), this))
            }
            else {
                list.add(Triple(this.getKey(), this.powerTo.pow(exp), this))
            }
        }
        else {
            for (element in numerator) {
                when (element) {
                    is UnknownEntity -> {
                        if (element.multiplier != null) {
                            if (switch) {
                                if (element.onlyNumber()) {
                                    list.add(Triple(element.getKey(value = true), -element.multiplier!!.pow(exp), element))
                                }
                                else {
                                    list.add(Triple(element.getKey(), -element.multiplier!!.pow(exp), element))
                                }
                            }
                            else {
                                if (element.onlyNumber()) {
                                    list.add(Triple(element.getKey(value = true), element.multiplier!!.pow(exp), element))
                                }
                                else {
                                    list.add(Triple(element.getKey(), element.multiplier!!.pow(exp), element))
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
                            list.add(Triple(element.getKey(), -element.powerTo.pow(exp), element))
                        }
                        else {
                            list.add(Triple(element.getKey(), element.powerTo.pow(exp), element))
                        }
                    }
                    is Fraction -> {
                        list.addAll(element.getKeys(switch, this.powerTo.pow(exp)))
                    }
                }
            }

            if (denominator != null) {
                for (element in denominator!!) {
                    when (element) {
                        is UnknownEntity -> {
                            if (element.multiplier != null) {
                                if (switch) {
                                    if (element.onlyNumber()) {
                                        list.add(Triple(element.getKey(value = true), element.multiplier!!.pow(exp), element))
                                    }
                                    else {
                                        list.add(Triple(element.getKey(), element.multiplier!!.pow(exp), element))
                                    }
                                }
                                else {
                                    if (element.onlyNumber()) {
                                        list.add(Triple(element.getKey(value = true), -element.multiplier!!.pow(exp), element))
                                    }
                                    else {
                                        list.add(Triple(element.getKey(), -element.multiplier!!.pow(exp), element))
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
                                list.add(Triple(element.getKey(), element.powerTo.pow(exp), element))
                            }
                            else {
                                list.add(Triple(element.getKey(), -element.powerTo.pow(exp), element))
                            }
                        }
                        is Fraction -> {
                            list.addAll(element.getKeys(!switch, this.powerTo.pow(exp)))
                        }
                    }
                }
            }
        }

        return list
    }

    private fun getOutMultiplication() {
        if (multiplicative != null) {
            multiplicative!!.getOutPowerTo()

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

            if (denominatorExp != null) {
                this.numerator = denominatorExp
            }
            else {
                denominatorExp = mutableListOf(UnknownEntity(1.0, 'f', 0.0))
                this.numerator = denominatorExp
            }

            this.denominator = numeratorExp
            this.powerTo += 1.0
            var i = this.powerTo
            this.powerTo = 1.0
            while  (i < 0) {
                this.multiplyByItself(denominatorExp, numeratorExp)
                i += 1.0
            }
        }
    }

    private fun getOutCount() {
        if (count != null && !count!!.isOne()) {
            if (equationHasOperators(numerator)) {
                numerator = mutableListOf(Fraction(numerator))
            }
            numerator.add(count!!)
        }
        count = null
    }

    private fun getOutCountRecursive() {
        this.getOutCount()

        for (element in numerator) {
            if (element is Fraction) {
                element.getOutCountRecursive()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
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

    private fun fractionCalculationsRecursive() {
        this.numerator = finalCalculation(this.numerator)
        if (this.denominator != null) {
            this.denominator = finalCalculation(this.denominator!!)
        }
    }

    private fun finalCalculation(equation: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<Any>()
        var buffer = Fraction()

        var operator = '×'
        for (elementA in equation) {
            var found = false

            if (elementA == '+' || elementA == '-') {
                for (elementB in output) {
                    if (elementB is Fraction) {
                        if (buffer == elementB) {
                            if (buffer.count == null) {
                                buffer.count = Fraction(mutableListOf(UnknownEntity(1.0, 'f', 0.0)))
                            }

                            if (elementB.count != null && buffer.count == elementB.count) {
                                elementB.count = elementB.count!! + buffer.count!!
                                found = true
                            }
                            else if (elementB.count == null && buffer.count!!.onlyNumber()){
                                elementB.count = Fraction(mutableListOf(UnknownEntity(1.0, 'f', 0.0))) + buffer.count!!
                                found = true
                            }
                            break
                        }
                    }
                }

                if (!found) {
                    output.add(buffer)
                    output.add(elementA)
                }
                else {
                    if (output.last() is Char) {
                        output.removeLast()
                    }
                }
                buffer = Fraction()
            }
            else if (elementA == '/' || elementA == '×') {
                operator = elementA as Char
            }
            else {
                if (operator == '/') {
                    if (buffer.denominator != null) {
                        buffer.denominator!!.add(elementA)
                    }
                    else {
                        buffer.denominator = mutableListOf(elementA)
                    }
                }
                else {
                    if (elementA is Fraction) {
                        elementA.fractionCalculationsRecursive()
                    }

                    buffer.numerator.add(elementA)

                    if (elementA is Fraction) {
                        if (buffer.count == null) {
                            buffer.count = elementA.count
                            elementA.count = null
                        }
                        else {
                            if (elementA.count != null) {
                                buffer.count = buffer.count!! + elementA.count!!
                                elementA.count = null
                            }
                        }
                    }
                    else if (elementA is Function) {
                        if (buffer.count == null) {
                            buffer.count = elementA.count
                            elementA.count = null
                        }
                        else {
                            if (elementA.count != null) {
                                buffer.count = buffer.count!! + elementA.count!!
                                elementA.count = null
                            }
                        }
                    }
                }
                operator = '×'
            }
        }

        var found = false
        for (elementB in output) {
            if (elementB is Fraction) {
                if (buffer == elementB) {
                    if (buffer.count == null) {
                        buffer.count = Fraction(mutableListOf(UnknownEntity(1.0, 'f', 0.0)))
                    }

                    if (elementB.count != null && buffer.count == elementB.count) {
                        elementB.count = elementB.count!! + buffer.count!!
                        found = true
                    }
                    else if (elementB.count == null && buffer.count!!.onlyNumber()){
                        elementB.count = Fraction(mutableListOf(UnknownEntity(1.0, 'f', 0.0))) + buffer.count!!
                        found = true
                    }
                    break
                }
            }
        }

        if (!found) {
            output.add(buffer)
        }

        if (output.last() is Char) {
            output.removeLast()
        }

        return output
    }

    fun finalShort() {
        for (element in numerator) {
            if (element is Fraction) {
                element.finalShort()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    element.finalShort()
                }
            }
        }

        this.makeCalculations()
        this.setFraction()
    }

    private fun calculationsPossible(): Boolean {
        if (this.powerTo != 1.0) return false

        for (element in numerator) {
            if (element is Function) return false
            else if (element is Fraction) {
                if (!element.calculationsPossible()) return false
            }
        }

        return true
    }

    private fun makeMultiplication(input: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<Any>()

        for (elementA in input) {
            var found = false
            for ((index, elementB) in output.withIndex()) {
                if ((elementA as UnknownEntity).variable == (elementB as UnknownEntity).variable) {
                    output[index] = elementA * elementB
                    found = true
                    break
                }
            }

            if (!found) {
                output.add(elementA)
            }
        }

        val toRemove = mutableListOf<UnknownEntity>()
        val newEntity = UnknownEntity(1.0, 'f', 0.0)
        for (element in output) {
            if (element is UnknownEntity) {
                newEntity.multiplier = newEntity.multiplier?.times(element.multiplier!!)
                element.multiplier = 1.0

                if (element.onlyNumber()) {
                    toRemove.add(element)
                }
            }
        }
        output.add(newEntity)

        for (i in toRemove) {
            output.remove(i)
        }

        return output
    }

    private fun sortCalculable(input: MutableList<Any>, multiplication: MutableList<Any> = mutableListOf(), additional: MutableList<Any>, negative: Boolean): MutableList<MutableList<Any>> {
        val output = mutableListOf<MutableList<Any>>()

        val multiplicative = mutableListOf<MutableList<Any>>()
        var fragment = mutableListOf<Any>()
        var operator = '+'
        for (element in input) {
            if (element is UnknownEntity) {
                if (operator == '-') {
                    if (!negative) {
                        element.multiplier = -element.multiplier!!
                    }
                    fragment.add(element)
                    operator = '+'
                }
                else {
                    if (negative) {
                        element.multiplier = -element.multiplier!!
                    }
                    fragment.add(element)
                }
            }
            else if (element is Char) {
                multiplicative.add(sortFragment(makeMultiplication(fragment)))
                fragment = mutableListOf()

                operator = element
            }
        }
        if (fragment.isNotEmpty()) {
            multiplicative.add(sortFragment(makeMultiplication(fragment)))
        }

        val base = mutableListOf<MutableList<Any>>()
        fragment = mutableListOf()
        operator = '+'
        for (element in additional) {
            if (element is UnknownEntity) {
                if (operator == '-') {
                    if (!negative) {
                        element.multiplier = -element.multiplier!!
                    }
                    fragment.add(element)
                    operator = '+'
                }
                else {
                    if (negative) {
                        element.multiplier = -element.multiplier!!
                    }
                    fragment.add(element)
                }
            }
            else if (element is Char) {
                base.add(sortFragment(makeMultiplication(fragment)))
                fragment = mutableListOf()

                operator = element
            }
        }
        if (fragment.isNotEmpty()) {
            base.add(sortFragment(makeMultiplication(fragment)))
        }

        if (base.isNotEmpty() && multiplicative.isNotEmpty()) {
            for (listA in base) {
                for (listB in multiplicative) {
                    val newElement = (listA + listB + multiplication).toMutableList()
                    output.add(newElement)
                    output.add(mutableListOf('+'))
                }
            }
            output.removeLast()
        }
        else if (base.isNotEmpty()) {
            for (list in base) {
                val newElement = (list + multiplication).toMutableList()
                output.add(newElement)
                output.add(mutableListOf('+'))
            }
            output.removeLast()
        }
        else if (multiplicative.isNotEmpty()) {
            for (list in multiplicative) {
                val newElement = (list + multiplication).toMutableList()
                output.add(newElement)
                output.add(mutableListOf('+'))
            }
            output.removeLast()
        }

        input.clear()
        additional.clear()

        return output
    }

    private fun getCalculable(negative: Boolean = false, multiplication: MutableList<Any> = mutableListOf()): MutableList<MutableList<Any>> {
        val output = mutableListOf<MutableList<Any>>()
        val buffer = mutableListOf<Any>()
        val fractionList = mutableListOf<Any>()

        for (element in numerator) {
            when (element) {
                '+' -> {
                    output.addAll(sortCalculable(buffer, multiplication, fractionList, negative))

                    if (negative) {
                        output.add(mutableListOf('-'))
                    }
                    else {
                        output.add(mutableListOf('+'))
                    }
                }
                '-' -> {
                    output.addAll(sortCalculable(buffer, multiplication, fractionList, negative))

                    if (negative) {
                        output.add(mutableListOf('+'))
                    }
                    else {
                        output.add(mutableListOf('-'))
                    }

                    buffer.clear()
                }
                is UnknownEntity -> {
                    buffer.add(element)
                }
                is Fraction -> {
                    if (output.isNotEmpty() && output.last().size == 1 && output.last().last() == '-') {
                        val result = element.getCalculable(!negative, buffer.toMutableList())

                        if (fractionList.isNotEmpty()) {
                            val newFractionList = mutableListOf<Any>()
                            for (list in result) {
                                newFractionList.addAll(list)
                            }

                            val calculation = sortCalculable(newFractionList, additional = fractionList, negative = negative)

                            fractionList.clear()
                            fractionList.addAll(calculation.flatten())
                        }
                        else {
                            fractionList.addAll(result.flatten())
                        }

                        if (result.isNotEmpty()) {
                            buffer.clear()
                        }
                    }
                    else {
                        val result = element.getCalculable(negative, buffer.toMutableList())

                        if (fractionList.isNotEmpty()) {
                            val newFractionList = mutableListOf<Any>()
                            for (list in result) {
                                newFractionList.addAll(list)
                            }

                            val calculation = sortCalculable(newFractionList, additional = fractionList, negative = negative)

                            fractionList.clear()

                            fractionList.addAll(calculation.flatten())
                        }
                        else {
                            fractionList.addAll(result.flatten())
                        }

                        if (result.isNotEmpty()) {
                            buffer.clear()
                        }
                    }
                }
                else -> buffer.add(element)
            }
        }
        output.addAll(sortCalculable(buffer, multiplication, fractionList, negative))

        return output
    }

    private fun calculateBasicOperations(input: MutableList<MutableList<Any>>): MutableList<Any> {
        val output = mutableListOf<Any>()

        var operatorList = mutableListOf<Any>()
        for ((index, listA) in input.withIndex()) {
            if (listA.isEmpty()) continue
            for (listB in input.drop(index+1)) {
                if (listB.isEmpty()) continue
                if (listB.size == 1 && listB.last() is Char) {
                    operatorList = listB
                    continue
                }

                if (listA.size == listB.size) {
                    var same = true
                    for ((i, element) in listA.withIndex()) {
                        if (element is UnknownEntity) {
                            if (listB[i] is UnknownEntity) {
                                if (element != listB[i]) {
                                    same = false
                                }
                            }
                        }
                    }

                    if (same) {
                        for ((i, element) in listA.withIndex()) {
                            if (element is UnknownEntity) {
                                if (element.onlyNumber()) {
                                    listA[i] = element + listB[i] as UnknownEntity
                                    listB.clear()
                                    operatorList.clear()
                                    break
                                }
                            }
                        }
                    }
                }
            }

            for (element in listA) {
                if (element is UnknownEntity) {
                    if (element.multiplier == 0.0) {
                        listA.clear()
                        break
                    }
                }
            }
        }

        for (list in input) {
            output.addAll(list)
        }

        return output
    }

    private fun checkForFractions(input: MutableList<Any>): Boolean {
        for (element in input) {
            if (element is Function) {
                return true
            }
            else if (element is Fraction) {
                if (checkForFractions(element.numerator)) return true
                if (element.denominator != null) {
                    if (checkForFractions(element.denominator!!)) return true
                }
            }
        }

        return false
    }

    private fun calculateFunctions(input: MutableList<Any>): MutableList<Any> {
        val output = mutableListOf<MutableList<Any>>()
        var fragment = mutableListOf<Any>()

        for (element in input) {
            if (element is Char) {
                var found = false
                for (entity in fragment) {
                    if (entity is UnknownEntity && entity.onlyNumber()) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    fragment.add(UnknownEntity(1.0, 'f', 0.0))
                }

                output.add(fragment)
                output.add(mutableListOf(element))

                fragment = mutableListOf()
                continue
            }
            fragment.add(element)
        }
        var found = false
        for (entity in fragment) {
            if (entity is UnknownEntity && entity.onlyNumber()) {
                found = true
                break
            }
        }
        if (!found) {
            fragment.add(UnknownEntity(1.0, 'f', 0.0))
        }
        output.add(fragment)

        for ((index, listA) in output.withIndex()) {
            if (listA.isEmpty()) continue
            if (listA.size == 1 && listA.last() is Char) continue

            if (checkForFractions(listA)) {
                for (listB in output.drop(index+1)) {
                    if (listB.isEmpty()) continue
                    if (listB.size == 1 && listA.last() is Char) continue
                    if (listA == listB) {
                        for ((i, element) in listA.withIndex()) {
                            if (element is UnknownEntity && element.onlyNumber()) {
                                listA[i] = element + UnknownEntity(1.0, 'f', 0.0)
                                listB.clear()
                                break
                            }
                        }
                    }
                }
            }
            else {
                continue
            }
        }

        return output.flatten().toMutableList()
    }

    private fun calculationsForFunctions() {
        this.numerator = calculateFunctions(numerator)
        if (denominator != null) denominator = calculateFunctions(denominator!!)
    }

    private fun makeCalculations() {
        if (this.calculationsPossible()) {
            this.numerator = calculateBasicOperations(this.getCalculable())
            this.convertDoublesToFractionInNumerator()
        }
    }

    fun calculateFraction() {
        for (element in numerator) {
            if (element is Fraction) {
                element.calculateFraction()
            }
        }

        if (denominator != null) {
            for (element in denominator!!) {
                if (element is Fraction) {
                    element.calculateFraction()
                }
            }
        }

        this.getOutMultiplication()
        this.getOutCount()
        this.getOutPowerTo()
        this.setFraction()
    }

    private fun setFraction() {
        clearMaps()
        calculationsForFunctions()

        updateFractionNumerator()
        if (denominator != null) updateFractionDenominator()
        if (denominator != null) shortenNumeratorWithDenominator()

        if (!withoutGCD) {
            setMultiplicative(getMultiplicativeNumerator(), getMultiplicativeDenominator())
        }

        updateNumeratorMaps()
        updateDenominatorMaps()

        rebuildFractionNumerator()
        if (denominator != null) rebuildFractionDenominator()
        if (denominator != null && denominator!!.isEmpty()) denominator = null

        getOutMultiplication()
        getOutCount()
        getOutPowerTo()

        convertDoublesToFractionInNumerator()
        if (denominator != null) convertDoublesToFractionInDenominator()
        sortEverything()

        shortenFraction()
    }

    private fun getValue(): UnknownEntity {
        // Check for unknown entity
        if (numerator.size == 1 && numerator.last() is UnknownEntity) {
            if (denominator != null) {
                if (denominator!!.size == 1 && denominator!!.last() is UnknownEntity) {
                    if ((numerator.last() as UnknownEntity).onlyNumber() && (denominator!!.last() as UnknownEntity).onlyNumber()) {
                        return (numerator.last() as UnknownEntity) / (denominator!!.last() as UnknownEntity)
                    }
                }
                else if ((denominator!!.size == 1 && denominator!!.last() is Fraction)) {
                    return (numerator.last() as UnknownEntity) / (denominator!!.last() as Fraction).getValue()
                }
            }
            else {
                if ((numerator.last() as UnknownEntity).onlyNumber()) {
                    return (numerator.last() as UnknownEntity)
                }
            }
        }

        // Check fractions
        if (numerator.size == 1 && numerator.last() is Fraction) {
            if (denominator != null) {
                if (denominator!!.size == 1 && denominator!!.last() is Fraction) {
                    return (numerator.last() as Fraction).getValue() / (denominator!!.last() as Fraction).getValue()
                }
                else if ((denominator!!.size == 1 && denominator!!.last() is UnknownEntity)) {
                    return (numerator.last() as Fraction).getValue() / (denominator!!.last() as UnknownEntity)
                }
            }
            else {
                return (numerator.last() as Fraction).getValue()
            }
        }

        return UnknownEntity()
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
        if (equation.size > 2) {
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

        return true
    }

    private fun sortFragment(input: MutableList<Any>): MutableList<Any> {
        return input.sortedBy {
            when (it) {
                is UnknownEntity -> {
                    when (it.variable) {
                        'x' -> 6
                        'y' -> 5
                        'z' -> 4
                        'e' -> 3
                        'π' -> 2
                        else -> 1
                    }
                }
                is Function -> {
                    when (it.content.first()) {
                        's' -> 7
                        'c' -> 8
                        't' -> 9
                        else -> 10
                    }
                }
                is Fraction -> 100 - it.numerator.size
                else -> 1000
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
                            val multiplication = inside.isNotEmpty() && inside.first() == 'F'

                            if (notInBrackets && !multiplication) {
                                fragment.add('(')
                            }

                            if (multiplication) {
                                inside.removeFirst()
                            }

                            fragment.addAll(inside)

                            if (notInBrackets && !multiplication) {
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

        if (!equationNotInBrackets(output) && !withMultiplication) {
            output.removeFirst()
            output.removeLast()
        }

        return output
    }

    fun isOne(): Boolean {
        return this.getValue().multiplier == 1.0
    }

    fun isZero(): Boolean {
        return this.getValue().multiplier == 0.0
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
                    fraction.addAll(count!!.getFraction(withMultiplication = false, flatFraction = true))
                    fraction.add(')')
                    fraction.add('×')
                    fraction.add('(')
                }
                else {
                    fraction.add('F')
                    fraction.addAll(count!!.getFraction(withMultiplication = false, flatFraction = true))
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

            if (count != null && !count!!.isOne() && !flatFraction && !withoutCount && withMultiplication) {
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
                        if (entity.variable == element.variable) {
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
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (numeratorMap[key.first] == null && key.third !is UnknownEntity) {
                                numeratorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity.variable == (key.third as UnknownEntity).variable) {
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
                            else if (key.third is Fraction && (key.third as Fraction).isNumberFraction()) {
                                val number = (key.third as Fraction).getValue()

                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity.variable == number.variable) {
                                        if (sign == '/') {
                                            if (key.second < 0) {
                                                entities[i] = entity * number
                                            }
                                            else {
                                                entities[i] = entity / number
                                            }
                                        }
                                        else {
                                            if (key.second < 0) {
                                                entities[i] = entity / number
                                            }
                                            else {
                                                entities[i] = entity * number
                                            }
                                        }
                                        found = true
                                        break
                                    }
                                }

                                if (!found) {
                                    entities.add(number)
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
                                val newEntity = UnknownEntity(1.0, 'f', 0.0)
                                val toRemove = mutableListOf<UnknownEntity>()
                                for (entity in entities) {
                                    newEntity.multiplier = newEntity.multiplier?.times(entity.multiplier!!)
                                    entity.multiplier = 1.0

                                    if (entity.onlyNumber()) {
                                        toRemove.add(entity)
                                    }
                                }
                                if (newEntity.multiplier != 1.0) {
                                    entities.add(newEntity)
                                }

                                for (i in toRemove) {
                                    if (entities.size != 1) entities.remove(i)
                                }

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
            val newEntity = UnknownEntity(1.0, 'f', 0.0)
            val toRemove = mutableListOf<UnknownEntity>()

            for (entity in entities) {
                newEntity.multiplier = newEntity.multiplier?.times(entity.multiplier!!)
                entity.multiplier = 1.0
                if (entity.onlyNumber()) {
                    toRemove.add(entity)
                }
            }

            if (newEntity.multiplier != 1.0) {
                entities.add(newEntity)
            }

            for (i in toRemove) {
                if (entities.size != 1) entities.remove(i)
            }

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
                        if (entity.variable == element.variable) {
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
                    val keysList = element.getKeys()

                    for (key in keysList) {
                        if (key.first.isNotEmpty()) {
                            if (denominatorMap[key.first] == null && key.third !is UnknownEntity) {
                                denominatorElement.add(key.third)
                            }

                            if (key.third is UnknownEntity) {
                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity.variable == (key.third as UnknownEntity).variable) {
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
                            else if (key.third is Fraction && (key.third as Fraction).isNumberFraction()) {
                                val number = (key.third as Fraction).getValue()

                                var found = false
                                for ((i, entity) in entities.withIndex()) {
                                    if (entity.variable == number.variable) {
                                        if (sign == '/') {
                                            if (key.second < 0) {
                                                entities[i] = entity * number
                                            }
                                            else {
                                                entities[i] = entity / number
                                            }
                                        }
                                        else {
                                            if (key.second < 0) {
                                                entities[i] = entity / number
                                            }
                                            else {
                                                entities[i] = entity * number
                                            }
                                        }
                                        found = true
                                        break
                                    }
                                }

                                if (!found) {
                                    entities.add(number)
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
                                val newEntity = UnknownEntity(1.0, 'f', 0.0)
                                val toRemove = mutableListOf<UnknownEntity>()
                                for (entity in entities) {
                                    newEntity.multiplier = newEntity.multiplier?.times(entity.multiplier!!)
                                    entity.multiplier = 1.0

                                    if (entity.onlyNumber()) {
                                        toRemove.add(entity)
                                    }
                                }
                                if (newEntity.multiplier != 1.0) {
                                    entities.add(newEntity)
                                }

                                for (i in toRemove) {
                                    if (entities.size != 1) entities.remove(i)
                                }

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
                                denominatorMaps.add(denominatorMap)
                                denominatorMap = hashMapOf()

                                denominatorElements.add(denominatorElement)
                                denominatorElement = mutableListOf()

                                denominatorOperators.add(element)
                            }
                        }
                    }
                    sign = element
                }
            }
        }
        if (entities.isNotEmpty()) {
            val newEntity = UnknownEntity(1.0, 'f', 0.0)
            val toRemove = mutableListOf<UnknownEntity>()

            for (entity in entities) {
                newEntity.multiplier = newEntity.multiplier?.times(entity.multiplier!!)
                entity.multiplier = 1.0
                if (entity.onlyNumber()) {
                    toRemove.add(entity)
                }
            }

            if (newEntity.multiplier != 1.0) {
                entities.add(newEntity)
            }

            for (i in toRemove) {
                if (entities.size != 1) entities.remove(i)
            }

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

        if (denominatorMap.isNotEmpty() && denominatorElement.isNotEmpty()) {
            denominatorMaps.add(denominatorMap)
            denominatorElements.add(denominatorElement)
        }
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

    private fun onlyNumber(): Boolean {
        return this.isNumberFraction() && (this.denominator == null || this.denominator!!.isEmpty()) && this.numerator.size == 1 && this.numerator.last() is UnknownEntity
    }

    private fun findGCDNumerator(input: Double? = null): Pair<Double, Double> {
        // Get all multipliers of unknown entities
        val allNumeratorMultipliers = mutableListOf<Fraction>()
        var biggestDecimalPoint = 0
        for (list in numeratorElements) {
            for (element in list) {
                if (element is UnknownEntity) {
                    if (element.onlyNumber()) {
                        allNumeratorMultipliers.add(Fraction(mutableListOf(UnknownEntity(ceil(round(element.multiplier!! * 1000) / 1000)))))
                        val decimalPoint = countDecimalPlaces(ceil(round(element.multiplier!! * 1000) / 1000))
                        if (decimalPoint > biggestDecimalPoint) {
                            biggestDecimalPoint = decimalPoint
                        }
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

            for (divisor in allDivisors) {
                commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
            }
        }

        val allCommonGCD = commonGCD.filter { (_, v) -> v == allGCD.size }.keys

        var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
        for (value in allCommonGCD) {
            if (input != null) {
                if (gcd < value && value <= input) {
                    gcd = value
                }
            }
            else {
                if (gcd < value) {
                    gcd = value
                }
            }
        }

        return Pair(gcd.toDouble(), decimalPoint.toDouble())
    }

    private fun findGCDDenominator(input: Double? = null): Pair<Double, Double> {
        // Get all multipliers of unknown entities
        val allNumeratorMultipliers = mutableListOf<Fraction>()
        var biggestDecimalPoint = 0
        for (list in denominatorElements) {
            for (element in list) {
                if (element is UnknownEntity) {
                    if (element.onlyNumber()) {
                        allNumeratorMultipliers.add(Fraction(mutableListOf(UnknownEntity(ceil(round(element.multiplier!! * 1000) / 1000)))))
                        val decimalPoint = countDecimalPlaces(ceil(round(element.multiplier!! * 1000) / 1000))
                        if (decimalPoint > biggestDecimalPoint) {
                            biggestDecimalPoint = decimalPoint
                        }
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

            for (divisor in allDivisors) {
                commonGCD[divisor] = commonGCD.getOrDefault(divisor, 0) + 1
            }
        }

        val allCommonGCD = commonGCD.filter { (_, v) -> v == allGCD.size }.keys

        var gcd = if (allCommonGCD.isNotEmpty()) allCommonGCD.last() else 1
        for (value in allCommonGCD) {
            if (input != null) {
                if (gcd < value && value <= input) {
                    gcd = value
                }
            }
            else {
                if (gcd < value) {
                    gcd = value
                }
            }
        }

        return Pair(gcd.toDouble(), decimalPoint.toDouble())
    }

    private fun getMultiplicativeNumerator(): MutableList<Any> {
        // Find commons for numerator
        if (numeratorMaps.isNotEmpty() && numeratorMaps.size != 1) {
            var startCommonForNumerator = numeratorMaps.first()

            var noCommonEntity = false
            val toRemove = mutableListOf<MutableList<Any>>()
            for (map in numeratorMaps) {
                if (map === startCommonForNumerator) continue
                var found = false
                for ((key, value) in startCommonForNumerator) {
                    if (itIsUnknown(key) && !noCommonEntity) {
                        for ((k, v) in map) {
                            if (itIsUnknown(k)) {
                                if (key.last() == k.last()) {
                                    val newPower = min(key.first() as Double, k.first() as Double)

                                    val newKey = mutableListOf(newPower, key[1], k.last())

                                    if (commonForNumerator[newKey] != null) {
                                        commonForNumerator[newKey] = min(commonForNumerator[newKey]!!, v)
                                        commonForNumerator[newKey] = min(commonForNumerator[newKey]!!, value)
                                    } else {
                                        commonForNumerator[newKey] = min(v, value)
                                    }

                                    if (newKey != key) {
                                        commonForNumerator[key] = 0.0
                                        toRemove.add(key)
                                    }

                                    found = true
                                }
                            }
                        }
                    } else if (map[key] == null && startCommonForNumerator[key] != null) {
                        toRemove.add(key)
                    } else if (map[key] == null) {
                        continue
                    } else {
                        if (startCommonForNumerator[key] != null && map[key]!! < value) {
                            commonForNumerator[key] = map[key]!!
                        } else if (startCommonForNumerator[key] != null && map[key]!! == value) {
                            if (map !== numeratorMaps.last()) {
                                commonForNumerator[key] = value
                            }
                        } else if (startCommonForNumerator[key] != null && map[key]!! > value) {
                            commonForNumerator[key] = value
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

                for (i in toRemove) {
                    commonForNumerator.remove(i)
                }

                startCommonForNumerator = commonForNumerator
            }

            // Get possible gcd
            val gcdList = mutableListOf<Pair<Double, Double>>()
            for (element in commonForNumerator) {
                if (itIsUnknown(element.key) && element.key.last() == 'f') {
                    val gcd = findGCDNumerator(element.value)
                    if (!hasDecimal(gcd.first)) {
                        gcdList.add(gcd)
                    }
                }
            }

            var gcd = if (gcdList.isNotEmpty()) gcdList.last().first else 1.0
            var decimalPoint = 1.0
            for (element in gcdList) {
                if (gcd > element.first) {
                    gcd = element.first
                    decimalPoint = element.second
                }
            }

            // Remove commons from fraction numerator and update numerator map
            for (map in numeratorMaps) {
                val newKeys = mutableListOf<MutableList<Any>>()

                for ((original, _) in map) {
                    for ((common, count) in commonForNumerator) {
                        if (itIsUnknown(original)) {
                            if (original == common && original.last() == 'f') {
                                map[original] = map[original]!! / gcd * decimalPoint
                            } else if (original.last() == common.last()) {
                                val newPower = original.first() as Double - common.first() as Double

                                map[original] = 0.0

                                if (newPower != 0.0) {
                                    val newKey = mutableListOf(newPower, '^', original.last())
                                    newKeys.add(newKey)
                                }
                            }
                        } else if (original == common) {
                            map[original] = map[original]!! - count
                        }
                    }
                }

                for (key in newKeys) {
                    map[key] = 1.0
                }
            }

            // Update commons for numerator
            for (element in commonForNumerator) {
                if (itIsUnknown(element.key)) {
                    if (element.key.last() == 'f') {
                        commonForNumerator[element.key] = gcd / decimalPoint
                    }
                    else {
                        commonForNumerator[element.key] = 1.0
                    }
                }
            }

            // Get all common entities
            val numeratorOutput = mutableListOf<Any>()
            val commonsToFind = hashMapOf<MutableList<Any>, Double>()
            val entities = mutableListOf<UnknownEntity>()

            for ((key, value) in commonForNumerator) {
                if (value != 0.0) {
                    commonsToFind[key] = value

                    if (itIsUnknown(key)) {
                        val entity = UnknownEntity()

                        entity.variable = key.last() as Char
                        entity.powerTo = key.first() as Double
                        if (key.last() == 'f') {
                            entity.multiplier = value
                        }
                        else {
                            entity.multiplier = 1.0
                        }
                        entities.add(entity)
                    }
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
                            is Function -> {
                                if (key == (numeratorElements[index][element] as Function).getKey()) {
                                    if (commonsToFind[key] != null && commonsToFind[key]!! > 0) {
                                        numeratorOutput.add((numeratorElements[index][element] as Function).copy())
                                        (numeratorOutput.last() as Function).powerTo = v
                                        commonsToFind[key] = 0.0
                                    }
                                }
                            }

                            is Fraction -> {
                                val keys = (numeratorElements[index][element] as Fraction).getKeys()

                                for (k in keys) {
                                    if (key == k.first) {
                                        if (commonsToFind[key] != null && commonsToFind[key]!! > 0) {
                                            when (k.third) {
                                                is Function -> {
                                                    numeratorOutput.add((k.third as Function).copy())
                                                    (numeratorOutput.last() as Function).powerTo = v
                                                }

                                                is Fraction -> {
                                                    numeratorOutput.add((k.third as Fraction).copy())
                                                    (numeratorOutput.last() as Fraction).powerTo = v
                                                }
                                            }
                                            commonsToFind[key] = 0.0
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

            for (entity in entities) {
                if (entity.powerTo == 0.0) {
                    entity.variable = 'f'
                }

                if (!(entity.multiplier == 1.0 && entity.onlyNumber())) {
                    if (decimalPoint != 1.0) {
                        numeratorOutput.add(
                            Fraction(
                                mutableListOf(entity),
                                mutableListOf(UnknownEntity(decimalPoint))
                            )
                        )
                    } else {
                        numeratorOutput.add(entity)
                    }
                }
            }

            return numeratorOutput
        }

        return mutableListOf()
    }

    private fun getMultiplicativeDenominator(): MutableList<Any>? {
        if (denominator == null) return null

        // Find commons for denominator
        if (denominatorMaps.isNotEmpty() && denominatorMaps.size != 1) {
            var startCommonForDenominator = denominatorMaps.first()

            var noCommonEntity = false
            val toRemove = mutableListOf<MutableList<Any>>()
            for (map in denominatorMaps) {
                if (map === startCommonForDenominator) continue
                var found = false
                for ((key, value) in startCommonForDenominator) {
                    if (itIsUnknown(key) && !noCommonEntity) {
                        for ((k, v) in map) {
                            if (itIsUnknown(k)) {
                                if (key.last() == k.last()) {
                                    val buffer = min(key.first() as Double, k.first() as Double)
                                    val newPower = min(startCommonForDenominator[key]!!, buffer)

                                    val newKey = mutableListOf(newPower, key[1], k.last())

                                    if (commonForDenominator[newKey] != null) {
                                        commonForDenominator[newKey] = min(commonForDenominator[newKey]!!, v)
                                        commonForDenominator[newKey] = min(commonForDenominator[newKey]!!, value)
                                    } else {
                                        commonForDenominator[newKey] = min(v, value)
                                    }

                                    if (newKey != key) {
                                        commonForDenominator[key] = 0.0
                                        toRemove.add(key)
                                    }

                                    found = true
                                }
                            }
                        }
                    } else if (map[key] == null && startCommonForDenominator[key] != null) {
                        toRemove.add(key)
                    } else if (map[key] == null) {
                        continue
                    } else {
                        if (startCommonForDenominator[key] != null && map[key]!! < value) {
                            commonForDenominator[key] = map[key]!!
                        } else if (startCommonForDenominator[key] != null && map[key]!! == value) {
                            if (map !== denominatorMaps.last()) {
                                commonForDenominator[key] = value
                            }
                        } else if (startCommonForDenominator[key] != null && map[key]!! > value) {
                            commonForDenominator[key] = value
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

                startCommonForDenominator = commonForDenominator
            }

            for (i in toRemove) {
                commonForDenominator.remove(i)
            }

            // Get possible gcd
            val gcdList = mutableListOf<Pair<Double, Double>>()
            for (element in commonForDenominator) {
                if (itIsUnknown(element.key) && element.key.last() == 'f') {
                    val gcd = findGCDDenominator(element.value)
                    if (!hasDecimal(gcd.first)) {
                        gcdList.add(gcd)
                    }
                }
            }

            var gcd = if (gcdList.isNotEmpty()) gcdList.last().first else 1.0
            var decimalPoint = 1.0
            for (element in gcdList) {
                if (gcd > element.first) {
                    gcd = element.first
                    decimalPoint = element.second
                }
            }

            // Remove commons from fraction denominator and update denominator map
            for (map in denominatorMaps) {
                val newKeys = mutableListOf<MutableList<Any>>()

                for ((original, _) in map) {
                    for ((common, count) in commonForDenominator) {
                        if (itIsUnknown(original)) {
                            if (original == common && original.last() == 'f') {
                                map[original] = map[original]!! / gcd * decimalPoint
                            } else if (original.last() == common.last()) {
                                val newPower = original.first() as Double - common.first() as Double

                                map[original] = 0.0

                                if (newPower != 0.0) {
                                    val newKey = mutableListOf(newPower, '^', original.last())
                                    newKeys.add(newKey)
                                }
                            }
                        } else if (original == common) {
                            map[original] = map[original]!! - count
                        }
                    }
                }

                for (key in newKeys) {
                    map[key] = 1.0
                }
            }

            // Update commons for denominator
            for (element in commonForDenominator) {
                if (itIsUnknown(element.key)) {
                    if (element.key.last() == 'f') {
                        commonForDenominator[element.key] = gcd / decimalPoint
                    }
                    else {
                        commonForDenominator[element.key] = 1.0
                    }
                }
            }

            // Get all common entities
            val denominatorOutput = mutableListOf<Any>()
            val commonsToFind = hashMapOf<MutableList<Any>, Double>()
            val entities = mutableListOf<UnknownEntity>()

            for ((key, value) in commonForDenominator) {
                if (value != 0.0) {
                    commonsToFind[key] = value

                    if (itIsUnknown(key)) {
                        val entity = UnknownEntity()

                        entity.variable = key.last() as Char
                        entity.powerTo = key.first() as Double
                        if (key.last() == 'f') {
                            entity.multiplier = value
                        }
                        else {
                            entity.multiplier = 1.0
                        }
                        entities.add(entity)
                    }
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
                            is Function -> {
                                if (key == (denominatorElements[index][element] as Function).getKey()) {
                                    if (commonsToFind[key] != null && commonsToFind[key]!! > 0) {
                                        denominatorOutput.add((denominatorElements[index][element] as Function).copy())
                                        (denominatorOutput.last() as Function).powerTo = v
                                        commonsToFind[key] = 0.0
                                    }
                                }
                            }

                            is Fraction -> {
                                val keys = (denominatorElements[index][element] as Fraction).getKeys()

                                for (k in keys) {
                                    if (key == k.first) {
                                        if (commonsToFind[key] != null && commonsToFind[key]!! > 0) {
                                            when (k.third) {
                                                is Function -> {
                                                    denominatorOutput.add((k.third as Function).copy())
                                                    (denominatorOutput.last() as Function).powerTo = v
                                                }

                                                is Fraction -> {
                                                    denominatorOutput.add((k.third as Fraction).copy())
                                                    (denominatorOutput.last() as Fraction).powerTo = v
                                                }
                                            }
                                            commonsToFind[key] = 0.0
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

            for (entity in entities) {
                if (entity.powerTo == 0.0) {
                    entity.variable = 'f'
                }

                if (!(entity.multiplier == 1.0 && entity.onlyNumber())) {
                    if (decimalPoint != 1.0) {
                        denominatorOutput.add(
                            Fraction(
                                mutableListOf(entity),
                                mutableListOf(UnknownEntity(decimalPoint))
                            )
                        )
                    } else {
                        denominatorOutput.add(entity)
                    }
                }
            }

            return denominatorOutput
        }

        return mutableListOf()
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

        if (multiplicative != null) {
            if (multiplicative!!.isOne()) {
                multiplicative = null
            }
            else if (multiplicative!!.denominator != null && multiplicative!!.numerator.isEmpty()) {
                multiplicative!!.numerator = mutableListOf(UnknownEntity(1.0, 'f', 0.0))
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

    private fun shortenNumeratorWithDenominator() {
        var commons = hashMapOf<MutableList<Any>, Double>()
        for (element in denominatorMaps.last()) {
            commons[element.key] = element.value
        }

        for (map in denominatorMaps) {
            val newCommons = hashMapOf<MutableList<Any>, Double>()

            for (element in commons) {
                for ((key, value) in map) {
                    if (key == element.key) {
                        newCommons[element.key] = min(value, element.value)
                    }
                }
            }
            commons = newCommons
        }

        for (map in numeratorMaps) {
            val newCommons = hashMapOf<MutableList<Any>, Double>()

            for (element in commons) {
                for ((key, value) in map) {
                    if (key == element.key) {
                        newCommons[element.key] = min(value, element.value)
                    }
                }
            }
            commons = newCommons
        }

        val gcdList = mutableListOf<Pair<Double, Double>>()
        for (element in commons) {
            if (itIsUnknown(element.key)) {
                val gcd = findGCDNumerator(element.value)
                gcdList.add(gcd)
            }
        }
        for (element in commons) {
            if (itIsUnknown(element.key)) {
                val gcd = findGCDDenominator(element.value)
                gcdList.add(gcd)
            }
        }

        var gcd = Double.MAX_VALUE
        var decimalPoint = 1.0
        for (element in gcdList) {
            if (gcd > element.first) {
                gcd = element.first
                decimalPoint = element.second
            }
        }

        for (element in commons) {
            for (map in numeratorMaps) {
                for ((key, _) in map) {
                    if (key == element.key) {
                        if (itIsUnknown(key)) {
                            map[key] = map[key]!! / gcd * decimalPoint
                        }
                        else {
                            map[key] = map[key]!! - element.value
                        }
                    }
                }
            }

            for (map in denominatorMaps) {
                for ((key, _) in map) {
                    if (key == element.key) {
                        if (itIsUnknown(key)) {
                            map[key] = map[key]!! / gcd * decimalPoint
                        }
                        else {
                            map[key] = map[key]!! - element.value
                        }
                    }
                }
            }
        }

        updateNumeratorMaps()
        updateDenominatorMaps()
    }

    private fun updateNumeratorMaps() {
        for ((index, map) in numeratorMaps.withIndex()) {
            val toRemove = mutableListOf<MutableList<Any>>()

            for (element in map) {
                if (element.value == 0.0 || (element.value == 1.0 && element.key.last() == 'f')) {
                    toRemove.add(element.key)
                }
            }

            for (j in toRemove) {
                numeratorMaps[index].remove(j)
            }

            if (numeratorMaps[index].isEmpty()) {
                numeratorMaps[index][mutableListOf(0.0, '^', 'f')] = 1.0
            }
        }
    }

    private fun updateDenominatorMaps() {
        for ((index, map) in denominatorMaps.withIndex()) {
            val toRemove = mutableListOf<MutableList<Any>>()

            for (element in map) {
                if (element.value == 0.0 || (element.value == 1.0 && element.key.last() == 'f')) {
                    toRemove.add(element.key)
                }
            }

            for (j in toRemove) {
                denominatorMaps[index].remove(j)
            }

            if (denominatorMaps[index].isEmpty()) {
                denominatorMaps[index][mutableListOf(0.0, '^', 'f')] = 1.0
            }
        }
    }

    private fun rebuildFractionNumerator() {
        val outputNumerator = mutableListOf<Any>()
        var operatorIndex = 0

        for (index in 0..< numeratorMaps.size) {
            val fragment = mutableListOf<Any>()

            // Get fractions and functions
            for (entity in numeratorElements[index]) {
                for ((key, v) in numeratorMaps[index]) {
                    when (entity) {
                        is Function -> {
                            if (key == entity.getKey()) {
                                if (v != 0.0) {
                                    entity.powerTo = v
                                    fragment.add(entity)
                                }
                            }
                        }
                        is Fraction -> {
                            if (key == entity.getKey()) {
                                if (v != 0.0) {
                                    entity.powerTo = v
                                    fragment.add(entity)
                                }
                            }
                        }
                    }
                }
            }

            // Get unknown entities
            for ((key, v) in numeratorMaps[index]) {
                if (itIsUnknown(key)) {
                    if (v != 0.0) {
                        val newEntity = UnknownEntity()
                        newEntity.variable = key.last() as Char
                        newEntity.powerTo = key.first() as Double
                        if (newEntity.variable != 'f' && newEntity.powerTo == 0.0) newEntity.variable = 'f'

                        if (key.last() == 'f') {
                            newEntity.multiplier = v
                        }
                        else {
                            newEntity.multiplier = 1.0
                        }
                        fragment.add(newEntity)
                    }
                }
            }

            if (numeratorElements[index].isNotEmpty()) {
                outputNumerator.addAll(sortFragment(fragment))
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

        numerator = if (equationHasOperators(outputNumerator)) mutableListOf(Fraction(outputNumerator)) else outputNumerator
    }

    private fun rebuildFractionDenominator() {
        val outputDenominator = mutableListOf<Any>()
        var operatorIndex = 0

        for (index in 0..< denominatorMaps.size) {
            val fragment = mutableListOf<Any>()

            // Get fractions and functions
            for (entity in denominatorElements[index]) {
                for ((key, v) in denominatorMaps[index]) {
                    when (entity) {
                        is Function -> {
                            if (key == entity.getKey()) {
                                if (v != 0.0) {
                                    entity.powerTo = v
                                    fragment.add(entity)
                                }
                            }
                        }
                        is Fraction -> {
                            if (key == entity.getKey()) {
                                if (v != 0.0) {
                                    entity.powerTo = v
                                    fragment.add(entity)
                                }
                            }
                        }
                    }
                }
            }

            // Get unknown entities
            for ((key, v) in denominatorMaps[index]) {
                if (itIsUnknown(key)) {
                    if (v != 0.0) {
                        val newEntity = UnknownEntity()
                        newEntity.variable = key.last() as Char
                        newEntity.powerTo = key.first() as Double
                        if (newEntity.variable != 'f' && newEntity.powerTo == 0.0) newEntity.variable = 'f'

                        if (key.last() == 'f') {
                            newEntity.multiplier = v
                        }
                        else {
                            newEntity.multiplier = 1.0
                        }
                        fragment.add(newEntity)
                    }
                }
            }

            if (denominatorElements[index].isNotEmpty()) {
                outputDenominator.addAll(sortFragment(fragment))
            }

            if (operatorIndex < denominatorOperators.size && outputDenominator.isNotEmpty()) {
                outputDenominator.add(denominatorOperators[operatorIndex])
                operatorIndex++
            }
            else {
                operatorIndex++
            }
        }
        if (outputDenominator.isNotEmpty() && outputDenominator.last() is Char) {
            outputDenominator.removeLast()
        }

        denominator = if (equationHasOperators(outputDenominator)) mutableListOf(Fraction(outputDenominator)) else outputDenominator
    }

    private fun isNumberFraction(): Boolean {
        return !this.getValue().isEmpty()
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
                is Function -> result = max(i.powerTo, result)
                is Fraction -> {
                    result = max(i.powerTo, result)
                    result = max(i.getPower(), result)
                }
            }
        }

        return result
    }

    operator fun plus(other: Fraction): Fraction {
        if (other.isZero()) {
            return this
        }
        if (this.isZero()) {
            return other
        }

        if (this.denominator == other.denominator) {
            if (other.numerator.isNotEmpty()) {
                if (equationHasOperators(numerator)) {
                    if (equationHasOperators(other.numerator)) {
                        numerator = mutableListOf(Fraction(numerator), '+', Fraction(other.numerator))
                    }
                    else {
                        numerator = mutableListOf(Fraction(numerator))
                        numerator.add('+')
                        numerator.addAll(other.numerator)
                    }
                }
                else {
                    if (equationHasOperators(other.numerator)) {
                        numerator.add('+')
                        numerator.add(Fraction(other.numerator))
                    }
                    else {
                        numerator.add('+')
                        numerator.addAll(other.numerator)
                    }
                }
            }
        }
        else {
            if (other.denominator != null) {
                if (equationHasOperators(numerator)) {
                    if (equationHasOperators(other.denominator!!)) {
                        numerator = mutableListOf(Fraction(numerator), Fraction(other.denominator!!))
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

            if (this.denominator != null) {
                if (equationHasOperators(other.numerator)) {
                    if (equationHasOperators(this.denominator!!)) {
                        other.numerator = mutableListOf(Fraction(other.numerator), Fraction(this.denominator!!))
                    }
                    else {
                        other.numerator = mutableListOf(Fraction(numerator))
                        other.numerator.addAll(this.denominator!!)
                    }
                }
                else {
                    if (equationHasOperators(this.denominator!!)) {
                        other.numerator.add(Fraction(this.denominator!!))
                    }
                    else {
                        other.numerator.addAll(this.denominator!!)
                    }
                }
            }

            if (this.denominator != null) {
                if (this.denominator!!.isEmpty()) {
                    this.denominator = other.denominator
                }
                else {
                    if (equationHasOperators(this.denominator!!)) {
                        if (equationHasOperators(other.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!), Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                    else {
                        if (equationHasOperators(other.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                }
            }
            else {
                this.denominator = other.denominator
            }

            if (equationHasOperators(numerator)) {
                if (equationHasOperators(other.numerator)) {
                    numerator = mutableListOf(Fraction(numerator), '+', Fraction(other.numerator))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.add('+')
                    numerator.addAll(other.numerator)
                }
            }
            else {
                if (equationHasOperators(other.numerator)) {
                    numerator.add('+')
                    numerator.add(Fraction(other.numerator))
                }
                else {
                    numerator.add('+')
                    numerator.addAll(other.numerator)
                }
            }
        }
        return this
    }

    operator fun plus(other: UnknownEntity): Fraction {
        if (other.isZero()) {
            return this
        }
        if (this.isZero()) {
            return Fraction(mutableListOf(other))
        }

        if (denominator == null) {
            numerator.add('+')
            numerator.add(other)
        }
        else {
            val toAppend: Fraction
            if (equationHasOperators(denominator!!)) {
                toAppend = Fraction(mutableListOf(other, Fraction(denominator!!)))
            }
            else {
                toAppend = Fraction(denominator!!)
                toAppend.numerator.add(Fraction(mutableListOf(other)))
            }

            numerator.add('+')
            numerator.add(toAppend)
        }

        return this
    }

    operator fun minus(other: Fraction): Fraction {
        if (other.isZero()) {
            return this
        }
        if (this.isZero()) {
            return other
        }

        if (this.denominator == other.denominator) {
            if (other.numerator.isNotEmpty()) {
                if (equationHasOperators(numerator)) {
                    if (equationHasOperators(other.numerator)) {
                        numerator = mutableListOf(Fraction(numerator), '-', Fraction(other.numerator))
                    }
                    else {
                        numerator = mutableListOf(Fraction(numerator))
                        numerator.add('-')
                        numerator.addAll(other.numerator)
                    }
                }
                else {
                    if (equationHasOperators(other.numerator)) {
                        numerator.add('-')
                        numerator.add(Fraction(other.numerator))
                    }
                    else {
                        numerator.add('-')
                        numerator.addAll(other.numerator)
                    }
                }
            }
        }
        else {
            if (other.denominator != null) {
                if (equationHasOperators(numerator)) {
                    if (equationHasOperators(other.denominator!!)) {
                        numerator = mutableListOf(Fraction(numerator), Fraction(other.denominator!!))
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

            if (this.denominator != null) {
                if (equationHasOperators(other.numerator)) {
                    if (equationHasOperators(this.denominator!!)) {
                        other.numerator = mutableListOf(Fraction(other.numerator), Fraction(this.denominator!!))
                    }
                    else {
                        other.numerator = mutableListOf(Fraction(numerator))
                        other.numerator.addAll(this.denominator!!)
                    }
                }
                else {
                    if (equationHasOperators(this.denominator!!)) {
                        other.numerator.add(Fraction(this.denominator!!))
                    }
                    else {
                        other.numerator.addAll(this.denominator!!)
                    }
                }
            }

            if (this.denominator != null) {
                if (this.denominator!!.isEmpty()) {
                    this.denominator = other.denominator
                }
                else {
                    if (equationHasOperators(this.denominator!!)) {
                        if (equationHasOperators(other.denominator!!)) {
                            this.denominator = mutableListOf(Fraction(this.denominator!!), Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator = mutableListOf(Fraction(this.denominator!!))
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                    else {
                        if (equationHasOperators(other.denominator!!)) {
                            this.denominator!!.add(Fraction(other.denominator!!))
                        }
                        else {
                            this.denominator!!.addAll(other.denominator!!)
                        }
                    }
                }
            }
            else {
                this.denominator = other.denominator
            }

            if (equationHasOperators(numerator)) {
                if (equationHasOperators(other.numerator)) {
                    numerator = mutableListOf(Fraction(numerator), '-', Fraction(other.numerator))
                }
                else {
                    numerator = mutableListOf(Fraction(numerator))
                    numerator.add('-')
                    numerator.addAll(other.numerator)
                }
            }
            else {
                if (equationHasOperators(other.numerator)) {
                    numerator.add('-')
                    numerator.add(Fraction(other.numerator))
                }
                else {
                    numerator.add('-')
                    numerator.addAll(other.numerator)
                }
            }
        }
        return this
    }

    operator fun minus(other: UnknownEntity): Fraction {
        if (other.isZero()) {
            return this
        }
        if (this.isZero()) {
            return Fraction(mutableListOf(other))
        }

        if (denominator == null) {
            numerator.add('-')
            numerator.add(other)
        }
        else {
            val toAppend: Fraction
            if (equationHasOperators(denominator!!)) {
                toAppend = Fraction(mutableListOf(other, Fraction(denominator!!)))
            }
            else {
                toAppend = Fraction(denominator!!)
                toAppend.numerator.add(other)
            }

            numerator.add('-')
            numerator.add(toAppend)
        }

        return this
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

    override operator fun equals(other: Any?): Boolean {
        if (other is Fraction) {
            return if (this.isNumberFraction() && other.isNumberFraction()) {
                if (this.denominator == null && other.denominator == null) {
                    if ((this.numerator.last() as UnknownEntity).onlyNumber() || (other.numerator.last() as UnknownEntity).onlyNumber()) {
                        ((this.numerator.last() as UnknownEntity).onlyNumber() && (other.numerator.last() as UnknownEntity).onlyNumber())
                    }
                    else {
                        this.numerator.last() as UnknownEntity == other.numerator.last() as UnknownEntity
                    }
                } else if (this.denominator != null && other.denominator != null) {
                    this.numerator.last() == other.numerator.last() && this.denominator == other.denominator
                } else {
                    false
                }
            } else {
                this.getKey() == other.getKey() && this.getSeriesOfPowersTo() == other.getSeriesOfPowersTo()
            }
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