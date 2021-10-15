package gh.marad.cmdwindow.app.domain

import clojure.lang.RT

object ClojureScripts {
    fun startNreplServer(port: Int = 7888) {
        eval(
            """
                (do 
                    (require 'nrepl.server)
                    (defonce server (nrepl.server/start-server :port $port))
                    (println "Server started " server))
            """.trimIndent()
        )
    }

    fun eval(code: String) {
        RT.`var`("clojure.core", "eval").invoke(
            RT.`var`("clojure.core", "read-string").invoke(code)
        )
    }

    fun bindVariable(varName: String, obj: Any) {
        RT.`var`("user", varName, obj)
    }

    fun loadSetup() {
        RT.loadResourceScript("setup.clj")
    }
}