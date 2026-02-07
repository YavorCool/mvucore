package com.yavorcool.mvucore

fun interface EventObserver<State : Any, in Event : Any> {
    fun onEvent(event: Event, updatedState: State, oldState: State)
}
