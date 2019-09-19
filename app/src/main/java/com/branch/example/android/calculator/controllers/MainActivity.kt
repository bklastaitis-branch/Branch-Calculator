package com.branch.example.android.calculator.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.branch.example.android.calculator.R
import com.branch.example.android.calculator.data.CalculatorViewModel
import com.branch.example.android.calculator.data.Symbol
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rows.*

class MainActivity : AppCompatActivity() {

    private lateinit var invalidOpToast: Toast
    private lateinit var model: CalculatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) // https://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        invalidOpToast = Toast.makeText(this, "Invalid operation", Toast.LENGTH_LONG)

        model = ViewModelProviders.of(this)[CalculatorViewModel::class.java]
        model.expression.observe(this, Observer<MutableList<Symbol>>{ updateDisplay() })

        setOnClickListener()
    }

    private fun setOnClickListener() {
        val rows: List<ViewGroup> = listOf(row1, row2, row3, row4)
        for (row in rows) {
            for (child in row.children) {
                when (child.id) {
                    delete.id -> child.setOnClickListener { model.removeLast() }
                    equal.id -> child.setOnClickListener { model.compute(this) }
                    else -> child.setOnClickListener { onNumberOrOperatorClick(it) }
                }
            }
        }
    }

    private fun onNumberOrOperatorClick(it: View) {
        if (it !is TextView) return
        model.onNumberOrOperatorClick(Symbol(it.text.toString()), invalidOpToast)
    }

    private fun updateDisplay() {
        computations_display?.text = model.concatenate()
    }

}
