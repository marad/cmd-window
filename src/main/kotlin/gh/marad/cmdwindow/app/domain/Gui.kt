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
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import gh.marad.cmdwindow.app.data.CommandName


object Gui {
    const val mainWindowTitle = "Command Window"
    private val promptWindowSize = WindowSize(400.dp, 100.dp)
    private lateinit var closeAppCallback: () -> Unit

    // main window
    private val promptWindowOpen = mutableStateOf(false)

    // message window
    private val messageWindowOpen = mutableStateOf(false)
    private val messageWindowTitle = mutableStateOf("")
    private val messageWindowMessage = mutableStateOf("")
    private val messageWindowSize = mutableStateOf(WindowSize(300.dp, 200.dp))

    // input window
    private val inputWindowOpen = mutableStateOf(false)
    private val inputWindowPrompt: MutableState<String?> = mutableStateOf(null)
    private val inputWindowCallback: MutableState<(String) -> Unit> = mutableStateOf({})

    fun toggleMainWindow() { promptWindowOpen.value = !promptWindowOpen.value }
    fun exitApplication() = closeAppCallback()

    @Composable
    fun start(invokeCommand: (CommandName) -> Unit, exitAppCallback: () -> Unit) {
        this.closeAppCallback = exitAppCallback
        TrayIcon.setup()
        definePromptWindow(invokeCommand)
        defineMessageWindow()
        defineInputWindow()
    }

    @Composable
    fun definePromptWindow(invokeCommand: (CommandName) -> Unit) {
        var isOpen by remember { promptWindowOpen }
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

    @Composable
    fun defineMessageWindow() {
        var isOpen by remember { messageWindowOpen }
        val title by remember { messageWindowTitle }
        val windowSize by remember { messageWindowSize }
        val message by remember { messageWindowMessage }

        if (isOpen) {
            Window(
                onCloseRequest = { isOpen = false },
                icon = AppIcon,
                title = title,
                state = rememberWindowState(size = windowSize, position = WindowPosition.Aligned(Alignment.Center))
            ) {
                layout { Text(message) }
            }
        }
    }

    fun message(title: String, message: String, width: Int, height: Int) {
        messageWindowTitle.value = title
        messageWindowMessage.value = message
        messageWindowSize.value = WindowSize(width.dp, height.dp)
        messageWindowOpen.value = true
    }

    @Composable
    fun defineInputWindow() {
        var isOpen by remember { inputWindowOpen }
        val prompt by remember { inputWindowPrompt }
        val callback by remember { inputWindowCallback }


        if (isOpen) {
            Window(
                onCloseRequest = { isOpen = false },
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
                            isOpen = false
                            callback(it)
                        },
                        onEscape = {
                            isOpen = false
                        }
                    )
                }
            }
        }
    }

    fun input(prompt: String?, onResponse: (String) -> Unit) {
        inputWindowPrompt.value = prompt
        inputWindowCallback.value = onResponse
        inputWindowOpen.value = true
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
                    size = WindowSize(promptWindowSize.width, 650.dp),
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
                                        when (it.nativeKeyEvent.keyCode) {
                                            NativeKeyEvent.VK_ENTER -> {
                                                isOpen = false
                                                startInThread {
                                                    optionItems.firstOrNull()?.handler?.invoke()
                                                }
                                                true
                                            }
                                            NativeKeyEvent.VK_ESCAPE -> {
                                                isOpen = false
                                                true
                                            }
                                            else -> false
                                        }
                                        false // TODO: wyczaić o co tu chodzi z tymi nieużywanymi
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
                    when(key.nativeKeyEvent.keyCode) {
                        NativeKeyEvent.VK_ENTER -> {
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

    private fun startInThread(handler: () -> Unit) {
        Thread {
            try {
                handler()
            } catch(ex: Throwable) {
                TrayIcon.trayState.sendNotification(
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

    object TrayIcon {
        lateinit var trayState: TrayState
        @Composable
        fun setup() {
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
}

data class SelectOption(
    val name: String,
    val description: String?,
    val handler: () -> Unit)