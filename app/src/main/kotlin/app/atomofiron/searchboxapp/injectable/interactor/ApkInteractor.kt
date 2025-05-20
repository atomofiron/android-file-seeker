package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.common.util.DialogMaker
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.Operation
import app.atomofiron.searchboxapp.utils.Rslt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ApkInteractor(
    private val scope: CoroutineScope,
    private val apkService: ApkService,
    private val explorerService: ExplorerService,
    private val dialogs: DialogMaker,
) {
    fun install(item: Node, tab: NodeTabKey? = null) {
        val content = item.content as? NodeContent.File.AndroidApp
        content?: return
        val file = File(item.path)
        scope.launch(Dispatchers.IO) {
            if (tab != null) {
                val allowed = explorerService.tryMarkInstalling(tab, item, Operation.Installing)
                if (allowed != true) return@launch
                explorerService.tryMarkInstalling(tab, item, installing = null)
            }
            val result = when {
                content.splitApk -> apkService.installApks(file, action = Intents.ACTION_INSTALL_APP)
                else -> apkService.installApk(file, action = Intents.ACTION_INSTALL_APP)
            }
            if (result is Rslt.Err) {
                withMain {
                    dialogs.showError(result.error)
                }
            }
        }
    }

    fun launchable(item: Node): Boolean {
        val info = (item.content as? NodeContent.File.AndroidApp)?.info
        info ?: return false
        return apkService.launchable(info.packageName)
    }

    fun launch(item: Node) {
        val content = item.content as? NodeContent.File.AndroidApp
        launch(content ?: return)
    }

    fun launch(content: NodeContent.File.AndroidApp) {
        val info = content.info ?: return
        apkService.launchApk(info.packageName)
    }
}