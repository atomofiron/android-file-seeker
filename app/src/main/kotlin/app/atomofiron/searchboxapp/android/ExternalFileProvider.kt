package app.atomofiron.searchboxapp.android

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Rslt
import uniffi.native_lib.ReadResult
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.LazyThreadSafetyMode.NONE

private const val HMAC256 = "HmacSHA256"
private const val TOKEN = "token"
private const val AS_SU = "as_su"

private val Hasher by lazy(NONE) {
    Mac.getInstance(HMAC256).apply {
        val sessionKey = SecureRandom().generateSeed(32)
        init(SecretKeySpec(sessionKey, HMAC256))
    }
}

fun File.getUriForExternalFile(asSu: Boolean): Uri {
    val token = Hasher.doFinal(absolutePath.toByteArray())
        .joinToString("") { Const.HEX_BYTE.format(it) }
    return Uri.Builder()
        .scheme(Const.SCHEME_CONTENT)
        .authority(BuildConfig.EXTERNAL_AUTHORITY)
        .path(absolutePath)
        .appendQueryParameter(TOKEN, token)
        .appendQueryParameter(AS_SU, asSu.toString())
        .build()
}

class ExternalFileProvider : ContentProvider() {

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val path = uri.path
        path ?: throw IllegalArgumentException("path = null")
        uri.verify(context)
        return when {
            uri.asSu() -> readFileAsSu(path)
            else -> ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
        }
    }

    private fun readFileAsSu(path: String): ParcelFileDescriptor? {
        val (readFd, writeFd) = ParcelFileDescriptor.createPipe()
        val result = NativeBridge.readFile(NodeRef(path), asSu = true)
        val reader = when (result) {
            is Rslt.Ok -> result.value
            is Rslt.Err -> throw Exception("Open file error: ${result.message}")
        }
        Thread {
            FileOutputStream(writeFd.fileDescriptor).use { out ->
                while (true) when (val result = reader.next()) {
                    is ReadResult.Ok -> out.write(result.v1)
                    is ReadResult.End -> {
                        out.flush()
                        writeFd.close()
                        break
                    }
                    is ReadResult.Err -> {
                        out.flush()
                        writeFd.close()
                        throw Exception("Read file error: ${result.v1}")
                    }
                }
            }
        }.start()
        return readFd
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        projection ?: return null
        val path = uri.path ?: return null
        uri.verify(context)
        val ref = NodeRef(path)
        val metadata = projection.mapNotNull { name ->
            when (name) {
                OpenableColumns.DISPLAY_NAME -> name to ref.name
                OpenableColumns.SIZE -> File(path)
                    .takeIf { it.canRead() }
                    .let { it?.length() ?: NativeBridge.meta(ref, asSu = uri.asSu()).value?.length?.toLong() }
                    .takeIf { it != null }
                    ?.let { name to it }
                else -> null
            }
        }
        val cursor = MatrixCursor(metadata.map { (name, _) -> name }.toTypedArray())
        cursor.addRow(metadata.map { (_, value) -> value })
        return cursor
    }

    override fun getType(uri: Uri): String? = Const.MIME_TYPE_ANY

    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? = null

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int = 0

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int = 0
}

private fun Uri.asSu() = getBooleanQueryParameter(AS_SU, false)

private fun Uri.verify(context: Context?) {
    val path = path
    path ?: return debugFail { "why" }
    val tokenGot = getQueryParameter(TOKEN)
    val token = Hasher.doFinal(path.toByteArray())
        .joinToString("") { Const.HEX_BYTE.format(it) }
    if (tokenGot != token) {
        throw SecurityException(context?.getString(R.string.file_access_expired).toString())
    }
}
