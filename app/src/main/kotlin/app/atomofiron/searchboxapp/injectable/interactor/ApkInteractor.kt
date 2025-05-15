package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.Operation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ApkInteractor(
    private val scope: CoroutineScope,
    private val apkService: ApkService,
    private val explorerService: ExplorerService,
) {
    fun install(item: Node, tab: NodeTabKey? = null) {
        if (tab == null) {
            apkService.installApk(File(item.path))
            return
        }
        scope.launch(Dispatchers.IO) {
            val allowed = explorerService.tryMarkInstalling(tab, item, Operation.Installing)
            if (allowed != true) return@launch
            apkService.installApk(File(item.path))
            explorerService.tryMarkInstalling(tab, item, installing = null)
        }
    }

    fun launchable(item: Node): Boolean {
        val info = (item.content as? NodeContent.File.Apk)?.info
        info ?: return false
        return apkService.launchable(info.packageName)
    }

    fun launch(item: Node) {
        val content = item.content as? NodeContent.File.Apk
        launch(content ?: return)
    }

    fun launch(content: NodeContent.File.Apk) {
        val info = content.info ?: return
        apkService.launchApk(info.packageName)
    }
}