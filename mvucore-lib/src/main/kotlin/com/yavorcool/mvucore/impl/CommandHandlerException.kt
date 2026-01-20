package com.yavorcool.mvucore.impl

import com.yavorcool.mvucore.ICommandHandler

class CommandHandlerException(
    handlerClass: Class<out ICommandHandler<*, *>>,
    cause: Throwable
) : RuntimeException("Exception in ${handlerClass.canonicalName}", cause)
