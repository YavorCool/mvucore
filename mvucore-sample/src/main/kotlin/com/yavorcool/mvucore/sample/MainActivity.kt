package com.yavorcool.mvucore.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yavorcool.mvucore.ICommandHandler
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.Next
import com.yavorcool.mvucore.Update
import com.yavorcool.mvucore.impl.Store
import com.yavorcool.mvucore.lifecycle.wrapStoreWithViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Sample activity demonstrating the MVU (Model-View-Update) pattern.
 *
 * This example shows a simple counter with async increment functionality.
 */
class MainActivity : AppCompatActivity() {

    // Store wrapped with ViewModel to survive configuration changes
    private val store: CounterStore by lazy {
        wrapStoreWithViewModel { createCounterStore() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val counterText = findViewById<TextView>(R.id.counterText)
        val incrementButton = findViewById<Button>(R.id.incrementButton)
        val decrementButton = findViewById<Button>(R.id.decrementButton)
        val asyncIncrementButton = findViewById<Button>(R.id.asyncIncrementButton)

        // Dispatch UI events to the store
        incrementButton.setOnClickListener { store.dispatch(CounterEvent.Increment) }
        decrementButton.setOnClickListener { store.dispatch(CounterEvent.Decrement) }
        asyncIncrementButton.setOnClickListener { store.dispatch(CounterEvent.AsyncIncrement) }

        // Collect state updates
        lifecycleScope.launch {
            store.state.collect { state ->
                counterText.text = "Count: ${state.count}"
                asyncIncrementButton.isEnabled = !state.isLoading
                asyncIncrementButton.text = if (state.isLoading) "Loading..." else "Async +1"
            }
        }

        // Collect effects (one-time events)
        lifecycleScope.launch {
            store.effects.collect { effect ->
                when (effect) {
                    is CounterEffect.ShowMessage -> {
                        // Show toast or snackbar
                    }
                }
            }
        }
    }
}

// State
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false
)

// Events (UI actions)
sealed interface CounterEvent {
    data object Increment : CounterEvent
    data object Decrement : CounterEvent
    data object AsyncIncrement : CounterEvent
    data class AsyncIncrementResult(val value: Int) : CounterEvent
}

// Commands (side effects to execute)
sealed interface CounterCommand {
    data class LoadIncrement(val currentValue: Int) : CounterCommand
}

// Effects (one-time UI events)
sealed interface CounterEffect {
    data class ShowMessage(val message: String) : CounterEffect
}

// Type alias for the store
typealias CounterStore = IStore<CounterState, CounterEvent, CounterEffect>

// Update function - pure function that produces new state
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

// Command handler - handles side effects
class AsyncIncrementHandler : ICommandHandler<CounterCommand, CounterEvent> {
    override fun handle(commands: Flow<CounterCommand>): Flow<CounterEvent> {
        return commands
            .filter { it is CounterCommand.LoadIncrement }
            .map { command ->
                val loadCommand = command as CounterCommand.LoadIncrement
                // Simulate async operation
                delay(1000)
                CounterEvent.AsyncIncrementResult(loadCommand.currentValue + 1)
            }
    }
}

// Factory function to create the store
fun createCounterStore(): CounterStore = Store(
    initialState = CounterState(),
    update = counterUpdate,
    commandHandlers = listOf(AsyncIncrementHandler())
)
