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
import app.atomofiron.searchboxapp.model.preference.ActionApk
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.common.util.Android

private val rootOptions = listOf(R.id.menu_create)
private val directoryOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_create, R.id.menu_clone, R.id.menu_copy_path)
private val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with, R.id.menu_clone, R.id.menu_copy_path)
private val manyFilesOptions = listOf(R.id.menu_delete)

class FileOperationsDelegate(
    private val preferences: PreferenceStore,
    private val apks: ApkInteractor,
    private val dialogs: DialogMaker,
) {
    private val itemComposition by preferences.explorerItemComposition

    fun operations(items: List<Node>, supported: List<Int>? = null): ExplorerItemOptions? {
        val merged = items.merge()
        val checked = mutableListOf<Int>()
        val disabled = mutableListOf<Int>()
        val first = merged.firstOrNull() ?: return null
        val ids = when {
            merged.size > 1 -> manyFilesOptions
            first.isRoot -> rootOptions
            first.isDirectory -> directoryOptions
            else -> oneFileOptions.mutate {
                if (first.content is File.Apk) {
                    when (preferences.actionApk.value) {
                        ActionApk.Ask -> Unit
                        ActionApk.Launch -> checked.add(R.id.menu_launch)
                        ActionApk.Install -> checked.add(R.id.menu_install)
                    }
                    add(R.id.menu_apk)
                    add(R.id.menu_launch)
                    add(R.id.menu_install)
                    if (!apks.launchable(first)) {
                        disabled.add(R.id.menu_launch)
                    }
                }
            }
        }.filter { supported?.contains(it) != false }
        return ExplorerItemOptions(ids, merged, itemComposition, checked = checked, disabled = disabled)
    }

    fun processApk(item: Node, tab: NodeTabKey? = null) {
        when (preferences.actionApk.value) {
            ActionApk.Ask -> if (apks.launchable(item)) askAboutApk(item, tab) else apks.install(item, tab)
            ActionApk.Install -> apks.install(item, tab)
            ActionApk.Launch -> if (apks.launchable(item)) apks.launch(item) else apks.install(item, tab)
        }
    }

    private fun askAboutApk(item: Node, tab: NodeTabKey?) {
        val content = item.content as? File.Apk
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
            withCheckbox = DialogMaker.CheckBox.RememberMyChoice,
            negative = UniText(R.string.cancel) to { },
            positive = UniText(R.string.install),
            onPositiveClick = { checked ->
                if (checked) preferences { setActionApk(ActionApk.Install) }
                apks.install(item, tab)
            },
            neutral = UniText(R.string.launch) to { checked ->
                if (checked) preferences { setActionApk(ActionApk.Launch) }
                apks.launch(content)
            },
        )
    }
}