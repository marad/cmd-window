package gh.marad.cmdwindow.app.domain

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

// https://lexikos.github.io/v2/docs/AutoHotkey.htm
// https://www.baeldung.com/java-jna-dynamic-libraries
// https://github.com/HotKeyIt/ahkdll/blob/master/source/exports.h
// https://hotkeyit.github.io/v2/docs/commands/ahkFunction.htm

class Ahk {
    interface Autohotkey2Dll : Library {
        fun ahkExec(cmd: CharArray): Int
        fun ahktextdll(script: CharArray)
        fun ahkTerminate(timeout: Int): Int
        fun ahkassign(name: CharArray, value: CharArray): Int
        fun ahkgetvar(name: CharArray, getVar: Int = 0): Pointer
        fun ahkIsUnicode(): Boolean
    }
    val lib = Native.load("AutohotkeyV1", Autohotkey2Dll::class.java)

    init {
        lib.ahktextdll("#Persistent\n#NoTrayIcon".nullTerminatedCharArray())
    }

    fun exec(cmd: String): Int {
        return lib.ahkExec(cmd.nullTerminatedCharArray())
    }

    fun setVar(name: String, value: String): Boolean {
        val result = lib.ahkassign(name.nullTerminatedCharArray(), value.nullTerminatedCharArray())
        return result == 0
    }

    fun getVar(name: String): String {
        return lib.ahkgetvar(name.nullTerminatedCharArray(), 0).getWideString(0)
    }

    fun terminate() {
        lib.ahkTerminate(0)
    }

    fun isUnicode(): Boolean = lib.ahkIsUnicode()

    private fun String.nullTerminatedCharArray(): CharArray
        = "$this\u0000\u0000".toCharArray()
}