package com.example.mathmaster.customviews

data class Fraction(var numerator: Int, var denominator: Int) {
    operator fun times(multiplier: Int): Fraction {
        return Fraction(numerator*multiplier, denominator*multiplier)
    }

    operator fun div(divisor: Int): Fraction {
        return Fraction(numerator/divisor, denominator/divisor)
    }

    operator fun div(divider: Fraction): Fraction {
        return Fraction(numerator*divider.denominator, denominator*divider.numerator)
    }
}

class Fractions {
    private fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
    }

    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }

    fun convertIntoFraction(input: Double, denominatorLimit: Int = 0): Fraction {
        var numerator = input
        var denominator = 1

        while (hasDecimal(numerator) || denominator < denominatorLimit) {
            numerator *= 10
            denominator *= 10
        }

        return Fraction(numerator.toInt(), denominator)
    }

    fun convertListIntoFractionsWithSameBase(input: MutableList<UnknownEntity>): MutableList<Fraction> {
        val fractionsList = mutableListOf<Fraction>()

        // Get fractions
        for (entity in input) {
            if (entity.multiplier != null) {
                fractionsList.add(convertIntoFraction(entity.multiplier!!))
            }
        }

        // Find and set fractions to same base
        var highestBase = 0
        for (fraction in fractionsList) {
            if (fraction.denominator > highestBase) {
                highestBase = fraction.denominator
            }
        }

        for (fraction in fractionsList) {
            while (fraction.denominator < highestBase) {
                fraction.numerator *= 10
                fraction.denominator *= 10
            }
        }

        return fractionsList
    }

    fun getAllNumeratorsOfList(input: MutableList<Fraction>): MutableList<Int> {
        val numeratorsList = mutableListOf<Int>()

        for (fraction in input) {
            numeratorsList.add(fraction.numerator)
        }

        return numeratorsList
    }

    fun shortenFraction(fraction: Fraction): Fraction {
        val gcd = gcd(fraction.numerator, fraction.denominator)
        return fraction/gcd
    }
}