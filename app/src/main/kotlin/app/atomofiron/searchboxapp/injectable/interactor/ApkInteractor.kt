package app.atomofiron.searchboxapp.injectable.interactor

import android.content.Context
import androidx.core.content.FileProvider
import app.atomofiron.searchboxapp.BuildConfig
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
    private val context: Context,
    private val scope: CoroutineScope,
    private val apkService: ApkService,
    private val explorerService: ExplorerService,
) {
    fun install(tab: NodeTabKey, item: Node) {
        scope.launch(Dispatchers.IO) {
            val allowed = explorerService.tryMarkInstalling(tab, item, Operation.Installing)
            if (allowed != true) return@launch
            val file = File(item.path)
            val uri = FileProvider.getUriForFile(context, BuildConfig.AUTHORITY, file)
            apkService.installApk(uri)
            explorerService.tryMarkInstalling(tab, item, installing = null)
        }
    }

    fun launch(item: Node) {
        val info = (item.content as? NodeContent.File.Apk)?.info
        info ?: return
        apkService.launchApk(info.packageName)
    }
}