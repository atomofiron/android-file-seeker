package app.atomofiron.searchboxapp.screens.delegates

import android.content.ContentResolver
import androidx.core.net.toUri
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.UnreachableException
import app.atomofiron.common.util.dialog.DialogConfig
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.extension.then
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.debugDelay
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.getApksContent
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.getApkContent
import app.atomofiron.searchboxapp.utils.unwrapOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import kotlin.math.abs

private val rootOptions = listOf(R.id.menu_create, R.id.menu_copy_path)
private val directoryOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_create, R.id.menu_clone, R.id.menu_copy_path)
private val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with, R.id.menu_clone, R.id.menu_copy_path)
private val manyFilesOptions = listOf(R.id.menu_delete)
private val readWrite = listOf(R.id.menu_create, R.id.menu_rename, R.id.menu_clone)

class FileOperationsDelegate(
    preferences: PreferenceStore,
    private val apks: ApkInteractor,
    private val dialogs: DialogDelegate,
    private val utils: UtilService,
) {
    private val itemComposition by preferences.explorerItemComposition

    fun operations(items: List<Node>, readOnly: Boolean = false): ExplorerItemOptions? {
        val merged = items.merge()
        val first = merged.firstOrNull() ?: return null
        val ids = when {
            merged.size > 1 -> manyFilesOptions
            first.content.rootType?.editable == true -> rootOptions
            first.isRoot -> return null
            first.isDirectory -> directoryOptions
            else -> oneFileOptions.buildOperations(first)
        }.filter {
            readWrite.takeIf { readOnly }?.contains(abs(it)) != true
        }
        return ExplorerItemOptions(ids, merged, itemComposition)
    }

    fun askForAndroidApp(content: NodeContent.AndroidApp, tab: NodeTabKey? = null) = askForAndroidApp(content, contentResolver = null, tab)

    fun askForApks(ref: NodeRef, contentResolver: ContentResolver) = askForAndroidApp(NodeContent.AndroidApp.apks(ref), contentResolver)

    private fun List<Int>.buildOperations(first: Node): List<Int> = mutate {
        if (first.content is NodeContent.AndroidApp) {
            add(R.id.menu_apk)
            add(R.id.menu_install)
            add(R.id.menu_launch)
            if (!apks.launchable(first)) {
                add(-R.id.menu_launch)
            }
        } else if (utils.canUseAs(first)) {
            add(R.id.menu_use_as)
        }
    }

    private fun askForAndroidApp(
        content: NodeContent.AndroidApp,
        contentResolver: ContentResolver?,
        tab: NodeTabKey? = null,
    ) {
        var content = content
        var scope: CoroutineScope? = null
        val updater = dialogs show DialogConfig(
            cancelable = content.info != null,
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.install),
            onPositiveClick = { apks.install(content, tab) },
            onDismiss = { scope?.cancel() },
        ).update(content)
        updater ?: return
        scope = CoroutineScope(Job())
        val job = scope.launch {
            debugDelay(2)
            val forSignature = content.info != null
            var result = content.resolve(contentResolver, signature = forSignature)
            withMain {
                content = result.unwrapOrElse {
                    if (!forSignature) updater.showError(it)
                    return@withMain
                }
                updater.update { update(content, forSignature) }
            }
            if (forSignature) {
                return@launch
            }
            debugDelay(2)
            result = content.resolve(contentResolver, signature = true)
            withMain {
                content = result.unwrapOrElse {
                    return@withMain
                }
                updater.update { update(content, withSignature = true) }
            }
        }
        scope.launch {
            job.join()
            scope.cancel()
        }
    }

    private fun NodeContent.AndroidApp.resolve(resolver: ContentResolver?, signature: Boolean): Rslt<NodeContent.AndroidApp> {
        val stream = when {
            !splitApk -> return getApkContent(ref.path, signature)
            !ref.isContent -> FileInputStream(ref.path)
            resolver == null -> throw UnreachableException()
            else -> resolver.openInputStream(ref.path.toUri())
        }
        return getApksContent(stream, signature)
    }

    private fun DialogConfig.update(content: NodeContent.AndroidApp, withSignature: Boolean = false): DialogConfig = copy(
        cancelable = content.info != null,
        icon = if (content.info == null) dialogs.loadingIcon() else content.info.icon?.drawable,
        title = UniText(content.info?.appName) ?: UniText(R.string.fetching),
        message = content.info.toMessage(withSignature),
        neutral = apks.launchable(content.info) then { UniText(R.string.launch) to { apks.launch(content.info) } },
    )

    private fun ApkInfo?.toMessage(withSignature: Boolean): UniText {
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
                    ?.let { "v${it.version}\n${it.issuerName}\n${it.hashAlg}: ${it.hash}" }
                    ?: unavailable.takeIf { withSignature }
                    ?: dialogs[UniText(R.string.ellipsis)],
            )
        }
        return UniText(R.string.apk_info, *args)
    }
}