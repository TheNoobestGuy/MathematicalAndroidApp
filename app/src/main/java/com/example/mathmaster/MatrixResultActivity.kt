package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixResultMenu

class MatrixResultActivity : ComponentActivity() {

    private var sign = "="
    private var showSignCounter = 1
    private val handler = Handler(Looper.getMainLooper())

    private fun clickFunction(button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity,
                               sign: String, resultMatrix: IntArray,
                               resultMatrixRows: Int, resultMatrixColumns: Int) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            intent.putExtra("show", true)
            intent.putExtra("sign", sign)
            intent.putExtra("matrixCounter", 2)
            intent.putExtra("resultMatrix", resultMatrix)
            intent.putExtra("resultMatrixRows", resultMatrixRows)
            intent.putExtra("resultMatrixColumns", resultMatrixColumns)

            startActivity(intent)
        }
    }

    // Counter function to show equation sign after pressing enter
    private val showSign = object : Runnable {
        override fun run() {
            // Get content
            val showSign: TextView = findViewById<TextView>(R.id.showSign)
            val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)
            val matrixMenu: MatrixResultMenu = findViewById<MatrixResultMenu>(R.id.MenuBlock)

            // Disable visibility of content
            showSign.text = sign
            showSign.visibility = View.VISIBLE

            matrix.visibility = View.INVISIBLE
            matrixMenu.visibility = View.INVISIBLE

            // Update counter
            if (showSignCounter > 0) {
                handler.postDelayed(this, 1000)
            } else {
                showSign.visibility = View.INVISIBLE

                matrix.visibility = View.VISIBLE
                matrixMenu.visibility = View.VISIBLE

                showSignCounter = 2
                handler.removeCallbacks(this)
            }

            showSignCounter--
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrixresult_activity)

        // Show equality sign before showing result
        handler.post(showSign)

        // Matrix
        val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)
        var resultMatrix: IntArray = intent.getIntArrayExtra("resultMatrix")!!
        val resultMatrixRows: Int = intent.getIntExtra("resultMatrixRows", 0)
        val resultMatrixColumns: Int = intent.getIntExtra("resultMatrixColumns", 0)
        matrix.setResultMatrix(matrix, resultMatrix, resultMatrixRows, resultMatrixColumns, false)

        // After power
        var resultMatrixBuffor: IntArray = resultMatrix.copyOf()
        val resultMatrixAfterExp: IntArray = IntArray(resultMatrixRows*resultMatrixColumns)

        // After inverse
        val resultMatrixAfterInverse: IntArray = IntArray(resultMatrixRows*resultMatrixColumns)

        // Menu buttons
        val matrixMenu: MatrixResultMenu = findViewById<MatrixResultMenu>(R.id.MenuBlock)
        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked
        val unClickedButtonStyle = R.drawable.menubutton_background

        matrixMenu.matrixIsQuadratic()

        // On click functions
        clickFunction(matrixMenu.getMultiplyButton(), clickedButtonStyle, MatrixCalculatorActivity(),
            "×", resultMatrix, resultMatrixRows, resultMatrixColumns)
        clickFunction(matrixMenu.getAddButton(), clickedButtonStyle, MatrixCalculatorActivity(),
            "+", resultMatrix, resultMatrixRows, resultMatrixColumns)
        clickFunction(matrixMenu.getSubtractButton(), clickedButtonStyle, MatrixCalculatorActivity(),
            "-", resultMatrix, resultMatrixRows, resultMatrixColumns)

        // Create custom view for information!!!!
        clickFunction(matrixMenu.getInfoButton(), clickedButtonStyle, MatrixCalculatorActivity(),
            "i", resultMatrix, resultMatrixRows, resultMatrixColumns)

        // Power matrix to handler
        matrixMenu.clickPowerButton()

        for (i in  0 until matrixMenu.getPowersToButtons().size) {
            matrixMenu.getPowersToButtons()[i].setOnClickListener {
                matrixMenu.getPowersToButtons()[i].setBackgroundResource(clickedButtonStyle)

                val powerTo = i + 1
                var iterator = 0
                while (iterator < powerTo) {
                    var resultMatrixIndex = 0
                    var row = 0
                    while (resultMatrixIndex < resultMatrix.size) {
                        var leapLimit = 0

                        while (leapLimit < resultMatrixColumns) {
                            var leap = leapLimit
                            var equation = 0
                            var col = 0

                            while (col < resultMatrixColumns) {
                                val index = (row * resultMatrixColumns) + col
                                equation += resultMatrix[index] * resultMatrixBuffor[leap]
                                leap += resultMatrixColumns
                                col++
                            }

                            resultMatrixAfterExp[resultMatrixIndex] = equation
                            resultMatrixIndex++
                            leapLimit++
                        }

                        row++
                    }

                    resultMatrixBuffor = resultMatrixAfterExp.copyOf()
                    iterator++
                }

                resultMatrix = resultMatrixAfterExp.copyOf()
                matrix.setResultMatrix(matrix, resultMatrix, resultMatrixRows, resultMatrixColumns, false)

                Handler(Looper.getMainLooper()).postDelayed({
                    matrixMenu.getPowersToButtons()[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }

        matrixMenu.clickBackButton()

        // Inverse matrix handler

    }

    override fun onBackPressed() {
        val intent = Intent(this, MatrixCalculatorMenuActivity()::class.java)
        startActivity(intent)
    }
}
