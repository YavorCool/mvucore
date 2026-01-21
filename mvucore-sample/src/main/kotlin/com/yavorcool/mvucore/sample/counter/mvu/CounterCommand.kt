package com.yavorcool.mvucore.sample.counter.mvu

sealed interface CounterCommand {
    data class LoadIncrement(val currentValue: Int) : CounterCommand
}
