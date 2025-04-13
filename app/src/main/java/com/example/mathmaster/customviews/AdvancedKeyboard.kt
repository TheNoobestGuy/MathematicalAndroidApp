package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import com.example.mathmaster.R

data class PairEquation<Double, Int> (var first: Double, var second: Int)
data class TripleSolve<T, C, D>(var list: T, var iterator: C, var compute: D)
data class Operators<Boolean, Int> (var occurrence: Int, var degrees: Boolean,
                                    var level: Int, var functionIndex: Int)

class AdvancedKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Calculator
    private val calculator = Calculator()

    // Grid layout
    private val gridLayout: GridLayout

    // Buttons styles
    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    // Main buttons
    private val enterButton: Button
    private val deleteButton: Button
    private val clearButton: Button

    // Number buttons
    private val buttons: Array<Button>
    private val oneButton: Button
    private val twoButton: Button
    private val threeButton: Button
    private val fourButton: Button
    private val fiveButton: Button
    private val sixButton: Button
    private val sevenButton: Button
    private val eightButton: Button
    private val nineButton: Button
    private val zeroButton: Button

    // Basic calculator buttons
    private val basicCalcButtons: Array<Button>
    private val addButton: Button
    private val subtractButton: Button
    private val multiplyButton: Button
    private val divideButton: Button

    // Special calculator buttons
    private val powerButton: Button
    private val dotButton: Button
    private val percentButton: Button
    private val factorialButton: Button
    private val fractionButton: Button
    private val numberPIButton: Button
    private val numberEulerButton: Button
    private val rootButton: Button

    private var dotUsed: Boolean = false

    // Functions of calculator
    private var functionsBeginnings: MutableList<Int> = mutableListOf()
    private val functionsButtons: Array<Button>
    private val logarithmButton: Button
    private val naturalLogarithmButton: Button
    private val sinButton: Button
    private val cosButton: Button
    private val tgButton: Button

    // Brackets
    private var bracketsCounter: Int = 0
    private val openBracketButton: Button
    private val closeBracketButton: Button

    private var bracketsLevel: ArrayDeque<Boolean> = ArrayDeque()
    private var functionEnds: ArrayDeque<Int> = ArrayDeque()
    private var functionIndex: Int = 0
    private var functionLevel: Int = 0

    private var operatorsOccurrence: ArrayDeque<Operators<Boolean, Int>> = ArrayDeque()
    private var degreesInUse: Boolean = false
    private var addDegree: Boolean = false

    // Options
    private val degreeButton: Button
    private val changeFunctionsButton: Button
    private var secondFunctions: Boolean = false
    private var radians: Boolean = true

    // Optional variable button
    private var functionChartMode: Boolean = false
    private var unknownsCalculatorMode: Boolean = false
    private var derivativeCalculatorMode: Boolean = false
    private var variableButton: Button
    private var equalSign: Boolean = false

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.advancedcalculator_layout, this, true)
        gridLayout = findViewById(R.id.AdvanceCalculatorKeyboard)

        // Main buttons
        enterButton = findViewById(R.id.Equal)
        deleteButton = findViewById(R.id.Delete)
        clearButton = findViewById(R.id.Clear)

        // Numbers
        zeroButton = findViewById(R.id.Zero)
        oneButton = findViewById(R.id.One)
        twoButton = findViewById(R.id.Two)
        threeButton = findViewById(R.id.Three)
        fourButton = findViewById(R.id.Four)
        fiveButton = findViewById(R.id.Five)
        sixButton = findViewById(R.id.Six)
        sevenButton = findViewById(R.id.Seven)
        eightButton = findViewById(R.id.Eight)
        nineButton = findViewById(R.id.Nine)

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
        buttons = arrayOf(
            zeroButton,
            oneButton,
            twoButton,
            threeButton,
            fourButton,
            fiveButton,
            sixButton,
            sevenButton,
            eightButton,
            nineButton
        )

        // Basic operations buttons
        addButton = findViewById(R.id.Plus)
        subtractButton = findViewById(R.id.Minus)
        multiplyButton = findViewById(R.id.Multiply)
        divideButton = findViewById(R.id.Divide)

        basicCalcButtons = arrayOf(
            addButton,
            subtractButton,
            multiplyButton,
            divideButton
        )

        // Special operations buttons
        powerButton = findViewById(R.id.PowerTo)
        dotButton = findViewById(R.id.Dot)
        percentButton = findViewById(R.id.Procent)
        factorialButton = findViewById(R.id.Factorial)
        fractionButton = findViewById(R.id.Fraction)
        numberPIButton = findViewById(R.id.PInumber)
        numberEulerButton = findViewById(R.id.Euler)
        rootButton = findViewById(R.id.Root)

        // Function operators
        logarithmButton = findViewById(R.id.Logaritm)
        naturalLogarithmButton = findViewById(R.id.NLogaritm)
        sinButton = findViewById(R.id.Sin)
        cosButton = findViewById(R.id.Cos)
        tgButton = findViewById(R.id.Tan)

        functionsButtons = arrayOf(
            logarithmButton,
            naturalLogarithmButton,
            sinButton,
            cosButton,
            tgButton
        )

        // Brackets
        openBracketButton = findViewById(R.id.FirstBracket)
        closeBracketButton = findViewById(R.id.SecondBracket)

        // Options
        degreeButton = findViewById(R.id.Degree)
        changeFunctionsButton = findViewById(R.id.Change)

        // Optional variable button
        variableButton = findViewById(R.id.Variable)
    }

    // Advance calculator
    private fun checkIsItDouble(number: Double): Boolean {
        return number % 1 != 0.0
    }

    private fun resultOfCalculate(textView: TextView, resultTextView: TextView) {
        if (clearButton.text == "AC") {
            clearButton.text = "C"
        }

        // Calculation
        val equation = calculator.transformEquation(textView.text.toString())
        val resultOfCalculations = calculator.calculateEquation(equation, 0)

        if (checkIsItDouble(resultOfCalculations.first)) {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            } else {
                val text = "= " + resultOfCalculations.first.toFloat().toString()
                resultTextView.text = text
            }
        } else {
            if (resultOfCalculations.first.isNaN()) {
                resultTextView.text = context.getString(R.string.Error)
            } else {
                val text = "= " + resultOfCalculations.first.toInt().toString()
                resultTextView.text = text
            }
        }
    }

    private fun enterButtonClick(
        textView: TextView,
        resultTextView: TextView,
        historyTextView: TextView
    ) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            // Create modified string for history text view
            if (resultTextView.text.isNotEmpty()) {
                var textBuffer = textView.text.toString()
                var counter = 0
                for (char in textBuffer) {
                    if (char == '(') {
                        counter++
                    } else if (char == ')') {
                        counter--
                    }
                }

                while (counter > 0) {
                    textBuffer += ')'
                    counter--
                }

                // Update history text view with created string
                historyTextView.append("\n")
                historyTextView.append("\n")
                historyTextView.append(textBuffer)
                historyTextView.append("\n")
                historyTextView.append(resultTextView.text)
            }

            // Update input and result text views
            if (resultTextView.text.isNotEmpty()) {
                textView.text = resultTextView.text.substring(2)
            }

            resultTextView.text = ""

            Handler(Looper.getMainLooper()).postDelayed({
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun numberButtonClick(textView: TextView, resultTextView: TextView) {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                var addedNumber = false

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != ')' && textView.text.last() != 'π'
                        && textView.text.last() != '!' && textView.text.last() != 'e'
                        && textView.text.last() != '%' && textView.text.last() != 'x'
                        && textView.text.last() != 'y' && textView.text.last() != 'z'
                    ) {
                        if (textView.text.last() == '°') {
                            textView.text = textView.text.dropLast(1)
                        }
                        if (!radians && functionLevel > 0) {
                            if (operatorsOccurrence.isNotEmpty()) {
                                if (operatorsOccurrence.last().functionIndex == functionIndex) {
                                    if (operatorsOccurrence.last().level == functionLevel) {
                                        if (operatorsOccurrence.last().degrees) {
                                            textView.append(i.toString())
                                            textView.append("°")
                                            degreesInUse = true
                                        } else {
                                            textView.append(i.toString())
                                        }
                                    }
                                } else if (bracketsLevel.last()) {
                                    textView.append(i.toString())
                                    textView.append("°")
                                    degreesInUse = true
                                } else {
                                    textView.append(i.toString())
                                }
                            } else {
                                textView.append(i.toString())
                                textView.append("°")
                                degreesInUse = true
                            }
                        } else {
                            textView.append(i.toString())
                        }

                        addedNumber = true
                    }
                } else {
                    textView.append(i.toString())
                    addedNumber = true
                }

                if (addedNumber) {
                    if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                        resultOfCalculate(textView, resultTextView)
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    buttons[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    private fun basicCalcButtonClick(textView: TextView) {
        for (i in basicCalcButtons.indices) {
            basicCalcButtons[i].setOnClickListener {
                basicCalcButtons[i].setBackgroundResource(clickedButtonStyle)

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == ')'
                        || textView.text.last() == 'π' || textView.text.last() == 'e'
                        || textView.text.last() == '!' || textView.text.last() == '%'
                        || textView.text.last() == 'x' || textView.text.last() == 'y'
                        || textView.text.last() == 'z'
                    ) {

                        textView.append(basicCalcButtons[i].text)

                        if (!radians && functionLevel > 0) {
                            if (bracketsLevel.last()) {
                                operatorsOccurrence.addLast(
                                    Operators(
                                        textView.text.length - 1, true,
                                        functionLevel, functionIndex
                                    )
                                )
                                addDegree = true
                            }
                        }

                        dotUsed = false
                    } else if (textView.text.last() == '(') {
                        if (basicCalcButtons[i].text == "-") {
                            textView.append(basicCalcButtons[i].text)
                        }
                        dotUsed = false
                    } else if (textView.text.last() == '°') {
                        val operator = basicCalcButtons[i].text
                        textView.append(operator)

                        if (operator == "×" || operator == "/") {
                            operatorsOccurrence.addLast(
                                Operators(
                                    textView.text.length - 1, false,
                                    functionLevel, functionIndex
                                )
                            )
                        }

                        dotUsed = false
                    }
                } else {
                    if (basicCalcButtons[i].text == "-") {
                        textView.append(basicCalcButtons[i].text)
                    }
                    dotUsed = false
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    basicCalcButtons[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    private fun powerButtonClick(textView: TextView) {
        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'π' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == '°'
                    || textView.text.last() == 'y' || textView.text.last() == 'z'
                ) {
                    textView.append(powerButton.text.toString())

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(
                                    textView.text.length - 1, false,
                                    functionLevel, functionIndex
                                )
                            )
                            addDegree = true
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun dotButtonClick(textView: TextView) {
        dotButton.setOnClickListener {
            dotButton.setBackgroundResource(clickedButtonStyle)

            if (!dotUsed) {
                if (textView.text.isNotEmpty()) {
                    if (textView.text.last().isDigit() || textView.text.last() == '°') {
                        if (textView.text.last() == '°') {
                            textView.text = textView.text.dropLast(1)
                        }

                        val text = dotButton.text
                        textView.append(text)

                        if (!radians) {
                            textView.append("°")
                        }

                        dotUsed = true
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                dotButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun rootButtonClick(textView: TextView) {
        rootButton.setOnClickListener {
            rootButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == '+'
                    || textView.text.last() == '-' || textView.text.last() == '×'
                    || textView.text.last() == '/' || textView.text.last() == 'π'
                    || textView.text.last() == '(' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z'
                ) {
                    textView.append("√")

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(
                                    textView.text.length - 1, false,
                                    functionLevel, functionIndex
                                )
                            )
                            addDegree = true
                        }
                    }
                }
            } else {
                textView.append("√")
            }

            Handler(Looper.getMainLooper()).postDelayed({
                rootButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun factorialButtonClick(textView: TextView, resultTextView: TextView) {
        factorialButton.setOnClickListener {
            factorialButton.setBackgroundResource(clickedButtonStyle)
            var appendedFactorial = false
            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z'
                ) {
                    textView.append("!")
                    appendedFactorial = true
                }
            }

            if (appendedFactorial) {
                if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                factorialButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun fractionButtonClick(textView: TextView) {
        fractionButton.setOnClickListener {
            fractionButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'π' || textView.text.last() == 'e'
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z' || textView.text.last() == '°'
                ) {
                    textView.append("^(-")

                    if (!radians && functionLevel > 0) {
                        if (bracketsLevel.last()) {
                            operatorsOccurrence.addLast(
                                Operators(
                                    textView.text.length - 1, false,
                                    functionLevel, functionIndex
                                )
                            )
                            addDegree = true
                        }
                    }

                    bracketsCounter++
                    bracketsLevel.addLast(false)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                fractionButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun percentButtonClick(textView: TextView, resultTextView: TextView) {
        percentButton.setOnClickListener {
            percentButton.setBackgroundResource(clickedButtonStyle)

            var appendedPercent = false
            if (textView.text.isNotEmpty()) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == 'x' || textView.text.last() == 'y'
                    || textView.text.last() == 'z'
                ) {
                    textView.append("%")
                    appendedPercent = true
                }
            }

            if (appendedPercent) {
                if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                percentButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun numberPIButtonClick(textView: TextView, resultTextView: TextView) {
        numberPIButton.setOnClickListener {
            numberPIButton.setBackgroundResource(clickedButtonStyle)

            var addedNumber = false

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'π'
                    && textView.text.last() != 'e' && textView.text.last() != '°'
                ) {
                    textView.append(numberPIButton.text.toString())
                    addedNumber = true
                }
            } else {
                textView.append(numberPIButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberPIButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun numberEulerButtonClick(textView: TextView, resultTextView: TextView) {
        numberEulerButton.setOnClickListener {
            numberEulerButton.setBackgroundResource(clickedButtonStyle)

            var addedNumber = false

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'π'
                    && textView.text.last() != 'e' && textView.text.last() != '°'
                ) {
                    textView.append(numberEulerButton.text.toString())
                    addedNumber = true
                }
            } else {
                textView.append(numberEulerButton.text.toString())
                addedNumber = true
            }

            if (addedNumber) {
                if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                    resultOfCalculate(textView, resultTextView)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                numberEulerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun clearButtonClick(
        textView: TextView,
        resultTextView: TextView,
        historyTextView: TextView
    ) {
        clearButton.setOnClickListener {
            clearButton.setBackgroundResource(clickedButtonStyle)

            bracketsCounter = 0
            operatorsOccurrence.clear()
            bracketsLevel.clear()
            functionLevel = 0
            functionEnds.clear()
            dotUsed = false
            equalSign = false
            textView.text = ""

            if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                if (clearButton.text == "AC") {
                    historyTextView.text = ""
                }

                clearButton.text = context.getString(R.string.AC)
                resultTextView.text = ""
            }
            if (derivativeCalculatorMode) {
                resultTextView.text = ""
            }

            Handler(Looper.getMainLooper()).postDelayed({
                clearButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun openBracketButtonClick(textView: TextView) {
        openBracketButton.setOnClickListener {
            openBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '°') {
                    val text = openBracketButton.text.toString()
                    textView.append(text)
                    bracketsCounter++
                    bracketsLevel.addLast(false)
                }
            } else {
                textView.append(openBracketButton.text)
                bracketsCounter++
                bracketsLevel.addLast(false)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                openBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun closeBracketButtonClick(textView: TextView) {
        closeBracketButton.setOnClickListener {
            closeBracketButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && bracketsCounter > 0) {
                if (textView.text.last().isDigit() || textView.text.last() == ')'
                    || textView.text.last() == '!' || textView.text.last() == 'π'
                    || textView.text.last() == 'e' || textView.text.last() == '°'
                    || textView.text.last() == '%' || textView.text.last() == 'x'
                    || textView.text.last() == 'y' || textView.text.last() == 'z'
                ) {
                    val text = closeBracketButton.text.toString()
                    textView.append(text)
                    bracketsCounter--

                    if (bracketsLevel.last()) {
                        functionEnds.addLast(textView.text.length - 1)
                        functionLevel--

                        if (functionLevel == 0) {
                            degreesInUse = false
                            addDegree = false
                        }
                    }
                    bracketsLevel.removeLast()
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                closeBracketButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun deleteButtonClick(textView: TextView, resultTextView: TextView) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() == '(') {
                    var deleted = false
                    for (end in functionsBeginnings) {
                        if (textView.text.length - 1 == end) {
                            textView.text = textView.text.dropLast(1)

                            while (textView.text.isNotEmpty() && textView.text.last().isLetter()
                                && textView.text.last() != 'x' && textView.text.last() != 'y' && textView.text.last() != 'z') {
                                textView.text = textView.text.dropLast(1)
                            }
                            deleted = true
                            break
                        }
                    }
                    if (!deleted) {
                        textView.text = textView.text.dropLast(1)
                    }
                    bracketsCounter--

                    if (bracketsLevel.last()) {
                        functionLevel--
                    }
                    bracketsLevel.removeLast()

                    if (functionLevel == 0) {
                        functionIndex--
                    }
                } else if (textView.text.last() == ')') {
                    if (functionEnds.isNotEmpty()) {
                        if (textView.text.length - 1 == functionEnds.last()) {
                            bracketsLevel.addLast(true)
                            functionLevel++
                            functionEnds.removeLast()
                            if (degreesInUse) {
                                addDegree = true
                            }
                        } else {
                            bracketsLevel.addLast(false)
                        }
                    } else {
                        bracketsLevel.addLast(false)
                    }

                    textView.text = textView.text.dropLast(1)
                    bracketsCounter++
                } else if (textView.text.last() == '°') {
                    textView.text = textView.text.dropLast(1)

                    if (textView.text.last().isDigit()) {
                        textView.text = textView.text.dropLast(1)

                        if (textView.text.last().isDigit()) {
                            textView.append("°")
                        }
                    }
                } else if (textView.text.last() == '.') {
                    dotUsed = false
                    textView.text = textView.text.dropLast(1)
                } else if (textView.text.last() == '=') {
                    equalSign = false
                    textView.text = textView.text.dropLast(1)
                } else {
                    if (operatorsOccurrence.isNotEmpty()) {
                        if (textView.text.length - 1 == operatorsOccurrence.last().occurrence) {
                            addDegree = operatorsOccurrence.last().degrees
                            operatorsOccurrence.removeLast()
                        }
                    }
                    textView.text = textView.text.dropLast(1)
                }
            }

            if (!functionChartMode && !unknownsCalculatorMode && !derivativeCalculatorMode) {
                if (textView.text.isNotEmpty()) {
                    resultOfCalculate(textView, resultTextView)
                } else {
                    resultTextView.text = ""
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun functionButtonClick(textView: TextView) {
        functionsButtons.forEach { button ->
            button.setOnClickListener {
                button.setBackgroundResource(clickedButtonStyle)

                if (textView.text.isNotEmpty()) {
                    if (textView.text.last() != '.') {
                        if (functionLevel == 0) {
                            functionIndex++
                        }

                        val text = button.text.toString() + "("
                        textView.append(text)
                        functionsBeginnings.add(textView.text.length - 1)
                        bracketsCounter++
                        dotUsed = false
                        functionLevel++
                        bracketsLevel.addLast(true)

                        if (!radians) {
                            degreesInUse = true
                            addDegree = true
                        }
                    }
                } else {
                    if (functionLevel == 0) {
                        functionIndex++
                    }

                    val text = button.text.toString() + "("
                    textView.append(text)
                    functionsBeginnings.add(textView.text.length - 1)
                    bracketsCounter++
                    dotUsed = false
                    functionLevel++
                    bracketsLevel.addLast(true)

                    if (!radians) {
                        degreesInUse = true
                        addDegree = true
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    button.setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    private fun degreeButtonClick() {
        degreeButton.setOnClickListener {
            degreeButton.setBackgroundResource(clickedButtonStyle)

            if (degreeButton.text == "deg") {
                degreeButton.text = context.getString(R.string.RadiansCalc)
                radians = true
            } else if (!secondFunctions) {
                degreeButton.text = context.getString(R.string.DegreeCalc)
                radians = false
                if (!degreesInUse) {
                    addDegree = true
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                degreeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun changeFunctionsButtonClick() {
        changeFunctionsButton.setOnClickListener {
            changeFunctionsButton.setBackgroundResource(clickedButtonStyle)

            if (changeFunctionsButton.text == context.getString(R.string.SecondCalc)) {
                changeFunctionsButton.text = context.getString(R.string.FirstCalc)
                sinButton.text = context.getString(R.string.Sin)
                cosButton.text = context.getString(R.string.Cos)
                tgButton.text = context.getString(R.string.Tan)

                sinButton.textSize = 16f
                cosButton.textSize = 16f
                tgButton.textSize = 16f

                secondFunctions = false
            } else {
                changeFunctionsButton.text = context.getString(R.string.SecondCalc)
                sinButton.text = context.getString(R.string.ASin)
                cosButton.text = context.getString(R.string.ACos)
                tgButton.text = context.getString(R.string.ATan)

                sinButton.textSize = 10f
                cosButton.textSize = 10f
                tgButton.textSize = 10f

                secondFunctions = true
                degreeButton.text = context.getString(R.string.RadiansCalc)
                radians = true
            }

            Handler(Looper.getMainLooper()).postDelayed({
                changeFunctionsButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setAllClickListenersForAdvanceCalculator(
        equation: TextView,
        result: TextView,
        history: TextView
    ) {
        numberButtonClick(equation, result)
        basicCalcButtonClick(equation)
        functionButtonClick(equation)
        openBracketButtonClick(equation)
        closeBracketButtonClick(equation)
        powerButtonClick(equation)
        dotButtonClick(equation)
        rootButtonClick(equation)
        factorialButtonClick(equation, result)
        numberPIButtonClick(equation, result)
        numberEulerButtonClick(equation, result)
        percentButtonClick(equation, result)
        fractionButtonClick(equation)

        enterButtonClick(equation, result, history)
        deleteButtonClick(equation, result)
        clearButtonClick(equation, result, history)
        degreeButtonClick()
        changeFunctionsButtonClick()
    }

    // Draw function chart
    fun setFunctionChartMode() {
        functionChartMode = true
        variableButton.text = "x"
        enterButton.text = ""

        var params = enterButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(6, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        enterButton.layoutParams = params

        params = variableButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(6, 1f)
        params.columnSpec = GridLayout.spec(4, 1f)
        variableButton.layoutParams = params
    }

    private fun variableButtonClick(textView: TextView) {
        variableButton.setOnClickListener {
            variableButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' &&  textView.text.last() != 'z'
                    && textView.text.last() != '°'
                ) {
                    textView.append(variableButton.text.toString())
                }
            } else {
                textView.append(variableButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                variableButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setAllClickListenersForFunctionChart(equation: TextView, blank: TextView) {
        numberButtonClick(equation, blank)
        basicCalcButtonClick(equation)
        functionButtonClick(equation)
        openBracketButtonClick(equation)
        closeBracketButtonClick(equation)
        powerButtonClick(equation)
        dotButtonClick(equation)
        rootButtonClick(equation)
        factorialButtonClick(equation, blank)
        numberPIButtonClick(equation, blank)
        numberEulerButtonClick(equation, blank)
        percentButtonClick(equation, blank)
        fractionButtonClick(equation)
        variableButtonClick(equation)

        deleteButtonClick(equation, blank)
        clearButtonClick(equation, blank, blank)
        degreeButtonClick()
        changeFunctionsButtonClick()
    }

    // Unknowns calculator
    fun setUnknownsCalculatorMode() {
        unknownsCalculatorMode = true

        gridLayout.removeView(sinButton)
        gridLayout.removeView(cosButton)
        gridLayout.removeView(tgButton)
        gridLayout.removeView(factorialButton)
        gridLayout.removeView(fractionButton)

        var params = rootButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(4, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        rootButton.layoutParams = params

        params = powerButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(3, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        powerButton.layoutParams = params

        params = changeFunctionsButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(1, 1f)
        changeFunctionsButton.layoutParams = params

        params = openBracketButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(2, 1f)
        openBracketButton.layoutParams = params

        params = closeBracketButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(3, 1f)
        closeBracketButton.layoutParams = params

        params = changeFunctionsButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        changeFunctionsButton.layoutParams = params

        params = degreeButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(3, 1f)
        degreeButton.layoutParams = params

        params = deleteButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(1, 1f)
        deleteButton.layoutParams = params

        params = clearButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        clearButton.layoutParams = params

        params = percentButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(1, 1f)
        params.columnSpec = GridLayout.spec(4, 1f)
        percentButton.layoutParams = params

        logarithmButton.text = "x"
        naturalLogarithmButton.text = "y"
        degreeButton.text = "z"
        changeFunctionsButton.text = context.getString(R.string.EqAdd)
        percentButton.text = context.getString(R.string.EqSubtract)
        variableButton.text = "✓"
    }

    private fun xVariableClick(textView: TextView) {
        logarithmButton.setOnClickListener {
            logarithmButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z'
                    && textView.text.last() != '°'
                ) {
                    textView.append(logarithmButton.text.toString())
                }
            } else {
                textView.append(logarithmButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                logarithmButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun yVariableClick(textView: TextView) {
        naturalLogarithmButton.setOnClickListener {
            naturalLogarithmButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z'
                    && textView.text.last() != '°'
                ) {
                    textView.append(naturalLogarithmButton.text.toString())
                }
            } else {
                textView.append(naturalLogarithmButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                naturalLogarithmButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun zVariableClick(textView: TextView) {
        degreeButton.setOnClickListener {
            degreeButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty()) {
                if (textView.text.last() != '.' && textView.text.last() != 'x'
                    && textView.text.last() != 'y' && textView.text.last() != 'z'
                    && textView.text.last() != '°'
                ) {
                    textView.append(degreeButton.text.toString())
                }
            } else {
                textView.append(degreeButton.text.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                degreeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun getAddEquation(): Button {
        return changeFunctionsButton
    }

    fun getSubtractEquation(): Button {
        return percentButton
    }

    fun getCheckButton(): Button {
        return variableButton
    }

    private fun enterWhenInUnknownsCalculatorMode(textView: TextView) {
        enterButton.setOnClickListener {
            enterButton.setBackgroundResource(clickedButtonStyle)

            if (textView.text.isNotEmpty() && !equalSign) {
                if (textView.text.last().isDigit() || textView.text.last() == 'x'
                    || textView.text.last() == 'y' || textView.text.last() == 'z'
                    || textView.text.last() == ')'
                ) {
                    textView.append(enterButton.text.toString())
                    equalSign = true
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                enterButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun setEqualSign() {
        equalSign = true
    }

    fun unsetEqualSign() {
        equalSign = false
    }

    fun getEqualSign(): Boolean {
        return equalSign
    }

    fun refreshAllClickListenersForUnknownsCalculator(textView: TextView, blank: TextView) {
        numberButtonClick(textView, blank)
        basicCalcButtonClick(textView)
        powerButtonClick(textView)
        dotButtonClick(textView)
        rootButtonClick(textView)
        numberPIButtonClick(textView, blank)
        numberEulerButtonClick(textView, blank)
        clearButtonClick(textView, blank, blank)
        openBracketButtonClick(textView)
        closeBracketButtonClick(textView)
        deleteButtonClick(textView, blank)
        xVariableClick(textView)
        yVariableClick(textView)
        zVariableClick(textView)
        enterWhenInUnknownsCalculatorMode(textView)
    }

    // Derivative calculator
    fun setDerivativeCalculatorMode() {
        derivativeCalculatorMode = true

        variableButton.text = "x"
        enterButton.text = "✓"
    }

    fun setAllClickListenersForDerivativeCalculator(equation: TextView, derivative: TextView, blank: TextView) {
        numberButtonClick(equation, blank)
        basicCalcButtonClick(equation)
        functionButtonClick(equation)
        openBracketButtonClick(equation)
        closeBracketButtonClick(equation)
        powerButtonClick(equation)
        dotButtonClick(equation)
        rootButtonClick(equation)
        factorialButtonClick(equation, blank)
        numberPIButtonClick(equation, blank)
        numberEulerButtonClick(equation, blank)
        percentButtonClick(equation, blank)
        fractionButtonClick(equation)
        variableButtonClick(equation)

        deleteButtonClick(equation, blank)
        clearButtonClick(equation, derivative, blank)
        degreeButtonClick()
        changeFunctionsButtonClick()
    }

    fun getEnterButton(): Button {
        return enterButton
    }

    fun clickEnterButton() {
        enterButton.setBackgroundResource(clickedButtonStyle)
    }

    fun unClickEnterButton() {
        Handler(Looper.getMainLooper()).postDelayed({
            enterButton.setBackgroundResource(unClickedButtonStyle)
        }, 100)
    }
}