package app.atomofiron.searchboxapp.utils

import app.atomofiron.common.util.extension.logE
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream


object Shell {
    private const val SU = "su"
    private const val SH = "sh"

    private const val TOYBOX = "{toybox}"
    var toyboxPath = ToyboxVariant.System.path

    const val VERSION = "{toybox} --version"

    private val oneByteNbps = String(byteArrayOf(0xA0.toByte()), Charsets.UTF_8)
    private const val twoBytesNbps = "\u00A0"

    operator fun get(template: String, toyboxPath: String = Shell.toyboxPath): String = template.replace(TOYBOX, toyboxPath)

    fun exec(command: String, su: Boolean, processObserver: ((Process) -> Unit)? = null, forEachLine: ((String) -> Unit)? = null): Output {
        var code = -1
        var output = ""
        var error: String

        var process: Process? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var errorStream: InputStream? = null

        try {
            process = Runtime.getRuntime().exec(if (su) SU else SH)
            processObserver?.invoke(process)
            inputStream = process.inputStream!!
            outputStream = process.outputStream
            errorStream = process.errorStream!!
            val osw = outputStream.writer()

            osw.write(command)
            osw.write("\n")
            osw.flush()
            osw.close()

            val reader = inputStream.reader()
            when (forEachLine) {
                null -> output = reader.readText().replace(oneByteNbps, twoBytesNbps)
                else -> reader.forEachLine(forEachLine)
            }
            error = errorStream.reader().readText()
            code = process.waitFor()
        } catch (e: InterruptedIOException) {
            // process was destroyed, stopped by user
            error = ""
        } catch (e: Exception) {
            logE(e.toString())
            e.printStackTrace()
            error = e.toString()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                logE(e.toString())
            }
        }
        return Output(code, output.trim(), error.trim())
    }

    data class Output(
        val code: Int,
        val output: String,
        val error: String,
    ) {
        val success: Boolean = code == 0
        val killed: Boolean = code == 137 // 1-byte -9?
    }
}

/*
/system/bin/device_config get activity_manager max_phantom_processes
settings get global settings_enable_monitor_phantom_procs

/system/bin/device_config put activity_manager max_phantom_processes 2147483647
settings put global settings_enable_monitor_phantom_procs false
*/
