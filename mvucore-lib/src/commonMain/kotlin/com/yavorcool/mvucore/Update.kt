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
                observer.onEvent(event = event, updatedState = it.state, oldState = state)
            }
        }

        inline fun <State : Any, Command : Any, Effect : Any, reified T : State> checkState(
            state: State,
            log: (String?) -> Unit,
            skipLog: Boolean = false,
            update: (T) -> Next<State, Command, Effect>,
        ): Next<State, Command, Effect> {
            return if (state is T) {
                update(state)
            } else {
                if (!skipLog) {
                    log(T::class.qualifiedName)
                }
                Next(state = state)
            }
        }
    }
}
