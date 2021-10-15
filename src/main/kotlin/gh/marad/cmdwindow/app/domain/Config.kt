package gh.marad.cmdwindow.app.domain

import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.*

object Config {
    val configsPath: Path = FileSystems.getDefault().getPath(System.getenv("APPDATA"), "cmd-window")
    val properties: Properties = Properties().also {
        it.load(configsPath.resolve("config.properties").toFile().inputStream())
    }
    val customConfigPath: String = configsPath.toAbsolutePath().resolve("config").toString()
    val scriptsPath: String? = properties.getProperty("scriptsDir")
}