package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixKeyboard
import com.example.mathmaster.customviews.MatrixCalculator

class MatrixCalculatorActivity : ComponentActivity() {

    private val matrixCalculator = MatrixCalculator()

    // Variables
    private var showSignCounter = 1
    private val handler = Handler(Looper.getMainLooper())

    // Matrix
    private lateinit var firstMatrix: Array<DoubleArray>
    private lateinit var secondMatrix: Array<DoubleArray>
    private var firstMatrixRows: Int = 0
    private var firstMatrixColumns: Int = 0

    // Counter function to show equation sign after pressing enter
    private val showSign = object : Runnable {
        override fun run() {
            // Get content
            val sign: String = intent.getStringExtra("sign")!!
            val showSign: TextView = findViewById(R.id.showSign)
            val matrix: Matrix = findViewById(R.id.Matrix)
            val keyboard: MatrixKeyboard = findViewById(R.id.Keyboard)

            // Change sign
            showSign.text = sign
            showSign.visibility = View.VISIBLE

            matrix.visibility = View.INVISIBLE
            keyboard.visibility = View.INVISIBLE

            // Update counter
            if (showSignCounter > 0) {
                handler.postDelayed(this, 1000)
            } else {
                showSign.visibility = View.INVISIBLE

                matrix.visibility = View.VISIBLE
                keyboard.visibility = View.VISIBLE

                showSignCounter = 2
                handler.removeCallbacks(this)
            }

            showSignCounter--
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrixcalculator_activity)

        // Sign
        val sign: String = intent.getStringExtra("sign")!!
        val show: Boolean = intent.getBooleanExtra("show", false)

        // Matrix
        val matrix: Matrix = findViewById(R.id.Matrix)

        var matrixCounter = intent.getIntExtra("matrixCounter", 1)
        val originalMatrix = intent.getDoubleArrayExtra("resultMatrix")!!
        var resultMatrixRows: Int = intent.getIntExtra("resultMatrixRows", 0)
        var resultMatrixColumns: Int = intent.getIntExtra("resultMatrixColumns", 0)
        var resultMatrix: Array<DoubleArray>

        // Interactive menu
        val keyboard: MatrixKeyboard = findViewById(R.id.Keyboard)

        // Check is it first matrix that u calculate or user continue with matrix before
        if (sign == "i") {
            matrixCounter = 2
        }

        if (show) {
            handler.post(showSign)
        }

        // Calculate matrix that was before calculated
        if (matrixCounter >= 2) {
            firstMatrix = matrixCalculator.convertToMatrix(originalMatrix, resultMatrixRows, resultMatrixColumns)
            firstMatrixRows = resultMatrixRows
            firstMatrixColumns = resultMatrixColumns

            // Remove redundant buttons and change size of keyboard
            when (sign) {
                "×" -> {
                    if (firstMatrixRows != 1 || firstMatrixColumns != 1) {
                        keyboard.matrixMultiplicationMode()
                    }

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

                    // Change matrix size
                    val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
                    val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
                    val params = matrix.layoutParams as ConstraintLayout.LayoutParams
                    params.matchConstraintPercentWidth = newWidth
                    params.matchConstraintPercentHeight = newHeight
                    matrix.layoutParams = params
                }
                "i" -> {

                }
                else -> {
                    keyboard.removeMatrixButtons()

                    var limit = matrix.getMatrixRows()
                    var run = false
                    while (limit < firstMatrixRows) {
                        matrix.addRow()
                        limit++
                        run = true
                    }

                    limit = matrix.getMatrixColumns()
                    while (limit < firstMatrixColumns) {
                        matrix.addColumn()
                        limit++
                        run = true
                    }

                    if (run) {
                        matrix.getClickedMatrixCell()
                    }

                    // Change matrix size
                    val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
                    val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
                    var params = matrix.layoutParams as ConstraintLayout.LayoutParams
                    params.matchConstraintPercentWidth = newWidth
                    params.matchConstraintPercentHeight = newHeight
                    matrix.layoutParams = params

                    // Change calculator size
                    params = keyboard.layoutParams as ConstraintLayout.LayoutParams
                    params.matchConstraintPercentHeight = 0.3f
                    keyboard.layoutParams = params
                }

            }
        }

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
            var wait: Long = 200

            if (matrixCounter >= 2) {
                wait = 0
            }

            handler.postDelayed({
                if (matrixCounter == 1) {
                    // Disable visibility of content
                    matrix.visibility = View.INVISIBLE
                    keyboard.visibility = View.INVISIBLE
                    handler.post(showSign)

                    firstMatrixRows = matrix.getMatrixRows()
                    firstMatrixColumns = matrix.getMatrixColumns()
                    firstMatrix = matrix.getMatrixValues()

                    matrix.clearMatrix()

                    // Remove redundant buttons and change size of keyboard
                    if (sign == "×") {
                        if (firstMatrixRows == 1 && firstMatrixColumns == 1) {
                            while (matrix.getMatrixRows() < 2) {
                                matrix.addRow()
                            }

                            while (matrix.getMatrixColumns() < 2) {
                                matrix.addColumn()
                            }

                            matrix.getClickedMatrixCell()
                        }
                        else {
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
                        }

                        val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
                        val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
                        val params = matrix.layoutParams as ConstraintLayout.LayoutParams
                        params.matchConstraintPercentWidth = newWidth
                        params.matchConstraintPercentHeight = newHeight
                        matrix.layoutParams = params
                    } else {
                        keyboard.removeMatrixButtons()

                        // Change calculator size
                        val params = keyboard.layoutParams as ConstraintLayout.LayoutParams
                        params.matchConstraintPercentHeight = 0.3f
                    }

                    wait = 0
                    matrixCounter++
                    keyboard.unClickEnterButton()
                } else {
                    secondMatrix = matrix.getMatrixValues()

                    // Make calculations
                    var scalarFirst = false
                    var scalarSecond = false
                    if (firstMatrixRows == 1 && firstMatrixColumns == 1) {
                        scalarFirst = true
                    }
                    if (matrix.getMatrixRows() == 1 && matrix.getMatrixColumns() == 1) {
                        scalarSecond = true
                    }

                    when (sign) {
                        "+" -> {
                            resultMatrix = matrixCalculator.addition(firstMatrix, secondMatrix)
                        }
                        "-" ->  {
                            resultMatrix = matrixCalculator.subtraction(firstMatrix, secondMatrix)
                        }
                        "i" -> {
                            resultMatrix = secondMatrix
                        }
                        // Handle multiplication
                        else -> {
                            resultMatrix = if (!scalarFirst && !scalarSecond) {
                                matrixCalculator.multiplicationWithMatrix(firstMatrix, secondMatrix)
                            } else {
                                if (scalarFirst) {
                                    matrixCalculator.multiplicationWithScalar(secondMatrix, firstMatrix[0][0])
                                } else {
                                    matrixCalculator.multiplicationWithScalar(firstMatrix, secondMatrix[0][0])
                                }
                            }
                        }
                    }

                    resultMatrixRows = resultMatrix.size
                    resultMatrixColumns = resultMatrix[0].size

                    // Go to end page
                    val intent = Intent(this, MatrixResultActivity()::class.java)
                    intent.putExtra("resultMatrix", matrixCalculator.convertTo1D(resultMatrix))
                    intent.putExtra("resultMatrixRows", resultMatrixRows)
                    intent.putExtra("resultMatrixColumns", resultMatrixColumns)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }, wait)
        }

        // Handle the back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@MatrixCalculatorActivity, MatrixCalculatorMenuActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@MatrixCalculatorActivity,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}