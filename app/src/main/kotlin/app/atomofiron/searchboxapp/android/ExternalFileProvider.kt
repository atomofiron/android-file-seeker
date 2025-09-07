package app.atomofiron.searchboxapp.android

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Const
import java.io.File
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.LazyThreadSafetyMode.NONE

private const val HMAC256 = "HmacSHA256"
private const val TOKEN = "token"

private val Hasher by lazy(NONE) {
    Mac.getInstance(HMAC256).apply {
        val sessionKey = SecureRandom().generateSeed(32)
        init(SecretKeySpec(sessionKey, HMAC256))
    }
}

fun File.getUriForExternalFile(): Uri {
    val token = Hasher.doFinal(absolutePath.toByteArray())
        .joinToString("") { Const.HEX_BYTE.format(it) }
    return Uri.Builder()
        .scheme(Const.SCHEME_CONTENT)
        .authority(BuildConfig.EXTERNAL_AUTHORITY)
        .path(absolutePath)
        .appendQueryParameter(TOKEN, token)
        .build()
}

class ExternalFileProvider : ContentProvider() {

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val path = uri.path ?: return null
        val tokenGot = uri.getQueryParameter(TOKEN)
        val token = Hasher.doFinal(path.toByteArray())
            .joinToString("") { Const.HEX_BYTE.format(it) }
        val file = File(path)
        when {
            tokenGot != token -> SecurityException(context?.getString(R.string.file_access_expired).toString())
            else -> return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        }
        return null
    }

    override fun getType(uri: Uri): String? = "*/*"

    override fun onCreate(): Boolean = true

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings2: Array<String>?, s2: String?): Cursor? = null

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? = null

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int = 0

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int = 0
}