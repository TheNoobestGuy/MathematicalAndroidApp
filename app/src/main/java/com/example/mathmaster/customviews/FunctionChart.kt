package com.example.mathmaster.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.example.mathmaster.R
import kotlin.math.round
import kotlin.math.abs

class FunctionChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var points: MutableList<Pair<Float, Float>> = mutableListOf()
    private var asymptotes: MutableList<Pair<Float, Float>> = mutableListOf()
    private var zeroPlaces: MutableList<Float> = mutableListOf()

    private var gridSpacing: Float = 0f
    private val gridColor = context.getColor(R.color.VeryLightGrey)
    private val axisColor = context.getColor(R.color.Black)
    private val linesColor = context.getColor(R.color.LimeGreen)
    private val dashEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)

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
        paint.strokeWidth = 3f
        gridSpacing = width/20f
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

        // Draw labels
        paint.textSize = 20f
        paint.style = Paint.Style.FILL
        iterator = gridSpacing
        var xAxisLabels = -((width/(gridSpacing))/2).toInt() + 1
        while (iterator <= width) {
            canvas.drawText(xAxisLabels.toString(), iterator+5f, height/2f+20f, paint)
            iterator += gridSpacing
            xAxisLabels++
        }

        iterator = gridSpacing
        var yAxisLabels: Int = ((height/(gridSpacing))/2).toInt()-1
        while (iterator <= height) {
            canvas.drawText(yAxisLabels.toString(), width/2f+5f, iterator+20f, paint)
            iterator += gridSpacing
            yAxisLabels--
        }

        // Draw a function
        paint.style = Paint.Style.STROKE
        paint.color = linesColor
        paint.strokeWidth = 8f
        for (i in 0 until points.size - 1) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[i + 1]

            if (!(abs(y1-y2) >= height)) {
                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }

        // Draw asymptotes
        paint.strokeWidth = 4f
        paint.color = context.getColor(R.color.LightGrey)
        paint.pathEffect = dashEffect
        for (asymptote in asymptotes) {
            canvas.drawLine(asymptote.first, 0f, asymptote.first, height.toFloat(), paint)
        }

        // Draw a frame
        paint.pathEffect = null
        paint.color = axisColor
        paint.strokeWidth = 10f
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        canvas.drawLine(width.toFloat(),  0f, width.toFloat(),  height.toFloat(), paint)
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

    fun updateInformation(zeroPlacesTextView: TextView, asymptotesTextView: TextView) {
        zeroPlacesTextView.text = context.getString(R.string.ZeroPlaces)
        asymptotesTextView.text = context.getString(R.string.Asymptotes)

        var added = false
        for (place in zeroPlaces) {
            val text = " $place,"
            zeroPlacesTextView.append(text)
            added = true
        }
        if (added) {
            zeroPlacesTextView.text = zeroPlacesTextView.text.dropLast(1)
        }

        added = false
        for (asymptote in asymptotes) {
            val text = " ${asymptote.second},"
            asymptotesTextView.append(text)
            added = true
        }
        if (added) {
            asymptotesTextView.text = asymptotesTextView.text.dropLast(1)
        }
    }

    fun drawAFunction(input: String, calculator: AdvancedKeyboard) {
        zeroPlaces.clear()
        asymptotes.clear()
        points.clear()

        paint.color = axisColor
        paint.strokeWidth = 200f

        val equation = calculator.transformEquation(input)

        // Draw function
        var lastPoint = 0.0
        var addedAsymptote = false
        if (equation.isNotEmpty()) {
            var x = -(width/(gridSpacing))/2f
            var iterator = 0f
            while (iterator <= width) {
                // Find Y value for X
                val equationAfterSubstitution = substituteVariable(equation, x.toDouble())
                val bufferX = width/2f + (x * gridSpacing)
                val y = calculator.calculate(equationAfterSubstitution, 0)

                // Check is it a zero place
                if (y.first == 0.0) {
                    zeroPlaces.add(x)
                }

                y.first = (height/2f) - (y.first*(gridSpacing))

                // Check is it asymptote
                if (abs(lastPoint-y.first) >= height) {
                    if ((lastPoint > 0 && y.first < 0) || (lastPoint < 0 && y.first > 0)) {
                        if (!addedAsymptote) {
                            asymptotes.add(Pair(bufferX, x))
                            points.add(Pair(bufferX, y.first.toFloat()))
                            addedAsymptote = true
                        }
                    }
                }
                else {
                    points.add(Pair(bufferX, y.first.toFloat()))
                    addedAsymptote = false
                }

                lastPoint = y.first
                x = round(x*100+1)/100
                if (noDecimalPoint(x)) {
                    iterator += gridSpacing
                }
            }
        }

        invalidate()
    }
}