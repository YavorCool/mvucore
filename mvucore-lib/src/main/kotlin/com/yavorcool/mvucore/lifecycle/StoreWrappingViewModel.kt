package com.yavorcool.mvucore.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.lifecycle.StoreWrappingViewModel.Factory

class StoreWrappingViewModel<out S: IStore<*, *, *>>(val store: S): ViewModel() {

    fun interface Factory<S: IStore<*, *, *>> {
        fun create(): StoreWrappingViewModel<S>
    }

    // Using IO based on this article (IO threads are no different from Default,
    // but there can be more of them, which will benefit us with many parallel tasks)
    // https://www.techyourchance.com/coroutines-dispatchers-default-and-dispatchers-io-considered-harmful/
    // possibly later we'll make our own dispatcher as described there.
    init {
        store.launch(viewModelScope)
    }

    companion object {
        fun <S: IStore<*, *, *>> createStoreWrappingViewModelFactory(getStore: () -> S): Factory<S> = Factory {
            StoreWrappingViewModel(getStore())
        }
    }
}
