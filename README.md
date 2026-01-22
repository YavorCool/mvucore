# MVU Core

A lightweight Model-View-Update (MVU) architecture library for Android.

[![](https://jitpack.io/v/yavorcool/mvucore.svg)](https://jitpack.io/#yavorcool/mvucore)

## Overview

MVU Core provides a unidirectional data flow architecture inspired by The Elm Architecture. It helps you build predictable, testable, and maintainable Android applications.

### How it works

```
           UiEvent                         State Λ   Λ Effect
 View        │                                   │   │
-------------│-----------------------------------│---│------
             │    ┌──────────────<───────────────┤   │
             │    │                              │   │
             │    │        ╭────────────╮ State  │   │
             │    │  State │            ├────────┘   │
             V    └────────>            │ Effect     │
             │       Event │   Update   ├────────────┘
 Store       ├─────────────>            │ Commands
             │             │            ├───────────────┐
             Λ             ╰────────────╯               │
             │                                          │
             │         ╭─────────────────────╮          │
             │        ╭┴────────────────────╮│          │
             │ Events │                     ││ Commands │
             └────────┤    CommandHandler   <───────────┘
                      │                     ├╯
                      ╰────────Λ───┬────────╯
------------------------------│---│------------------------
 Model                        │   │
              Data (or events)┘   V Side effects
```

## Installation

Add JitPack repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        // ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.yavorcool:mvucore:0.2.0")
}
```

## Quick Start

### 1. Define your State, Events, Commands, and Effects

```kotlin
// State - immutable data class representing UI state
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false
)

// Events - user actions and internal events
sealed interface CounterEvent {
    data object Increment : CounterEvent
    data object Decrement : CounterEvent
    data object AsyncIncrement : CounterEvent
    data class AsyncIncrementResult(val value: Int) : CounterEvent
}

// Commands - side effects to execute (network calls, database operations)
sealed interface CounterCommand {
    data class LoadIncrement(val currentValue: Int) : CounterCommand
}

// Effects - one-time UI events (navigation, showing toasts)
sealed interface CounterEffect {
    data class ShowMessage(val message: String) : CounterEffect
}
```

### 2. Create the Update function

The Update function is a pure function that takes the current state and an event, and returns the next state along with any commands or effects.

```kotlin
val counterUpdate = Update<CounterState, CounterEvent, CounterCommand, CounterEffect> { state, event ->
    when (event) {
        CounterEvent.Increment -> Next(
            state = state.copy(count = state.count + 1)
        )
        CounterEvent.Decrement -> Next(
            state = state.copy(count = state.count - 1)
        )
        CounterEvent.AsyncIncrement -> Next(
            state = state.copy(isLoading = true),
            command = CounterCommand.LoadIncrement(state.count)
        )
        is CounterEvent.AsyncIncrementResult -> Next(
            state = state.copy(count = event.value, isLoading = false),
            effect = CounterEffect.ShowMessage("Async increment completed!")
        )
    }
}
```

### 3. Create Command Handlers

Command handlers execute side effects and emit events back to the store. Use `FilteringHandler` to handle specific command types:

```kotlin
class LoadIncrementCommandHandler : FilteringHandler<CounterCommand.LoadIncrement, CounterCommand, CounterEvent>(
    CounterCommand.LoadIncrement::class
) {
    override suspend fun handleCommand(command: CounterCommand.LoadIncrement): CounterEvent {
        delay(1000) // Simulate async operation
        return CounterEvent.AsyncIncrementResult(command.currentValue + 1)
    }
}
```

### 4. Create and use the Store

```kotlin
typealias CounterStore = IStore<CounterState, CounterEvent, CounterEffect>

// Create a ViewModel to survive configuration changes
class CounterViewModel : ViewModel() {
    val store: CounterStore = Store(
        initialState = CounterState(),
        update = counterUpdate,
        commandHandlers = listOf(LoadIncrementCommandHandler())
    )

    init {
        store.launch(viewModelScope)
    }
}

// In your Compose Activity
class CounterComposeActivity : ComponentActivity() {

    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Collect state as Compose state
            val state by viewModel.store.state.collectAsStateWithLifecycle()

            // Handle one-time effects with lifecycle awareness
            viewModel.store.effects.observeWithLifecycle { effect ->
                when (effect) {
                    is CounterEffect.ShowMessage -> showToast(effect.message)
                }
            }

            CounterScreen(
                state = state,
                onIncrement = { viewModel.store.dispatch(CounterEvent.Increment) },
                onDecrement = { viewModel.store.dispatch(CounterEvent.Decrement) },
                onAsyncIncrement = { viewModel.store.dispatch(CounterEvent.AsyncIncrement) },
            )
        }
    }
}

@Composable
fun CounterScreen(
    state: CounterState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAsyncIncrement: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Count: ${state.count}")

        Row {
            Button(onClick = onDecrement) { Text("-1") }
            Button(onClick = onIncrement) { Text("+1") }
        }

        Button(
            onClick = onAsyncIncrement,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "Loading..." else "Async +1")
        }
    }
}

// Helper extension for lifecycle-aware effect collection
@Composable
fun <T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) {
    LaunchedEffect(lifecycleOwner, minActiveState, action) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            collect(action)
        }
    }
}
```

## API Reference

### Core Interfaces

| Interface | Description |
|-----------|-------------|
| `IStore<State, Event, Effect>` | Main store interface with state flow and effect flow |
| `IEventDispatcher<Event>` | Interface for dispatching events |
| `ICommandHandler<Command, Event>` | Interface for handling commands and producing events |
| `Update<State, Event, Command, Effect>` | Pure function interface for state transitions |

### Classes

| Class | Description |
|-------|-------------|
| `Store` | Default implementation of `IStore` |
| `Next` | Result of an update containing new state, commands, and effects |
| `FilteringHandler` | Base class for command handlers that filter by command type. Set `cancelPreviousOnNewCommand = true` to cancel previous operations when new commands arrive |
| `FilteringHandlerToFlow` | Like `FilteringHandler`, but returns a `Flow<Event>` instead of a single event |

### Factory Functions

| Function | Description |
|----------|-------------|
| `filteringHandler` | Creates a `FilteringHandler` inline with a lambda |
| `filteringHandlerToFlow` | Creates a `FilteringHandlerToFlow` inline with a lambda |

## License

```
Copyright 2024 Nikita

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
