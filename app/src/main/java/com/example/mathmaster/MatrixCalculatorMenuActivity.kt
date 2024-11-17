package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.BackButtonWithBar
import com.example.mathmaster.customviews.MatrixResultMenu

class MatrixCalculatorMenuActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity, sign: String) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            intent.putExtra("sign", sign)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrixcalculatormenu_activity)

        // Menu buttons
        val matrixMenu: MatrixResultMenu = findViewById<MatrixResultMenu>(R.id.MenuBlock)
        val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
        bottomBar.changeBackToExit()

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(matrixMenu.getMultipluButton(), clickedButtonStyle, MatrixCalculatorActivity(), "×")
        clickFunction(matrixMenu.getAddButton(), clickedButtonStyle, MatrixCalculatorActivity(), "+")
        clickFunction(matrixMenu.getSubtractButton(), clickedButtonStyle, MatrixCalculatorActivity(), "-")
        clickFunction(matrixMenu.getInfoButton(), clickedButtonStyle, ToolsActivity(), "0")
        clickFunction(bottomBar.returnBackButton(), clickedButtonStyle, ToolsActivity(), "0")
    }

    override fun onBackPressed() {
        // Do nothing, which disables the back button
    }
}