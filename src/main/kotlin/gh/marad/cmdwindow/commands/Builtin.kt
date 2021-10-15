package gh.marad.cmdwindow.commands

import androidx.compose.desktop.AppManager
import gh.marad.cmdwindow.app.domain.Commands
import gh.marad.cmdwindow.app.domain.Gui
import gh.marad.cmdwindow.app.domain.SelectOption

fun createHelpCommand(listCommands: () -> List<Commands.Cmd>) = Commands.Cmd(
    name = "?",
    description = "Shows all available commands",
    handler = {
        Gui.select(
            listCommands().map {
                SelectOption(it.name, it.description, it.handler)
            },
            showFilter = true
        )
    }
)

fun createExitCommand() = Commands.Cmd(
    name = "exit",
    description = "closes the program",
    handler = { AppManager.exit() })
