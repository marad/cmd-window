package gh.marad.cmdwindow.app.domain

import gh.marad.cmdwindow.ScriptApi
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object KotlinScripts {
    private lateinit var scriptEngine: ScriptEngine
    private val beforeReloadCallbacks = mutableListOf<() -> Unit>()

    fun initScriptingEngine(scriptApi: ScriptApi) {
        try {
            scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!
            scriptEngine.put("api", scriptApi)
            scriptEngine.put("registerReloadCallback", this::registerBeforeReloadCallback)

            scriptEngine.eval("""
                fun beforeReload(action: () -> Unit) {
                  val registerCallback = bindings["registerReloadCallback"]!! as ((() -> Unit) -> Unit);
                  registerCallback(action)
                }
            """.trimIndent())

            val scriptsPath = Paths.get(scriptApi.getScriptsPath()!!)
            println("Loading bootstrap script...")
            scriptEngine.eval(Files.newBufferedReader(scriptsPath.resolve("Bootstrap.kts")))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            Gui.message("Error while loading Kotlin scripts: ${ex.message}",
                title = "Kotlin Scripts Error",
                width = 500,
                height = 300)
        }
    }

    fun reload(scriptApi: ScriptApi) {
        beforeReloadCallbacks.forEach { it() }
        beforeReloadCallbacks.clear()
        initScriptingEngine(scriptApi)
    }

    fun registerBeforeReloadCallback(callback: () -> Unit) {
        beforeReloadCallbacks.add(callback)
    }
}