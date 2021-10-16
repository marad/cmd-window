package gh.marad.cmdwindow.app.domain

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import gh.marad.cmdwindow.app.data.CommandName


object Gui {
    const val mainWindowTitle = "Command Window"
    private val promptWindowSize = DpSize(400.dp, 100.dp)

    // main window
    private val promptWindowOpen = mutableStateOf(false)
    private lateinit var trayState: TrayState

    fun toggleMainWindow() { promptWindowOpen.value = !promptWindowOpen.value }

    fun sendNotification(notification: Notification) = trayState.sendNotification(notification)

    fun start(invokeCommand: (CommandName) -> Unit) = application {
        setupTrayIcon()
        definePromptWindow(invokeCommand)
    }

    @Composable
    private fun definePromptWindow(invokeCommand: (CommandName) -> Unit) {
        var isOpen by remember { promptWindowOpen}
        if (isOpen) {
            Window(
                onCloseRequest = { isOpen = false },
                icon = AppIcon,
                state = rememberWindowState(
                    position = WindowPosition.Aligned(Alignment.Center),
                    size = promptWindowSize
                ),
                title = mainWindowTitle,
                undecorated = true,
                alwaysOnTop = true,
                resizable = false,
            ) {
                layout {
                    userInputField(
                        placeholder = "Start typing...",
                        onEnter = { commandName ->
                            isOpen = false
                            startInThread {
                                invokeCommand(commandName)
                            }
                        },
                        onEscape = {
                            isOpen = false
                        },
                    )
                }
            }
        }
    }

    fun message(title: String, message: String, width: Int, height: Int) = application {
        Window(
            onCloseRequest = ::exitApplication,
            icon = AppIcon,
            title = title,
            state = rememberWindowState(size = DpSize(width.dp, height.dp), position = WindowPosition.Aligned(Alignment.Center))
        ) {
            layout { Text(message) }
        }
    }

    fun input(prompt: String?, onResponse: (String) -> Unit) = application {
        Window(
            onCloseRequest = ::exitApplication,
            icon = AppIcon,
            undecorated = true,
            state = rememberWindowState(
                size = promptWindowSize,
                position = WindowPosition.Aligned(Alignment.Center)
            )
        ) {
            layout {
                userInputField(
                    placeholder = prompt,
                    onEnter = {
                        exitApplication()
                        startInThread {
                            onResponse(it)
                        }
                    },
                    onEscape = {
                        exitApplication()
                    }
                )
            }
        }
    }

    fun select(options: List<SelectOption>,
               title: String? = null,
               showFilter: Boolean = false,
    ) = application {
        var isOpen by remember { mutableStateOf(true) }
        if (isOpen) {
            Window(
                onCloseRequest = ::exitApplication,
                title = title ?: "",
                undecorated = false,
                state = rememberWindowState(
                    size = DpSize(promptWindowSize.width, 650.dp),
                    position = WindowPosition.Aligned(Alignment.Center)
                ),
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
                                        when {
                                            it.type == KeyEventType.KeyDown && it.nativeKeyEvent.keyCode == NativeKeyEvent.VK_ENTER -> {
                                                isOpen = false
                                                startInThread {
                                                    optionItems.firstOrNull()?.handler?.invoke()
                                                }
                                                true
                                            }
                                            it.type == KeyEventType.KeyDown && it.nativeKeyEvent.keyCode == NativeKeyEvent.VK_ESCAPE -> {
                                                isOpen = false
                                                true
                                            }
                                            else -> false
                                        }
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
                                                isOpen = false
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
    }

    @Composable
    fun userInputField(
        onEscape: (() -> Unit)? = null,
        onEnter: ((String) -> Unit)? = null,
        label: String? = null,
        placeholder: String? = null,
    ) {
        var text by remember { mutableStateOf("") }
        val focusRequester = FocusRequester()
        TextField(
            modifier = Modifier.fillMaxWidth()
                .focusRequester(focusRequester)
                .border(width = 2.dp, color = MaterialTheme.colors.primary)
                .onKeyEvent { key ->
                    when {
                        key.type == KeyEventType.KeyDown && key.nativeKeyEvent.keyCode == NativeKeyEvent.VK_ENTER -> {
                            if (onEnter != null) {
                                onEnter(text)
                            }
                            text = ""
                            true
                        }
                        key.type == KeyEventType.KeyDown && key.nativeKeyEvent.keyCode == NativeKeyEvent.VK_ESCAPE -> {
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

    fun startInThread(handler: () -> Unit) {
        Thread {
            try {
                handler()
            } catch(ex: Throwable) {
                trayState.sendNotification(
                    Notification(
                        "Exception",
                        "${ex.message}",
                        Notification.Type.Error
                    )
                )
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

    object AppIcon : Painter() {
        override val intrinsicSize = Size(256f, 256f)

        override fun DrawScope.onDraw() {
            drawOval(Color.Green)
        }
    }

    @Composable
    fun ApplicationScope.setupTrayIcon() {
        trayState = rememberTrayState()
        Tray(
            state = trayState,
            icon = AppIcon,
            menu = {
                Item(
                    "Toggle window",
                    onClick = { toggleMainWindow() }
                )
                Item(
                    "Close",
                    onClick = { exitApplication() }
                )
            }
        )
    }
}

data class SelectOption(
    val name: String,
    val description: String?,
    val handler: () -> Unit)