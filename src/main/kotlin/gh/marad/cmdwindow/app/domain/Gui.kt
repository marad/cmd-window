package gh.marad.cmdwindow.app.domain

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v1.MenuItem
import androidx.compose.ui.window.v1.Tray
import gh.marad.cmdwindow.app.data.CommandName
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.SwingUtilities
import kotlin.concurrent.schedule


object Gui {
    private val promptWindowSize = IntSize(400, 100)

    const val mainWindowTitle = "Command Window"

    fun start(invokeCommand: (CommandName) -> Unit) {
        Window(
            centered = true,
            undecorated = true,
            title = mainWindowTitle,
            size = promptWindowSize
        ) {
            TrayIcon.setup()
            layout {
                userInputField(
                    placeholder = "Start typing...",
                    onEnter = { commandName ->
                        MainWindow.hide()
                        startInThread {
                            invokeCommand(commandName)
                        }
                    },
                    onEscape = {
                        MainWindow.hide()
                    },
                    shouldCloseAfterOnEnter = false,
                )
            }
        }

        SwingUtilities.invokeLater {
            MainWindow.hide()
        }
    }

    @Composable
    fun layout(content: @Composable () -> Unit) {
        MaterialTheme(
            colors = darkThemeColors
        ) {
            Surface(
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colors.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    fun message(message: String, title: String, width: Int = 300, height: Int = 100) {
        Window(
            title = title,
            size = IntSize(width, height)
        ) {
            layout { Text(message) }
        }
    }

    fun input(prompt: String?, onResponse: (String) -> Unit) {
        Window(
            centered = true,
            undecorated = true,
            size = promptWindowSize
        ) {
            layout {
                userInputField(
                    placeholder = prompt,
                    onEnter = onResponse
                )
            }
        }
    }

    fun select(options: List<SelectOption>,
               title: String? = null,
               showFilter: Boolean = false,
    ) {
        Window(
            title = title ?: "",
            centered = true,
            undecorated = false,
            size = IntSize(promptWindowSize.width, 650)
        ) {
            var filterText by remember { mutableStateOf("") }
            val focusRequester = FocusRequester()
            val optionItems = options.filter {
                filterText.isBlank() ||
                        it.name.contains(filterText, ignoreCase = true) ||
                        it.description?.contains(filterText, ignoreCase = true) ?: false
            }
            layout {
                Column {
                    if (showFilter) {
                        TextField(
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                                .onKeyEvent {
                                    when(it.nativeKeyEvent.keyCode) {
                                        NativeKeyEvent.VK_ENTER -> {
                                            AppManager.focusedWindow?.close()
                                            startInThread {
                                                optionItems.firstOrNull()?.handler?.invoke()
                                            }
                                            true
                                        }
                                        NativeKeyEvent.VK_ESCAPE -> {
                                            AppManager.focusedWindow?.close()
                                            true
                                        }
                                        else -> false
                                    }
                                    false
                                },
                            value = filterText,
                            onValueChange = { filterText = it },
                            singleLine = true,
                        )

                        DisposableEffect(Unit) {
                            focusRequester.requestFocus()
                            onDispose { }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        LazyColumn {
                            items(optionItems) { option ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                        .clickable {
                                            AppManager.focusedWindow?.close()
                                            option.handler()
                                        },
                                    elevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Text(option.name, style = MaterialTheme.typography.h6)
                                        if (option.description != null) {
                                            Text(option.description, style = MaterialTheme.typography.subtitle1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun userInputField(
        onEscape: (() -> Unit)? = {
            AppManager.focusedWindow?.close()
        },
        onEnter: ((String) -> Unit)? = null,
        label: String? = null,
        placeholder: String? = null,
        shouldCloseAfterOnEnter: Boolean = true,
    ) {
        var text by remember { mutableStateOf("") }
        val focusRequester = FocusRequester()
        TextField(
            modifier = Modifier.fillMaxWidth()
                .focusRequester(focusRequester)
                .border(width = 2.dp, color = MaterialTheme.colors.primary)
                .onKeyEvent { key ->
                    when(key.nativeKeyEvent.keyCode) {
                        NativeKeyEvent.VK_ENTER -> {
                            if (shouldCloseAfterOnEnter) {
                                AppManager.focusedWindow?.close()
                            }
                            if (onEnter != null) {
                                onEnter(text)
                            }
                            text = ""
                            true
                        }
                        NativeKeyEvent.VK_ESCAPE -> {
                            if (onEscape != null) {
                                onEscape()
                            }
                            true
                        }
                        else -> false
                    }
                },
            value = text,
            onValueChange = { text = it },
            label = {
                if (label != null) {
                    Text(label)
                }
            },
            placeholder = {
                if (placeholder != null) {
                    Text(placeholder)
                }
            },
            singleLine = true,
        )

        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }

    private fun startInThread(handler: () -> Unit) {
        Thread {
            try {
                handler()
            } catch(ex: Throwable) {
                message(
                    ex.message!!,
                    title = "Exception")
            }
        }.start()
    }


    private val lightThemeColors = lightColors(
        primary = Color(0xFF855446),
        primaryVariant = Color(0xFF9C684B),
        secondary = Color(0xFF03DAC5),
        secondaryVariant = Color(0xFF0AC9F0),
        background = Color.White,
        surface = Color.White,
        error = Color(0xFFB00020),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White
    )

    private val darkThemeColors = darkColors(
        primary = Color(0xFF1F1F1F),
        primaryVariant = Color(0xFF3E2723),
        secondary = Color(0xFF03DAC5),
        background = Color(0xFF121212),
        surface = Color.DarkGray,
        error = Color(0xFFCF6679),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.DarkGray
    )

    object MainWindow {
        fun getMainWindow(): AppWindow? =
            AppManager.windows.find { it.title == Gui.mainWindowTitle } as AppWindow?

        fun toggleVisibility() {
            val frame = getMainWindow()
            if (frame != null) {
                frame.window.isVisible = !frame.window.isVisible
                center(frame)
            }
        }

        fun show() {
            val frame = getMainWindow()
            if (frame != null) {
                frame.window.isVisible = true
                center(frame)
            }
        }

        fun hide() {
            val frame = getMainWindow()
            if (frame != null) {
                frame.window.isVisible = false
                center(frame)
            }
        }

        private fun center(frame: AppWindow) {
            Timer("Center Window", false).schedule(20) {
                frame.setWindowCentered()
            }
        }
    }

    object TrayIcon {
        @Composable
        fun setup() {
            DisposableEffect(Unit) {
                val tray = Tray().apply {
                    icon(getTrayIcon())
                    trayContextMenu()
                }
                onDispose {
                    tray.remove()
                }
            }
        }

        fun Tray.trayContextMenu() {
            menu(
                MenuItem(
                    name = "Toggle window",
                    onClick = { MainWindow.toggleVisibility() }
                ),
                MenuItem("Close", {
                    AppManager.exit()
                })
            )
        }

        fun getTrayIcon(): BufferedImage {
            val size = 256;
            val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()
            graphics.color = java.awt.Color.green
            graphics.fillOval(0, 0, size, size)
            graphics.dispose()
            return image
        }
    }
}

data class SelectOption(
    val name: String,
    val description: String?,
    val handler: () -> Unit)