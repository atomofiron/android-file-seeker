package app.atomofiron.searchboxapp.injectable.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.human
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.launch
import app.atomofiron.searchboxapp.utils.launchable
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ApkService(
    private val context: Context,
    private val installer: PackageInstaller,
) {
    fun installApks(zip: File, action: String? = null): Rslt<Unit> {
        return ZipInputStream(BufferedInputStream(FileInputStream(zip))).use { stream ->
            install(zip.length(), action) {
                var entry: ZipEntry? = stream.nextEntry
                    ?: return@install Rslt.Err("archive is empty")
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(Const.DOT_APK, ignoreCase = true)) {
                        openWrite(entry.name, 0, entry.size).use { output ->
                            stream.copyTo(output)
                            fsync(output)
                        }
                    }
                    entry = stream.nextEntry
                }
                return@install Rslt.Ok()
            }
        }
    }

    fun installApk(file: File, action: String? = null, silently: Boolean = false): Rslt<Unit> {
        return install(file.length(), action, silently) {
            openWrite(file.path, 0, file.length()).use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
                fsync(output)
            }
            return@install Rslt.Ok()
        }
    }

    private fun install(
        length: Long,
        action: String? = null,
        silently: Boolean = false,
        block: PackageInstaller.Session.() -> Rslt<Unit>,
    ) = try {
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (silently) {
            params.setAppPackageName(context.packageName)
            params.setSize(length)
            if (Android.S) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            if (Android.T) params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.block()
            val intent = Intents.installing(context)
            intent.action = action
            intent.setPackage(context.packageName)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            try {
                session.commit(pendingIntent.intentSender)
                Rslt.Ok()
            } catch (e: Exception) {
                Rslt.Err(e.human())
            }
        }
    } catch (e: Exception) {
        Rslt.Err(e.human())
    }

    fun launchable(packageName: String): Boolean = context.packageManager.launchable(packageName)

    fun launchApk(packageName: String) = context.launch(packageName)
}