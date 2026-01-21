package com.yavorcool.mvucore.sample.counter.mvu

import androidx.compose.runtime.Immutable

@Immutable
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
)
