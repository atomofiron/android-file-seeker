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

    const val COPY = "{toybox} cp -r '%s' '%s'"
    const val VERSION = "{toybox} --version"
    const val MV = "{toybox} mv '%s' '%s'"

    // grep: No 'E' with 'F'
    const val FIND_GREP_HCS = "{toybox} find '%s' -type f -maxdepth %d -print0 | xargs -0r {toybox} file | {toybox} grep -E 'ASCII text$' | awk -F: -v ORS='\\0' '{print \$1}' | xargs -0r {toybox} grep -Hcs -e '%s'"
    const val FIND_GREP_HCS_I = "{toybox} find '%s' -type f -maxdepth %d -print0 | xargs -0r {toybox} file | {toybox} grep -E 'ASCII text$' | awk -F: -v ORS='\\0' '{print \$1}' | xargs -0r {toybox} grep -Hcs -ie '%s'"
    const val FIND_GREP_HCS_E = "{toybox} find '%s' -type f -maxdepth %d -print0 | xargs -0r {toybox} file | {toybox} grep -E 'ASCII text$' | awk -F: -v ORS='\\0' '{print \$1}' | xargs -0r {toybox} grep -Hcs -E '%s'"
    const val FIND_GREP_HCS_IE = "{toybox} find '%s' -type f -maxdepth %d -print0 | xargs -0r {toybox} file | {toybox} grep -E 'ASCII text$' | awk -F: -v ORS='\\0' '{print \$1}' | xargs -0r {toybox} grep -Hcs -iE '%s'"
    // /storage/emulated/0/fadb/sba.txt:15

    // -H is necessary
    const val GREP_HCS = "{toybox} grep -Hcs -e '%s' '%s'"
    const val GREP_HCS_I = "{toybox} grep -Hcs -ie '%s' '%s'"
    const val GREP_HCS_E = "{toybox} grep -Hcs -E '%s' '%s'"
    const val GREP_HCS_IE = "{toybox} grep -Hcs -iE '%s' '%s'"
    // /storage/emulated/0/fadb/sba.txt:15

    const val FIND_FD = "{toybox} find '%s' -maxdepth %d \\( -type f -o -type d \\)"
    const val FIND_F = "{toybox} find '%s' -maxdepth %d -type f"

    const val GREP_BONS = "{toybox} grep -bons -e '%s' '%s'"
    const val GREP_BONS_I = "{toybox} grep -bons -ie '%s' '%s'"
    const val GREP_BONS_E = "{toybox} grep -bons -E '%s' '%s'"
    const val GREP_BONS_IE = "{toybox} grep -bons -iE '%s' '%s'"
    // 241:6916:work

    // %s grep -c -s -F -i -e "%s" "%s"

    // FASTEST toybox find %s -name "*.%s" -type f | xargs grep "%s" -c
    // find . -maxdepth 2 -exec grep -H -c -s "k[e]" {} \;

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
