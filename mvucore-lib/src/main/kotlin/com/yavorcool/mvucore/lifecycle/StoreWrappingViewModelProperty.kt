package com.yavorcool.mvucore.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.yavorcool.mvucore.IStore
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class StoreWrappingViewModelProperty<S : IStore<*, *, *>>(
    private val viewModelStoreOwnerProvider: () -> ViewModelStoreOwner,
    private val sharedViewModelKey: String?,
    private val storeWrappingViewModelFactory: StoreWrappingViewModel.Factory<S>
) : ReadOnlyProperty<Any, S> {

    private var store: S? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): S {
        return store ?: createStore(property).also { store = it }
    }

    private fun createStore(property: KProperty<*>): S {
        val storeOwner = viewModelStoreOwnerProvider()
        val key = sharedViewModelKey ?: keyFromProperty(storeOwner, property)

        val viewModelProviderFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>) = storeWrappingViewModelFactory.create() as VM
        }
        val viewModelProvider = ViewModelProvider(storeOwner, viewModelProviderFactory)

        val vm = viewModelProvider[key, StoreWrappingViewModel::class.java] as StoreWrappingViewModel<S>
        return vm.store
    }

    private fun keyFromProperty(thisRef: ViewModelStoreOwner, property: KProperty<*>): String {
        return thisRef::class.java.canonicalName!! + "#" + property.name
    }

    companion object {

        fun <S : IStore<*, *, *>> ViewModelStoreOwner.createStoreWrappedByViewModelProperty(
            sharedViewModelKey: String?,
            storeWrappingViewModelFactory: StoreWrappingViewModel.Factory<S>
        ): ReadOnlyProperty<ViewModelStoreOwner, S> {
            return StoreWrappingViewModelProperty({ this }, sharedViewModelKey, storeWrappingViewModelFactory)
        }
    }
}
