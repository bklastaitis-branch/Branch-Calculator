package com.branch.example.android.calculator.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import android.widget.Toast
import com.branch.example.android.calculator.R
import com.branch.example.android.calculator.utils.isFirstLaunch
import com.branch.example.android.calculator.utils.putPref
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.keyboard.*

class MainActivity : AppCompatActivity() {

    private val zeroDivisionMessage: Toast = Toast.makeText(
        this, "Cannot divide by zero!", Toast.LENGTH_LONG)
    private var expression = mutableListOf<Symbol>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isFirstLaunch()) {
            val onboardingPanel = onboarding_viewstub?.inflate()
            putPref(R.string.pref_is_first_launch, false)
            onboardingPanel?.setOnClickListener { onboardingPanel.visibility = GONE }
        }

        one?.setOnClickListener { numberButtonListener }
        two?.setOnClickListener { numberButtonListener }
        three?.setOnClickListener { numberButtonListener }
        four?.setOnClickListener { numberButtonListener }
        five?.setOnClickListener { numberButtonListener }
        six?.setOnClickListener { numberButtonListener }
        seven?.setOnClickListener { numberButtonListener }
        eight?.setOnClickListener { numberButtonListener }
        nine?.setOnClickListener { numberButtonListener }
        zero?.setOnClickListener { numberButtonListener }

        plus?.setOnClickListener { operatorButtonListener }
        minus?.setOnClickListener { operatorButtonListener }
        divide?.setOnClickListener { operatorButtonListener }
        multiply?.setOnClickListener { operatorButtonListener }
        left_parentheses?.setOnClickListener { operatorButtonListener }
        right_parentheses?.setOnClickListener { operatorButtonListener }

        delete?.setOnClickListener { expression.removeAt(expression.size - 1) }
        equal?.setOnClickListener {  }
    }

    private var numberButtonListener: View.OnClickListener = View.OnClickListener {
        if (it !is TextView) return@OnClickListener

        val symbol = Symbol(it.text.toString())

        if (expression.isEmpty()) {
            expression.add(symbol)
        } else {
            val last = expression.last()
            if (!last.isOp) {
                if (last.stringSymbol == "0") {
                    expression[expression.size - 1] = symbol
                } else {
                    expression[expression.size - 1] =
                        Symbol(last.stringSymbol + symbol.stringSymbol)
                }
            } else {
                if (last.stringSymbol == "/" && symbol.stringSymbol == "0") {
                    // zero division, show toast if not yet showing
                    if (zeroDivisionMessage.view?.isShown == false) {
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
            if (symbol.stringSymbol != "(" || symbol.stringSymbol != "-") return@OnClickListener
            expression.add(symbol)
        } else {
            val last = expression.last()
            if (!last.isOp) {
                // last symbol was a digit
                if (symbol.stringSymbol == "(") return@OnClickListener
                expression.add(symbol)
            } else {
                // last symbol was an operator
                if (symbol.stringSymbol == "(") {
                    expression.add(symbol)
                }
            }
        }

        updateDisplay()
    }

    private fun updateDisplay() {
        var expressionString = ""
        for (s in expression) { expressionString += s.stringSymbol }
        computations_display?.text = expressionString
    }

    private fun compute() {

        var result: String
        while (containsPriorityOps()) {
            // replace priority sublist with computed val
            val sublist = getFirstPrioritySublist()
        }
    }

    private fun validExpression(sublist: List<Symbol>) {

    }

    private fun containsPriorityOps(): Boolean {
        for (element in expression) {
            if (element.isPriorityOp) return true
        }
        return false
    }

    private fun getFirstPrioritySublist(): List<Symbol> {
        var startIndex: Int = -1; var endIndex: Int = -1
        for ((index, element) in expression.iterator().withIndex()) {
            if (element.isPriorityOp) {
                if (element.isParentheses) {
                    startIndex = index + 1
                } else {

                }

                if (startIndex < 0) {
                    startIndex = index
                } else {
                    endIndex = index; break
                }
            }
        }
        if (startIndex < 0 || endIndex < 0) return emptyList()
        expression.subList(startIndex, endIndex)
    }

    private class Symbol(val stringSymbol: String) {
        var isParentheses: Boolean = stringSymbol == "(" || stringSymbol == ")"
        var isPriorityOp: Boolean = stringSymbol == "*" || stringSymbol == "/" || isParentheses
        var isOp: Boolean = stringSymbol == "+" || stringSymbol == "-" || isPriorityOp
    }
}
