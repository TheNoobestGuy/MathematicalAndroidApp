package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.BackButtonWithBar
import com.example.mathmaster.customviews.Keyboard
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixKeyboard

class MatrixCalculatorActivity : ComponentActivity() {

    // Variables
    private val sign = "+"
    private var showSignCounter = 2
    private var matrixCounter = 1
    private val handler = Handler(Looper.getMainLooper())

    // Matrix
    private var firstMatrix: MutableList<Int> = mutableListOf()
    private var secondMatrix: MutableList<Int> = mutableListOf()
    private var resultMatrix: MutableList<Int> = mutableListOf()

    // Counter function for counting down before start of practice
    private val showSign = object : Runnable {
        override fun run() {
            // Get content
            val showSign: TextView = findViewById<TextView>(R.id.showSign)
            val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)
            val keyboard: MatrixKeyboard = findViewById<MatrixKeyboard>(R.id.Keyboard)
            val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
            bottomBar.changeBackToExit()

            // Change sign
            showSign.text = sign

            // Disable visibility of content
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

        // Matrix
        val matrix: Matrix = findViewById<Matrix>(R.id.Matrix)
        val currentCell: EditText = findViewById<EditText>(R.id.text) // TEST

        // Interactive menu
        val keyboard: MatrixKeyboard = findViewById<MatrixKeyboard>(R.id.Keyboard)

        // Menu buttons
        val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
        bottomBar.changeBackToExit()

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(bottomBar.returnBackButton(), clickedButtonStyle, ToolsActivity())

        // Keyboard
        keyboard.addRow(matrix)
        keyboard.addColumn(matrix)
        keyboard.removeRow(matrix)
        keyboard.removeColumn(matrix)
        keyboard.numberButtonClick(currentCell)
        keyboard.deleteButtonClick(currentCell)

        keyboard.getEnterButton().setOnClickListener {
            keyboard.clickEnterButton()

            if (matrixCounter == 1) {
                firstMatrix = matrix.getMatrixValues()

                handler.post(showSign)

                //keyboard.removeMatrixButtons()
                matrix.clearMatrix()
                keyboard.removeMatrixButtons()
                matrixCounter++
            }
            else {
                secondMatrix = matrix.getMatrixValues()

                // Make calculations
                for(i in firstMatrix.indices) {
                    resultMatrix.add(firstMatrix[i] + secondMatrix[i])
                }

                matrixCounter = 1
            }

            keyboard.unClickEnterButton()
        }
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}