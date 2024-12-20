package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.example.mathmaster.R
import kotlin.math.*

class MatrixResultMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var quadraticMatrix: Boolean = false

    // Base matrix data
    private lateinit var matrix: Matrix
    private lateinit var backupMatrix: DoubleArray
    private var backupMatrixRows: Int = 0
    private var backupMatrixColumns: Int = 0
    private lateinit var resultMatrix: DoubleArray
    private var resultMatrixRows: Int = 0
    private var resultMatrixColumns: Int = 0

    // Transpose
    private lateinit var transposeMatrix: DoubleArray

    // Complements
    private lateinit var complementsMatrix: DoubleArray

    // Power to
    private lateinit var matrixBeforePowerTo: DoubleArray
    private lateinit var resultMatrixBuffer: DoubleArray
    private lateinit var resultMatrixBeforeExp: DoubleArray
    private lateinit var resultMatrixAfterExp: DoubleArray

    // Inverse
    private lateinit var inverseMatrix: DoubleArray

    private val multiplyButton: Button
    private val addButton: Button
    private val subtractButton: Button
    private val rankButton: Button
    private val transposeButton: Button
    private val undoButton: Button

    private val powerButton: Button
    private val complementButton: Button
    private val inverseButton: Button
    private val detRankButton: Button

    private val powerButtonsArray: Array<Button>
    private val powerTo2Button: Button
    private val powerTo3Button: Button
    private val undoPowerButton: Button
    private val backPowerButton: Button

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.matrixresultmenu_layout, this, true)

        // Get buttons
        multiplyButton = findViewById(R.id.MultiplyMatrix)
        addButton = findViewById(R.id.AddMatrix)
        subtractButton = findViewById(R.id.SubtractMatrix)
        transposeButton = findViewById(R.id.TransposeMatrix)
        rankButton = findViewById(R.id.RankMatrix)
        undoButton = findViewById(R.id.UndoMatrix)

        // Square matrix menu buttons
        powerButton = findViewById(R.id.PowerMatrix)
        complementButton = findViewById(R.id.ComplementMatrix)
        inverseButton = findViewById(R.id.InverseMatrix)
        detRankButton = findViewById(R.id.DetRankMatrix)

        // Power to menu
        powerTo2Button = findViewById(R.id.PowerTo2)
        powerTo3Button = findViewById(R.id.PowerTo3)
        undoPowerButton = findViewById(R.id.UndoPower)
        backPowerButton = findViewById(R.id.BackPower)

        powerButtonsArray = arrayOf(
            powerTo2Button,
            powerTo3Button
        )

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
    }

    fun setMatrix(obj: Matrix, array: DoubleArray, rows: Int, columns:Int) {
        matrix = obj
        backupMatrix = array.copyOf()
        backupMatrixRows = rows
        backupMatrixColumns = columns

        resultMatrix = array.copyOf()
        resultMatrixRows = rows
        resultMatrixColumns = columns

        resultMatrixBuffer = array.copyOf()
        resultMatrixBeforeExp = array.copyOf()
    }

    fun matrixIsQuadratic() {
        rankButton.visibility = View.GONE

        powerButton.visibility = View.VISIBLE
        inverseButton.visibility = View.VISIBLE
        detRankButton.visibility = View.VISIBLE
        complementButton.visibility = View.VISIBLE

        var params = transposeButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(0, 1f)
        transposeButton.layoutParams = params

        params = undoButton.layoutParams as GridLayout.LayoutParams
        params.rowSpec = GridLayout.spec(2, 1f)
        params.columnSpec = GridLayout.spec(2, 1f)
        undoButton.layoutParams = params

        quadraticMatrix = true
    }

    fun getMultiplyButton(): Button {
        return multiplyButton
    }

    fun getAddButton(): Button {
        return addButton
    }

    fun getSubtractButton(): Button {
        return subtractButton
    }

    private fun transpose(array: DoubleArray, dimension: Int): DoubleArray {
        val bufferArray = DoubleArray(array.size)

        var index = 0
        var iterator = 0
        var currentCol = 1
        while (currentCol * dimension <= array.size) {
            bufferArray[iterator] = array[index]

            index += dimension
            iterator++

            if (index >= array.size) {
                index = currentCol
                currentCol++
            }
        }

        return bufferArray
    }

    private fun subMatrix(array: DoubleArray, dimension: Int, row: Int, col: Int): DoubleArray {
        val subMatrix = DoubleArray(dimension*dimension)

        var iterator = 0
        var leapRow = row * (dimension+1)
        var leapCol = col

        for (cell in array.indices) {
            var found = false

            if (cell == leapRow && leapRow < ((row+1) * (dimension+1))) {
                leapRow++
                found = true
            }

            if (cell == leapCol) {
                leapCol += dimension+1
                found = true
            }

            if (!found) {
                subMatrix[iterator] = array[cell]
                iterator++
            }
        }

        return subMatrix
    }

    private fun determinant(array: DoubleArray, dimension: Int): Double {
        if (dimension == 1) {
            return array[0]
        }
        if (dimension == 2) {
            return (array[0] * array[3]) - (array[1] * array[2])
        }

        var result = 0.0
        var rowIndex = 0
        var colIndex = 0
        while (rowIndex < dimension) {
            if (array[colIndex] != 0.0) {
                val subMatrix = subMatrix(array, dimension-1, rowIndex, colIndex)
                val firstEquationPart = (-1.0).pow(colIndex+rowIndex)*array[colIndex]
                result += firstEquationPart * determinant(subMatrix, dimension-1)
            }

            colIndex++

            if (colIndex >= dimension) {
                rowIndex++
                colIndex = 0
            }
        }

        return result
    }

    private fun complementsMatrix(array: DoubleArray, dimension: Int) {
        complementsMatrix = DoubleArray(dimension*dimension)

        var rowIndex = 0
        var colIndex = 0
        for (cell in array.indices) {
            val subMatrix = subMatrix(array, dimension-1, rowIndex, colIndex)
            val firstEquationPart = (-1.0).pow(colIndex+rowIndex)
            complementsMatrix[cell] = firstEquationPart * determinant(subMatrix, dimension-1)

            colIndex++

            if (colIndex >= dimension) {
                rowIndex++
                colIndex = 0
            }
        }
    }

    private fun inverseMatrix(array: DoubleArray, dimension: Int): Boolean {
        val determinant = determinant(array, dimension)

        if (determinant == 0.0) {
            Toast.makeText(context, "Det(A) = 0, so inverse matrix doesn't exist!", Toast.LENGTH_LONG).show()
            return false
        }
        else {
            inverseMatrix = DoubleArray(dimension*dimension)

            complementsMatrix(array, dimension)
            complementsMatrix = transpose(complementsMatrix, dimension).copyOf()

            val inverseDeterminant = 1/determinant
            for (cell in complementsMatrix.indices) {
                inverseMatrix[cell] = inverseDeterminant * complementsMatrix[cell]
            }

            complementsMatrix = resultMatrix.copyOf()

            return true
        }
    }

    fun clickTransposeButton() {
        transposeButton.setOnClickListener {
            transposeButton.setBackgroundResource(clickedButtonStyle)

            // Transpose rows and columns
            transposeMatrix = DoubleArray(resultMatrixRows * resultMatrixColumns)
            transposeMatrix = transpose(resultMatrix, resultMatrixRows).copyOf()

            // Set new matrix
            val buffer = resultMatrixRows
            resultMatrixRows = resultMatrixColumns
            resultMatrixColumns = buffer
            matrix.setResultMatrix(transposeMatrix, resultMatrixRows, resultMatrixColumns, false)
            resultMatrix = transposeMatrix.copyOf()

            matrixBeforePowerTo = transposeMatrix.copyOf()
            resultMatrixBeforeExp = transposeMatrix.copyOf()
            resultMatrixBuffer = transposeMatrix.copyOf()

            Handler(Looper.getMainLooper()).postDelayed({
                transposeButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickUndoButton() {
        undoButton.setOnClickListener {
            undoButton.setBackgroundResource(clickedButtonStyle)

            matrix.setResultMatrix(backupMatrix, backupMatrixRows, backupMatrixColumns, false)

            resultMatrix = backupMatrix.copyOf()
            resultMatrixRows = backupMatrixRows
            resultMatrixColumns = backupMatrixColumns

            matrixBeforePowerTo = backupMatrix.copyOf()
            resultMatrixBeforeExp = backupMatrix.copyOf()
            resultMatrixBuffer = backupMatrix.copyOf()

            Handler(Looper.getMainLooper()).postDelayed({
                undoButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickPowerButton() {
        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(clickedButtonStyle)

            Handler(Looper.getMainLooper()).postDelayed({
                // Hide menu
                multiplyButton.visibility = View.GONE
                addButton.visibility = View.GONE
                subtractButton.visibility = View.GONE
                rankButton.visibility = View.GONE
                undoButton.visibility = View.GONE
                powerButton.visibility = View.GONE
                inverseButton.visibility = View.GONE
                detRankButton.visibility = View.GONE
                complementButton.visibility = View.GONE
                transposeButton.visibility = View.GONE

                // Show menu
                powerTo2Button.visibility = View.VISIBLE
                powerTo3Button.visibility = View.VISIBLE
                undoPowerButton.visibility = View.VISIBLE
                backPowerButton.visibility = View.VISIBLE

                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickPowersToButtons() {
        for (i in powerButtonsArray.indices) {
            powerButtonsArray[i].setOnClickListener {
                powerButtonsArray[i].setBackgroundResource(clickedButtonStyle)

                resultMatrixAfterExp = DoubleArray(resultMatrixRows*resultMatrixColumns)

                val powerTo = i + 1
                var iterator = 0
                while (iterator < powerTo) {
                    var resultMatrixIndex = 0
                    var row = 0
                    while (resultMatrixIndex < resultMatrix.size) {
                        var leapLimit = 0

                        while (leapLimit < resultMatrixColumns) {
                            var leap = leapLimit
                            var equation = 0.0
                            var col = 0

                            while (col < resultMatrixColumns) {
                                val index = (row * resultMatrixColumns) + col
                                equation += resultMatrixBeforeExp[index] * resultMatrixBuffer[leap]
                                leap += resultMatrixColumns
                                col++
                            }

                            resultMatrixAfterExp[resultMatrixIndex] = equation
                            resultMatrixIndex++
                            leapLimit++
                        }

                        row++
                    }

                    iterator++
                }

                resultMatrixBeforeExp = resultMatrixAfterExp.copyOf()
                resultMatrixBuffer = resultMatrixAfterExp.copyOf()
                matrix.setResultMatrix(resultMatrixBeforeExp, resultMatrixRows, resultMatrixColumns, false)

                Handler(Looper.getMainLooper()).postDelayed({
                    powerButtonsArray[i].setBackgroundResource(unClickedButtonStyle)
                }, 100)
            }
        }
    }

    fun clickUndoPowerButton() {
        undoPowerButton.setOnClickListener {
            undoPowerButton.setBackgroundResource(clickedButtonStyle)

            matrix.setResultMatrix(matrixBeforePowerTo, resultMatrixRows, resultMatrixColumns, false)

            resultMatrixBuffer = backupMatrix.copyOf()
            resultMatrixBeforeExp = backupMatrix.copyOf()

            Handler(Looper.getMainLooper()).postDelayed({
                undoPowerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickBackButton() {
        backPowerButton.setOnClickListener {
            backPowerButton.setBackgroundResource(clickedButtonStyle)

            Handler(Looper.getMainLooper()).postDelayed({
                // Hide menu
                powerTo2Button.visibility = View.GONE
                powerTo3Button.visibility = View.GONE
                undoPowerButton.visibility = View.GONE
                backPowerButton.visibility = View.GONE

                // Show menu
                multiplyButton.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
                subtractButton.visibility = View.VISIBLE
                rankButton.visibility = View.VISIBLE
                transposeButton.visibility = View.VISIBLE
                undoButton.visibility = View.VISIBLE

                if (quadraticMatrix) {
                    rankButton.visibility = View.GONE
                    powerButton.visibility = View.VISIBLE
                    inverseButton.visibility = View.VISIBLE
                    detRankButton.visibility = View.VISIBLE
                    complementButton.visibility = View.VISIBLE
                }

                backPowerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickComplementButton() {
        complementButton.setOnClickListener {
            complementButton.setBackgroundResource(clickedButtonStyle)
            val determinant = determinant(resultMatrix, resultMatrixRows)

            if (determinant != 0.0) {
                complementsMatrix(resultMatrix, resultMatrixRows)
                matrix.setResultMatrix(complementsMatrix, resultMatrixRows, resultMatrixColumns, false)
                resultMatrix = complementsMatrix.copyOf()

                matrixBeforePowerTo = complementsMatrix.copyOf()
                resultMatrixBeforeExp = complementsMatrix.copyOf()
                resultMatrixBuffer = complementsMatrix.copyOf()
            }
            else {
                Toast.makeText(context, "Det(A) = 0, so complements matrix doesn't exist!",
                    Toast.LENGTH_LONG).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                complementButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickInverseButton() {
        inverseButton.setOnClickListener {
            inverseButton.setBackgroundResource(clickedButtonStyle)

            val run = inverseMatrix(resultMatrix, resultMatrixRows)
            if (run) {
                matrix.setResultMatrix(inverseMatrix, resultMatrixRows, resultMatrixColumns,false)
                resultMatrix = inverseMatrix.copyOf()

                matrixBeforePowerTo = inverseMatrix.copyOf()
                resultMatrixBeforeExp = inverseMatrix.copyOf()
                resultMatrixBuffer = inverseMatrix.copyOf()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                inverseButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    private fun subMatrixForRank(array: DoubleArray, dimension: Int, row: Int, col: Int, columns: Int, flag: Boolean): DoubleArray {
        val subMatrix = DoubleArray(dimension*dimension)

        var iterator = 0
        var columnLimit = 1
        var rowLimit = 0

        var currentRow = row
        var currentColumn = col
        for (cell in array.indices) {
            if (iterator > subMatrix.size) {
                return subMatrix
            }

            if (columnLimit > dimension) {
                rowLimit++
                if (rowLimit == dimension) {
                    break
                }

                currentColumn = if (flag) {
                    (++currentRow * columns) + (col % columns)
                } else {
                    (++currentRow * (dimension+1)) + (col % (dimension+1))
                }

                columnLimit = 1
            }

            if (cell == currentColumn) {
                subMatrix[iterator] = array[cell]
                currentColumn++
                columnLimit++
                iterator++
            }
        }

        return subMatrix
    }

    private fun checkSubMatricesForRank(array: DoubleArray, dimension: Int, columns: Int, flag: Boolean): Int {
        var rank = 0
        var row = 0
        var column = 0
        var columnLimit = 0
        val dimensionLimit = if (flag) {
            dimension-1
        }
        else {
            if (dimension == columns) dimension-1 else dimension
        }

        for (cell in array.indices) {
            // Move column index if reached end of possibilities of creating a new sub arrays
            if ((columnLimit + dimensionLimit) > columns) {
                row++
                column = if (flag) {
                    row*columns
                }
                else {
                    row*dimension
                }
                columnLimit = 0

                // Check does new sub matrix can exist
                var counter = 1
                var rowBuffer = row
                var run = false
                while(counter < dimension) {
                    rowBuffer++

                    val columnBuffer = if (flag) {
                        if (columns == dimension-1) {
                            rowBuffer*columns
                        }
                        else {
                            rowBuffer*dimension
                        }
                    }
                    else {
                        rowBuffer*dimension
                    }

                    if (columnBuffer > array.size) {
                        run = true
                        break
                    }

                    counter++
                }

                if (run) {
                    break
                }
            }

            // Create new sub matrix for every possibilities of creating it
            if (cell == column) {
                if (rank < dimension-1) {
                    val subMatrix = subMatrixForRank(array, dimension-1, row, column, columns, flag)
                    val determinant = determinant(subMatrix, dimension-1)

                    if (determinant != 0.0) {
                        rank = if (dimension-1 > rank) dimension-1 else rank
                    }
                    else {
                        val buffer = checkSubMatricesForRank(subMatrix, dimension-1, dimension, false)
                        rank = if (buffer > rank) buffer else rank
                    }
                }
                else {
                    return rank
                }

                column++
                columnLimit++
            }
        }

        return rank
    }

    private fun findRank(flag: Boolean): Int {
        if (resultMatrixRows == resultMatrixColumns) {
            val determinant = determinant(resultMatrix, resultMatrixRows)

            if (determinant != 0.0) {
                return resultMatrixRows
            }
        }

        // Find rank of matrix
        val dimension = if (flag) {
            if (resultMatrixRows > resultMatrixColumns) resultMatrixColumns else resultMatrixRows
        }
        else {
            resultMatrixColumns-1
        }
        val rank = checkSubMatricesForRank(resultMatrix, dimension+1, resultMatrixColumns, flag)

        return rank
    }

    fun clickRankButton() {
        rankButton.setOnClickListener {
            rankButton.setBackgroundResource(clickedButtonStyle)

            val rank = findRank(true)

            Toast.makeText(context, "Rank(A) = $rank", Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
                rankButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickDeterminantRankButton() {
        detRankButton.setOnClickListener {
            detRankButton.setBackgroundResource(clickedButtonStyle)

            val determinant = determinant(resultMatrix, resultMatrixRows)

            if (determinant == 0.0) {
                val rank = findRank(false)
                Toast.makeText(context, "Det(A) = 0! But Rank(A) = $rank", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(context, "Det(A) = $determinant", Toast.LENGTH_LONG).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
               detRankButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }
}