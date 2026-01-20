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
    implementation("com.github.yavorcool:mvucore:0.1.0")
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

Command handlers execute side effects and emit events back to the store.

```kotlin
class AsyncIncrementHandler : ICommandHandler<CounterCommand, CounterEvent> {
    override fun handle(commands: Flow<CounterCommand>): Flow<CounterEvent> {
        return commands
            .filter { it is CounterCommand.LoadIncrement }
            .map { command ->
                val loadCommand = command as CounterCommand.LoadIncrement
                delay(1000) // Simulate async operation
                CounterEvent.AsyncIncrementResult(loadCommand.currentValue + 1)
            }
    }
}
```

### 4. Create and use the Store

```kotlin
// Create the store
typealias CounterStore = IStore<CounterState, CounterEvent, CounterEffect>

fun createCounterStore(): CounterStore = Store(
    initialState = CounterState(),
    update = counterUpdate,
    commandHandlers = listOf(AsyncIncrementHandler())
)

// In your Activity/Fragment
class MainActivity : AppCompatActivity() {

    // Wrap store with ViewModel to survive configuration changes
    private val store: CounterStore by lazy {
        wrapStoreWithViewModel { createCounterStore() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dispatch events
        incrementButton.setOnClickListener { store.dispatch(CounterEvent.Increment) }

        // Collect state
        lifecycleScope.launch {
            store.state.collect { state ->
                counterText.text = "Count: ${state.count}"
            }
        }

        // Collect effects
        lifecycleScope.launch {
            store.effects.collect { effect ->
                when (effect) {
                    is CounterEffect.ShowMessage -> showToast(effect.message)
                }
            }
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
| `FilteringHandler` | Base class for command handlers that filter by command type |
| `LatestFilteringHandler` | Handler that cancels previous operations when new commands arrive |

### Lifecycle Extensions

| Function | Description |
|----------|-------------|
| `wrapStoreWithViewModel` | Wraps store in a ViewModel for lifecycle awareness |
| `wrapStoreWithViewModelProperty` | Property delegate for lazy store initialization |
| `IStore.collect` | Extension for collecting state and effects with lifecycle awareness |

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
