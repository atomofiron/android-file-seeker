package app.atomofiron.searchboxapp.screens.common.delegates

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.debugFailUnreachable
import app.atomofiron.common.util.extension.takeIfNotEmpty
import app.atomofiron.common.util.extension.unit
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.LongItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.di.dependencies.delegate.ApkDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.ByCopying
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.ByMoving
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Copy
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.CopyPath
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Create
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Delete
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Duplicate
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.InstallApp
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.LaunchApp
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.OpenWith
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Paste
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Rename
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.Share
import app.atomofiron.searchboxapp.screens.common.delegates.Operations.UseAs
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isInaccessible
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.toOk
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

private val Empty = null to null

class FileOperationDelegate @Inject constructor(
    override val scope: CoroutineScope,
    preferences: PreferenceStore,
    private val apks: ApkDelegate,
    private val utils: UtilService,
    private val store: ExplorerStore,
    private val sharing: FileSharingDelegate,
    private val service: ExplorerService,
) : CoroutineLauncher {
    private enum class Mode(
        val rw: Boolean,
        val pasting: Boolean,
    ) {
        Copy(rw = false, pasting = false),
        CopyPaste(rw = true, pasting = false),
        Paste(rw = true, pasting = true),
    }

    private val itemComposition by preferences.explorerItemComposition
        .map { it.copy(visibleBox = false) }

    fun operations(targets: List<Node>, readOnly: Boolean = false): Rslt<ExplorerItemOptions> {
        return operations(targets, mode = if (readOnly) Mode.Copy else Mode.CopyPaste)
    }

    private fun operations(targets: List<Node>, mode: Mode): Rslt<ExplorerItemOptions> {
        val merged = targets.merge()
        when {
            merged.isEmpty() -> return Rslt.Err(utils[R.string.empty])
            merged.all { it.isInaccessible() } -> return Rslt.Err(utils[R.string.inaccessible])
        }
        val operations = when {
            merged.size > 1 -> Rslt.Ok(Operations.run { listOf(Share, Copy, Delete) })
            else -> buildOption(merged, mode)
        }
        return when (operations) {
            is Rslt.Ok -> ExplorerItemOptions(operations.value, merged, itemComposition).toOk()
            is Rslt.Err -> return Rslt.Err(operations.message)
        }
    }

    private fun buildOption(targets: List<Node>, mode: Mode): Rslt<List<MenuItem>> = buildList {
        val first = targets.firstOrNull()
            ?: return Rslt.Err()
        val single = targets.size == 1
        if (mode.rw) add(Create)
        if (single) add(CopyPath)
        if (single && mode.rw) add(Duplicate)
        if (single && mode.rw) add(Rename)
        add(Copy)
        val copied = store.pasteBuffer
        val pasteable = single && first.isDirectory && copied.isNotEmpty() && copied.none { it.ref == first.ref || it.parentRef == first.ref || first.ref.isChildOf(it.ref) }
        val allDirs = copied.isNotEmpty() && copied.all { it.isDirectory }
        add(Paste.copy(
            icon = if (allDirs) R.drawable.ic_insert_folder else R.drawable.ic_insert_file,
            enabled = mode.rw && pasteable && !mode.pasting,
            extra = mode.pasting,
        ))
        if (mode.pasting) {
            add(ByCopying.copy(icon = if (allDirs) R.drawable.ic_insert_copy_folder else R.drawable.ic_insert_copy_file))
            add(ByMoving.copy(icon = if (allDirs) R.drawable.ic_insert_move_folder else R.drawable.ic_insert_move_file))
        }
        add(Delete)
        if (single && first.content is NodeContent.AndroidApp) {
            val index = sumOf<MenuItem> { it.content.cells } % LongItem
            add(index, LaunchApp.copy(enabled = apks.launchable(first)))
            add(index, InstallApp)
        } else if (single && utils.canUseAs(first)) {
            add(0, UseAs)
        }
    }.let { Rslt.Ok(it) }

    fun action(id: Int, targets: List<Node>, key: NodeTabKey? = null): Pair<Alert.Uni?, ExplorerItemOptions?> {
        val first = targets.firstOrNull()
        first ?: return Empty.also { debugFail { "targets are empty" } }
        when (id) {
            Delete.id -> deleteFile(targets, key)
            Share.id -> sharing.shareWith(targets.filter { it.isFile })
            OpenWith.id -> sharing.openWith(first)
            InstallApp.id -> apks.install(first, key)
            LaunchApp.id -> apks.launch(first)
            UseAs.id -> utils.useAs(first)
            CopyPath.id -> return utils.copyToClipboard(first, withAlert = false) to null
            Copy.id -> {
                store.setForCopy(targets)
                return Alert(R.string.copied) to operations(targets, readOnly = key == null).ok()?.value
            }
            Paste.id -> return null to operations(targets, mode = Mode.Paste).ok()?.value
            ByCopying.id,
            ByMoving.id -> {
                val copied = store.pasteBuffer
                    .takeIfNotEmpty()
                    ?: return Empty.also { debugFailUnreachable() }
                val key = key ?: return Empty.also { debugFailUnreachable() }
                io { service.tryCopy(key, copied, first, asMoving = id == ByMoving.id) }
            }
            else -> Unit
        }
        return Empty
    }

    private fun deleteFile(targets: List<Node>, key: NodeTabKey?) = io {
        service.tryDelete(key, targets)
    }.unit()
}