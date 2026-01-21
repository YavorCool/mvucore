package com.yavorcool.mvucore.sample.counter.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.yavorcool.mvucore.sample.R
import com.yavorcool.mvucore.sample.counter.CounterViewModel
import com.yavorcool.mvucore.sample.counter.mvu.CounterEffectHandler
import com.yavorcool.mvucore.sample.counter.mvu.CounterUiEvent
import kotlinx.coroutines.launch

class CounterViewActivity : AppCompatActivity() {

    private val viewModel: CounterViewModel by viewModels()
    private val effectHandler by lazy { CounterEffectHandler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val counterText = findViewById<TextView>(R.id.counterText)
        val incrementButton = findViewById<Button>(R.id.incrementButton)
        val decrementButton = findViewById<Button>(R.id.decrementButton)
        val asyncIncrementButton = findViewById<Button>(R.id.asyncIncrementButton)

        // Dispatch UI events to the store
        incrementButton.setOnClickListener { viewModel.store.dispatch(CounterUiEvent.Increment) }
        decrementButton.setOnClickListener { viewModel.store.dispatch(CounterUiEvent.Decrement) }
        asyncIncrementButton.setOnClickListener { viewModel.store.dispatch(CounterUiEvent.AsyncIncrement) }

        // Collect state updates
        lifecycleScope.launch {
            viewModel.store.state.collect { state ->
                counterText.text = "Count: ${state.count}"
                asyncIncrementButton.isEnabled = !state.isLoading
                asyncIncrementButton.text = if (state.isLoading) "Loading..." else "Async +1"
            }
        }

        // Collect effects (one-time events)
        lifecycleScope.launch {
            viewModel.store.effects.collect { effect ->
                effectHandler.handleEffect(effect)
            }
        }
    }
}
