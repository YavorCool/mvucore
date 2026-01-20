package com.yavorcool.mvucore

fun interface Update<State : Any, in Event : Any, out Command : Any, out Effect : Any> {

    fun update(state: State, event: Event): Next<State, Command, Effect>

    companion object {
        /**
         * Subscribe an additional [observer] to events, primarily for analytics.
         */
        fun <State : Any, Event : Any, Command : Any, Effect : Any> Update<State, Event, Command, Effect>.withObserver(
            observer: EventObserver<State, Event>,
        ) = Update<State, Event, Command, Effect> { state, event ->
            this@withObserver.update(state, event).also {
                observer.onEvent(state, event)
            }
        }
    }
}
