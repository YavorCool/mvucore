package com.yavorcool.mvucore.sample.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit,
) {
    LaunchedEffect(
        key1 = lifecycleOwner,
        key2 = minActiveState,
        key3 = action,
        block = {
            lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
                collect(action)
            }
        }
    )
}
