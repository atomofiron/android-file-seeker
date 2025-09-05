package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.common.util.extension.withMain
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.service.ApkService
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.utils.Rslt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkInteractor(
    private val scope: CoroutineScope,
    private val apkService: ApkService,
    private val explorerService: ExplorerService,
    private val apkChannel: ApkChannel,
) {
    fun install(item: Node, tab: NodeTabKey? = null) {
        val content = item.content as? AndroidApp
        content ?: return
        install(content, tab)
    }

    fun install(content: AndroidApp, tab: NodeTabKey? = null) {
        scope.launch(Dispatchers.IO) {
            if (tab != null) {
                val allowed = explorerService.tryMarkInstalling(tab, content.ref, NodeOperation.Installing)
                if (allowed != true) return@launch
                explorerService.tryMarkInstalling(tab, content.ref, installing = null)
            }
            val result = apkService.install(content, Intents.ACTION_INSTALL_APP)
            if (result is Rslt.Err) {
                withMain {
                    apkChannel.errorMessage(result.message)
                }
            }
        }
    }

    fun launchable(item: Node): Boolean {
        val info = (item.content as? AndroidApp)?.info
        return launchable(info)
    }

    fun launchable(info: ApkInfo?): Boolean {
        info ?: return false
        return apkService.launchable(info.packageName)
    }

    fun launch(item: Node) {
        val content = item.content as? AndroidApp
        launch(content?.info ?: return)
    }

    fun launch(info: ApkInfo?) {
        info ?: return
        apkService.launchApk(info.packageName)
    }
}