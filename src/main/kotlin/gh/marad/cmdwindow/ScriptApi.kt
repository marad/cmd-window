package gh.marad.cmdwindow

import androidx.compose.ui.window.Notifier
import gh.marad.cmdwindow.app.data.CommandHandler
import gh.marad.cmdwindow.app.data.CommandRegistry
import gh.marad.cmdwindow.app.domain.*

class ScriptApi(private val commandRegistry: CommandRegistry,
                private val ahk: Ahk
) {
    private val notifier = Notifier()

    fun getScriptsPath() = Config.scriptsPath
    fun getCustomConfigPath() = Config.customConfigPath
    fun registerCommand(cmd: Commands.Cmd) {
        println("Registering command ${cmd.name}...")
        Commands.registerCommand(commandRegistry, cmd)
    }

    fun registerCommand(name: String, desc: String, handler: CommandHandler) {
        println("Registering command $name...")
        Commands.registerCommand(commandRegistry, Commands.Cmd(name, desc, handler))
    }

    fun listCommands() = Commands.listCommands(commandRegistry)
    fun invokeCommand(commandName: String) = Commands.invokeCommand(commandRegistry, {
        Gui.message("There is no '$it' command", title = "Command not found")
    }, commandName)

    fun ahk(ahkCommand: String) = ahk.exec(ahkCommand)
    fun ahkSetVar(name: String, value: String) = ahk.setVar(name, value)
    fun ahkGetVar(name: String) = ahk.getVar(name)

    fun guiMessage(msg: String, title: String, width: Int = 300, height: Int = 200) = Gui.message(msg, title, width, height)
    fun guiInput(prompt: String?, onResponse: (String) -> Unit) = Gui.input(prompt, onResponse)
    fun guiSelect(options: List<SelectOption>, title: String? = null, showFilter: Boolean = false) = Gui.select(options, title, showFilter)
    fun guiNotify(title: String, message: String) = notifier.notify(title, message)
    fun guiWarn(title: String, message: String) = notifier.warn(title, message)
    fun guiError(title: String, message: String) = notifier.error(title, message)
}