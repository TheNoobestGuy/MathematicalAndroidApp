package com.example.mathmaster.customviews

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.mathmaster.R

data class MutableTriple<T, U, V>(var row: T, var col: U, var cell: V)

class Matrix @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val cellsArray: MutableList<MutableTriple<Int, Int, EditText>> = mutableListOf()
    private val gridLayout: GridLayout
    private var clickedCell: EditText? = null

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.matrix_layout, this, true)

        // Initialize grid layout
        gridLayout = findViewById<GridLayout>(R.id.GridLayoutMatrix)
        var row = 0
        var col = 0
        for (i in 0 until gridLayout.childCount) {
            if (col >= gridLayout.columnCount) {
                row++
                col = 0
            }
            val cell =  gridLayout.getChildAt(i) as EditText
            val buffor = MutableTriple(row, col, cell)

            cellsArray.add(buffor)
            col++
        }

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
    }

    private fun createCell(): MutableTriple<Int, Int, EditText> {
        val cell = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            filters = arrayOf(InputFilter.LengthFilter(4))
            background = AppCompatResources.getDrawable(context, R.drawable.matrix_cell_background)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
            }
        }
        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        cell.setTextColor(ContextCompat.getColor(context, R.color.White))
        cell.isFocusable = false
        cell.isCursorVisible = false
        cell.isClickable = true
        cell.hint = "0"
        cell.setHintTextColor(ContextCompat.getColor(context, R.color.White))
        cell.setPadding(0)

        return MutableTriple(0, 0, cell)
    }

    private fun updateGrid() {
        var row = 0
        var col = 0
        cellsArray.forEach { cell ->
            if (col >= gridLayout.columnCount) {
                row++
                col = 0
            }

            val params = cell.cell.layoutParams as GridLayout.LayoutParams
            params.rowSpec = GridLayout.spec(row, 1f)
            params.columnSpec = GridLayout.spec(col, 1f)
            params.setMargins(4)
            cell.row = row
            cell.col = col
            cell.cell.layoutParams = params
            col++
        }
    }

    fun addRow() {
        if (gridLayout.rowCount >= 4) {
            return
        }

        // Create cells and append them to their list
        for (col in 0 until gridLayout.columnCount) {
            val cell = createCell()
            cellsArray.add(cell)
        }

        // Add row to girdLayout
        gridLayout.rowCount++

        // Update positions
        updateGrid()

        // Append additional cells
        val index = cellsArray.size - gridLayout.columnCount

        for (i in index until cellsArray.size) {
            gridLayout.addView(cellsArray[i].cell)
        }
    }

    fun removeRow() {
        if (gridLayout.rowCount <= 1) {
            return
        }

        // Remove redundant cells
        val iterator = cellsArray.iterator()
        while (iterator.hasNext()) {
            val cell = iterator.next()
            var delete = false
            if (cell.row == (gridLayout.rowCount - 1)) {
                val paramsFirst = cell.cell.layoutParams as GridLayout.LayoutParams
                var i = 0
                var run = true
                while (i < gridLayout.childCount && run) {
                    val paramsSecond = gridLayout.getChildAt(i).layoutParams as GridLayout.LayoutParams

                    if (paramsFirst.rowSpec == paramsSecond.rowSpec) {
                        gridLayout.removeView(gridLayout.getChildAt(i))
                        delete = true
                        run = false
                    }
                    i++
                }
            }
            if (delete) {
                iterator.remove()
            }
        }

        // Update grid
        gridLayout.rowCount--
        updateGrid()
    }

    fun addColumn() {
        if (gridLayout.columnCount >= 4) {
            return
        }
        gridLayout.removeAllViews()
        for(cell in cellsArray) {
            gridLayout.addView(cell.cell)
        }
        // Variables for creating cells
        val rowCount = gridLayout.rowCount
        var leap = gridLayout.columnCount

        // Add column to girdLayout
        gridLayout.columnCount++

        // Create cells and append them to their list on proper places
        for (row in 0 until rowCount) {
            val cell = createCell()

            if (leap >= cellsArray.size) {
                cellsArray.add(cell)
            }
            else {
                cellsArray.add(leap, cell)
            }

            leap += gridLayout.columnCount
        }

        // Update positions
        updateGrid()

        // Append additional cells
        leap = gridLayout.columnCount
        for (i in (leap - 1) until cellsArray.size step leap) {
            gridLayout.addView(cellsArray[i].cell)
        }
    }

    fun removeColumn() {
        if (gridLayout.columnCount <= 1) {
            return
        }

        // Remove redundant cells
        val iterator = cellsArray.iterator()
        while (iterator.hasNext()) {
            val cell = iterator.next()
            var delete = false
            if (cell.col == (gridLayout.columnCount - 1)) {
                val paramsFirst = cell.cell.layoutParams as GridLayout.LayoutParams
                var i = 0
                var run = true
                while (i < gridLayout.childCount && run) {
                    val paramsSecond = gridLayout.getChildAt(i).layoutParams as GridLayout.LayoutParams

                    if (paramsFirst.columnSpec == paramsSecond.columnSpec) {
                        gridLayout.removeView(gridLayout.getChildAt(i))
                        delete = true
                        run = false
                    }
                    i++
                }
            }
            if(delete) {
                iterator.remove()
            }
        }

        // Update grid
        gridLayout.columnCount--
        updateGrid()
    }

    private fun hasDecimal(num: Double): Boolean {
        return num % 1.0 != 0.0
    }

    fun setResultMatrix(resultMatrix: DoubleArray, rows: Int, columns: Int, clickable: Boolean) {
        // Fill matrix
        cellsArray.clear()
        for(i in resultMatrix) {
            val cell = createCell()

            if (hasDecimal(i)) {
                cell.cell.setText(i.toString())
            }
            else {
                cell.cell.setText(i.toInt().toString())
            }

            if(!clickable) {
                cell.cell.isClickable = false
            }
            cellsArray.add(cell)
        }

        // Change matrix properties
        gridLayout.removeAllViews()
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns

        // Update matrix
        updateGrid()
        for(cell in cellsArray) {
            gridLayout.addView(cell.cell)
        }

        // Change matrix size
        val newWidth: Float = (columns * 0.25f)
        val newHeight: Float = (rows * 0.25f)
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = newWidth
        params.matchConstraintPercentHeight = newHeight
        this.layoutParams = params
    }

    fun getMatrixValues(): MutableList<Double> {
        val listOfValues: MutableList<Double> = mutableListOf()

        for (cell in cellsArray) {
            val value = cell.cell.text.toString()
            if (value == "") {
                listOfValues.add(0.0)
            }
            else {
                listOfValues.add(value.toDouble())
            }
        }

        return listOfValues
    }

    fun getClickedMatrixCell() {
        for(i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i) as EditText
            child.setOnClickListener {
                if (clickedCell != null) {
                    clickedCell!!.setBackgroundResource(unClickedButtonStyle)
                    clickedCell!!.hint = "0"
                }
                child.hint = ""
                child.setBackgroundResource(clickedButtonStyle)
                clickedCell = child
            }
        }
    }

    fun clearMatrix() {
        if (clickedCell != null) {
            clickedCell!!.setBackgroundResource(unClickedButtonStyle)
            clickedCell!!.hint = "0"
            clickedCell = null
        }

        for (cell in cellsArray) {
            cell.cell.setText("")
            cell.cell.hint = "0"
        }

        gridLayout.removeAllViews()
        for(cell in cellsArray) {
            gridLayout.addView(cell.cell)
        }
    }

    fun getMatrixRows(): Int {
        return gridLayout.rowCount
    }

    fun getMatrixColumns(): Int {
        return gridLayout.columnCount
    }

    fun getClickedCell(): EditText? {
        return clickedCell
    }
}