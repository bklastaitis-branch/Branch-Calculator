package com.branch.example.android.calculator.data

class Symbol(val stringSymbol: String) {
    val isPriorityOp: Boolean = stringSymbol == "*" || stringSymbol == "/"
    val isOp: Boolean = stringSymbol == "+" || stringSymbol == "-" || isPriorityOp
    val num: Float? = if (!isOp) stringSymbol.toFloat() else null
}