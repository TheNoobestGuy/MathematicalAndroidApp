package com.example.mathmaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mathmaster.customviews.BackButtonWithBar
import com.example.mathmaster.customviews.Matrix
import com.example.mathmaster.customviews.MatrixKeyboard
import com.example.mathmaster.customviews.MatrixResultMenu

class MatrixResultActivity : ComponentActivity() {

    private var showSignCounter = 1
    private val handler = Handler(Looper.getMainLooper())

    private fun clickFunction(button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
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
            val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
            bottomBar.changeBackToExit()

            // Disable visibility of content
            showSign.visibility = View.VISIBLE
            matrix.visibility = View.INVISIBLE
            matrixMenu.visibility = View.INVISIBLE
            bottomBar.visibility = View.INVISIBLE

            // Update counter
            if (showSignCounter > 0) {
                handler.postDelayed(this, 1000)
            } else {
                showSign.visibility = View.INVISIBLE

                matrix.visibility = View.VISIBLE
                matrixMenu.visibility = View.VISIBLE
                bottomBar.visibility = View.VISIBLE

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
        val resultMatrix: IntArray = intent.getIntArrayExtra("resultMatrix")!!
        val resultMatrixRows: Int = intent.getIntExtra("resultMatrixRows", 0)
        val resultMatrixColumns: Int = intent.getIntExtra("resultMatrixColumns", 0)
        matrix.setResultMatrix(matrix, resultMatrix, resultMatrixRows, resultMatrixColumns)

        // Menu buttons
        val matrixMenu: MatrixResultMenu = findViewById<MatrixResultMenu>(R.id.MenuBlock)
        val bottomBar: BackButtonWithBar = findViewById<BackButtonWithBar>(R.id.BottomBar)
        bottomBar.changeBackToExit()

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(bottomBar.returnBackButton(), clickedButtonStyle, MatrixCalculatorMenuActivity())
    }
}
