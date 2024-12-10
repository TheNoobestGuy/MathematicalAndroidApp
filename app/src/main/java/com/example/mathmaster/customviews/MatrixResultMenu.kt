package com.example.mathmaster.customviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.mathmaster.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatrixResultMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val multiplyButton: Button
    private val addButton: Button
    private val subtractButton: Button
    private val powerButton: Button
    private val inverseButton: Button
    private val infoButton: Button

    private val buttonsArray: Array<Button>
    private val powerTo2Button: Button
    private val powerTo3Button: Button
    private val powerTo4Button: Button
    private val backButton: Button

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.matrixresultmenu_layout, this, true)

        // Get buttons
        multiplyButton = findViewById<Button>(R.id.MultiplyMatrix)
        addButton = findViewById<Button>(R.id.AddMatrix)
        subtractButton = findViewById<Button>(R.id.SubtractMatrix)
        powerButton = findViewById<Button>(R.id.PowerMatrix)
        inverseButton = findViewById<Button>(R.id.InverseMatrix)
        infoButton = findViewById<Button>(R.id.InfoMatrix)

        // Power to menu
        powerTo2Button = findViewById<Button>(R.id.PowerTo2)
        powerTo3Button = findViewById<Button>(R.id.PowerTo3)
        powerTo4Button = findViewById<Button>(R.id.PowerTo4)
        backButton = findViewById<Button>(R.id.Back)

        buttonsArray = arrayOf(
            powerTo2Button,
            powerTo3Button,
            powerTo4Button
        )

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
    }

    fun matrixIsQuadratic() {
        powerButton.visibility = View.VISIBLE
        inverseButton.visibility = View.VISIBLE
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

    fun clickPowerButton() {
        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(clickedButtonStyle)

            Handler(Looper.getMainLooper()).postDelayed({
                // Hide menu
                multiplyButton.visibility = View.GONE
                addButton.visibility = View.GONE
                subtractButton.visibility = View.GONE
                powerButton.visibility = View.GONE
                inverseButton.visibility = View.GONE
                infoButton.visibility = View.GONE

                // Show menu
                powerTo2Button.visibility = View.VISIBLE
                powerTo3Button.visibility = View.VISIBLE
                powerTo4Button.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE

                powerButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun getPowersToButtons(): Array<Button> {
        return buttonsArray
    }

    fun clickBackButton() {
        backButton.setOnClickListener {
            backButton.setBackgroundResource(clickedButtonStyle)

            Handler(Looper.getMainLooper()).postDelayed({
                // Hide menu
                powerTo2Button.visibility = View.GONE
                powerTo3Button.visibility = View.GONE
                powerTo4Button.visibility = View.GONE
                backButton.visibility = View.GONE

                // Show menu
                multiplyButton.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
                subtractButton.visibility = View.VISIBLE
                powerButton.visibility = View.VISIBLE
                inverseButton.visibility = View.VISIBLE
                infoButton.visibility = View.VISIBLE

                backButton.setBackgroundResource(unClickedButtonStyle)
            }, 100)
        }
    }

    fun getInverseButton(): Button {
        return inverseButton
    }

    fun getInfoButton(): Button {
        return infoButton
    }
}