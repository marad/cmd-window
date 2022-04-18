package gh.marad.cmdwindow.app.domain

import gh.marad.cmdwindow.ScriptApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText

class JsScripts(private val scriptApi: ScriptApi) {
    private lateinit var context: Context
    companion object {
        private lateinit var engine: JsScripts
        fun initScriptingEngine(api: ScriptApi) {
            engine = JsScripts(api)
        }
    }
    init {
        scriptApi.registerCommand("r", "reloads scripts") {
            reload()
        }
        reload()
    }

    fun reload() {
        context = Context.newBuilder()
            .allowAllAccess(true)
            .allowHostAccess(HostAccess.ALL)
            .allowIO(true)
            .currentWorkingDirectory(Paths.get(scriptApi.getScriptsPath()!!))
            .build()

        context.getBindings("js")
            .putMember("api", scriptApi)

        val scriptsPath = Paths.get(scriptApi.getScriptsPath()!!)
        val bootstrapScriptPath = scriptsPath.resolve("bootstrap.js")
        if (bootstrapScriptPath.exists()) {
            println("Loading bootstrap js module...")
            try {
                evalModule(bootstrapScriptPath)
            }  catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            println("JS bootstrap file does not exist at ${bootstrapScriptPath.absolutePathString()}")
        }
    }

    fun evalModule(jsModuleFile: Path) {
        val code = jsModuleFile.readText()
        val source = Source.newBuilder("js", code, jsModuleFile.name)
            .mimeType("application/javascript+module")
            .build()
        context.eval(source)
    }
}