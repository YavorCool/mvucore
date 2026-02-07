package com.yavorcool.mvucore.impl

import com.yavorcool.mvucore.ICommandHandler
import kotlin.reflect.KClass

class CommandHandlerException(
    handlerClass: KClass<out ICommandHandler<*, *>>,
    cause: Throwable
) : RuntimeException("Exception in ${handlerClass.qualifiedName}", cause)
