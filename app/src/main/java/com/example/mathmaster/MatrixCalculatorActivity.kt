package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.Keyboard
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

        // Interactive menu
        val keyboard: MatrixKeyboard = findViewById<MatrixKeyboard>(R.id.Keyboard)

        // Menu buttons
        val backButton: Button = findViewById<Button>(R.id.Back)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(backButton, clickedButtonStyle, ToolsActivity())

        // Keyboard
        keyboard.numberButtonClick()
        keyboard.deleteButtonClick()
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}