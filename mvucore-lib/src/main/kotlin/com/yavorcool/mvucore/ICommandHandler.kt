package com.yavorcool.mvucore

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlin.reflect.KClass

interface ICommandHandler<in Command: Any, out Event: Any> {

    fun handle(commands: Flow<Command>): Flow<Event>
}

abstract class FilteringHandler<in FilteredCommand : Any, in ParentCommand : Any, out Event : Any>(
    private val commandClass: KClass<FilteredCommand>,
) : ICommandHandler<ParentCommand, Event> {
    override fun handle(commands: Flow<ParentCommand>): Flow<Event> {
        val filtered = commands.filter { commandClass.isInstance(it) } as Flow<FilteredCommand>
        return filtered.map(::handleCommand)
    }

    abstract suspend fun handleCommand(command: FilteredCommand): Event
}

abstract class LatestFilteringHandler<in FilteredCommand : Any, in ParentCommand : Any, out Event : Any>(
    private val commandClass: KClass<FilteredCommand>,
) : ICommandHandler<ParentCommand, Event> {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun handle(commands: Flow<ParentCommand>): Flow<Event> {
        val filtered = commands.filter { commandClass.isInstance(it) } as Flow<FilteredCommand>
        return filtered.mapLatest(::handleCommand)
    }

    abstract suspend fun handleCommand(command: FilteredCommand): Event
}

inline fun <reified FilteredCommand : ParentCommand, ParentCommand : Any, Event : Any> filteringHandler(
    noinline handle: suspend (FilteredCommand) -> Event,
) = object : FilteringHandler<FilteredCommand, ParentCommand, Event>(FilteredCommand::class) {
    override suspend fun handleCommand(command: FilteredCommand) = handle(command)
}

abstract class FilteringHandlerToFlow<in FilteredCommand : Any, in ParentCommand : Any, out Event : Any>(
    private val commandClass: KClass<FilteredCommand>,
) : ICommandHandler<ParentCommand, Event> {
    override fun handle(commands: Flow<ParentCommand>): Flow<Event> {
        val filtered = commands.filter { commandClass.isInstance(it) } as Flow<FilteredCommand>
        return filtered.flatMapMerge(transform = ::handleCommand)
    }

    abstract suspend fun handleCommand(command: FilteredCommand): Flow<Event>
}

inline fun <reified FilteredCommand : ParentCommand, ParentCommand : Any, Event : Any> filteringHandlerToFlow(
    noinline handle: suspend (FilteredCommand) -> Flow<Event>,
) = object : FilteringHandlerToFlow<FilteredCommand, ParentCommand, Event>(FilteredCommand::class) {
    override suspend fun handleCommand(command: FilteredCommand) = handle(command)
}
