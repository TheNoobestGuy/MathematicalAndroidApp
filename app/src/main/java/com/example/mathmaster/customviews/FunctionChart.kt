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

    private val calculator = Calculator()

    private val paint = Paint()
    private var points: MutableList<Pair<Float, Float>> = mutableListOf()
    private var verticalAsymptotes: MutableList<Pair<Float, Float>> = mutableListOf()
    private var horizontalAsymptote: MutableList<Pair<Float, Float>> = mutableListOf()
    private var zeroPlaces: MutableList<Pair<Float, Float>> = mutableListOf()

    private var gridSpacing: Float = 0f
    private val gridColor = context.getColor(R.color.VeryLightGrey)
    private val axisColor = context.getColor(R.color.Black)
    private val asymptoteColor = context.getColor(R.color.LightGrey)
    private val linesColor = context.getColor(R.color.LimeGreen)
    private val dashEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)

    private var showZeroPlaces: Boolean = false
    private var showHorizontalAsymptote: Boolean = false
    private var showVerticalAsymptotes: Boolean = false

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
        if (!showZeroPlaces && !showHorizontalAsymptote && !showVerticalAsymptotes) {
            // Draw labels
            paint.textSize = 25f
            paint.style = Paint.Style.FILL
            iterator = gridSpacing
            var xAxisLabels = -((width/(gridSpacing))/2).toInt() + 1
            while (iterator <= width) {
                canvas.drawText(xAxisLabels.toString(), iterator+5f, height/2f+25f, paint)
                iterator += gridSpacing
                xAxisLabels++
            }

            iterator = gridSpacing
            var yAxisLabels: Int = ((height/(gridSpacing))/2).toInt()-1
            while (iterator <= height) {
                canvas.drawText(yAxisLabels.toString(), width/2f+5f, iterator+25f, paint)
                iterator += gridSpacing
                yAxisLabels--
            }
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

        // Asymptotes paint style
        paint.strokeWidth = 5f
        paint.color = asymptoteColor
        paint.pathEffect = dashEffect

        // Draw vertical asymptotes
        if (!showHorizontalAsymptote && !showZeroPlaces) {
            for (asymptote in verticalAsymptotes) {
                canvas.drawLine(asymptote.first, 0f, asymptote.first, height.toFloat(), paint)

                if (showVerticalAsymptotes) {
                    val circleSize = 8f
                    val xValue = asymptote.first
                    val yValue = height/2f

                    // Draw label
                    paint.style = Paint.Style.FILL
                    paint.color = axisColor
                    canvas.drawText(asymptote.second.toString(), xValue+5f, yValue-15f, paint)

                    // Draw a point
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(xValue, yValue, circleSize, paint)

                    paint.color = asymptoteColor
                }
            }
        }

        // Draw horizontal asymptote
        if (!showVerticalAsymptotes && !showZeroPlaces) {
            for (asymptote in horizontalAsymptote) {
                canvas.drawLine(0f, asymptote.second, width.toFloat(), asymptote.second, paint)

                if (showHorizontalAsymptote) {
                    val circleSize = 8f
                    val xValue = height/2f
                    val yValue = asymptote.second

                    // Draw label
                    paint.style = Paint.Style.FILL
                    paint.color = axisColor
                    canvas.drawText(asymptote.first.toString(), xValue+5f, yValue-15f, paint)

                    // Draw a point
                    paint.style = Paint.Style.FILL
                    paint.color = axisColor
                    canvas.drawCircle(xValue, yValue, circleSize, paint)
                }
            }
        }
        paint.pathEffect = null

        // Draw zero places
        if (showZeroPlaces) {
            val circleSize = 8f
            for (point in zeroPlaces) {
                val xValue = width/2f + (point.first * gridSpacing)

                // Draw label
                paint.style = Paint.Style.FILL
                paint.color = axisColor
                canvas.drawText(point.first.toString(), xValue+5f, point.second-15f, paint)

                // Draw a point
                paint.style = Paint.Style.FILL
                paint.color = axisColor
                canvas.drawCircle(xValue, point.second, circleSize, paint)
            }
        }

        // Draw a frame
        paint.style = Paint.Style.STROKE
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

    fun updateInformation(zeroPlacesTextView: TextView, asymptotesTextView: TextView, horizontalTextView: TextView) {
        // Zero places
        if (zeroPlaces.size == 1) {
            zeroPlacesTextView.text = context.getString(R.string.ZeroPlace)
        }
        else {
            zeroPlacesTextView.text = context.getString(R.string.ZeroPlaces)
        }

        var added = false
        for (place in zeroPlaces) {
            val text = " ${place.first},"
            zeroPlacesTextView.append(text)
            added = true
        }
        if (added) {
            zeroPlacesTextView.text = zeroPlacesTextView.text.dropLast(1)
        }
        else {
            val text = " ${context.getString(R.string.None)}"
            zeroPlacesTextView.append(text)
        }

        // Horizontal asymptote
        if (horizontalAsymptote.size == 1) {
            horizontalTextView.text = context.getString(R.string.HorizontalAsymptote)
        }
        else {
            horizontalTextView.text = context.getString(R.string.HorizontalAsymptotes)
        }

        added = false
        for (asymptote in horizontalAsymptote) {
            val text = " ${asymptote.first},"
            horizontalTextView.append(text)
            added = true
        }
        if (added) {
            horizontalTextView.text = horizontalTextView.text.dropLast(1)
        }
        else {
            val text = " ${context.getString(R.string.None)}"
            horizontalTextView.append(text)
        }

        // Vertical asymptotes
        if (verticalAsymptotes.size == 1) {
            asymptotesTextView.text = context.getString(R.string.VerticalAsymptote)
        }
        else {
            asymptotesTextView.text = context.getString(R.string.VerticalAsymptotes)
        }

        added = false
        for (asymptote in verticalAsymptotes) {
            val text = " ${asymptote.second},"
            asymptotesTextView.append(text)
            added = true
        }
        if (added) {
            asymptotesTextView.text = asymptotesTextView.text.dropLast(1)
        }
        else {
            val text = " ${context.getString(R.string.None)}"
            asymptotesTextView.append(text)
        }
    }

    fun drawAFunction(input: String) {
        zeroPlaces.clear()
        verticalAsymptotes.clear()
        horizontalAsymptote.clear()
        points.clear()

        paint.color = axisColor
        paint.strokeWidth = 200f

        val equation = calculator.transformEquation(input)
        val xAxis = round((height/2f).toDouble()*100)/100

        // Draw function
        if (equation.isNotEmpty()) {
            var lastPoint = 0.0
            var addedAsymptote = false

            var zeroPlaceLastPoint = Double.MAX_VALUE
            var zeroPlace: Float = Float.MAX_VALUE
            var addZeroPlace = false

            var firstRun = true
            var x = -(width/(gridSpacing))/2f
            var iterator = 0f
            while (iterator <= width) {
                // Find Y value for X
                val equationAfterSubstitution = substituteVariable(equation, x.toDouble())
                val bufferX = width/2f + (x * gridSpacing)
                val y = calculator.calculateEquation(equationAfterSubstitution, 0)

                // Check is it a zero place
                if (abs(zeroPlaceLastPoint) > abs(y.first) && round(y.first*100)/100 >= -0.02 && round(y.first*100)/100 <= 0.02) {
                    zeroPlace = x
                    addZeroPlace = true
                }
                else if (addZeroPlace) {
                    val yBuffer = round((xAxis - (y.first*(gridSpacing)))*100)/100
                    zeroPlaces.add(Pair(zeroPlace, yBuffer.toFloat()))
                    zeroPlace = Float.MAX_VALUE
                    addZeroPlace = false
                }

                zeroPlaceLastPoint = y.first
                y.first = round((xAxis - (y.first*(gridSpacing)))*100)/100

                // Check is it vertical asymptote
                if (abs(lastPoint-y.first) >= height && !firstRun) {
                    if ((lastPoint > xAxis && y.first < xAxis) || (lastPoint < xAxis && y.first > xAxis) ||
                        y.first.isInfinite()) {
                        if (!addedAsymptote) {
                            verticalAsymptotes.add(Pair(bufferX, x))
                            points.add(Pair(bufferX, y.first.toFloat()))
                            addedAsymptote = true
                        }
                    }
                }
                else if (lastPoint.isNaN() || lastPoint.isInfinite() && !firstRun) {
                    if (y.first.isInfinite()) {
                        verticalAsymptotes.add(Pair(bufferX, x))
                        points.add(Pair(bufferX, y.first.toFloat()))
                        addedAsymptote = true
                    }
                    else {
                        points.add(Pair(bufferX, y.first.toFloat()))
                        addedAsymptote = false
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
                firstRun = false
            }

            // Find horizontal asymptotes
            val lastThreePoints = mutableListOf<Float>()
            for (point in points) {
                if (lastThreePoints.size < 2) {
                    lastThreePoints.add(point.second)
                }
                else{
                    lastThreePoints.removeFirst()
                    lastThreePoints.add(point.second)
                }
                // Check for asymptote
                if (lastThreePoints.size == 2) {
                    if (lastThreePoints.sum()/2 == lastThreePoints.last()){
                        val bufferY = round(((xAxis.toFloat() - point.second)/gridSpacing)*100)/100
                        if(!bufferY.isInfinite()) {
                            if (horizontalAsymptote.isNotEmpty()) {
                                var found = false
                                for (asymptote in horizontalAsymptote) {
                                    if (asymptote.first == bufferY) {
                                        found = true
                                    }
                                }
                                if (!found) {
                                    horizontalAsymptote.add(Pair(bufferY, lastThreePoints.last()))
                                }
                            }
                            else {
                                horizontalAsymptote.add(Pair(bufferY, lastThreePoints.last()))
                            }
                        }
                    }
                }
            }
        }

        invalidate()
    }

    fun setZeroPlaces() {
        showHorizontalAsymptote = false
        showVerticalAsymptotes = false
        showZeroPlaces = !showZeroPlaces
        invalidate()
    }

    fun setHorizontalAsymptote() {
        showVerticalAsymptotes = false
        showZeroPlaces = false
        showHorizontalAsymptote = !showHorizontalAsymptote
        invalidate()
    }

    fun setVerticalAsymptotes() {
        showHorizontalAsymptote = false
        showZeroPlaces = false
        showVerticalAsymptotes = !showVerticalAsymptotes
        invalidate()
    }

}