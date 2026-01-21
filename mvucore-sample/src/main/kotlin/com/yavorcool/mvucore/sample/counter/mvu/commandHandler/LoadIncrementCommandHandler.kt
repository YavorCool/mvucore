package com.yavorcool.mvucore.sample.counter.mvu.commandHandler

import com.yavorcool.mvucore.FilteringHandler
import com.yavorcool.mvucore.sample.counter.mvu.CounterCommand
import com.yavorcool.mvucore.sample.counter.mvu.CounterEvent
import kotlinx.coroutines.delay

class LoadIncrementCommandHandler : FilteringHandler<CounterCommand.LoadIncrement, CounterCommand, CounterEvent>(
    CounterCommand.LoadIncrement::class
) {
    override suspend fun handleCommand(command: CounterCommand.LoadIncrement): CounterEvent {
        // Simulate async operation
        delay(1000)
        return CounterEvent.AsyncIncrementResult(command.currentValue + 1)
    }
}
