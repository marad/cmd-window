# Cmd Window

> Never spend 6 minutes doing something by hand when you can spend 6 hours failing to automate it - [Zhuowej Zhang](https://twitter.com/zhuowei/status/1254266079532154880)

## What is this about?
This tool will make you a computer ninja. It can automate everything you do on your PC. You can:

- launch any program
- open any predefined folder
- automate mouse and keyboard input (and much more) with [AutoHotkey](https://www.autohotkey.com/)
- automate browser with [Selenium](https://www.selenium.dev/)

You have to be a bit of a ninja already since you have to know something about Kotlin to use it. Not much but still... 
For now there is also a possibility to use Clojure as a scripting language if this is your jam.

## How does this work?
You press Ctrl+Space and small input window appears on your screen. Type in the command, press enter and your code will
magically do stuff.

Where do commands come from? They come from *you*! You write them in kotlin scripts. You can make them as simple as 
just running simple Autohotkey command like `api.ahk("Run notepad")"` or as complicated as a whole program. Choice
is yours.

## Configuration
After you build and install (instructions below). It's just a matter of creating a configuration file and your `Bootstrap.kts` file.

The configuration file should be located in: `C:\Users\YOUR_USER_NAME\AppData\Roaming\cmd-window\config.properties`. And 
its contents should be very simple:

```
scriptsDir=C:/path/to/your/scripts
```

The `scriptsDir` should point to a place that contains `Bootstrap.kts` - this script will be executed after starting cmd-window.

Within the script there is an `api` variable that contains the reference to `[ScriptApi](https://github.com/marad/cmd-window/blob/main/src/main/kotlin/gh/marad/cmdwindow/ScriptApi.kt)`
class that serves as a bridge between the scripts and the hosts. It's API will probably change in the future, but it is 
what it is for now.

## How do I use it?

Just edit the `Bootstrap.kts` you've set up in the previous section. Then invoke the `r` command to reload the script.

Simple 'hello world' command look like this:

```kotlin
api.registerCommand("hello", "this shows hello") {
    api.guiMessage("Hello world!", "Hello!")
}
```

You could also open some folders with AutoHotkey:

```kotlin
api.registerCommand("down", "Open downloads folder") {
    api.ahk("run C:\\Users\\%A_Username%\\Downloads")
}

api.registerCommand("rec", "Open recycle bin") {
    api.ahk("run ::{645FF040-5081-101B-9F08-00AA002F954E}")
}
```

or even make a simple command to insert your email since it's too much writing, right?

```kotlin
api.registerCommand("@", "insert email address") {
    api.ahk("Send your@email.com")
}
```

## Built-in commands
Right now there are just a few:
- `r` reloads the `Bootstrap.kts` file
- `?` shows a window that lists all available commands
- `exit` will exit cmd-window app (how unexpected)

## Building and installing

In `build.gradle.kts` there is a task `installDist`. You'll probably want to edit the destination path where you want 
cmd-window to be installed on your disk. You can place it wherever you want.

## Additional resources
- [This](https://xkcd.com/1205/) will help you judge if something is worth automating at all ;)

## I'd like to help!
Great! If you spot some bugs or have an idea just post an [issue](https://github.com/marad/cmd-window/issues/new) 
You can also submit a [pull request](https://github.com/marad/cmd-window/compare).
