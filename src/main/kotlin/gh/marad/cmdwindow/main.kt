package gh.marad.cmdwindow

import gh.marad.cmdwindow.app.domain.*
import gh.marad.cmdwindow.commands.createExitCommand
import gh.marad.cmdwindow.commands.createHelpCommand
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object App {
    private val commandRegistry = Commands.createCommandRegistry()
    private val ahk = Ahk()
    private val scriptApi = ScriptApi(commandRegistry, ahk)

    fun start() {
        runBlocking {
            launch { createDefaultCommands().map(scriptApi::registerCommand) }
            launch {
                Gui.start(scriptApi::invokeCommand)
                setupScriptingWithKotlin()
                Windows.registerGlobalHotkeyAndStartWinApiThread {
                    Gui.MainWindow.toggleVisibility()
                }
            }
        }
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
