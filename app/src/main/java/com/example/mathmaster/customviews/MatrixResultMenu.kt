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

class MatrixResultMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Matrix calculator
    private val matrixCalculator = MatrixCalculator()

    // Base matrix data
    private lateinit var matrix: Matrix
    private lateinit var backupMatrix: Array<DoubleArray>
    private var quadraticMatrix: Boolean = false
    private var backupMatrixRows: Int = 0
    private var backupMatrixColumns: Int = 0
    private lateinit var resultMatrix: Array<DoubleArray>
    private var resultMatrixRows: Int = 0
    private var resultMatrixColumns: Int = 0

    // Transpose
    private lateinit var transposeMatrix: Array<DoubleArray>

    // Complements
    private lateinit var complementsMatrix: Array<DoubleArray>

    // Power to
    private lateinit var matrixBeforePowerTo: Array<DoubleArray>
    private lateinit var resultMatrixBuffer: Array<DoubleArray>
    private lateinit var resultMatrixBeforeExp: Array<DoubleArray>
    private lateinit var resultMatrixAfterExp: Array<DoubleArray>

    // Inverse
    private lateinit var inverseMatrix: Array<DoubleArray>

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
        backupMatrix = matrixCalculator.convertToMatrix(array, rows, columns)
        backupMatrixRows = rows
        backupMatrixColumns = columns

        resultMatrix = matrixCalculator.convertToMatrix(array, rows, columns)
        resultMatrixRows = rows
        resultMatrixColumns = columns

        resultMatrixBuffer = matrixCalculator.convertToMatrix(array, rows, columns)
        resultMatrixBeforeExp = matrixCalculator.convertToMatrix(array, rows, columns)
        matrixBeforePowerTo = matrixCalculator.convertToMatrix(array, rows, columns)
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

    fun clickTransposeButton() {
        transposeButton.setOnClickListener {
            transposeButton.setBackgroundResource(clickedButtonStyle)

            // Transpose rows and columns
            transposeMatrix = matrixCalculator.transpose(resultMatrix)

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

                if (i == 1) {
                    resultMatrixAfterExp = matrixCalculator.multiplicationWithMatrix(resultMatrixBeforeExp, resultMatrixBeforeExp)
                    resultMatrixAfterExp = matrixCalculator.multiplicationWithMatrix(resultMatrixAfterExp, resultMatrixBeforeExp)
                }
                else {
                    resultMatrixAfterExp = matrixCalculator.multiplicationWithMatrix(resultMatrixBeforeExp, resultMatrixBeforeExp)
                }

                resultMatrixBeforeExp = resultMatrixAfterExp.copyOf()
                resultMatrixBuffer = resultMatrixAfterExp.copyOf()
                matrix.setResultMatrix(resultMatrixBeforeExp, resultMatrixRows, resultMatrixColumns, false)
                resultMatrix = resultMatrixAfterExp.copyOf()

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

            resultMatrix = matrixBeforePowerTo.copyOf()
            resultMatrixBuffer = matrixBeforePowerTo.copyOf()
            resultMatrixBeforeExp = matrixBeforePowerTo.copyOf()

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
            val determinant = matrixCalculator.determinant(resultMatrix)

            if (determinant != 0.0) {
                complementsMatrix = matrixCalculator.complementsMatrix(resultMatrix)
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

            val determinant = matrixCalculator.determinant(resultMatrix)

            if (determinant == 0.0) {
                Toast.makeText(context, "Det(A) = 0, so inverse matrix doesn't exist!", Toast.LENGTH_LONG).show()
            }
            else {
                inverseMatrix = matrixCalculator.inverseMatrix(resultMatrix)

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

    fun clickRankButton() {
        rankButton.setOnClickListener {
            rankButton.setBackgroundResource(clickedButtonStyle)

            val dimension = if (resultMatrix.size > resultMatrix[0].size) {
                resultMatrix[0].size
            }
            else {
                resultMatrix.size
            }

            val rank = matrixCalculator.findRankForNonQuadraticMatrix(resultMatrix, dimension)
            Toast.makeText(context, "Rank(A) = $rank", Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
                rankButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun clickDeterminantRankButton() {
        detRankButton.setOnClickListener {
            detRankButton.setBackgroundResource(clickedButtonStyle)

            val determinant = matrixCalculator.determinant(resultMatrix)

            if (determinant == 0.0) {
                val rank = matrixCalculator.findRank(resultMatrix)
                if (rank != 0) {
                    Toast.makeText(context, "Det(A) = 0! But Rank(A) = $rank", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, "Det(A) = 0! So is Rank(A) = $rank", Toast.LENGTH_LONG).show()
                }
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