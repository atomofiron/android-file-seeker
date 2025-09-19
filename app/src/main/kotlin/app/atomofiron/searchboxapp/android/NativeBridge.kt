package app.atomofiron.searchboxapp.android

object NativeBridge {
    init {
        System.loadLibrary("native_lib")
    }
    external fun run(command: String, args: Array<String>): ByteArray
    external fun runAsync(command: String, args: Array<String>, callback: (ByteArray) -> Unit)
}