package app.atomofiron.searchboxapp.injectable.service

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.human
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.utils.Rslt
import java.io.File

class ApkService(
    private val context: Context,
    private val installer: PackageInstaller,
) {
    fun installApk(file: File, action: String? = null, silently: Boolean = false) = try {
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (silently) {
            params.setAppPackageName(context.packageName)
            params.setSize(file.length())
            if (Android.S) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            if (Android.T) params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite(context.packageName, 0, file.length()).use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
                session.fsync(output)
            }
            val intent = Intents.installing(context)
            intent.action = action
            intent.setPackage(context.packageName)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            try {
                session.commit(pendingIntent.intentSender)
                Rslt.Ok(Unit)
            } catch (e: Exception) {
                Rslt.Err(e.human())
            }
        }
    } catch (e: Exception) {
        Rslt.Err(e.human())
    }

    fun launchable(packageName: String): Boolean {
        return context.packageManager.getLaunchIntentForPackage(packageName) != null
    }

    fun launchApk(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent ?: return
        context.startActivity(launchIntent)
    }
}