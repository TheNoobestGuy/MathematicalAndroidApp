package com.example.mathmaster.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mathmaster.R
import kotlinx.coroutines.*

class MatrixKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    private val calculatorGrid: GridLayout
    private val calculatorButtons: Array<Button>
    private val matrixButtons: Array<Button>

    private val enterButton: Button
    private val deleteButton: Button

    private val rowPlusButton: Button
    private val rowMinusButton: Button
    private val colPlusButton: Button
    private val colMinusButton: Button

    private val oneButton: Button
    private val twoButton: Button
    private val threeButton: Button
    private val fourButton: Button
    private val fiveButton: Button
    private val sixButton: Button
    private val sevenButton: Button
    private val eightButton: Button
    private val nineButton: Button
    private val zeroButton: Button

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.matrixkeyboard_layout, this, true)

        // Get grid layout
        calculatorGrid = findViewById<GridLayout>(R.id.MatrixKeyboard)

        // Buttons
        enterButton = findViewById<Button>(R.id.Enter)
        deleteButton = findViewById<Button>(R.id.Delete)

        rowPlusButton = findViewById<Button>(R.id.RowPlus)
        rowMinusButton = findViewById<Button>(R.id.RowMinus)
        colPlusButton = findViewById<Button>(R.id.ColPlus)
        colMinusButton = findViewById<Button>(R.id.ColMinus)

        zeroButton = findViewById<Button>(R.id.Zero)
        oneButton = findViewById<Button>(R.id.One)
        twoButton = findViewById<Button>(R.id.Two)
        threeButton = findViewById<Button>(R.id.Three)
        fourButton = findViewById<Button>(R.id.Four)
        fiveButton = findViewById<Button>(R.id.Five)
        sixButton = findViewById<Button>(R.id.Six)
        sevenButton = findViewById<Button>(R.id.Seven)
        eightButton = findViewById<Button>(R.id.Eight)
        nineButton = findViewById<Button>(R.id.Nine)

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
        calculatorButtons = arrayOf(
            zeroButton,
            oneButton,
            twoButton,
            threeButton,
            fourButton,
            fiveButton,
            sixButton,
            sevenButton,
            eightButton,
            nineButton
        )

        matrixButtons = arrayOf(
            rowPlusButton,
            rowMinusButton,
            colPlusButton,
            colMinusButton
        )
    }

    fun addRow(matrix: Matrix) {
        rowPlusButton.setOnClickListener {
            rowPlusButton.setBackgroundResource(clickedButtonStyle)

            // Add row to matrix
            matrix.addRow()

            // Inflate matrix size
            val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
            val params = matrix.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentHeight = newHeight
            matrix.layoutParams = params

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                rowPlusButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun removeRow(matrix: Matrix) {
        rowMinusButton.setOnClickListener {
            rowMinusButton.setBackgroundResource(clickedButtonStyle)

            // Remove row from matrix
            matrix.removeRow()

            // Cut down matrix size
            val newHeight: Float = (matrix.getMatrixRows() * 0.25f)
            val params = matrix.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentHeight = newHeight
            matrix.layoutParams = params

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                rowMinusButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun addColumn(matrix: Matrix) {
        colPlusButton.setOnClickListener {
            colPlusButton.setBackgroundResource(clickedButtonStyle)

            // Add col to matrix
            matrix.addColumn()

            // Inflate matrix size
            val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
            val params = matrix.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = newWidth
            matrix.layoutParams = params

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                colPlusButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun removeColumn(matrix: Matrix) {
        colMinusButton.setOnClickListener {
            colMinusButton.setBackgroundResource(clickedButtonStyle)

            // Remove column from matrix
            matrix.removeColumn()

            // Cut down matrix size
            val newWidth: Float = (matrix.getMatrixColumns() * 0.25f)
            val params = matrix.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = newWidth
            matrix.layoutParams = params

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                colMinusButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun removeMatrixButtons() {
        // Remove buttons
        var i = 0
        var limit = calculatorGrid.childCount
        val paramsFirst = matrixButtons[0].layoutParams as GridLayout.LayoutParams
        while (i < limit) {
            val paramsSecond = calculatorGrid.getChildAt(i).layoutParams as GridLayout.LayoutParams

            if (paramsFirst.rowSpec == paramsSecond.rowSpec) {
                println("error: x")
                calculatorGrid.removeView(calculatorGrid.getChildAt(i))
            }
            i++
        }
        /*
        // Update gird
        val secondRowParams = calculatorButtons[4].layoutParams as GridLayout.LayoutParams
        val thirdRowParams = calculatorButtons[7].layoutParams as GridLayout.LayoutParams

        for(k in 0 until calculatorGrid.childCount) {
            val childParams = calculatorGrid.getChildAt(k).layoutParams as GridLayout.LayoutParams
            var row = 0
            println(row)
            if (childParams.rowSpec == secondRowParams.rowSpec) {
                row = 1
            }
            else if (childParams.rowSpec == thirdRowParams.rowSpec) {
                row = 2
            }

            childParams.rowSpec = GridLayout.spec(row, 1f)
            calculatorGrid.getChildAt(k).layoutParams = childParams
        }*/

        calculatorGrid.rowCount--
    }

    fun numberButtonClick(editText: EditText?) {
        for (i in calculatorButtons.indices) {
            calculatorButtons[i].setOnClickListener {
                calculatorButtons[i].setBackgroundResource(clickedButtonStyle)

                editText?.append(i.toString())

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    calculatorButtons[i].setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

    fun deleteButtonClick(editText: EditText?) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            if (editText != null) {
                val currentText = editText.text.toString()
                val newText = currentText.substring(0, currentText.length - 1)
                editText.setText(newText)
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(200)
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }
        }
    }

    fun clickEnterButton() {
        enterButton.setBackgroundResource(clickedButtonStyle)
    }

    fun unClickEnterButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            enterButton.setBackgroundResource(unClickedButtonStyle)
        }
    }

    fun getEnterButton(): Button {
        return enterButton
    }
}