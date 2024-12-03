package app.atomofiron.searchboxapp.injectable.service

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.atomofiron.searchboxapp.android.InstallReceiver
import app.atomofiron.searchboxapp.utils.Const

class ApkService(
    private val context: Context,
    private val installer: PackageInstaller,
    private val resolver: ContentResolver,
) {
    fun installApk(uri: Uri) {
        resolver.openInputStream(uri)?.use { apkStream ->
            val length = DocumentFile.fromSingleUri(context, uri)?.length() ?: -1
            if (length < 0) return@use

            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = installer.createSession(params)
            val session = installer.openSession(sessionId)

            session.openWrite("unused", 0, length).use { sessionStream ->
                apkStream.copyTo(sessionStream)
                session.fsync(sessionStream)
            }

            val intent = Intent(context, InstallReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Const.REQUEST_CODE_INSTALL,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )

            session.commit(pendingIntent.intentSender)
            session.close()
        }
    }
}