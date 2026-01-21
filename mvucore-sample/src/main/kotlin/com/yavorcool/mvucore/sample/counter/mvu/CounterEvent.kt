package com.yavorcool.mvucore.sample.counter.mvu

sealed interface CounterEvent {
    data class AsyncIncrementResult(val value: Int) : CounterEvent
}

sealed interface CounterUiEvent : CounterEvent {
    data object Increment : CounterUiEvent
    data object Decrement : CounterUiEvent
    data object AsyncIncrement : CounterUiEvent
}
