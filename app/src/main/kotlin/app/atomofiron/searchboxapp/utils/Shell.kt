package app.atomofiron.searchboxapp.utils

import app.atomofiron.searchboxapp.logE
import java.io.InputStream
import java.io.OutputStream


object Shell {
    private const val SU = "su"
    private const val SH = "sh"

    private const val TOYBOX = "{toybox}"
    var toyboxPath = Const.DEFAULT_TOYBOX_PATH

    const val TOUCH = "{toybox} touch \"%s\""
    const val COPY = "{toybox} cp -r \"%s\" \"%s\""
    const val VERSION = "{toybox} --version"
    const val MKDIR = "{toybox} mkdir \"%s\""
    const val RM_RF = "{toybox} rm -rf \"%s\""
    const val MV = "{toybox} mv \"%s\" \"%s\""
    const val LS_LAHL = "{toybox} ls -lAhL \"%s\""
    const val LS_LAHLD = "{toybox} ls -lAhLd \"%s\""
    const val FILE_B = "{toybox} file -b \"%s\""

    // grep: No 'E' with 'F'
    const val FIND_GREP_HCS = "{toybox} find \"%s\" -type f -maxdepth %d | xargs {toybox} grep -Hcs -e \"%s\""
    const val FIND_GREP_HCS_I = "{toybox} find \"%s\" -type f -maxdepth %d | xargs {toybox} grep -Hcs -ie \"%s\""
    const val FIND_GREP_HCS_E = "{toybox} find \"%s\" -type f -maxdepth %d | xargs {toybox} grep -Hcs -E \"%s\""
    const val FIND_GREP_HCS_IE = "{toybox} find \"%s\" -type f -maxdepth %d | xargs {toybox} grep -Hcs -iE \"%s\""
    // /storage/emulated/0/fadb/sba.txt:15

    // -H is necessary
    const val GREP_HCS = "{toybox} grep -Hcs -e \"%s\" \"%s\""
    const val GREP_HCS_I = "{toybox} grep -Hcs -ie \"%s\" \"%s\""
    const val GREP_HCS_E = "{toybox} grep -Hcs -E \"%s\" \"%s\""
    const val GREP_HCS_IE = "{toybox} grep -Hcs -iE \"%s\" \"%s\""
    // /storage/emulated/0/fadb/sba.txt:15

    // \( %s \) -exec {toybox} grep -H -c -s -E "work" {} \;"
    const val FIND_EXEC_GREP = "{toybox} find \"%s\" -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -Hcs -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_I = "{toybox} find \"%s\" -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -Hcs -iE \"%s\" {} \\;"
    const val FIND_EXEC_GREP_E = "{toybox} find \"%s\" -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -Hcs -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_IE = "{toybox} find \"%s\" -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -Hcs -iE \"%s\" {} \\;"

    const val FIND_FD = "{toybox} find \"%s\" -maxdepth %d \\( -type f -o -type d \\)"
    const val FIND_F = "{toybox} find \"%s\" -maxdepth %d -type f"

    const val HEAD_TAIL = "{toybox} head \"%s\" -n %d | {toybox} tail -n %d"
    const val LS_LOG = "{toybox} ls -log \"%s\""
    const val CP_F = "{toybox} cp -f %s %s"

    const val GREP_BONS = "{toybox} grep -bons -e \"%s\" \"%s\""
    const val GREP_BONS_I = "{toybox} grep -bons -ie \"%s\" \"%s\""
    const val GREP_BONS_E = "{toybox} grep -bons -E \"%s\" \"%s\""
    const val GREP_BONS_IE = "{toybox} grep -bons -iE \"%s\" \"%s\""
    // 241:6916:work

    // %s grep -c -s -F -i -e "%s" "%s"

    // FASTEST toybox find %s -name "*.%s" -type f | xargs grep "%s" -c
    // find . -maxdepth 2 -exec grep -H -c -s "k[e]" {} \;

    private val oneByteNbps = String(byteArrayOf(0xA0.toByte()), Charsets.UTF_8)
    private const val twoBytesNbps = "\u00A0"

    operator fun get(template: String, toyboxPath: String = Shell.toyboxPath): String = template.replace(TOYBOX, toyboxPath)

    fun checkSu(): Output {
        var code = -1
        var error = ""
        var process: Process? = null
        var outputStream: OutputStream? = null
        var errorStream: InputStream? = null

        try {
            process = Runtime.getRuntime().exec(SU)
            outputStream = process.outputStream
            errorStream = process.errorStream
            val osw = outputStream.writer()

            osw.write(SU)
            osw.flush()
            osw.close()

            code = process.waitFor()
            error = errorStream.reader().readText()
        } catch (e: Exception) {
            error = e.message ?: e.toString()
        } finally {
            try {
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) { }
        }
        return Output(code, "", error)
    }

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

            osw.write(String.format("%s\n", command))
            osw.flush()
            osw.close()

            val reader = inputStream.reader()
            when (forEachLine) {
                null -> output = reader.readText().replace(oneByteNbps, twoBytesNbps)
                else -> reader.forEachLine(forEachLine)
            }
            error = errorStream.reader().readText()
            code = process.waitFor()
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
        return Output(code, output, error)
    }

    class Output(
        val code: Int,
        val output: String,
        val error: String,
    ) {
        val success: Boolean = code == 0
    }
}