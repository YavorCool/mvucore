package com.yavorcool.mvucore.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.lifecycle.StoreWrappingViewModel.Factory

class StoreWrappingViewModel<out S: IStore<*, *, *>>(val store: S): ViewModel() {

    fun interface Factory<S: IStore<*, *, *>> {
        fun create(): StoreWrappingViewModel<S>
    }

    init {
        store.launch(viewModelScope)
    }

    companion object {
        fun <S: IStore<*, *, *>> createStoreWrappingViewModelFactory(getStore: () -> S): Factory<S> = Factory {
            StoreWrappingViewModel(getStore())
        }
    }
}
