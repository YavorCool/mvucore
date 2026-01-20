package com.yavorcool.mvucore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IStore<out State : Any, in Event : Any, out Effect : Any> : IEventDispatcher<Event> {

    val state: StateFlow<State>

    val effects: Flow<Effect>

    fun launch(coroutineScope: CoroutineScope)
}
