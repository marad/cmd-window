package gh.marad.cmdwindow.app.domain

import gh.marad.cmdwindow.app.data.CommandDescription
import gh.marad.cmdwindow.app.data.CommandHandler
import gh.marad.cmdwindow.app.data.CommandName
import gh.marad.cmdwindow.app.data.CommandRegistry


object Commands {
    data class Cmd(
        val name: CommandName,
        val description: CommandDescription,
        val handler: CommandHandler
    )

    fun createCommandRegistry(commands: List<Cmd> = emptyList()): CommandRegistry =
        commands.associateBy { it.name }.toMutableMap()

    fun registerCommand(commandRegistry: CommandRegistry, cmd: Cmd) {
        commandRegistry[cmd.name] = cmd
    }

    fun invokeCommand(
        commandRegistry: CommandRegistry,
        ifNotFound: (CommandName) -> Unit,
        name: CommandName
    ) {
        commandRegistry[name]?.handler?.invoke() ?: ifNotFound(name)
    }

    fun listCommands(commandRegistry: CommandRegistry): List<Cmd> = commandRegistry.values.toList()
}