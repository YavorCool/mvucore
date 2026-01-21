package com.yavorcool.mvucore.sample.counter.mvu

sealed interface CounterEffect {
    data class ShowMessage(val message: String) : CounterEffect
}
