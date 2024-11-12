package com.example.mathmaster.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.mathmaster.R

class BackButtonWithBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val clickedButtonStyle: Int
    private val unClickedButtonStyle: Int
    private val backButton: Button

    init {
        // Inflate the custom XML layout
        LayoutInflater.from(context).inflate(R.layout.backbuttonbar_layout, this, true)

        // Button
        clickedButtonStyle = R.drawable.menubutton_background_clicked
        unClickedButtonStyle = R.drawable.menubutton_background
        backButton = findViewById<Button>(R.id.BackButton)
    }

    fun changeBackToExit() {
        backButton.text = context.getString(R.string.Exit)
    }

    fun returnBackButton(): Button {
        return backButton
    }
}