package com.yavorcool.mvucore.sample.counter.mvu

import com.yavorcool.mvucore.Next
import com.yavorcool.mvucore.Update

class CounterUpdate : Update<CounterState, CounterEvent, CounterCommand, CounterEffect> {

    override fun update(
        state: CounterState,
        event: CounterEvent,
    ): Next<CounterState, CounterCommand, CounterEffect> = when (event) {

        CounterUiEvent.Increment -> Next(
            state = state.copy(count = state.count + 1)
        )

        CounterUiEvent.Decrement -> Next(
            state = state.copy(count = state.count - 1)
        )

        CounterUiEvent.AsyncIncrement -> Next(
            state = state.copy(isLoading = true),
            command = CounterCommand.LoadIncrement(state.count)
        )

        is CounterEvent.AsyncIncrementResult -> Next(
            state = state.copy(count = event.value, isLoading = false),
            effect = CounterEffect.ShowMessage("Async increment completed!")
        )
    }
}
