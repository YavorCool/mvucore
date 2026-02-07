package com.yavorcool.mvucore

class Next<out State : Any, out Command : Any, out Effect : Any>(
    val state: State,
    val commands: List<Command>,
    val effects: List<Effect> = emptyList(),
) {
    constructor(
        state: State,
        command: Command? = null,
        effect: Effect? = null,
        commands: List<Command> = listOfNotNull(command),
        effects: List<Effect> = listOfNotNull(effect),
    ) : this(state, commands, effects)
}
