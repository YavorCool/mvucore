package com.yavorcool.mvucore

fun interface EventObserver<State : Any, in Event : Any> {
    fun onEvent(state: State, event: Event)
}
