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

fun Node.copiable() = !isRoot

fun List<Node>.copiable() = when {
    isEmpty() -> false
    size == 1 -> first().copiable()
    else -> all { it.copiable() } // just in case
}

fun List<Node>.pasteable(dst: Node) = isNotEmpty() && none { it.ref == dst.ref || it.parentRef == dst.ref || dst.ref.isChildOf(it.ref) }

class FileOperationDelegate @Inject constructor(
    private val scope: CoroutineScope,
    preferences: PreferenceStore,
    private val apks: ApkDelegate,
    private val utils: UtilService,
    private val store: ExplorerStore,
    private val sharing: FileSharingDelegate,
    private val service: ExplorerService,
) : CoroutineLauncher by CoroutineLauncher(scope) {
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
        val merged = targets.merge()
        when {
            merged.isEmpty() -> return Rslt.Err(utils[R.string.empty])
            merged.all { it.isInaccessible() } -> return Rslt.Err(utils[R.string.inaccessible])
        }
        val operations = buildOperations(merged, first = merged.first(), readOnly)
        return ExplorerItemOptions(operations, merged, itemComposition).toOk()
    }

    private fun buildOperations(targets: List<Node>, first: Node, readOnly: Boolean): List<MenuItem> {
        return buildOperations(targets, first, mode = if (readOnly) Mode.Copy else Mode.CopyPaste)
    }

    private fun buildOperations(targets: List<Node>, first: Node, mode: Mode): List<MenuItem> = buildList {
        val single = targets.size == 1
        if (targets.all { it.isFile }) add(Share)
        if (single && mode.rw && first.isDirectory) add(Create)
        if (single) add(CopyPath)
        if (single && mode.rw && !first.isRoot) add(Duplicate)
        if (single && mode.rw && !first.isRoot) add(Rename)
        add(Copy.copy(enabled = targets.copiable()))
        val copied = store.pasteBuffer.value
        val allDirs = copied.isNotEmpty() && copied.all { it.isDirectory }
        add(Paste.copy(
            icon = if (allDirs) R.drawable.ic_insert_folder else R.drawable.ic_insert_file,
            enabled = single && mode.rw && first.isDirectory && copied.pasteable(first),
            activated = mode.pasting,
        ))
        if (mode.pasting) {
            add(ByCopying.copy(icon = if (allDirs) R.drawable.ic_insert_copy_folder else R.drawable.ic_insert_copy_file))
            add(ByMoving.copy(icon = if (allDirs) R.drawable.ic_insert_move_folder else R.drawable.ic_insert_move_file))
        }
        if (!first.isRoot) add(Delete)
        if (single && first.content is NodeContent.AndroidApp) {
            val index = sumOf<MenuItem> { it.content.cells } % LongItem
            add(index, LaunchApp.copy(enabled = apks.launchable(first)))
            add(index, InstallApp)
        } else if (single && utils.canUseAs(first)) {
            add(0, UseAs)
        }
    }

    fun action(item: MenuItem, targets: List<Node>, key: NodeTabKey? = null): Pair<Alert.Uni?, List<MenuItem>?> {
        val first = targets.firstOrNull()
        first ?: return Empty.also { debugFail { "targets are empty" } }
        when (item.id) {
            Delete.id -> deleteFile(targets, key)
            Share.id -> sharing.shareWith(targets.filter { it.isFile })
            OpenWith.id -> sharing.openWith(first)
            InstallApp.id -> apks.install(first, key)
            LaunchApp.id -> apks.launch(first)
            UseAs.id -> utils.useAs(first)
            CopyPath.id -> return utils.copyToClipboard(first) to null
            Copy.id -> {
                store.setForCopy(targets)
                return Alert(R.string.copied) to buildOperations(targets, first, readOnly = key == null)
            }
            Paste.id -> return null to buildOperations(targets, first, mode = if (item.activated) Mode.CopyPaste else Mode.Paste)
            ByCopying.id,
            ByMoving.id -> {
                val copied = store.pasteBuffer.value
                    .takeIfNotEmpty()
                    ?: return Empty.also { debugFailUnreachable() }
                val key = key ?: return Empty.also { debugFailUnreachable() }
                io { service.tryCopy(key, copied, first, withMoving = item.id == ByMoving.id) }
                store.resetCopyBuffer()
            }
            else -> Unit
        }
        return Empty
    }

    private fun deleteFile(targets: List<Node>, key: NodeTabKey?) = io {
        service.tryDelete(key, targets)
    }.unit()
}