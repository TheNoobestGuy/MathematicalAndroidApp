package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class MatrixCalculatorMenuActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity, sign: String) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val resultMatrix = DoubleArray(0)
            val resultMatrixRows = 0
            val resultMatrixColumns = 0

            val intent = Intent(this, view::class.java)
            intent.putExtra("show", false)
            intent.putExtra("sign", sign)
            intent.putExtra("matrixCounter", 1)
            intent.putExtra("resultMatrix", resultMatrix)
            intent.putExtra("resultMatrixRows", resultMatrixRows)
            intent.putExtra("resultMatrixColumns", resultMatrixColumns)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrixcalculatormenu_activity)

        // Menu buttons
        val multiplyButton = findViewById<Button>(R.id.MenuMultiply)
        val addButton = findViewById<Button>(R.id.MenuAdd)
        val subtractButton = findViewById<Button>(R.id.MenuSubtract)
        val otherButton = findViewById<Button>(R.id.MenuOther)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(multiplyButton, clickedButtonStyle, MatrixCalculatorActivity(), "×")
        clickFunction(addButton, clickedButtonStyle, MatrixCalculatorActivity(), "+")
        clickFunction(subtractButton, clickedButtonStyle, MatrixCalculatorActivity(), "-")
        clickFunction(otherButton, clickedButtonStyle, MatrixCalculatorActivity(), "i")

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@MatrixCalculatorMenuActivity, ToolsActivity()::class.java)
                startActivity(intent)
            }
        })
    }
}