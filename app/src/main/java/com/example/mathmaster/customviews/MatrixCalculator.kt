package com.example.mathmaster.customviews

import kotlin.math.pow

class MatrixCalculator {
    // Converters
    fun convertToMatrix(list: DoubleArray, rows: Int, cols: Int): Array<DoubleArray> {
        val resultMatrix = Array(rows) { DoubleArray(cols) {0.0} }

        var row = 0
        var col = 0
        for (element in list) {
            resultMatrix[row][col] = element
            col++

            if (col >= cols) {
                col = 0
                row++
            }
        }

        return resultMatrix
    }

    fun convertTo1D(matrix: Array<DoubleArray>): DoubleArray {
        val list = DoubleArray(matrix.size * matrix[0].size)

        var index = 0
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                list[index] = matrix[row][col]
                index++
            }
        }

        return list
    }

    // Calculations
    fun multiplicationWithMatrix(firstMatrix: Array<DoubleArray>, secondMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val resultMatrix = Array(firstMatrix.size) { DoubleArray(secondMatrix[0].size) {0.0} }

        val firstMatrixRows = firstMatrix.size
        val firstMatrixColumns = firstMatrix[0].size
        val secondMatrixColumns = secondMatrix[0].size

        for (i in 0 until firstMatrixRows) {
            for (j in 0 until secondMatrixColumns) {
                for (k in 0 until firstMatrixColumns) {
                    resultMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j]
                }
            }
        }

        return resultMatrix
    }

    fun multiplicationWithScalar(matrix: Array<DoubleArray>, scalar: Double): Array<DoubleArray> {
        val resultMatrix = Array(matrix.size) { DoubleArray(matrix[0].size) {0.0} }

        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                resultMatrix[row][col] = matrix[row][col] * scalar
            }
        }

        return resultMatrix
    }

    fun addition(firstMatrix: Array<DoubleArray>, secondMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val resultMatrix = Array(firstMatrix.size) { DoubleArray(firstMatrix[0].size) {0.0} }

        for (row in firstMatrix.indices) {
            for (col in firstMatrix[row].indices) {
                resultMatrix[row][col] = firstMatrix[row][col] + secondMatrix[row][col]
            }
        }

        return resultMatrix
    }

    fun subtraction(firstMatrix: Array<DoubleArray>, secondMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val resultMatrix = Array(firstMatrix.size) { DoubleArray(firstMatrix[0].size) {0.0} }

        for (row in firstMatrix.indices) {
            for (col in firstMatrix[row].indices) {
                resultMatrix[row][col] = firstMatrix[row][col] - secondMatrix[row][col]
            }
        }
        return resultMatrix
    }

    // Transformations
    fun transpose(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        val transposed = Array(cols) { DoubleArray(rows) {0.0} }

        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                transposed[col][row] = matrix[row][col]
            }
        }

        return transposed
    }

    private fun subMatrix(matrix: Array<DoubleArray>, rowIndex: Int, colIndex: Int): Array<DoubleArray> {
        val subMatrix = Array(matrix.size-1) { DoubleArray(matrix.size-1) {0.0} }

        var newRow = 0
        for (row in matrix.indices) {
            if (row == rowIndex) {
                continue
            }
            var newCol = 0
            for (col in matrix[row].indices) {
                if (col == colIndex) {
                    continue
                }
                subMatrix[newRow][newCol] = matrix[row][col]
                newCol++
            }
            newRow++
        }

        return subMatrix
    }

    fun determinant(matrix: Array<DoubleArray>): Double {
        if (matrix.size == 1) {
            return matrix[0][0]
        }
        if (matrix.size == 2) {
            return (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0])
        }

        var result = 0.0
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (matrix[row][col] != 0.0) {
                    val subMatrix = subMatrix(matrix, row, col)
                    val firstEquationPart = (-1.0).pow(row+col)*matrix[row][col]
                    result += firstEquationPart * determinant(subMatrix)
                }
            }
        }
        return result
    }

    fun complementsMatrix(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val complementsMatrix = Array(matrix.size) { DoubleArray(matrix.size) {0.0} }

        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                val subMatrix = subMatrix(matrix, row, col)
                val firstEquationPart = (-1.0).pow(row+col)
                complementsMatrix[row][col] = firstEquationPart * determinant(subMatrix)
            }
        }

        return complementsMatrix
    }

    fun inverseMatrix(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val inverseMatrix = Array(matrix.size) { DoubleArray(matrix.size) {0.0} }
        val complementsMatrix = complementsMatrix(matrix)
        val transposed = transpose(complementsMatrix)

        val inverseDeterminant = 1/determinant(matrix)

        for (row in inverseMatrix.indices) {
            for (col in inverseMatrix[row].indices) {
                inverseMatrix[row][col] = inverseDeterminant * transposed[row][col]
            }
        }

        return inverseMatrix
    }

    fun findRank(matrix: Array<DoubleArray>): Int {
        if (matrix.size == 1) {
            return if (matrix[0][0] != 0.0) {
                1
            } else {
                0
            }
        }

        var rank = 0

        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                val subMatrix = subMatrix(matrix, row, col)
                val determinant = determinant(subMatrix)

                if (determinant != 0.0) {
                    return subMatrix.size
                }
                else {
                    val newRank = findRank(subMatrix)

                    if (newRank > rank) {
                        rank = newRank
                    }
                }
            }
        }

        return rank
    }

    private fun subMatrixForNonQuadraticMatrix(matrix: Array<DoubleArray>, rowIndex: Int, colIndex: Int, dimension: Int): Array<DoubleArray> {
        val subMatrix = Array(dimension) { DoubleArray(dimension) {0.0} }

        var newRow = 0
        for (row in matrix.indices) {
            if (row >= dimension+rowIndex || row < rowIndex) {
                continue
            }
            var newCol = 0
            for (col in matrix[row].indices) {
                if (col >= dimension+colIndex || col < colIndex) {
                    continue
                }
                subMatrix[newRow][newCol] = matrix[row][col]
                newCol++
            }
            newRow++
        }

        return subMatrix
    }

    fun findRankForNonQuadraticMatrix(matrix: Array<DoubleArray>, dimension: Int): Int {
        var rank = 0

        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (dimension * row > matrix.size) {
                    break
                }
                if (dimension * col > matrix[0].size) {
                    continue
                }

                val subMatrix = subMatrixForNonQuadraticMatrix(matrix, row, col, dimension)
                val determinant = determinant(subMatrix)

                if (determinant != 0.0) {
                    return subMatrix.size
                }
                else {
                    val newRank = findRankForNonQuadraticMatrix(subMatrix, dimension-1)

                    if (newRank > rank) {
                        rank = newRank
                    }
                }
            }
        }

        return rank
    }



}