package com.example.mathmaster.customviews

import android.content.Context
import android.provider.Settings.Global
import android.util.AttributeSet
import android.view.LayoutInflater
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
    private val infoButton: Button

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.matrixresultmenu_layout, this, true)

        // Get buttons
        multiplyButton = findViewById<Button>(R.id.MultiplyMatrix)
        addButton = findViewById<Button>(R.id.AddMatrix)
        subtractButton = findViewById<Button>(R.id.SubtractMatrix)
        infoButton = findViewById<Button>(R.id.InfoMatrix)

        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
    }

    fun getMultipluButton(): Button {
        return multiplyButton
    }

    fun getAddButton(): Button {
        return addButton
    }

    fun getSubtractButton(): Button {
        return subtractButton
    }

    fun getInfoButton(): Button {
        return infoButton
    }

    fun clickMultiplyButton() {
        multiplyButton.setBackgroundResource(clickedButtonStyle)
    }

    fun clickAddButton() {
        addButton.setBackgroundResource(clickedButtonStyle)
    }

    fun clickSubtractButton() {
        subtractButton.setBackgroundResource(clickedButtonStyle)
    }

    fun clickInfoButton() {
        infoButton.setBackgroundResource(clickedButtonStyle)
    }

    fun unClickMultiplyButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            multiplyButton.setBackgroundResource(unClickedButtonStyle)
        }
    }

    fun unClickAddButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            addButton.setBackgroundResource(unClickedButtonStyle)
        }
    }

    fun unClickSubtractButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            subtractButton.setBackgroundResource(unClickedButtonStyle)
        }
    }

    fun unClickInfoButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            infoButton.setBackgroundResource(unClickedButtonStyle)
        }
    }
}