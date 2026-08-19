package app.atomofiron.searchboxapp.di.dependencies.delegate

import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.service.ApkService
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.utils.Rslt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkDelegate @Inject constructor(
    private val scope: AppScope,
    private val apkService: ApkService,
    private val explorerService: ExplorerService,
    private val apkChannel: ApkChannel,
) {
    fun install(item: Node, tab: NodeTabKey? = null) {
        val content = item.content as? AndroidApp
        content ?: return
        install(item.ref, content, tab)
    }

    fun install(ref: NodeRef, content: AndroidApp, tab: NodeTabKey? = null) {
        scope.launchOnIO {
            if (tab != null) {
                val allowed = explorerService.tryMarkInstalling(tab, ref, NodeOperation.Installing)
                if (!allowed) return@launchOnIO
            }
            val result = apkService.install(ref, content, Intents.ACTION_INSTALL_APP)
            if (result is Rslt.Err) {
                withMain {
                    apkChannel.errorMessage(result.message)
                }
            }
            if (tab != null) {
                explorerService.tryMarkInstalling(tab, ref, installing = null)
            }
        }
    }

    fun launchable(item: Node): Boolean {
        val info = (item.content as? AndroidApp)?.info
        return launchable(info)
    }

    fun launchable(info: ApkInfo?): Boolean {
        info ?: return false
        return launchable(info.packageName)
    }

    fun launchable(packageName: String): Boolean = apkService.launchable(packageName)

    fun launch(item: Node) {
        val content = item.content as? AndroidApp
        launch(content?.info ?: return)
    }

    fun launch(info: ApkInfo?) {
        info ?: return
        apkService.launchApk(info.packageName)
    }
}