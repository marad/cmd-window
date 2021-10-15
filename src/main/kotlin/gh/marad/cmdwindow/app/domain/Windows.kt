package gh.marad.cmdwindow.app.domain

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.Win32VK
import com.sun.jna.platform.win32.WinUser

object Windows {
    fun registerGlobalHotkeyAndStartWinApiThread(onHotkeyAction: () -> Unit) {
        val events = WindowsEventsThread()
        events.registerHotkey(WinUser.MOD_CONTROL, Win32VK.VK_SPACE.code) {
            onHotkeyAction()
        }
        events.run()
    }

    private class WindowsEventsThread : Thread() {
        private val msg = WinUser.MSG()
        private val user32 = User32.INSTANCE

        private val hotkeyHandlers = mutableListOf<() -> Unit>()

        init {
            isDaemon = true
        }

        fun registerHotkey(modifiers: Int, keyCode: Int, handler: () -> Unit) {
            if (user32.RegisterHotKey(null, hotkeyHandlers.size, modifiers, keyCode)) {
                hotkeyHandlers.add(handler)
            } else {
                val errorCode = Kernel32.INSTANCE.GetLastError()
                throw RuntimeException("Couldn't register hotkey. Windows error code: $errorCode")
            }
        }

        override fun run() {
            while (true) {
                if (user32.GetMessage(msg, null, 0, 0) != 0) {
                    if (msg.message == WinUser.WM_HOTKEY) {
                        val hotkeyId = msg.wParam.toInt()
                        hotkeyHandlers[hotkeyId]()
                    } else {
                        user32.TranslateMessage(msg)
                        user32.DispatchMessage(msg)
                    }
                }
            }
        }
    }

}