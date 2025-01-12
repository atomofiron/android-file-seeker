package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ActionApk
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.utils.mutate

class ExplorerItemActionListenerDelegate(
    private val viewState: ExplorerViewState,
    private val menuListenerDelegate: ExplorerCurtainMenuDelegate,
    private val explorerStore: ExplorerStore,
    private val router: ExplorerRouter,
    private val explorerInteractor: ExplorerInteractor,
    private val apks: ApkInteractor,
    private val dialogs: DialogInteractor,
    private val preferences: PreferenceStore,
) : ExplorerItemActionListener {

    private val currentTab get() = viewState.currentTab.value

    override fun onItemLongClick(item: Node) {
        val files: List<Node> = when {
            item.isChecked -> explorerStore.searchTargets.value
            else -> listOf(item)
        }
        val checked = mutableListOf<Int>()
        val disabled = mutableListOf<Int>()
        val ids = when {
            files.size > 1 -> viewState.manyFilesOptions
            files.first().isRoot -> viewState.rootOptions
            files.first().isDirectory -> viewState.directoryOptions
            else -> viewState.oneFileOptions.mutate {
                if (item.content is File.Apk) {
                    when (preferences.actionApk.value) {
                        ActionApk.Ask -> Unit
                        ActionApk.Launch -> checked.add(R.id.menu_launch)
                        ActionApk.Install -> checked.add(R.id.menu_install)
                    }
                    add(R.id.menu_apk)
                    add(R.id.menu_launch)
                    add(R.id.menu_install)
                    if (!apks.launchable(item)) {
                        disabled.add(R.id.menu_launch)
                    }
                }
            }
        }
        val options = ExplorerItemOptions(ids, files, viewState.itemComposition.value, checked = checked, disabled = disabled)
        menuListenerDelegate.showOptions(options)
    }

    override fun onItemClick(item: Node) = openItem(item)

    private fun openItem(item: Node) {
        when {
            item.isDirectory -> explorerInteractor.toggleDir(currentTab, item)
            item.content is File.Apk -> processApk(item)
            item.isFile -> router.showFile(item)
        }
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) = explorerInteractor.checkItem(currentTab, item, isChecked)

    override fun onItemsBecomeVisible(items: List<Node>) {
        items.forEach {
            explorerInteractor.updateItem(currentTab, it)
        }
    }

    private fun processApk(item: Node) {
        when (preferences.actionApk.value) {
            ActionApk.Ask -> if (apks.launchable(item)) askAboutApk(item) else apks.install(currentTab, item)
            ActionApk.Install -> apks.install(currentTab, item)
            ActionApk.Launch -> apks.launch(item)
        }
    }

    private fun askAboutApk(item: Node) {
        dialogs.builder()
            .setTitle(item.name)
            .setMessageWithOfferDontAskAnymore(true)
            .setPositiveButton(R.string.launch) { dontAskAnymore ->
                if (dontAskAnymore) preferences { setActionApk(ActionApk.Launch) }
                apks.launch(item)
            }.setNegativeButton(R.string.install) { dontAskAnymore ->
                if (dontAskAnymore) preferences { setActionApk(ActionApk.Install) }
                apks.install(currentTab, item)
            }.setNeutralButton(R.string.cancel)
            .show()
    }
}