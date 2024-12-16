package com.example.mathmaster

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class PracticeActivity : ComponentActivity() {

    private fun clickFunction (button: Button, drawable: Int, view: ComponentActivity) {
        button.setOnClickListener {
            button.setBackgroundResource(drawable)

            val intent = Intent(this, view::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )

            startActivity(intent, options.toBundle())
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.practice_activity)

        // Menu buttons
        val multiplyButton: Button = findViewById(R.id.Multiply)
        val divideButton: Button = findViewById(R.id.Divide)
        val addSubtractButton: Button = findViewById(R.id.AddSubtract)
        val mixedButton: Button = findViewById(R.id.Mixed)

        // Style of clicked button
        val clickedButtonStyle = R.drawable.menubutton_background_clicked

        // On click functions
        clickFunction(multiplyButton, clickedButtonStyle, MultiplyActivity())
        clickFunction(divideButton, clickedButtonStyle, DivideActivity())
        clickFunction(addSubtractButton, clickedButtonStyle, AddSubtractActivity())
        clickFunction(mixedButton, clickedButtonStyle, MixedActivity())

        // Handle the back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@PracticeActivity, MainMenuActivity()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    this@PracticeActivity,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        })
    }
}