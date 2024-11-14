package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.BackButtonWithBar
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixKeyboard

class MatrixCalculatorActivity : ComponentActivity() {

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
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}