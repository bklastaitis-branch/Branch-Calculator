package com.branch.example.android.calculator.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.branch.example.android.calculator.utils.notifyObserver

class MyViewModel : ViewModel() {
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

    fun size(): Int {
        return expression.value?.size ?: 0
    }

//    fun setLast(s: Symbol) {
//        expression.value?.set(expression.value!!.size - 1, s)
//        expression.notifyObserver()
//    }

    fun concatenate(): String {
        val iter = expression.value?.iterator() ?: return ""
        var expressionString = ""
        while (iter.hasNext()) { expressionString += iter.next().stringSymbol }
        return expressionString
    }

    fun reduceList(priorityOpsOnly: Boolean) {
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

    fun buttonClicked(symbol: Symbol): Boolean {
        if (!symbol.isOp) {
            if (isEmpty()) {
                add(symbol)
            } else {
                val last = last() ?: return false
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
                        // zero division, show toast if not yet showing
                        return false
                    } else {
                        add(symbol)
                    }
                }
            }
        } else {
            if (isEmpty()) {
                if (symbol.stringSymbol != "-") return false
                add(symbol)
            } else if (last()?.isOp == false) {
                // last symbol was a digit
                add(symbol)
            }
        }
        return true
    }
}