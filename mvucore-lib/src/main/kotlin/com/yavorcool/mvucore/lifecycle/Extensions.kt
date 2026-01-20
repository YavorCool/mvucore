package com.yavorcool.mvucore.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.yavorcool.mvucore.IStore
import com.yavorcool.mvucore.lifecycle.StoreWrappingViewModelProperty.Companion.createStoreWrappedByViewModelProperty
import kotlin.properties.ReadOnlyProperty

fun <S : IStore<*, *, *>> ViewModelStoreOwner.wrapStoreWithViewModelProperty(
    sharedViewModelKey: String? = null,
    getStore: () -> S
): ReadOnlyProperty<ViewModelStoreOwner, S> = createStoreWrappedByViewModelProperty(
    sharedViewModelKey,
    StoreWrappingViewModel.createStoreWrappingViewModelFactory(getStore)
)

inline fun <reified S : IStore<*, *, *>> ViewModelStoreOwner.wrapStoreWithViewModel(
    vmKeyPostfix: String = "",
    noinline getStore: () -> S
): S {
    val key = S::class.java.name + "#" + vmKeyPostfix

    val viewModelProviderFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>) =
            StoreWrappingViewModel.createStoreWrappingViewModelFactory(getStore).create() as VM
    }
    val viewModelProvider = ViewModelProvider(this, viewModelProviderFactory)

    val vm = viewModelProvider[key, StoreWrappingViewModel::class.java] as StoreWrappingViewModel<S>
    return vm.store
}
