package com.yavorcool.mvucore.sample.counter.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yavorcool.mvucore.sample.counter.CounterViewModel
import com.yavorcool.mvucore.sample.counter.mvu.CounterEffect
import com.yavorcool.mvucore.sample.counter.mvu.CounterState
import com.yavorcool.mvucore.sample.counter.mvu.CounterUiEvent
import com.yavorcool.mvucore.sample.util.observeWithLifecycle

class CounterComposeActivity : ComponentActivity() {

    private val viewModel: CounterViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by viewModel.store.state.collectAsStateWithLifecycle()
                val context = LocalContext.current

                viewModel.store.effects.observeWithLifecycle { effect ->
                    when (effect) {
                        is CounterEffect.ShowMessage -> {
                            Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Counter (Compose)") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    CounterScreen(
                        state = state,
                        onIncrement = { viewModel.store.dispatch(CounterUiEvent.Increment) },
                        onDecrement = { viewModel.store.dispatch(CounterUiEvent.Decrement) },
                        onAsyncIncrement = { viewModel.store.dispatch(CounterUiEvent.AsyncIncrement) },
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }
}

@Composable
fun CounterScreen(
    state: CounterState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAsyncIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Count: ${state.count}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = onDecrement) {
                Text("-1")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = onIncrement) {
                Text("+1")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAsyncIncrement,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "Loading..." else "Async +1")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterScreenPreview() {
    MaterialTheme {
        CounterScreen(
            state = CounterState(count = 5),
            onIncrement = {},
            onDecrement = {},
            onAsyncIncrement = {},
        )
    }
}
