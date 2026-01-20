package com.yavorcool.mvucore.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.yavorcool.mvucore.IStore

fun <State : Any, Effect : Any> IStore<State, *, Effect>.collect(
    lifecycleOwner: LifecycleOwner,
    stateCollector: ((State) -> Unit)? = null,
    effectsCollector: ((Effect) -> Unit)? = null
) = with(lifecycleOwner.lifecycleScope) {
    stateCollector?.let { launchWhenStarted { state.collect(stateCollector) } }
    effectsCollector?.let { launchWhenResumed { effects.collect(effectsCollector) } }
}
