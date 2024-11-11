package com.example.mathmaster.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import com.example.mathmaster.R
import kotlinx.coroutines.*

class Keyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int
    private val textField: TextView
    private val buttons: Array<Button>
    private val enterButton: Button
    private val deleteButton: Button
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
        LayoutInflater.from(context).inflate(R.layout.keyboard_layout, this, true)

        // Text field
        textField = findViewById<Button>(R.id.TextField)

        // Buttons
        enterButton = findViewById<Button>(R.id.Enter)
        deleteButton = findViewById<Button>(R.id.Delete)
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
        buttons = arrayOf(
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

    fun numberButtonClick() {
        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                buttons[i].setBackgroundResource(clickedButtonStyle)

                if (textField.text.length > 4) {
                    textField.text = ""
                }

                if (textField.text.length < 4)
                    textField.append(i.toString())

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    buttons[i].setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

    fun deleteButtonClick() {
        deleteButton.setOnClickListener {
            if (textField.text.isNotEmpty()) {
                deleteButton.setBackgroundResource(clickedButtonStyle)

                if (textField.text.length > 4) {
                } else if (textField.text.length == 1) {
                    textField.text = context.getString(R.string.EnterNumber)
                } else {
                    val currentText = textField.text.toString()
                    textField.text = currentText.substring(0, currentText.length - 1)
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    deleteButton.setBackgroundResource(unClickedButtonStyle)
                }
            }
        }
    }

    fun clickEnterButton() {
        enterButton.setBackgroundResource(clickedButtonStyle)
    }

    fun unClickEnterButton() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            enterButton.setBackgroundResource(unClickedButtonStyle)
        }
    }

    fun getEnterButton(): Button {
        return enterButton
    }

    fun getTextField (): Int {
        if (textField.text.length > 4)
            return 0

        return textField.text.toString().toInt()
    }

    fun resetTextField() {
        textField.text = context.getString(R.string.EnterNumber)
    }
}