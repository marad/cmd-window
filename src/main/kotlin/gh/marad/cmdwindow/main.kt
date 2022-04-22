package gh.marad.cmdwindow

import gh.marad.cmdwindow.app.domain.*
import gh.marad.cmdwindow.commands.createExitCommand
import gh.marad.cmdwindow.commands.createHelpCommand
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object App {
    private val commandRegistry = Commands.createCommandRegistry()
    private val ahk = Ahk()
    private val scriptApi = ScriptApi(commandRegistry, ahk)

    private val executor = ThreadPoolExecutor(4, 4, 100, TimeUnit.SECONDS, ArrayBlockingQueue(4))

    fun start() = runBlocking(executor.asCoroutineDispatcher()) {
        executor.threadFactory = ThreadFactory { Thread(it).apply { isDaemon = true } }
        launch { createDefaultCommands().map(scriptApi::registerCommand) }
        launch { Gui.start(scriptApi::invokeCommand) }
        launch { setupScriptingWithKotlin() }
        launch { JsScripts.initScriptingEngine(scriptApi) }
        launch {
            Windows.registerGlobalHotkeyAndStartWinApiThread {
                Gui.toggleMainWindow()
            }
        }
    }

    private fun setupScriptingWithKotlin() {
        println("Starting scripting engine...")
        KotlinScripts.initScriptingEngine(scriptApi)

        scriptApi.registerCommand("rk", "Reloads kotlin scripts") {
            KotlinScripts.reload(scriptApi)
            scriptApi.guiNotify("", "Kotlin scripts reloaded!")
        }
    }

    private fun createDefaultCommands(): List<Commands.Cmd> = listOf(
        createHelpCommand(scriptApi::listCommands, scriptApi::invokeCommand),
        createExitCommand(),
    )
}

fun main() {
    App.start()
}
