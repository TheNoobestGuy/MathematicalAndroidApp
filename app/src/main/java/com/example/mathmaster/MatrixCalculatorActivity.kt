package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.key
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mathmaster.customviews.BackButtonWithBar
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixKeyboard

class MatrixCalculatorActivity : ComponentActivity() {

    // Variables
    private var showSignCounter = 1
    private val handler = Handler(Looper.getMainLooper())

    // Matrix
    private var firstMatrix: MutableList<Int> = mutableListOf()
    private var firstMatrixRows: Int = 0
    private var firstMatrixColumns: Int = 0
    private var secondMatrix: MutableList<Int> = mutableListOf()

    // Counter function to show equation sign after pressing enter
    private val showSign = object : Runnable {
        override fun run() {
            // Get content
            val sign: String = intent.getStringExtra("sign")!!
            val showSign: TextView = findViewById<TextView>(R.id.showSign)
            val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)
            val keyboard: MatrixKeyboard = findViewById<MatrixKeyboard>(R.id.Keyboard)
            val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
            bottomBar.changeBackToExit()

            // Change sign
            showSign.text = sign
            showSign.visibility = View.VISIBLE

            matrix.visibility = View.INVISIBLE
            keyboard.visibility = View.INVISIBLE
            bottomBar.visibility = View.INVISIBLE

            // Update counter
            if (showSignCounter > 0) {
                handler.postDelayed(this, 1000)
            } else {
                showSign.visibility = View.INVISIBLE

                matrix.visibility = View.VISIBLE
                keyboard.visibility = View.VISIBLE
                bottomBar.visibility = View.VISIBLE

                showSignCounter = 2
                handler.removeCallbacks(this)
            }

            showSignCounter--
        }
    }

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrixcalculator_activity)

        // Sign
        val sign: String = intent.getStringExtra("sign")!!
        val show: Boolean = intent.getBooleanExtra("show", false)

        // Matrix
        val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)

        var matrixCounter = intent.getIntExtra("matrixCounter", 1)
        var resultMatrix: IntArray = intent.getIntArrayExtra("resultMatrix")!!
        var resultMatrixRows: Int = intent.getIntExtra("resultMatrixRows", 0)
        var resultMatrixColumns: Int = intent.getIntExtra("resultMatrixColumns", 0)

        // Interactive menu
        val keyboard: MatrixKeyboard = findViewById<MatrixKeyboard>(R.id.Keyboard)

        // Menu buttons
        val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
        bottomBar.changeBackToExit()

        // Check is it first matrix that u calculate or user continue with matrix before
        if (sign == "i") {
            matrixCounter = 2
        }

        if (show) {
            handler.post(showSign)
        }

        // Calculate matrix that was before calculated
        if (matrixCounter >= 2) {
            for (i in resultMatrix) {
                firstMatrix.add(i)
            }
            firstMatrixRows = resultMatrixRows
            firstMatrixColumns = resultMatrixColumns

            // Remove redundant buttons and change size of keyboard
            if (sign == "×") {
                keyboard.matrixMultiplicationMode()

                while (matrix.getMatrixColumns() > 1) {
                    matrix.removeColumn()
                }

                var limit = matrix.getMatrixRows()
                while (limit > firstMatrixColumns) {
                    matrix.removeRow()
                    limit--
                }

                var run = false
                while (limit < firstMatrixColumns) {
                    matrix.addRow()
                    limit++
                    run = true
                }
                if (run) {
                    matrix.getClickedMatrixCell()
                }

                val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
                val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
                val params = matrix.layoutParams as ConstraintLayout.LayoutParams
                params.matchConstraintPercentWidth = newWidth
                params.matchConstraintPercentHeight = newHeight
                matrix.layoutParams = params
            }
            else if (sign == "i") {

            }
            else {
                keyboard.removeMatrixButtons()

                // Change calculator size
                val params = keyboard.layoutParams as ConstraintLayout.LayoutParams
                params.matchConstraintPercentHeight = 0.3f
            }
        }

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(bottomBar.returnBackButton(), clickedButtonStyle, ToolsActivity())

        // Matrix event listener
        matrix.getClickedMatrixCell()
        keyboard.numberButtonClick(matrix)
        keyboard.deleteButtonClick(matrix)

        // Keyboard
        keyboard.addRow(matrix)
        keyboard.addColumn(matrix)
        keyboard.removeRow(matrix)
        keyboard.removeColumn(matrix)

        // Enter Button
        keyboard.getEnterButton().setOnClickListener {
            keyboard.clickEnterButton()

            if (matrixCounter == 1) {
                // Disable visibility of content
                matrix.visibility = View.INVISIBLE
                keyboard.visibility = View.INVISIBLE
                bottomBar.visibility = View.INVISIBLE
                handler.post(showSign)

                firstMatrix = matrix.getMatrixValues()
                firstMatrixRows = matrix.getMatrixRows()
                firstMatrixColumns = matrix.getMatrixColumns()
                matrix.clearMatrix()

                // Remove redundant buttons and change size of keyboard
                if (sign == "×") {
                    keyboard.matrixMultiplicationMode()

                    while (matrix.getMatrixColumns() > 1) {
                        matrix.removeColumn()
                    }

                    var limit = matrix.getMatrixRows()
                    while (limit > firstMatrixColumns) {
                        matrix.removeRow()
                        limit--
                    }

                    var run = false
                    while (limit < firstMatrixColumns) {
                        matrix.addRow()
                        limit++
                        run = true
                    }
                    if (run) {
                        matrix.getClickedMatrixCell()
                    }

                    val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
                    val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
                    val params = matrix.layoutParams as ConstraintLayout.LayoutParams
                    params.matchConstraintPercentWidth = newWidth
                    params.matchConstraintPercentHeight = newHeight
                    matrix.layoutParams = params
                }
                else {
                    keyboard.removeMatrixButtons()

                    // Change calculator size
                    val params = keyboard.layoutParams as ConstraintLayout.LayoutParams
                    params.matchConstraintPercentHeight = 0.3f
                }

                matrixCounter++
            }
            else {
                secondMatrix = matrix.getMatrixValues()

                // Make calculations
                resultMatrixRows = firstMatrixRows
                resultMatrixColumns = matrix.getMatrixColumns()
                val resultMatrixSize = resultMatrixRows * resultMatrixColumns
                resultMatrix = IntArray(resultMatrixSize)

                if (sign == "+") {
                    for(i in firstMatrix.indices) {
                        resultMatrix[i] = firstMatrix[i] + secondMatrix[i]
                    }
                }
                else if (sign == "-") {
                    for(i in firstMatrix.indices) {
                        resultMatrix[i] = firstMatrix[i] - secondMatrix[i]
                    }
                }
                // Handle information about matrix
                else if (sign == "i") {

                }
                // Handle multiplication
                else {
                    var resultMatrixIndex = 0
                    var row = 0
                    while (resultMatrixIndex < resultMatrix.size) {
                        var leapLimit = 0

                        while (leapLimit < matrix.getMatrixColumns()) {
                            var leap = leapLimit
                            var equation = 0
                            var col = 0

                            while (col < firstMatrixColumns) {
                                println(secondMatrix[leap])
                                val index = (row * firstMatrixColumns) + col
                                equation += firstMatrix[index] * secondMatrix[leap]
                                leap += matrix.getMatrixColumns()
                                col++
                            }

                            resultMatrix[resultMatrixIndex] = equation
                            resultMatrixIndex++
                            leapLimit++
                        }

                        row++
                    }
                }

                // Go to end page
                val intent = Intent(this, MatrixResultActivity()::class.java)
                intent.putExtra("resultMatrix", resultMatrix)
                intent.putExtra("resultMatrixRows", resultMatrixRows)
                intent.putExtra("resultMatrixColumns", resultMatrixColumns)

                startActivity(intent)
            }

            keyboard.unClickEnterButton()
        }
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}