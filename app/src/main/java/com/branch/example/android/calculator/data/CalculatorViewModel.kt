package com.branch.example.android.calculator.data

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.branch.example.android.calculator.controllers.MainActivity
import com.branch.example.android.calculator.utils.notifyObserver
import com.branch.example.android.calculator.utils.throwDebugException
import com.branch.example.android.calculator.utils.toast
import io.branch.referral.util.BRANCH_STANDARD_EVENT
import io.branch.referral.util.BranchEvent

class CalculatorViewModel : ViewModel() {
    val expression: MutableLiveData<MutableList<Symbol>> by lazy {
        MutableLiveData(mutableListOf<Symbol>())
    }

    fun removeLast() {
        expression.value?.removeAt(expression.value!!.size - 1)
        expression.notifyObserver()
    }

    fun isEmpty(): Boolean {
        if (expression.value == null) return true
        return expression.value!!.isEmpty()
    }

    fun add(s: Symbol) {
        expression.value?.add(s)
        expression.notifyObserver()
    }

    fun clear() {
        expression.value?.clear()
    }

    fun add(index: Int, s: Symbol) {
        expression.value?.add(index, s)
        expression.notifyObserver()
    }

    fun set(index: Int, s: Symbol) {
        expression.value?.set(index, s)
        expression.notifyObserver()
    }

    fun last(): Symbol? {
        return expression.value?.last()
    }

    private fun size(): Int {
        return expression.value?.size ?: 0
    }

    fun concatenate(): String {
        val iter = expression.value?.iterator() ?: return ""
        var expressionString = ""
        while (iter.hasNext()) { expressionString += iter.next().stringSymbol }
        return expressionString
    }

    private fun reduceList(priorityOpsOnly: Boolean, c: MainActivity) {
        val iter: Iterator<Symbol> = expression.value?.toTypedArray()?.iterator() ?: return
        var prev: Symbol? = null; var next: Symbol? = null

        var index = -1
        while (iter.hasNext()) {
            val element = iter.next(); index++

            if (if (priorityOpsOnly) element.isPriorityOp else element.isOp) {
                next = iter.next(); index++ // moved up

                val prevNum = prev?.num
                val nextNum = next.num
                if (prevNum == null || nextNum == null) return

                prev = singleOp(element.stringSymbol, prevNum, nextNum)
                expression.value!![index - 2] = prev
                expression.value!!.removeAt(index - 1); expression.value!!.removeAt(index - 1); index -= 2
            } else if (!element.isOp) {
                prev = element
            }
        }
        if (!priorityOpsOnly &&
            expression.value?.first()?.num != null &&
            expression.value?.first()?.num!! > 100f) {
            // https://docs.branch.io/apps/v2event/#android_2
            BranchEvent(BRANCH_STANDARD_EVENT.UNLOCK_ACHIEVEMENT).setDescription("Computation result exceeded 100!").logEvent(c)
        }
        expression.notifyObserver()
    }

    private fun singleOp(op: String, num1: Float, num2: Float): Symbol {
        val floatVal = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> num1 / num2
            else -> {
                0f
            }
        }
        return Symbol(floatVal.toString())
    }

    fun compute(c: MainActivity) {
        if (isEmpty()) return

        // check if valid expression
        val last = last() ?: return
        if (last.isOp) {
            c.toast("Invalid expression")
            return
        }

        // fixes expression
        if (expression.value?.first()?.stringSymbol == "-") {
            add(0, Symbol("0"))
        }

        // compute result
        reduceList(true, c)
        reduceList(false, c)

        if (expression.value?.size != 1) {
            c.throwDebugException("Computation failed, Expression size != 1")
        }
    }

    fun onNumberOrOperatorClick(symbol: Symbol, toast: Toast) {
        if (!symbol.isOp) {
            if (isEmpty()) {
                add(symbol)
            } else {
                val last = last() ?: return
                if (!last.isOp) {
                    // last symbol was a digit
                    if (last.stringSymbol == "0") {
                        // replace
                        set(size() - 1, symbol)
                    } else {
                        // combine
                        set(size() - 1, Symbol(last.stringSymbol + symbol.stringSymbol))
                    }
                } else {
                    // last symbol was an operator
                    if (last.stringSymbol == "/" && symbol.stringSymbol == "0") {
                        // zero division or other invalid operation, show toast if not yet showing
                        if (toast.view == null || toast.view?.isShown == false) {
                            toast.show()
                        }
                    } else {
                        add(symbol)
                    }
                }
            }
        } else {
            if (isEmpty()) {
                if (symbol.stringSymbol != "-") return
                add(symbol)
            } else if (last()?.isOp == false) {
                // last symbol was a digit
                add(symbol)
            }
        }
    }
}