package gh.marad.cmdwindow

import gh.marad.cmdwindow.app.data.CommandHandler
import gh.marad.cmdwindow.app.data.CommandRegistry
import gh.marad.cmdwindow.app.domain.*

class ScriptApi(private val commandRegistry: CommandRegistry,
                private val ahk: Ahk
) {
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
        guiWarn("Command not found", "There is no '$commandName' command!")
    }, commandName)

    fun ahk(ahkCommand: String) = ahk.exec(ahkCommand)
    fun ahkSetVar(name: String, value: String) = ahk.setVar(name, value)
    fun ahkGetVar(name: String) = ahk.getVar(name)

    fun guiMessage(title: String, msg: String, width: Int = 500, height: Int = 300) = Gui.message(msg, title, width, height)
    fun guiInput(prompt: String?, onResponse: (String) -> Unit) = Gui.input(prompt, onResponse)
    fun guiSelect(options: List<SelectOption>, title: String? = null, showFilter: Boolean = false) = Gui.select(options, title, showFilter)
    fun guiNotify(title: String, message: String) = Gui.sendNotification(message, title, NotificationType.NONE)
    fun guiWarn(title: String, message: String) = Gui.sendNotification(message, title, NotificationType.WARN)
    fun guiError(title: String, message: String) = Gui.sendNotification(message, title, NotificationType.ERROR)
}