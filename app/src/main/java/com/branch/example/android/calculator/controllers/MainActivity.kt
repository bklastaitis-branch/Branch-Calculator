package com.branch.example.android.calculator.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.branch.example.android.calculator.R
import com.branch.example.android.calculator.utils.throwDebugException
import com.branch.example.android.calculator.utils.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.keyboard.*

class MainActivity : AppCompatActivity() {

    private lateinit var zeroDivisionMessage: Toast
    private var expression = mutableListOf<Symbol>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zeroDivisionMessage = Toast.makeText(this, "Cannot divide by zero!", Toast.LENGTH_LONG)

        one?.setOnClickListener(numberButtonListener)
        two?.setOnClickListener(numberButtonListener)
        three?.setOnClickListener(numberButtonListener)
        four?.setOnClickListener(numberButtonListener)
        five?.setOnClickListener(numberButtonListener)
        six?.setOnClickListener(numberButtonListener)
        seven?.setOnClickListener(numberButtonListener)
        eight?.setOnClickListener(numberButtonListener)
        nine?.setOnClickListener(numberButtonListener)
        zero?.setOnClickListener(numberButtonListener)

        plus?.setOnClickListener(operatorButtonListener)
        minus?.setOnClickListener(operatorButtonListener)
        divide?.setOnClickListener(operatorButtonListener)
        multiply?.setOnClickListener(operatorButtonListener)

        delete?.setOnClickListener {
            expression.removeAt(expression.size - 1)
            updateDisplay()
        }
        equal?.setOnClickListener { compute(); updateDisplay() }
    }

    private var numberButtonListener: View.OnClickListener = View.OnClickListener {
        if (it !is TextView) return@OnClickListener

        val symbol = Symbol(it.text.toString())

        if (expression.isEmpty()) {
            expression.add(symbol)
        } else {
            val last = expression.last()
            if (!last.isOp) {
                // last symbol was a digit
                if (last.stringSymbol == "0") {
                    // replace
                    expression[expression.size - 1] = symbol
                } else {
                    // combine
                    expression[expression.size - 1] =
                        Symbol(last.stringSymbol + symbol.stringSymbol)
                }
            } else {
                // last symbol was an operator
                if (last.stringSymbol == "/" && symbol.stringSymbol == "0") {
                    // zero division, show toast if not yet showing
                    if (zeroDivisionMessage.view == null || zeroDivisionMessage.view?.isShown == false) {
                        zeroDivisionMessage.show()
                    }
                } else {
                    expression.add(symbol)
                }
            }
        }

        updateDisplay()
    }

    private var operatorButtonListener: View.OnClickListener = View.OnClickListener {
        if (it !is TextView) return@OnClickListener

        val symbol = Symbol(it.text.toString())

        if (expression.isEmpty()) {
            if (symbol.stringSymbol != "-") return@OnClickListener
            expression.add(symbol)
        } else if (!expression.last().isOp) {
            // last symbol was a digit
            expression.add(symbol)
        }

        updateDisplay()
    }

    private fun updateDisplay() {
        var expressionString = ""
        for (s in expression) { expressionString += s.stringSymbol }
        computations_display?.text = expressionString
    }

    private fun compute() {
        if (expression.isEmpty()) return

        // check if valid expression
        val last = expression.last()
        if (last.isOp) {
            toast("Invalid expression")
            return
        }

        // fixes expression
        if (expression.first().stringSymbol == "-") {
            expression.add(0, Symbol("0"))
        }

        //compute result
        expression = computeSublist(expression, true)
        expression = computeSublist(expression, false)

        if (expression.size != 1) {
            throwDebugException("Computation failed, Expression size != 1")
        }
    }

    private fun computeSublist(sublist: MutableList<Symbol>, priorityOpsOnly: Boolean):MutableList<Symbol> {
        val iter: Iterator<Symbol> = sublist.toTypedArray().iterator()
        var prev: Symbol? = null; var next: Symbol? = null

        var index = -1
        while (iter.hasNext()) {
            val element = iter.next(); index++

            if (if (priorityOpsOnly) element.isPriorityOp else element.isOp) {
                next = iter.next(); index++ // moved up

                val prevNum = prev?.num
                val nextNum = next.num
                if (prevNum == null || nextNum == null) throwDebugException()

                prev = singleOp(element.stringSymbol, prevNum!!, nextNum!!)
                sublist[index - 2] = prev
                sublist.removeAt(index - 1); sublist.removeAt(index - 1); index -= 2
            } else if (!element.isOp) {
                prev = element
            }
        }
        return sublist
    }


    private fun singleOp(op: String, num1: Float, num2: Float): Symbol {
        val floatVal = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> num1 / num2
            else -> {
                throwDebugException()
                0f
            }
        }
        return Symbol(floatVal.toString())
    }

    private class Symbol(val stringSymbol: String) {
        val isPriorityOp: Boolean = stringSymbol == "*" || stringSymbol == "/"
        val isOp: Boolean = stringSymbol == "+" || stringSymbol == "-" || isPriorityOp
        val num: Float? = if (!isOp) stringSymbol.toFloat() else null
    }
}
