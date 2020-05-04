package com.example.calculator

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal

class SplashScreenActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

data class State (val expression: String,
                  val result: String,
                  val hasDot: Boolean,
                  val inOperation: InOperation,
                  val lastValue: BigDecimal,
                  val exception: Boolean)

enum class InOperation {
    None,
    Plus,
    Minus,
    Mul,
    Div
}

class MainActivity : AppCompatActivity() {

    private val emptyState = State("", "", hasDot = false, inOperation = InOperation.None, lastValue = BigDecimal.valueOf(0), exception = false)
    private var _state = emptyState //AtomicReference(emptyState)

    fun getState() = _state//.get()
    fun updateResult(state: State) {
        tvExpression.text =
            when {
                state.expression.isEmpty() && state.inOperation != InOperation.None -> state.lastValue.toString()
                state.expression.isEmpty() -> "0"
                state.exception -> "Error"
                else -> state.expression
            }
        val symbol =
            when (state.inOperation) {
                InOperation.None -> " "
                InOperation.Plus -> "+"
                InOperation.Minus -> "-"
                InOperation.Mul -> "×"
                InOperation.Div -> "/"
            }
        tvResult.text = "${state.lastValue} " + symbol
        _state = state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateResult(getState())

        listOf(tvZero, tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix, tvSeven, tvEight, tvNine,
            tvDot, tvBack, tvClear, tvOpen, tvClose, tvDivide, tvMultiply, tvPlus, tvMinus, tvEquals).
            forEach {
                if (it is TextView) {
                    it.textSize = 25.0f
                }

                it.setOnClickListener { view ->
                    val state = getState()
                    val newState: State = when (view) {
                        is TextView -> {
                            fun toBigDecimal(str: String) : BigDecimal =
                                if (str.isBlank()) BigDecimal.valueOf(0) else str.toBigDecimal() // TODO: Deal with single dot
                            fun onOperation(inOperation: InOperation, f: (lastValue: BigDecimal, expression: BigDecimal) -> BigDecimal) : State =
                                if (state.inOperation == inOperation) {
                                    state.copy(hasDot = false, expression = "", lastValue = f(state.lastValue, toBigDecimal(state.expression)))
                                }
                                else {
                                    state.copy(inOperation = inOperation, hasDot = false, expression = "", lastValue = toBigDecimal(state.expression))
                                }

                            fun equalsOperation(f: (lastValue: BigDecimal, expression: BigDecimal) -> BigDecimal) : State =
                                state.copy(hasDot = false, expression = "", inOperation = InOperation.None, lastValue = f(state.lastValue, toBigDecimal(state.expression)))

                            when (view) {
                                tvZero, tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix, tvSeven, tvEight, tvNine ->
                                    state.copy(expression = state.expression + view.text)
                                tvClear ->
                                    emptyState
                                tvDot ->
                                    if (state.hasDot) state
                                    else state.copy(expression = if (state.expression.isEmpty()) "0." else (state.expression + "."), hasDot = true)

                                tvPlus -> onOperation(InOperation.Plus) { l, e -> l + e }
                                tvMinus -> onOperation(InOperation.Minus) { l, e -> l - e }
                                tvMultiply -> onOperation(InOperation.Mul) { l, e -> l * e }
                                tvDivide -> onOperation(InOperation.Div) { l, e -> l / e }

                                tvEquals -> {
                                    try {
                                        when (state.inOperation) {
                                            InOperation.Plus -> equalsOperation { l, e -> l + e }
                                            InOperation.Minus -> equalsOperation { l, e -> l - e }
                                            InOperation.Mul -> equalsOperation { l, e -> l * e }
                                            InOperation.Div -> equalsOperation { l, e -> l / e }
                                            InOperation.None -> state
                                        }
                                    } catch (e: Exception) {
                                        state.copy(exception = true)
                                    }
                                }

                                else -> state
                            }
                        }
                        is ImageView ->
                            when (view) {
                                tvBack ->
                                    if (state.expression.isEmpty()) state
                                    else {
                                        val expression = state.expression.substring(0, state.expression.length - 1)
                                        state.copy(expression = expression, hasDot = expression.indexOf('.') >= 0)
                                    }

                                else -> state
                            }
                        else -> state
                    }
                    updateResult(newState)
                }
        }
    }
}
