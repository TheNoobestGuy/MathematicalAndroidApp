package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.MatrixResultMenu

class MatrixCalculatorMenuActivity : ComponentActivity() {

    private fun clickFunction(button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            startActivity(intent)
        }
    }

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity, sign: String) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val resultMatrix: IntArray = IntArray(0)
            val resultMatrixRows: Int = 0
            val resultMatrixColumns: Int = 0

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
        val matrixMenu: MatrixResultMenu = findViewById<MatrixResultMenu>(R.id.MenuBlock)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(matrixMenu.getMultipluButton(), clickedButtonStyle, MatrixCalculatorActivity(), "×")
        clickFunction(matrixMenu.getAddButton(), clickedButtonStyle, MatrixCalculatorActivity(), "+")
        clickFunction(matrixMenu.getSubtractButton(), clickedButtonStyle, MatrixCalculatorActivity(), "-")
        clickFunction(matrixMenu.getInfoButton(), clickedButtonStyle, ToolsActivity(), "i")
    }

    override fun onBackPressed() {
        val intent = Intent(this, ToolsActivity()::class.java)
        startActivity(intent)
    }
}