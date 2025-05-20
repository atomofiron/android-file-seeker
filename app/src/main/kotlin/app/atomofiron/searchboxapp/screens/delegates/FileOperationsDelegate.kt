package app.atomofiron.searchboxapp.screens.delegates

import app.atomofiron.common.util.DialogMaker
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.common.util.Android

private val rootOptions = listOf(R.id.menu_create)
private val directoryOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_create, R.id.menu_clone, R.id.menu_copy_path)
private val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with, R.id.menu_clone, R.id.menu_copy_path)
private val manyFilesOptions = listOf(R.id.menu_delete)

class FileOperationsDelegate(
    preferences: PreferenceStore,
    private val apks: ApkInteractor,
    private val dialogs: DialogMaker,
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

    fun processApk(item: Node, tab: NodeTabKey? = null) {
        if (apks.launchable(item)) askAboutApk(item, tab) else apks.install(item, tab)
    }

    private fun askAboutApk(item: Node, tab: NodeTabKey?) {
        val content = item.content as? File.AndroidApp
        val info = content?.info ?: return
        val unavailable = dialogs[UniText(R.string.unavailable)]
        val args = arrayOf(
            info.appName,
            info.packageName,
            info.versionName,
            info.versionCode,
            "${info.minSdkVersion} (${Android[info.minSdkVersion] ?: unavailable})",
            "${info.targetSdkVersion} (${Android[info.targetSdkVersion] ?: unavailable})",
            info.compileSdkVersion
                ?.let { "$it (${Android[it] ?: unavailable})" }
                ?: unavailable,
            info.signature
                ?.let { "${it.issuerName} (v${it.version})" }
                ?: unavailable,
        )
        dialogs.show(
            icon = content.thumbnail?.drawable,
            title = UniText(item.name),
            message = UniText(R.string.apk_info, *args),
            negative = DialogMaker.Cancel,
            positive = UniText(R.string.install),
            onPositiveClick = { apks.install(item, tab) },
            neutral = UniText(R.string.launch) to { apks.launch(content) },
            cancelable = true,
        )
    }
}