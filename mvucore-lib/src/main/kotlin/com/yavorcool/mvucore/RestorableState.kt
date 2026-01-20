package com.yavorcool.mvucore

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.yavorcool.mvucore.impl.Store

interface RestorableState<T : RestorableState<T>> {
    fun saveState(): Bundle
    fun restoreState(bundle: Bundle): T
}

private const val PARCELABLE_STATE_KEY = "ParcelableState"

abstract class ParcelableState : RestorableState<ParcelableState>, Parcelable {
    override fun restoreState(bundle: Bundle): ParcelableState {
        return if (bundle.containsKey(PARCELABLE_STATE_KEY)) {
            bundle.classLoader = ParcelableState::class.java.classLoader
            @Suppress("DEPRECATION")
            bundle.getParcelable<ParcelableState>(PARCELABLE_STATE_KEY) as ParcelableState
        } else {
            this
        }
    }

    override fun saveState() = Bundle(1).also {
        it.putParcelable(PARCELABLE_STATE_KEY, this)
    }
}

fun <State : RestorableState<*>, Event : Any, UiEvent : Event, Command : Any, Effect : Any> restorableStore(
    initialState: State,
    update: Update<State, Event, Command, Effect>,
    commandHandlers: List<ICommandHandler<Command, Event>>,
    savedStateHandle: SavedStateHandle,
    stateKey: String,
): IStore<State, UiEvent, Effect> {
    val state = initialState.let {
        val savedState = savedStateHandle.get<Bundle>(stateKey)
        if (savedState != null) it.restoreState(savedState) as State else it
    }
    val store = Store<State, Event, UiEvent, Command, Effect>(state, update, commandHandlers)
    savedStateHandle.setSavedStateProvider(stateKey) {
        store.state.value.saveState()
    }
    return store
}
