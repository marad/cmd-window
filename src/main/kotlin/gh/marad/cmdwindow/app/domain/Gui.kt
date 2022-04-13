package gh.marad.cmdwindow.app.domain

import com.dustinredmond.fxtrayicon.FXTrayIcon
import com.jfoenix.controls.JFXListView
import gh.marad.cmdwindow.app.data.CommandName
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.function.Predicate


private fun createInputScene(
    prompt: String? = null,
    onEnter: (String) -> Unit = {},
    onEscape: (String) -> Unit = {},
    preferredWidth: Double = 600.0,
    preferredHeight: Double = 80.0,
): Scene {
    val textfield = TextField().apply {
        font = Font.font(30.0)
        setPrefSize(preferredWidth, preferredHeight)
        onKeyPressed = EventHandler {
            if (it.code == KeyCode.ENTER) {
                onEnter(text)
                it.consume()
                text = ""
            } else if (it.code == KeyCode.ESCAPE) {
                onEscape(text)
                it.consume()
                text = ""
            }
        }
    }

    val layout = GridPane()
    if (prompt != null) {
        val label = Label(prompt)
        label.font = Font.font(18.0)
        label.padding = Insets(10.0)
        layout.add(label, 0, 0)

    }
    layout.add(textfield, 0, 1)
//        layout.padding = Insets(20.0)

    GridPane.setHgrow(textfield, Priority.ALWAYS)
    GridPane.setVgrow(textfield, Priority.ALWAYS)
    return Scene(layout)
}

private class MainWindow(invokeCommand: (CommandName) -> Unit, private val stage: Stage) {
    private val trayIcon: FXTrayIcon

    init {
        trayIcon = setupTrayIcon()
        stage.scene = createInputScene(
            onEnter = { command ->
                Gui.startInThread {
                    invokeCommand(command)
                }
                stage.hide()
            },
            onEscape = { stage.hide() }
        )
        stage.initStyle(StageStyle.UNDECORATED)
        stage.isAlwaysOnTop = true
        stage.centerOnScreen()
    }

    private fun setupTrayIcon(): FXTrayIcon {
        val trayIcon = FXTrayIcon(stage)
//        trayIcon.setOnAction {
//            toggle()
//        }
        trayIcon.setApplicationTitle("Cmd Window")
        trayIcon.addMenuItem(MenuItem("Toggle").apply {
            onAction = EventHandler { toggle() }
        })
        trayIcon.addSeparator()
        trayIcon.addMenuItem(MenuItem("Exit").apply {
            onAction = EventHandler { globalInvokeCommand("exit") }
        })
        trayIcon.show()
        return trayIcon
    }

    fun toggle() = Platform.runLater {
        if (stage.isShowing) {
            stage.hide()
        } else {
            stage.show()
        }
    }

    fun sendNotification(message: String, title: String? = null, type: NotificationType = NotificationType.NONE) {
        when(type) {
            NotificationType.NONE ->
                if (title == null) {
                    trayIcon.showMessage(message)
                } else {
                    trayIcon.showMessage(title, message)
                }
            NotificationType.INFO ->
                if (title == null) {
                    trayIcon.showInfoMessage(message)
                } else {
                    trayIcon.showInfoMessage(title, message)
                }
            NotificationType.WARN ->
                if (title == null) {
                    trayIcon.showWarningMessage(message)
                } else {
                    trayIcon.showWarningMessage(title, message)
                }
            NotificationType.ERROR ->
                if (title == null) {
                    trayIcon.showErrorMessage(message)
                } else {
                    trayIcon.showErrorMessage(title, message)
                }
        }
    }
}

private lateinit var globalInvokeCommand: (CommandName) -> Unit
private lateinit var globalMainWindow: MainWindow

class CmdWindowApplication : Application() {
    override fun start(primaryStage: Stage) {
        globalMainWindow = MainWindow(globalInvokeCommand, primaryStage)
    }
}

enum class NotificationType {
    NONE,
    INFO,
    WARN,
    ERROR
}

object Gui {
    fun toggleMainWindow() {
        globalMainWindow.toggle()
    }

    fun sendNotification(message: String, title: String? = null, type: NotificationType = NotificationType.NONE) {
        globalMainWindow.sendNotification(message, title, type)
    }

    fun start(invokeCommand: (CommandName) -> Unit) {
        globalInvokeCommand = invokeCommand
        Application.launch(CmdWindowApplication::class.java)
    }

    fun message(title: String, message: String, width: Int, height: Int) {
        Platform.runLater {
            Stage().apply {
                val label = Label(message).apply {
                    font = Font.font(18.0)
                    alignment = Pos.CENTER
                }

                this.title = title
                scene = Scene(label, width.toDouble(), height.toDouble())
                centerOnScreen()
                show()
            }
        }
    }

    fun input(prompt: String?, onResponse: (String) -> Unit) {
        Platform.runLater {
            Stage().apply {
                title = "Wejście"
                scene = createInputScene(
                    prompt = prompt,
                    onEnter = {
                        onResponse(it)
                        hide()
                    },
                    onEscape = { hide() },
                )
                show()
            }
        }
    }

    fun select(
        options: List<SelectOption>,
        title: String? = null,
        showFilter: Boolean = false,
    ) {
        Platform.runLater {
            Stage().apply {
                this.title = title
                scene = createListWithFilterScene(
                    options,
                    showFilter = showFilter,
                    onSelect = {
                        startInThread {
                            it.handler.invoke()
                        }
                        close()
                    },
                    onEscape = {
                        close()
                    },
                )
                show()
            }
        }
    }

    fun startInThread(handler: () -> Unit) {
        Thread {
            try {
                handler()
            } catch (ex: Throwable) {
                sendNotification(
                    message = "${ex.message}",
                    title = "Exception",
                    NotificationType.ERROR
                )
            }
        }.start()
    }

}

class SelectOption(
    name: String,
    description: String?,
    val handler: () -> Unit
) : ListItem(name, description)


open class ListItem(val name: String, val description: String? = null) : VBox() {
    init {
        children.add(Label(name).apply {
            font = Font.font(18.0)
        })
        if (description != null && description.isNotBlank()) {
            children.add(Label(description))
        }
    }
}

fun <T : ListItem> createListWithFilterScene(
    items: List<T>,
    onSelect: (T) -> Unit = {},
    onEscape: (T) -> Unit = {},
    showFilter: Boolean = true,
): Scene {


    val predicateProperty = SimpleObjectProperty<Predicate<T>>(Predicate { true })
    val itemNodes = FXCollections.observableList(items).filtered { true }
    itemNodes.predicateProperty().bind(predicateProperty)
    predicateProperty.set(Predicate { true })

    val field = TextField()
    val listview = JFXListView<T>()

    val keyPressedHandler = EventHandler<KeyEvent> {
        if (it.code == KeyCode.ENTER) {
            onSelect(listview.selectionModel.selectedItem ?: itemNodes.first())
            it.consume()
        } else if (it.code == KeyCode.ESCAPE) {
            onEscape(listview.selectionModel.selectedItem ?: itemNodes.first())
            it.consume()
        }
    }

    field.padding = Insets(10.0)
    field.font = Font.font(18.0)
    field.onKeyTyped = EventHandler {
        predicateProperty.set { it.name.contains(field.text) }
    }
    field.onKeyPressed = keyPressedHandler

    listview.items = itemNodes
    listview.onKeyPressed = keyPressedHandler
    listview.onMouseClicked = EventHandler {

        if (it.clickCount >= 2) {
            onSelect(listview.selectionModel.selectedItem ?: itemNodes.first())
        }
    }

    val vbox = VBox()
    if (showFilter) {
        vbox.children.add(field)
    }
    VBox.setVgrow(listview, Priority.ALWAYS)
    vbox.children.add(listview)

    return Scene(vbox, 400.0, 500.0)
}