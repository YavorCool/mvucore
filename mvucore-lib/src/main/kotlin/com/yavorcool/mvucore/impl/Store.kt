package com.yavorcool.mvucore.impl

import com.yavorcool.mvucore.ICommandHandler
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.Update
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


/**
 *
 * How it works:
 * ```
 *
 *            UiEvent                         State Λ   Λ Effect
 *  View        │                                   │   │
 * -------------│-----------------------------------│---│------
 *              │    ┌──────────────<───────────────┤   │
 *              │    │                              │   │
 *              │    │        ╭────────────╮ State  │   │
 *              │    │  State │            ├────────┘   │
 *              V    └────────>            │ Effect     │
 *              │       Event │   Update   ├────────────┘
 *  Store       ├─────────────>            │ Commands
 *              │             │            ├───────────────┐
 *              Λ             ╰────────────╯               │
 *              │                                          │
 *              │         ╭─────────────────────╮          │
 *              │        ╭┴────────────────────╮│          │
 *              │ Events │                     ││ Commands │
 *              └────────┤    CommandHandler   <───────────┘
 *                       │                     ├╯
 *                       ╰────────Λ───┬────────╯
 * -------------------------------│---│------------------------
 *  Model                         │   │
 *               Data (or events) ┘   V Side effects
 * ```
 *
 * @see Update
 * @see CommandsFlowHandler
 */
class Store<State : Any, Event : Any, UiEvent : Event, Command : Any, Effect : Any>(
    initialState: State,
    private val update: Update<State, Event, Command, Effect>,
    private val commandHandlers: List<ICommandHandler<Command, Event>>,
) : IStore<State, UiEvent, Effect> {

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state

    private val _effects: MutableSharedFlow<Effect> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)
    private val effectsFlowSubscribeWaiter = Job()
    override val effects: SharedFlow<Effect> = _effects.onSubscription { effectsFlowSubscribeWaiter.complete() }

    private val commands: MutableSharedFlow<Command> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)
    private val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    private val isLaunched = AtomicBoolean(false)

    override fun dispatch(event: UiEvent) {
        if (!events.tryEmit(event)) error("Couldn't process $event, flow buffer overflow")
    }

    override fun launch(coroutineScope: CoroutineScope) {
        if (isLaunched.getAndSet(true)) error("The store has been already launched")

        val commandsFlowSubscribeWaiter = CountDownLatch(commandHandlers.size)
        val commandsFlow = commands.onSubscription {
            commandsFlowSubscribeWaiter.countDown()
        }
        for (commandHandler in commandHandlers) {

            // Collect commands on IO threads, as handlers will make network calls etc.
            // UNDISPATCHED is needed so we subscribe to commands before they start emitting.
            (coroutineScope + Dispatchers.IO).launch(start = CoroutineStart.UNDISPATCHED) {
                @Suppress("detekt:TooGenericExceptionCaught")
                try {
                    commandHandler.handle(commandsFlow).collect(events::emit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    throw CommandHandlerException(commandHandler::class.java, e)
                }
            }
        }

        // Collect events on main thread, because they either originate from UI or modify UI
        (coroutineScope + Dispatchers.Main.immediate).launch(start = CoroutineStart.UNDISPATCHED) {
            events.collect { event ->
                val next = update.update(state.value, event)
                _state.value = next.state

                if (next.commands.isNotEmpty()) {
                    commandsFlowSubscribeWaiter.await()
                    for (command in next.commands) {
                        commands.emit(command)
                    }
                }

                for (effect in next.effects) {
                    effectsFlowSubscribeWaiter.join()
                    _effects.emit(effect)
                }
            }
        }
    }

    /**
     * Analog of [java.util.concurrent.CountDownLatch] in coroutines world.
     */
    class CountDownLatch(count: Int) {
        private val waitJob = Job()
        private val counter = AtomicInteger(count)

        fun countDown() {
            if (counter.decrementAndGet() == 0) {
                waitJob.complete()
            }
        }

        suspend fun await() = waitJob.join()
    }
}
