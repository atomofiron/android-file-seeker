package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.android.getUriForExternalFile
import java.io.File

fun Context.getUriForFile(file: File, asSu: Boolean): Uri = try {
    FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, file)
} catch (_: IllegalArgumentException) {
    file.getUriForExternalFile(asSu)
}
