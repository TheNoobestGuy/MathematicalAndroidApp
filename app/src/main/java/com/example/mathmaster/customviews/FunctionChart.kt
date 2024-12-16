package com.example.mathmaster.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.mathmaster.R

class FunctionChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var points: MutableList<Pair<Float, Float>> = mutableListOf()

    private var gridSpacing: Float = 0f
    private val gridColor = context.getColor(R.color.VeryLightGrey)
    private val axisColor = context.getColor(R.color.Black)
    private val pointsColor = context.getColor(R.color.LimeGreen)

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(context.getColor(R.color.White))

        // Draw coordinate system
        paint.color = gridColor
        paint.strokeWidth = 5f
        gridSpacing = width/10f
        var iterator = 0f
        while (iterator <= width) {
            canvas.drawLine(iterator, 0f, iterator, height.toFloat(), paint)
            iterator += gridSpacing
        }

        iterator = 0f
        while (iterator <= height) {
            canvas.drawLine(0f, iterator, width.toFloat(), iterator, paint)
            iterator += gridSpacing
        }

        // Draw X and Y axis
        paint.color = axisColor
        canvas.drawLine(0f, height/2f, width.toFloat(), height/2f, paint)
        canvas.drawLine(width/2f, 0f,width/2f, height.toFloat(), paint)

        // Draw a frame
        paint.strokeWidth = 10f
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        canvas.drawLine(width.toFloat(),  0f, width.toFloat(),  height.toFloat(), paint)

        // Draw points
        paint.color = pointsColor
        paint.strokeWidth = 8f
        for (point in points) {
            canvas.drawPoint(point.first, point.second, paint)
        }

        // Connect points with lines
        for (i in 0 until points.size - 1) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[i + 1]
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    private fun substituteVariable(equation: MutableList<Any>, variable: Double): MutableList<Any> {
        val result = mutableListOf<Any>()

        for (element in equation) {
            when (element) {
                is Char -> {
                    if (element == 'x') {
                        result.add(variable)
                    }
                    else {
                        result.add(element)
                    }
                }
                else -> {
                    result.add(element)
                }
            }
        }

        return result
    }

    private fun noDecimalPoint(number: Float): Boolean {
        return number % 1 == 0f
    }

    fun drawAFunction(input: String, calculator: AdvancedKeyboard) {
        points.clear()

        paint.color = axisColor
        paint.strokeWidth = 200f

        val equation = calculator.transformEquation(input)
        //println(equation)
        // Draw function
        var x = -(width/(gridSpacing))/2f
        var iterator = 0f
        while (iterator <= width) {
            val equationAfterSubstitution = substituteVariable(equation, x.toDouble())
            val bufferX = width/2f + (x * gridSpacing)
            val y = calculator.calculate(equationAfterSubstitution, 0)
            y.first = (height/2f) - (y.first*(gridSpacing))
            points.add(Pair(bufferX, y.first.toFloat()))

            x += 0.3f
            if (noDecimalPoint(x)) {
                iterator += gridSpacing
            }
        }

        invalidate()
    }
}