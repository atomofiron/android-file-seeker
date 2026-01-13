package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import android.media.MediaScannerConnection
import android.os.StatFs
import app.atomofiron.common.util.CoroutineSafeList
import app.atomofiron.common.util.dropLast
import app.atomofiron.common.util.extension.clear
import app.atomofiron.common.util.extension.debug
import app.atomofiron.common.util.extension.debugDelay
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.common.util.extension.put
import app.atomofiron.common.util.extension.replace
import app.atomofiron.common.util.extension.setAt
import app.atomofiron.common.util.extension.takeIf
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.common.util.flow.TriggerFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.android.verifyNativeBin
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.ExplorerTabKey
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeGarden
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRootInfo
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeStateImpl
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.NodeTab
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.isMedia
import app.atomofiron.searchboxapp.model.explorer.isMovie
import app.atomofiron.searchboxapp.model.explorer.isPicture
import app.atomofiron.searchboxapp.model.explorer.other.Deepest
import app.atomofiron.searchboxapp.model.explorer.other.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.other.TabRootSorting
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.explorer.replace
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import app.atomofiron.searchboxapp.utils.ExplorerUtils
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.delete
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.rename
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveDirChildren
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.ExplorerUtils.theSame
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateWith
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.removeOneIf
import app.atomofiron.searchboxapp.utils.replaceEach
import app.atomofiron.searchboxapp.utils.showLongToast
import app.atomofiron.searchboxapp.utils.toAlert
import app.atomofiron.searchboxapp.utils.unwrapOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val SUB_PATH_CAMERA = "DCIM/Camera"
private const val SUB_PATH_PIC_SCREENSHOTS = "Pictures/Screenshots"
private const val SUB_PATH_DCIM_SCREENSHOTS = "DCIM/Screenshots"
private const val SUB_PATH_DOWNLOAD = "Download"
private const val SUB_PATH_DOWNLOAD_BLUETOOTH = "Download/Bluetooth"
private const val SUB_PATH_BLUETOOTH = "Bluetooth"

@Singleton
class ExplorerService @Inject constructor(
    private val context: Context,
    private val scope: AppScope,
    private val store: ExplorerStore,
    private val dao: ExplorerDao,
    private val preferences: PreferenceStore,
) : CoroutineLauncher by CoroutineLauncher(scope) {

    private var delayedRender: Job? = null

    private val asSu by preferences.asSu
    private val garden = NodeGarden()
    private val internalStorageRef = store.internalStorage.value.ref
    private val updateRootTrigger = TriggerFlow<Unit>()

    init {
        val suDefined = Job()
        io {
            garden { // lock due configuration
                store.mainTabs.forEach { get(it) } // init
                suDefined.join()
                if (asSu) checkSu()
                initRoots()
                restoreSorting()
            }
            combine(store.storage, preferences.asSu, updateRootTrigger) { volumes, asSu, _ ->
                updateRootsAsync(volumes, asSu)
            }.collect()
        }
        preferences.suCmd[scope] = { cmd ->
            NativeBridge.setSuCmd(cmd, binDir = context.filesDir.absolutePath)
            suDefined.complete()
        }
        store.currentSorting[scope] = { (key, sorting) ->
            garden(key) { render() }
        }
        store.currentDeepest.drop(1)[scope] = l@{ deepest ->
            deepest ?: return@l
            val tab = store.currentTabKey.value
            val root = garden[tab].getSelectedRoot()
                ?.takeIf { it.info != NodeRootInfo.Photos && it.info != NodeRootInfo.Videos }
                ?: return@l
            val new = Deepest(tabIndex = tab.index, rootId = root.id, deepest.ref)
            dao.put(new)
        }
    }

    private suspend fun checkSu() {
        val result = context.verifyNativeBin()
        if (result is Rslt.Err) {
            preferences.setUseSu(false)
            if (result.message.isNotEmpty()) {
                withMain {
                    context.showLongToast(result.message.toUni())
                }
            }
        }
    }

    fun getFlow(key: NodeTabKey): SharedFlow<NodeTabItems> {
        if (!garden.has(key)) {
            scope.launchOnIO {
                garden(key) {
                    render()
                }
            }
        }
        return garden.getFlow(key) // concurrency? unlikely
    }

    private fun NodeTab.restoreTree() {
        val key = key as? ExplorerTabKey
        when {
            key == null -> return
            !key.primary -> return
            tree.isNotEmpty() -> return
        }
        val root = getSelectedRoot()
        root ?: return
        val deepest = dao.getDeepest(key.index, root.id)
        deepest ?: return
        val tree = mutableListOf<NodeRef>()
        var ref = deepest.ref
        while (true) {
            if (ref.isEmpty) {
                return debugFail { "deepest=$deepest, tree=$tree" }
            }
            tree.add(ref)
            when (ref.uniqueId) {
                deepest.rootId -> break
                else -> ref = ref.parent
            }
        }
        tree.reverse()
        putTree(deepest.rootId, tree)
    }

    fun drop(vararg keys: NodeTabKey) = garden.drop(*keys)

    private fun NodeGarden.initRoots() {
        val roots = listOf(
            NodeRoot(NodeRootInfo.Photos, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_CAMERA),
            NodeRoot(NodeRootInfo.Videos, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_CAMERA),
            NodeRoot(NodeRootInfo.Screenshots, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_PIC_SCREENSHOTS, internalStorageRef + SUB_PATH_DCIM_SCREENSHOTS),
            NodeRoot(NodeRootInfo.Bluetooth, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_BLUETOOTH, internalStorageRef + SUB_PATH_DOWNLOAD_BLUETOOTH),
            NodeRoot(NodeRootInfo.Downloads, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_DOWNLOAD),
            NodeRoot(NodeRootInfo.SystemRoot, NodeSorting.Name, NodeRef.Root),
        )
        this.roots.addAll(roots)
    }

    private fun NodeGarden.restoreSorting() {
        for (root in roots) {
            restoreSorting(root)
        }
    }

    private fun NodeGarden.restoreSorting(root: NodeRoot) {
        for (tab in store.mainTabs) {
            val sorting = dao.getSorting(tab.index, root.id)
                ?.sorting
                ?: continue
            get(tab).setSorting(root.id, sorting)
        }
    }

    suspend fun tryToggleRoot(key: NodeTabKey, root: NodeRoot) {
        render(key) {
            val root = roots.find { it.id == root.id }
                ?: return
            if (selected(root)) {
                deselectRoot()
                store.setSorting(key, null)
            } else {
                select(root)
            }
            if (tree.isEmpty()) {
                restoreTree()
            }
        }
        tryCache(key, root.item)
    }

    suspend fun tryToggle(key: NodeTabKey, ref: NodeRef) {
        var rootItem: Node? = null
        render(key) {
            val root = getSelectedRoot() ?: return
            if (tree.isEmpty() && root.item.uniqueId != ref.uniqueId) {
                return
            }
            val item = findItem(ref.uniqueId)
                ?.takeIf { it.hasChildren }
                ?.takeIf { it.isOpened || states[it.uniqueId]?.isRemoving != true }
                ?: return
            val index = tree.indexOfFirst { it.uniqueId == item.uniqueId }
            if (tree.isEmpty()) {
                rootItem = root.item.copy(children = root.item.children?.fetch())
                tree.add(rootItem.ref)
            } else if (index == tree.lastIndex) {
                tree.dropLast()
            } else if (index >= 0) {
                tree.clear(from = index.inc())
            } else {
                val index = tree.indexOfFirst { it == item.parentRef }
                val parentRef = tree.getOrNull(index)
                parentRef ?: return
                tree.clear(from = index.inc())
                val parent = findItem(parentRef.uniqueId)
                parent ?: return
                val target = parent.children
                    ?.find { it.uniqueId == item.uniqueId }
                    ?: return
                tree.add(target.ref)
            }
        }
        rootItem?.let { tryCache(key, it) }
    }

    private fun NodeTab.onRemoving(items: List<Node>): Boolean {
        return tree.lastOrNull()?.let { deepest ->
            items.find { deepest == it.ref || deepest.isChildOf(it.ref) }
        }?.let { item ->
            val index = tree.indexOfFirst { it == item.ref }
            tree.clear(index)
        } != null
    }

    suspend fun updateRootsAsync() = updateRootTrigger()

    suspend fun updateRootsAsync(volumes: List<NodeStorage>, withSu: Boolean) {
        garden {
            volumes.forEach { updateStats(it) }
            removeMissed(volumes)
            val key = store.currentTabKey.value
            val tab = get(key)
            when {
                roots.none { it.id == tab.selectedRootId } -> tab.deselectRoot()
                withSu -> Unit
                tab.getSelectedRoot()?.info is NodeRootInfo.SystemRoot -> tab.deselectRoot()
            }
            tab.render()
            roots.forEach { root ->
                if (withSu || root.info !is NodeRootInfo.SystemRoot) {
                    updateRootAsync(key, root)
                }
            }
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        when {
            asSu -> Unit
            root.info !is NodeRootInfo.SystemRoot -> Unit
            else -> return
        }
        scope.launch {
            garden(key) {
                withCachingState(root.id) {
                    var updated = root.item.update(asSu)
                    updated = when (updated.error) {
                        is NodeError.NoSuchFileOrDir -> tryAlternative(root, updated)
                        else -> updated
                    }
                    updateRootSync(updated, key, root)
                    if (root.info is NodeRootInfo.Screenshots) {
                        store.updateScreenshots(root.item.ref)
                    }
                }
            }
        }
    }

    private suspend fun tryAlternative(root: NodeRoot, missing: Node): Node {
        val variants = root.pathVariants?.takeIf { it.isNotEmpty() }
        variants ?: return missing
        val items = variants.map { path ->
            path.toRoot(root.info).update(asSu)
        }
        val alt = items.find { it.error == null }
            ?: items.find { it.error !is NodeError.NoSuchFileOrDir }
        return alt ?: missing
    }

    suspend fun setSorting(key: ExplorerTabKey, root: NodeRootInfo, sorting: NodeSorting) {
        val root = garden(key) {
            roots.find { it.info == root }?.also {
                setSorting(it.id, sorting)
                render()
            }
        }
        root ?: return
        dao.put(TabRootSorting(key.index, root.id, sorting))
    }

    private fun NodeGarden.updateStats(storage: NodeStorage) {
        val index = roots.indexOfFirst { it.info is NodeRootInfo.Storage && it.info.kind == storage.kind && it.item.ref.string == storage.path }
        var root = roots.getOrNull(index)
        var key = root?.info ?: NodeRootInfo.Storage(storage)
        key = (key as NodeRootInfo.Storage).copy(info = storage)
        root = root ?: NodeRoot(key, NodeSorting.Name, NodeRef(storage.path))
        val restore = roots.none { it.id == root.id }
        roots.put(root) { it.id == root.id }
        if (restore) restoreSorting(root)
    }

    private fun NodeGarden.removeMissed(storage: List<NodeStorage>) {
        roots.removeAll { root ->
            root.info.removable && storage.none { it.path == root.item.ref.string }
        }
    }

    private fun filterMediaRootChildren(updated: Node, type: NodeRootInfo) {
        val onlyPhotos = type == NodeRootInfo.Photos || type == NodeRootInfo.Screenshots
        val onlyVideos = type == NodeRootInfo.Videos
        val onlyMedia = type == NodeRootInfo.Camera
        if (onlyPhotos || onlyVideos || onlyMedia) {
            updated.children?.update(updateMetadata = true) {
                replace {
                    when {
                        onlyPhotos && !it.content.isPicture() -> null
                        onlyVideos && !it.content.isMovie() -> null
                        onlyMedia && !it.content.isMedia() -> null
                        else -> it
                    }
                }
            }
        }
    }

    private fun updateRootThumbnail(updated: Node, targetRoot: NodeRoot): NodeRoot {
        val preview = targetRoot.previewSorting
            ?.let { updated.sortBy(targetRoot.previewSorting) }
            ?.children
            ?.firstOrNull()
        return when {
            preview == null -> targetRoot.copy(item = updated, thumbnail = null, thumbnailPath = "")
            targetRoot.thumbnailPath == preview.ref.string -> targetRoot
            else -> targetRoot.copy(item = updated, thumbnail = Thumbnail.FilePath, thumbnailPath = preview.ref.string)
        }
    }

    private suspend fun updateRootSync(updated: Node, key: NodeTabKey, targetRoot: NodeRoot) {
        filterMediaRootChildren(updated, targetRoot.info)
        val updatedRoot = updateRootThumbnail(updated, targetRoot)
        garden {
            states.updateState(updatedRoot.id) {
                nextState(cachingJob = null)
            }
            val tab = get(key)
            roots.replace { root ->
                when (root.id) {
                    targetRoot.id -> {
                        val updatedItem = root.item.updateWith(updatedRoot.item)
                        val info = root.info.takeIf<NodeRootInfo.Storage,_>()?.run {
                            val stat = StatFs(root.item.ref.string)
                            val info = info.copy(total = stat.totalBytes, used = stat.totalBytes - stat.freeBytes)
                            copy(info = info)
                        } ?: root.info
                        if (tab.key == key) updatedRoot.copy(item = updatedItem, info = info) else root.copy(
                            info = root.info,
                            thumbnail = updatedRoot.thumbnail,
                            thumbnailPath = updatedRoot.thumbnailPath,
                            item = updatedItem,
                        )
                    }
                    else -> root
                }
            }
            tabs.values.asSequence()
                .filter { it.key is ExplorerTabKey && it.key.primary == key.primary }
                .forEach { it.render() }
        }
    }

    suspend fun tryCache(key: NodeTabKey, item: Node) {
        garden(key) {
            roots.takeIf { item.isRoot }
                ?.find { it.item.uniqueId == item.uniqueId }
                ?.let { return updateRootAsync(key, it) }

            val current = findItem(item.uniqueId)
            current ?: return

            withCachingState(current.uniqueId) {
                cacheSync(key, current)
                if (item.isDirectory) resolveSizeAsync(key, item)
            }
        }
    }

    private fun NodeTab.resolveDirChildren(it: Node) {
        val children = it.children?.fetch() ?: return
        withCachingState(it.uniqueId) {
            val done = it.copy(children = children)
                .resolveDirChildren(asSu)
            garden {
                states.updateState(it.uniqueId) {
                    nextState(cachingJob = null)
                }
                if (!done) {
                    return@withCachingState
                }
                val item = findItem(it.uniqueId) ?: return@garden
                val items = item.children?.items ?: return@withCachingState
                items.forEachIndexed { index, current ->
                    val resolved = children.find { child -> child.uniqueId == current.uniqueId }
                    resolved ?: return@forEachIndexed
                    val updated = current.updateWith(resolved.content, resolved.meta)
                    val old = items[index]
                    items[index] = updated
                    if (item.opened() && !updated.areContentsTheSame(old)) {
                        renderUpdate(updated)
                    }
                }
            }
        }
    }

    suspend fun tryRename(key: NodeTabKey, ref: NodeRef, name: String) {
        val item = garden(key) {
            findItem(ref.uniqueId)
        }
        item ?: return
        // todo change uniqueId in state, create the new one state instance
        val renamed = item.rename(name, asSu)
            ?: return debugFail { "null after rename $ref to $name" }
        render(key) {
            replaceItem(item.uniqueId, renamed)
            var index = tree.indexOf(item.ref)
            if (index >= 0) {
                tree[index] = renamed.ref
                while (++index < tree.size) {
                    val next = tree[index]
                    debug {
                        if (!next.isChildOf(item.ref)) debugFail { "$next isn't child of ${item.ref}" }
                    }
                    tree[index] = next.replace(renamed.ref, replace = item.ref.length)
                }
            }
        }
    }

    suspend fun tryCreate(key: NodeTabKey, parent: Node, name: String, directory: Boolean) {
        val item = ExplorerUtils.create(parent, name, directory, asSu)
        item ?: return
        render(key) {
            val children = findItem(parent.uniqueId)
                ?.children
                ?: findItem(parent.parentRef.uniqueId)
                    ?.children
                    ?.find { it.uniqueId == parent.uniqueId }
                    ?.children
                ?: return
            when {
                item.isDirectory -> children.items.add(0, item)
                else -> {
                    var index = children.indexOfFirst { it.isFile }
                    if (index < 0) index = children.size
                    children.items.add(index, item)
                }
            }
        }
    }

    suspend fun tryCopy(key: NodeTabKey, targets: List<Node>, dst: Node, withMoving: Boolean) {
        if (withMoving) garden(key) {
            if (onRemoving(targets)) render()
        }
        val items = targets.map {
            scope.async {
                val to = it.mutate(ref = dst.ref + it.name)
                tryCopy(key, it, to, withMoving, withNotice = false)
            }
        }.awaitAll()
            .mapNotNull { it?.takeIf { it.error == null } }
        when {
            withMoving -> store.emitMoved(items)
            else -> store.emitCopied(items)
        }
    }

    suspend fun tryCopy(key: NodeTabKey, from: Node, to: Node, withMoving: Boolean, withNotice: Boolean = true): Node? {
        render(key) {
            states.updateState(from.uniqueId) {
                nextState(copying = NodeOperation.Copying(isSource = true, withMoving = withMoving))
            }.let {
                if (it?.isCopying != true) return null
            }
            states.updateState(to.uniqueId) {
                nextState(copying = NodeOperation.Copying(isSource = false))
            }
            findItem(to.parentRef.uniqueId) { _, _, item ->
                val children = item.children
                children?.indexOfLast { it.isDirectory }
                    ?.let { children.items.add(it.inc(), to) }
            }
        }
        val new = ExplorerUtils.copy(from, to, move = withMoving, asSu = asSu) {
            default {
                val copying = NodeOperation.Copying(isSource = false, it.progress)
                garden {
                    states.updateState(to.uniqueId) {
                        this ?: return@updateState null
                        nextState(copying = copying)
                    }
                    get(key).renderUpdate(to)
                }
            }
        }
        when {
            !withNotice -> Unit
            new == null || new.error != null -> store.emitAlert((new?.error ?: NodeError.Unknown).toAlert(from.content))
            withMoving -> store.emitMoved(from)
            else -> store.emitCopied(from)
        }
        render(key) {
            states.updateState(from.uniqueId) {
                nextState(copying = null)
            }
            states.updateState(to.uniqueId) {
                nextState(copying = null)
            }
            findItem(to.parentRef.uniqueId) { _, _, dst ->
                val children = dst.children
                children?.indexOfFirst { it.uniqueId == to.uniqueId }
                    ?.takeIf { it >= 0 }
                    ?.let { children.items.setAt(it, new) }
            }
        }
        if (withMoving) tryCache(key, from)
        if (new != null) {
            MediaScannerConnection.scanFile(context, arrayOf(to.ref.string), to.content.mimeType?.let { arrayOf(it) }, null)
        }
        return new
    }

    suspend fun tryCheck(key: ExplorerTabKey, refs: List<Node>, toChecked: Boolean) {
        garden(key) {
            val toRender = mutableListOf<Node>()
            for (item in refs) {
                if (states[item.uniqueId]?.withOperation != true && checked.tryUpdateCheck(item.uniqueId, toChecked)) {
                    toRender.add(item)
                }
            }
            if (toRender.size == 1) {
                val item = findItem(toRender.first().uniqueId)
                item ?: return@garden
                renderUpdate(item)
                renderChecked(key, item, toChecked)
            } else if (toRender.size > 1) {
                render()
            }
        }
    }

    suspend fun tryMarkInstalling(key: NodeTabKey, ref: NodeRef, installing: NodeOperation.Installing?): Boolean? {
        return garden {
            var state = states[ref.uniqueId]
            if (state?.operation == installing) return false
            state = states.updateState(ref.uniqueId) {
                nextState(installing = installing)
            }
            (state?.operation == installing).also {
                if (it) {
                    val tab = get(key)
                    val item = tab.findItem(ref.uniqueId)
                    tab.renderUpdate(item ?: return@also)
                }
            }
        }
    }

    /** @return action succeed */
    private fun MutableList<Int>.tryUpdateCheck(uniqueId: Int, toChecked: Boolean): Boolean {
        val iter = iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            when {
                item != uniqueId -> Unit
                toChecked -> return false
                else -> {
                    iter.remove()
                    return true
                }
            }
        }
        if (toChecked) add(uniqueId)
        return toChecked
    }

    suspend fun deleteEveryWhere(items: List<Node>) {
        // todo delete every where
        val files = items.filter { !it.isDirectory }
        val dirs = items.filter { it.isDirectory }
        val fileJob = scope.launch {
            for (file in files) {
                file.delete(asSu)
                store.emitDeleted(file.copy(children = null))
            }
        }
        val dirJobs = dirs.map { dir ->
            scope.launch {
                dir.delete(asSu)
                store.emitDeleted(dir.copy(children = null))
            }
        }
        fileJob.join()
        dirJobs.joinAll()
        store.emitDeleted(items)
    }

    suspend fun tryDelete(key: NodeTabKey?, its: List<Node>) {
        if (key == null) {
            return deleteEveryWhere(its)
        }
        var mediaRootAffected: NodeRoot? = null
        val items = mutableListOf<Node>()
        render(key) {
            mediaRootAffected = roots.find { selected(it) && it.withPreview }
            its.mapNotNull { item ->
                val state = states.updateState(item.uniqueId) {
                    if (this?.isDeleting == true) {
                        null
                    } else {
                        this?.cachingJob?.cancel()
                        checked.tryUpdateCheck(item.uniqueId, toChecked = false)
                        nextState(cachingJob = null, deleting = NodeOperation.Deleting)
                    }
                }
                findItem(item.uniqueId)
                    ?.takeIf { state?.isDeleting == true }
            }.let { items.addAll(it) }
            onRemoving(items)
        }
        val files = items.filter { !it.isDirectory }
        val dirs = items.filter { it.isDirectory }
        val deleted = CoroutineSafeList<Node>()
        debugDelay(1)
        val fileJob = scope.launch {
            for (file in files) {
                if (file.deleteIn(key)) {
                    deleted.add(file)
                }
            }
        }
        val dirJobs = dirs.map { dir ->
            scope.launch {
                if (dir.deleteIn(key)) {
                    deleted.add(dir)
                }
            }
        }
        fileJob.join()
        dirJobs.joinAll()
        store.emitDeleted(deleted)
        mediaRootAffected?.let { mediaRoot ->
            garden {
                updateRootAsync(key, mediaRoot)
            }
        }
    }

    private suspend fun Node.deleteIn(key: NodeTabKey): Boolean {
        val result = delete(asSu)
        garden(key) {
            replaceItem(uniqueId, result)
            states.updateState(uniqueId) { null }
            lazyRender()
        }
        result?.let {
            tryCache(key, it)
        }
        return result == null
    }

    suspend fun resetChecked(key: ExplorerTabKey) {
        garden(key) {
            store.emitChecked(key, emptyList())
            checked.mapNotNull { findItem(it) }
                .also { checked.clear() }
                .forEach { renderUpdate(it) }
        }
    }

    private suspend inline fun render(key: NodeTabKey, block: NodeTab.() -> Unit) {
        garden(key) {
            block()
            render()
        }
    }

    private fun NodeTab.lazyRender() {
        delayedRender = delayedRender ?: scope.launch {
            delay(Const.SMALL_DELAY)
            delayedRender = null
            garden(key) {
                render()
            }
        }
    }

    private suspend fun NodeTab.render() {
        delayedRender?.cancel()
        delayedRender = null
        incrementGeneration()
        states.entries.removeAll {
            it.value.isEmpty
        }
        val roots = renderRoots()
        roots.find { it.isSelected }
            ?.takeIf { !trees.containsKey(it.id) }
            ?.let { putTree(it.id, listOf(it.item.ref)) }

        val rendered = renderItems(roots)
        flow.emit(rendered)

        updateChecked(rendered.items)
        val checked = rendered.items.filter { it.isChecked }
        if (key is ExplorerTabKey) {
            store.setDeepestNode(key, rendered.deepest)
            store.emitChecked(key, checked)
            store.setCurrentItems(key, rendered.items)
        }
        require(this.roots.all { !it.isSelected })
        incrementGeneration()
    }

    private fun NodeTab.renderRoots(): List<NodeRoot> {
        return roots.mutate {
            replaceEach {
                when (it.id) {
                    selectedRootId -> it.copy(isSelected = true)
                    else -> it
                }
            }
            if (!asSu) {
                removeOneIf { it.info is NodeRootInfo.SystemRoot }
            }
        }
    }

    private fun NodeTab.updateChecked(items: List<Node>) {
        if (checked.isNotEmpty()) {
            val iterator = checked.listIterator()
            while (iterator.hasNext()) {
                val uniqueId = iterator.next()
                if (items.none { it.uniqueId == uniqueId }) {
                    iterator.remove()
                }
            }
        }
    }

    private fun NodeTab.renderItems(roots: List<NodeRoot>): NodeTabItems {
        val root = getSelectedRoot()
            ?: return NodeTabItems(roots, emptyList(), null)
        val items = mutableListOf<Node>()
        renderNode(root.item, content = root.item.defineDirKind(), isOpened = tree.isNotEmpty(), isDeepest = tree.size == 1)
            .also { items.add(it) }
            .takeIf { !it.isOpened }
            ?.let { return NodeTabItems(roots, items, null) }
        val sorting = getSorting(root.id)
        store.setSorting(key, sorting)
        var deepest = items.first()
        val openedIndexes = mutableListOf<Int>()
        val filteredCounts = when {
            mimeTypes.isEmpty() -> null
            mimeTypes == NodeContent.Directory.mimeTypes -> null
            // filteredCounts isn't always null! >:(
            else -> IntArray(tree.size)
        }
        var parent = items.first()
        for (i in tree.indices) {
            val level = findItem(tree[i].uniqueId)
            level ?: break
            level.sortBy(sorting)
            val nextLevelId = tree.getOrNull(i.inc())?.uniqueId
            for (j in 0..<level.childCount) {
                var item = level.children!![j]
                if (mismatch(item)) {
                    filteredCounts?.inc(i)
                    continue
                }
                val isOpened = item.uniqueId == nextLevelId
                item = renderNode(
                    item,
                    isDeepest = isOpened && i == tree.lastIndex.dec(),
                    isOpened = isOpened,
                    content = item.defineDirKind(i),
                )
                if (item.isDeepest) {
                    deepest = item
                }
                items.add(item)
                if (isOpened) {
                    parent.children?.items[j] = item
                    parent = item
                    openedIndexes.add(j)
                    break
                }
            }
        }
        for (i in openedIndexes.indices.reversed()) {
            if (i == tree.lastIndex) continue
            val level = findItem(tree[i].uniqueId)
            level ?: break
            val opened = openedIndexes[i]
            for (j in opened.inc() until level.childCount) {
                var item = level.children!![j]
                if (mismatch(item)) {
                    filteredCounts?.inc(i)
                    continue
                }
                item = renderNode(item, content = item.defineDirKind(i))
                items.add(item)
            }
            if (i < tree.lastIndex) {
                items.find { it.uniqueId == level.uniqueId }
                    ?.asSeparator()
                    ?.let { items.add(it) }
            }
        }
        var offset = 0
        if (filteredCounts != null) items.forEachIndexed { i, it ->
            if (!it.isOpened || it.isSeparator()) return@forEachIndexed
            val offset = offset++
            items[i] = it.copy(children = it.children?.copy(filteredOut = filteredCounts[offset]))
        }
        return NodeTabItems(roots, items, deepest)
    }

    private fun IntArray.inc(i: Int) = set(i, get(i).inc())

    private fun NodeTab.mismatch(item: Node): Boolean = mimeTypes.isNotEmpty() && item.isFile && !item.content.matchesAny(mimeTypes)

    private suspend fun NodeTab.renderUpdate(new: Node) {
        store.emitUpdate(renderNode(new))
    }

    private fun renderChecked(key: ExplorerTabKey, item: Node, toChecked: Boolean) {
        store.checked.value.mutate {
            when {
                toChecked -> add(item)
                else -> removeOneIf { it.uniqueId == item.uniqueId }
            }
            store.emitChecked(key, this)
        }
    }

    private fun NodeTab.renderNode(
        item: Node,
        isOpened: Boolean = tree.any { it.uniqueId == item.uniqueId },
        isDeepest: Boolean = tree.lastOrNull()?.uniqueId == item.uniqueId,
        content: NodeContent = item.defineDirKind(),
    ): Node {
        return item.copy(
            isChecked = checked.any { it == item.uniqueId },
            isDeepest = isDeepest,
            state = states[item.uniqueId] ?: item.state,
            children = item.children?.fetch(isOpened),
            generation = generation,
            content = content,
        )
    }

    private fun Node.defineDirKind(levelIndex: Int = -1): NodeContent = when {
        levelIndex > 0 -> content
        content !is NodeContent.Directory -> content
        internalStorageRef.length != (ref.length.dec() - name.length) -> content
        !ref.isChildOf(internalStorageRef) -> content
        else -> ExplorerUtils.getDirectoryType(name)
            .takeIf { it != DirectoryKind.Ordinary }
            ?.let { content.copy(kind = it) }
            ?: content
    }

    /** @return already existing caching job */
    private fun NodeTab.withCachingState(id: Int, caching: suspend CoroutineScope.() -> Unit): Job? {
        var state = states[id]
        if (state != null) return state.cachingJob
        val job = scope.launch(start = CoroutineStart.LAZY, block = caching)
        state = states.updateState(id) {
            nextState(cachingJob = job)
        }
        require(state?.cachingJob === job)
        job.start()
        return null
    }

    private suspend fun cacheSync(key: NodeTabKey, item: Node) {
        var updated = item.update(asSu)
        garden(key) {
            states.updateState(item.uniqueId) {
                nextState(cachingJob = null)
            }
            val current = findItem(item.uniqueId)
            current ?: return
            if (updated.error is NodeError.NoSuchFileOrDir) {
                if (removeNode(item.uniqueId)) {
                    render()
                }
                return
            }
            updated = current.updateWith(updated)
            // todo replace everywhere
            when {
                !replaceItem(updated) -> return
                updated.isDirectory -> resolveDirChildren(updated)
            }
            when (true) {
                updated.areContentsTheSame(item) -> Unit
                (updated.childCount == item.childCount),
                tree.none { it == item.ref } -> renderUpdate(updated)
                else -> render()
            }
        }
    }

    private fun resolveSizeAsync(key: NodeTabKey, item: Node) {
        scope.launch {
            val (length, size) = NativeBridge.usage(item.ref, asSu)
                .unwrapOrElse { NodeMeta.Empty.run { length to size } }
            if (length != item.length && size != item.size) garden(key) {
                val current = findItem(item.uniqueId)
                current ?: return@launch
                val updated = current.copy(meta = item.meta.copy(length = length, size = size))
                val replaced = replaceItem(updated)
                if (replaced && !updated.areContentsTheSame(item)) {
                    renderUpdate(updated)
                }
            }
        }
    }

    private fun NodeStateImpl?.nextState(
        cachingJob: Job? = this?.cachingJob,
        deleting: NodeOperation.Deleting? = this?.operation as? NodeOperation.Deleting,
        copying: NodeOperation.Copying? = this?.operation as? NodeOperation.Copying,
        installing: NodeOperation.Installing? = this?.operation as? NodeOperation.Installing,
    ): NodeStateImpl? {
        val nextOperation = when (this?.operation) {
            null -> deleting ?: copying ?: installing
            is NodeOperation.Deleting -> deleting ?: copying
            is NodeOperation.Copying -> copying ?: deleting
            is NodeOperation.Installing -> installing ?: deleting
        }
        val nextJob = when (cachingJob) {
            null -> null
            else -> this?.cachingJob ?: cachingJob
        }
        return when {
            nextJob == null && nextOperation == null -> null
            theSame(nextJob, nextOperation) -> return this
            else -> NodeStateImpl(nextJob, nextOperation)
        }
    }

    private fun MutableMap<NodeId, NodeStateImpl>.updateState(
        uniqueId: Int,
        block: NodeStateImpl?.() -> NodeStateImpl?,
    ): NodeStateImpl? {
        val new = get(uniqueId).block()
        when (new) {
            null -> remove(uniqueId)
            else -> put(uniqueId, new)
        }
        return new
    }

    private fun NodeTab.findItem(uniqueId: Int): Node? = findItem(uniqueId) { _, _, it -> it }

    private fun NodeTab.removeNode(uniqueId: Int) = replaceItem(uniqueId, null)

    private fun NodeTab.replaceItem(new: Node) = replaceItem(new.uniqueId, new)

    private fun NodeTab.replaceItem(uniqueId: Int, new: Node?) = findItem(uniqueId) { children, i, _ ->
        children?.items?.setAt(i, new)
        true
    } ?: false

    /** NodeChildren is null if uniqueId of the root item */
    private fun <R> NodeTab.findItem(
        uniqueId: Int,
        action: (NodeChildren?, Int, Node) -> R,
    ): R? {
        val root = getSelectedRoot()
        root ?: return null
        if (root.item.uniqueId == uniqueId) {
            return action(null, -1, root.item)
        }
        val tree = tree
        var children: NodeChildren? = null
        for (level in tree) {
            if (level.uniqueId == root.item.uniqueId) {
                children = root.item.children
            } else if (children != null) {
                 children = children.indexOfFirst { it.uniqueId == level.uniqueId }
                     .let { children.getOrNull(it) }
                     ?.children
            }
            children ?: break

            val index = children.indexOfFirst { it.uniqueId == uniqueId }
            children.getOrNull(index)
                ?.let { return action(children, index, it) }
        }
        return null
    }
}