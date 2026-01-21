package com.yavorcool.mvucore.sample.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.impl.Store
import com.yavorcool.mvucore.sample.counter.mvu.CounterCommand
import com.yavorcool.mvucore.sample.counter.mvu.CounterEffect
import com.yavorcool.mvucore.sample.counter.mvu.CounterEvent
import com.yavorcool.mvucore.sample.counter.mvu.CounterState
import com.yavorcool.mvucore.sample.counter.mvu.CounterUpdate
import com.yavorcool.mvucore.sample.counter.mvu.commandHandler.LoadIncrementCommandHandler

class CounterViewModel : ViewModel() {

    val store: IStore<CounterState, CounterEvent, CounterEffect> = Store(
        initialState = CounterState(),
        update = CounterUpdate(),
        commandHandlers = listOf(
            LoadIncrementCommandHandler(),
        ),
    )

    init {
        store.launch(viewModelScope)
    }
}
