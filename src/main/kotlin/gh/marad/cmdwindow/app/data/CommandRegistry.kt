package gh.marad.cmdwindow.app.data

import gh.marad.cmdwindow.app.domain.Commands

typealias CommandName = String
typealias CommandDescription = String
typealias CommandHandler = () -> Unit
typealias CommandRegistry = MutableMap<String, Commands.Cmd>
