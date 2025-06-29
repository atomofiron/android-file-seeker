package app.atomofiron.searchboxapp.screens.delegates

import android.content.ContentResolver
import androidx.core.net.toUri
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.dialog.DialogConfig
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.extension.then
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.debugDelay
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.getApksContent
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.unwrapOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream

private val rootOptions = listOf(R.id.menu_create)
private val directoryOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_create, R.id.menu_clone, R.id.menu_copy_path)
private val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with, R.id.menu_clone, R.id.menu_copy_path)
private val manyFilesOptions = listOf(R.id.menu_delete)

class FileOperationsDelegate(
    preferences: PreferenceStore,
    private val apks: ApkInteractor,
    private val dialogs: DialogDelegate,
) {
    private val itemComposition by preferences.explorerItemComposition

    fun operations(items: List<Node>, supported: List<Int>? = null): ExplorerItemOptions? {
        val merged = items.merge()
        val disabled = mutableListOf<Int>()
        val first = merged.firstOrNull() ?: return null
        val ids = when {
            merged.size > 1 -> manyFilesOptions
            first.content.rootType?.editable == true -> rootOptions
            first.isRoot -> return null
            first.isDirectory -> directoryOptions
            else -> oneFileOptions.mutate {
                if (first.content is File.AndroidApp) {
                    add(R.id.menu_apk)
                    add(R.id.menu_launch)
                    add(R.id.menu_install)
                    if (!apks.launchable(first)) {
                        disabled.add(R.id.menu_launch)
                    }
                }
            }
        }.filter { supported?.contains(it) != false }
        return ExplorerItemOptions(ids, merged, itemComposition, disabled = disabled)
    }

    fun askForApk(content: File.AndroidApp, tab: NodeTabKey? = null) {
        val info = content.info ?: return
        dialogs show DialogConfig(
            cancelable = true,
            icon = info.icon?.drawable,
            title = UniText(info.appName),
            message = info.toMessage(),
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.install),
            onPositiveClick = { apks.install(content, tab) },
            neutral = apks.launchable(info) then { UniText(R.string.launch) to { apks.launch(info) } },
        )
    }

    fun askForApks(ref: NodeRef, contentResolver: ContentResolver) {
        var scope: CoroutineScope? = null
        var content = File.AndroidApp.apks(ref)
        val updater = dialogs show DialogConfig(
            cancelable = false,
            icon = dialogs.loadingIcon(),
            title = UniText(R.string.fetching),
            message = null.toMessage(),
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.install),
            onPositiveClick = { apks.install(content) },
            onDismiss = { scope?.cancel() }
        )
        updater ?: return dialogs.showError()
        scope = CoroutineScope(Job())
        val showError: suspend (String?) -> Unit = {
            withMain { updater.showError(it) }
        }
        val stream = when {
            ref.isContent -> contentResolver.openInputStream(ref.path.toUri())
            else -> FileInputStream(ref.path)
        }
        val job = scope.launch {
            content = content
                .getApksContent(stream)
                .also { stream?.close() }
                .unwrapOrElse { return@launch showError(it) }
            withMain {
                debugDelay(3)
                val info = content.info!!
                updater.update {
                    copy(
                        cancelable = true,
                        icon = info.icon?.drawable,
                        title = UniText(info.appName),
                        message = info.toMessage(),
                        negative = DialogDelegate.Cancel,
                        positive = UniText(R.string.install),
                        neutral = apks.launchable(info) then { UniText(R.string.launch) to { apks.launch(info) } },
                    )
                }
            }
        }
        scope.launch {
            job.join()
            stream?.close()
            scope.cancel()
        }
    }

    private fun ApkInfo?.toMessage(): UniText {
        val args = if (this == null) {
            val ellipsis = dialogs[UniText(R.string.ellipsis)]
            Array(6) { ellipsis }
        } else {
            val unavailable = dialogs[UniText(R.string.unavailable)]
            arrayOf(
                packageName,
                "$versionName ($versionCode)",
                "$minSdkVersion (${Android[minSdkVersion] ?: unavailable})",
                "$targetSdkVersion (${Android[targetSdkVersion] ?: unavailable})",
                compileSdkVersion
                    ?.let { "$it (${Android[it] ?: unavailable})" }
                    ?: unavailable,
                signature
                    ?.let { "${it.issuerName} (v${it.version})" }
                    ?: unavailable,
            )
        }
        return UniText(R.string.apk_info, *args)
    }
}