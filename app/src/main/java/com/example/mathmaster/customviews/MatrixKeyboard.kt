package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
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
    private val matrixButtons: Array<Button>
    private val calculatorButtons: Array<Button>

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

        matrixButtons = arrayOf(
            rowPlusButton,
            rowMinusButton,
            colPlusButton,
            colMinusButton
        )

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
    }

    private fun refreshClickListeners(matrix: Matrix) {
        matrix.getClickedMatrixCell()
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

            refreshClickListeners(matrix)

            Handler(Looper.getMainLooper()).postDelayed({
                rowPlusButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
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

            Handler(Looper.getMainLooper()).postDelayed({
                rowMinusButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
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

            refreshClickListeners(matrix)
            Handler(Looper.getMainLooper()).postDelayed({
                colPlusButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
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

            Handler(Looper.getMainLooper()).postDelayed({
                colMinusButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun removeMatrixButtons() {
        // Remove buttons
        var i = 0
        var limit = calculatorGrid.childCount
        val paramsFirst = matrixButtons[0].layoutParams as GridLayout.LayoutParams
        var removedCount = 0
        while (i < limit && removedCount < 4) {
            val paramsSecond = calculatorGrid.getChildAt(i).layoutParams as GridLayout.LayoutParams

            if (paramsFirst.rowSpec == paramsSecond.rowSpec) {
                calculatorGrid.removeView(calculatorGrid.getChildAt(i))

                removedCount++
                limit--
                i--
            }
            i++
        }

        // Update gird
        var row = -1
        for(k in 0 until calculatorGrid.childCount) {
            if (k % 4 == 0) {
                row++
            }

            val childParams = calculatorGrid.getChildAt(k).layoutParams as GridLayout.LayoutParams
            childParams.rowSpec = GridLayout.spec(row, 1f)
            calculatorGrid.getChildAt(k).layoutParams = childParams
        }
        calculatorGrid.rowCount--
    }

    fun matrixMultiplicationMode() {
        var i = 0
        var limit = calculatorGrid.childCount
        var removedCount = 0
        var changedButtons = 0
        while (i < limit && (removedCount < 2 || changedButtons < 2)) {
            val button = calculatorGrid.getChildAt(i) as Button
            if (button.text == context.getString(R.string.RowPlus) || button.text == context.getString(R.string.RowMinus)) {
                calculatorGrid.removeView(button)
                removedCount++
                limit--
                i--
            }
            else if (button.text == context.getString(R.string.ColPlus)) {
                val params = button.layoutParams as GridLayout.LayoutParams
                params.columnSpec = GridLayout.spec(0, 2, 1f)
                params.rowSpec = GridLayout.spec(0, 1,1f)
                button.layoutParams = params
                changedButtons++
            }
            else if (button.text == context.getString(R.string.ColMinus)) {
                val params = button.layoutParams as GridLayout.LayoutParams
                params.columnSpec = GridLayout.spec(2, 2, 1f)
                params.rowSpec = GridLayout.spec(0, 1,1f)
                button.layoutParams = params
                changedButtons++
            }

            i++
        }
    }


    fun numberButtonClick(matrix: Matrix) {
        for (i in calculatorButtons.indices) {
            calculatorButtons[i].setOnClickListener {
                calculatorButtons[i].setBackgroundResource(clickedButtonStyle)

                val clickedCell: EditText? = matrix.getClickedCell()

                if (clickedCell != null) {
                    clickedCell.append(i.toString())
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    calculatorButtons[i].setBackgroundResource(unClickedButtonStyle)
                }, 200)
            }
        }
    }

    fun deleteButtonClick(matrix: Matrix) {
        deleteButton.setOnClickListener {
            deleteButton.setBackgroundResource(clickedButtonStyle)

            val clickedCell: EditText? = matrix.getClickedCell()

            if (clickedCell != null) {
                val currentText = clickedCell.text.toString()

                if (currentText.isNotEmpty()) {
                    val newText = currentText.substring(0, currentText.length - 1)
                    clickedCell.setText(newText)
                }

                if(clickedCell.text.isEmpty()) {
                    clickedCell.hint = "0"
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                deleteButton.setBackgroundResource(unClickedButtonStyle)
            }, 200)
        }
    }

    fun clickEnterButton() {
        enterButton.setBackgroundResource(clickedButtonStyle)
    }

    fun unClickEnterButton() {
        Handler(Looper.getMainLooper()).postDelayed({
            enterButton.setBackgroundResource(unClickedButtonStyle)
        }, 200)
    }

    fun getEnterButton(): Button {
        return enterButton
    }
}