package com.yavorcool.mvucore

fun interface IEventDispatcher<in Event : Any> {
    fun dispatch(event: Event)
}
