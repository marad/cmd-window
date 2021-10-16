package gh.marad.cmdwindow

import androidx.compose.ui.window.application
import gh.marad.cmdwindow.app.domain.*
import gh.marad.cmdwindow.commands.createExitCommand
import gh.marad.cmdwindow.commands.createHelpCommand

object App {
    private val commandRegistry = Commands.createCommandRegistry()
    private val ahk = Ahk()
    private val scriptApi = ScriptApi(commandRegistry, ahk)

    fun start() = application {
        createDefaultCommands().map(scriptApi::registerCommand)
        Gui.start(scriptApi::invokeCommand, ::exitApplication)
        Thread { setupScriptingWithKotlin() }.start()
        Thread {
            Windows.registerGlobalHotkeyAndStartWinApiThread {
                Gui.toggleMainWindow()
            }
        }.start()
    }

    private fun setupScriptingWithKotlin() {
        KotlinScripts.initScriptingEngine(scriptApi)

        scriptApi.registerCommand("r", "Reloads all scripts") {
            KotlinScripts.reload(scriptApi)
            scriptApi.guiNotify("", "Scripts reloaded!")
        }
    }

    private fun createDefaultCommands(): List<Commands.Cmd> = listOf(
        createHelpCommand(scriptApi::listCommands),
        createExitCommand()
    )
}

fun main() {
    App.start()
}
