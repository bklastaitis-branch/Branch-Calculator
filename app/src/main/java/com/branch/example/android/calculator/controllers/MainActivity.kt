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
import com.branch.example.android.calculator.data.MyViewModel
import com.branch.example.android.calculator.data.Symbol
import com.branch.example.android.calculator.utils.throwDebugException
import com.branch.example.android.calculator.utils.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rows.*

class MainActivity : AppCompatActivity() {

    private lateinit var invalidOpToast: Toast
    private lateinit var model: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        invalidOpToast = Toast.makeText(this, "Invalid operation", Toast.LENGTH_LONG)

        model = ViewModelProviders.of(this)[MyViewModel::class.java]
        model.expression.observe(this, Observer<MutableList<Symbol>>{ updateDisplay() })

        setOnClickListener()
    }

    private fun setOnClickListener() {
        val rows: List<ViewGroup> = listOf(row1, row2, row3, row4)
        for (row in rows) {
            for (child in row.children) {
                when (child.id) {
                    delete.id -> child.setOnClickListener { model.removeLast() }
                    equal.id -> child.setOnClickListener { compute() }
                    else -> child.setOnClickListener { numberOrOperatorClicked(it) }
                }
            }
        }
    }

    private fun numberOrOperatorClicked(it: View) {
        if (it !is TextView) return

        val symbol = Symbol(it.text.toString())

        if (!model.buttonClicked(symbol)) {
            // zero division or other invalid operation, show toast if not yet showing
            if (invalidOpToast.view == null || invalidOpToast.view?.isShown == false) {
                invalidOpToast.show()
            }
        }
    }

    private fun updateDisplay() {
        computations_display?.text = model.concatenate()
    }

    private fun compute() {
        if (model.isEmpty()) return

        // check if valid expression
        val last = model.last() ?: return
        if (last.isOp) {
            toast("Invalid expression")
            return
        }

        // fixes expression
        if (model.expression.value?.first()?.stringSymbol == "-") {
            model.add(0, Symbol("0"))
        }

        // compute result
        model.reduceList(true)
        model.reduceList(false)

        if (model.expression.value?.size != 1) {
            throwDebugException("Computation failed, Expression size != 1")
        }
    }

}
