package gh.marad.cmdwindow.commands

import gh.marad.cmdwindow.app.data.CommandName
import gh.marad.cmdwindow.app.domain.Commands
import gh.marad.cmdwindow.app.domain.Gui
import gh.marad.cmdwindow.app.domain.SelectOption
import kotlin.system.exitProcess

fun createHelpCommand(listCommands: () -> List<Commands.Cmd>,
                      invokeCommand: (CommandName) -> Unit) = Commands.Cmd(
    name = "?",
    description = "Shows all available commands",
    handler = {
        Gui.select(
            listCommands().map {
                SelectOption(it.name, it.description) {
                    Gui.startInThread {
                        invokeCommand(it.name)
                    }
                }
            },
            showFilter = true
        )
    }
)

fun createExitCommand() = Commands.Cmd(
    name = "exit",
    description = "closes the program",
    handler = { exitProcess(0) })
