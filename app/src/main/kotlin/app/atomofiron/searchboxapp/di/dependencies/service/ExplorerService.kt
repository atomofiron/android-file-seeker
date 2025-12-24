package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
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
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.android.verifyNativeBin
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.db.dao.Deepest
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.ExplorerTabKey
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeGarden
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeStateImpl
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.NodeTab
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.isMedia
import app.atomofiron.searchboxapp.model.explorer.isMovie
import app.atomofiron.searchboxapp.model.explorer.isPicture
import app.atomofiron.searchboxapp.model.explorer.other.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.explorer.replace
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.delete
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.rename
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveDirChildren
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortByName
import app.atomofiron.searchboxapp.utils.ExplorerUtils.theSame
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateWith
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.findWithIndex
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.removeOneIf
import app.atomofiron.searchboxapp.utils.replaceEach
import app.atomofiron.searchboxapp.utils.showLongToast
import app.atomofiron.searchboxapp.utils.unwrapOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private const val SUB_PATH_CAMERA = "DCIM/Camera"
private const val SUB_PATH_PIC_SCREENSHOTS = "Pictures/Screenshots"
private const val SUB_PATH_DCIM_SCREENSHOTS = "DCIM/Screenshots"
private const val SUB_PATH_DOWNLOAD = "Download"
private const val SUB_PATH_DOWNLOAD_BLUETOOTH = "Download/Bluetooth"
private const val SUB_PATH_BLUETOOTH = "Bluetooth"

class ExplorerService(
    private val context: Context,
    private val appScope: AppScope,
    private val store: ExplorerStore,
    private val db: ExplorerDao,
    private val preferences: PreferenceStore,
) {
    private var delayedRender: Job? = null

    private val asSu by preferences.asSu
    private val garden = NodeGarden()
    private val internalStorageRef = store.internalStorage.value.ref
    private val updateRootTrigger = TriggerFlow<Unit>()

    init {
        val suDefined = Job()
        appScope.launchOnIO {
            garden { // lock due configuration
                store.mainTabs
                    .forEach { get(it) } // init
                suDefined.join()
                if (asSu) checkSu()
                initRoots()
            }
            combine(store.storage, preferences.asSu, updateRootTrigger) { volumes, asSu, _ ->
                updateRootsAsync(volumes, asSu)
            }.collect()
        }
        combine(preferences.asSu, preferences.suCmd) { asSu, suCmd ->
            NativeBridge.setSuCmd(suCmd, binDir = context.filesDir.absolutePath)
            suDefined.complete()
        }.collect(appScope)
        store.currentDeepest.drop(1).collect(appScope) { deepest ->
            deepest ?: return@collect
            val tab = store.currentTabKey.value
            val root = garden[tab].getSelectedRoot()
                ?.takeIf { it.type != NodeRootType.Photos && it.type != NodeRootType.Videos }
                ?: return@collect
            val new = Deepest(tabIndex = tab.index, rootId = root.id, deepest.ref)
            db.put(new)
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
            appScope.launchOnIO {
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
        val deepest = db.get(key.index, root.id)
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
            NodeRoot(NodeRootType.Photos, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_CAMERA),
            NodeRoot(NodeRootType.Videos, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_CAMERA),
            NodeRoot(NodeRootType.Screenshots, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_PIC_SCREENSHOTS, internalStorageRef + SUB_PATH_DCIM_SCREENSHOTS),
            NodeRoot(NodeRootType.Bluetooth, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_BLUETOOTH, internalStorageRef + SUB_PATH_DOWNLOAD_BLUETOOTH),
            NodeRoot(NodeRootType.Downloads, NodeSorting.Date.Reversed, internalStorageRef + SUB_PATH_DOWNLOAD),
            NodeRoot(NodeRootType.SystemRoot, NodeSorting.Name, NodeRef.Root),
        )
        this.roots.addAll(roots)
    }

    suspend fun tryToggleRoot(key: NodeTabKey, root: NodeRoot) {
        render(key) {
            val root = roots.find { it.id == root.id }
            when {
                root == null -> return
                selected(root) -> deselectRoot()
                else -> select(root)
            }
            if (tree.isEmpty()) {
                restoreTree()
            }
        }
        tryCache(key, root.item)
    }

    suspend fun tryToggle(key: NodeTabKey, item: Node) {
        var rootItem: Node? = null
        render(key) {
            val root = getSelectedRoot() ?: return
            if (tree.isEmpty() && root.item.uniqueId != item.uniqueId) {
                return
            }
            val item = findItem(item.uniqueId)
                ?.takeIf { it.hasChildren }
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
                tab.getSelectedRoot()?.type is NodeRootType.SystemRoot -> tab.deselectRoot()
            }
            tab.render()
            roots.forEach { root ->
                if (withSu || root.type !is NodeRootType.SystemRoot) {
                    updateRootAsync(key, root)
                }
            }
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        when {
            asSu -> Unit
            root.type !is NodeRootType.SystemRoot -> Unit
            else -> return
        }
        appScope.launch {
            garden(key) {
                withCachingState(root.id) {
                    var updated = root.item.update(asSu)
                    updated = when (updated.error) {
                        is NodeError.NoSuchFileOrDir -> tryAlternative(root, updated)
                        else -> updated
                    }
                    updateRootSync(updated, key, root)
                    if (root.type is NodeRootType.Screenshots) {
                        store.updateScreenshots(root.item.ref)
                    }
                }
            }
        }
    }

    private fun tryAlternative(root: NodeRoot, missing: Node): Node {
        val variants = root.pathVariants?.takeIf { it.isNotEmpty() }
        variants ?: return missing
        val items = variants.map { path ->
            path.toRoot(root.type).update(asSu)
        }
        val alt = items.find { it.error == null }
            ?: items.find { it.error !is NodeError.NoSuchFileOrDir }
        return alt ?: missing
    }

    private fun NodeGarden.updateStats(storage: NodeStorage) {
        val index = roots.indexOfFirst { it.type is NodeRootType.Storage && it.type.kind == storage.kind && it.item.ref.string == storage.path }
        var root = roots.getOrNull(index)
        var type = root?.type ?: NodeRootType.Storage(storage)
        type = (type as NodeRootType.Storage).copy(storage)
        root = root ?: NodeRoot(type, NodeSorting.Name, NodeRef(storage.path))
        roots.put(root) { it.id == root.id }
    }

    private fun NodeGarden.removeMissed(storage: List<NodeStorage>) {
        roots.removeAll { root ->
            root.type.removable && storage.none { it.path == root.item.ref.string }
        }
    }

    private fun filterMediaRootChildren(updated: Node, type: NodeRootType) {
        val onlyPhotos = type == NodeRootType.Photos || type == NodeRootType.Screenshots
        val onlyVideos = type == NodeRootType.Videos
        val onlyMedia = type == NodeRootType.Camera
        if (onlyPhotos || onlyVideos || onlyMedia) {
            updated.children?.update {
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
        val newestChild = updated.takeIf { targetRoot.withPreview }
            ?.sortBy(targetRoot.sorting)
            ?.children
            ?.firstOrNull()
        return when {
            newestChild == null -> targetRoot.copy(item = updated, thumbnail = null, thumbnailPath = "")
            targetRoot.thumbnailPath == newestChild.ref.string -> targetRoot
            else -> targetRoot.copy(item = updated, thumbnail = Thumbnail.FilePath, thumbnailPath = newestChild.ref.string)
        }
    }

    private suspend fun updateRootSync(updated: Node, key: NodeTabKey, targetRoot: NodeRoot) {
        filterMediaRootChildren(updated, targetRoot.type)
        val updatedRoot = updateRootThumbnail(updated, targetRoot)
        garden {
            states.updateState(updatedRoot.id) {
                nextState(updatedRoot.id, cachingJob = null)
            }
            val tab = get(key)
            roots.replace { root ->
                when (root.id) {
                    targetRoot.id -> {
                        val updatedItem = root.item.updateWith(updatedRoot.item, targetRoot.sorting)
                        val type = root.type.takeIf<NodeRootType.Storage,_>()?.run {
                            val stat = StatFs(root.item.ref.string)
                            val info = info.copy(total = stat.totalBytes, used = stat.totalBytes - stat.freeBytes)
                            copy(info = info)
                        } ?: root.type
                        if (tab.key == key) updatedRoot.copy(item = updatedItem, type = type) else root.copy(
                            type = root.type,
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
                    nextState(it.uniqueId, cachingJob = null)
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
            replaceItem(renamed)
            var index = tree.indexOf(item.ref)
            if (index >= 0) {
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

    suspend fun tryCopy(key: NodeTabKey, from: Node, to: Node, asMoving: Boolean) {
        render(key) {
            states.updateState(from.uniqueId) {
                nextState(from.uniqueId, copying = NodeOperation.Copying(isSource = true, asMoving = asMoving))
            }.let {
                if (it?.isCopying != true) return
            }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = NodeOperation.Copying(isSource = false))
            }
            findItem(to.uniqueId) { children, _, _ ->
                children ?: return@findItem
                var index = children.indexOfFirst { it.isFile }
                if (index < 0) index = children.size
                children.items.add(index, to)
                children.sortByName()
            }
        }
        val new = ExplorerUtils.copy(from, to, asSu)
        render(key) {
            states.updateState(from.uniqueId) {
                nextState(from.uniqueId, copying = null)
            }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = null)
            }
            new ?: return debugFail { "null after copy ${from.ref} to ${to.ref}" }
            findItem(from.uniqueId) { children, i, _ ->
                children?.items?.add(i.inc(), new)
            }
        }
        tryCache(key, to)
    }

    suspend fun tryCheck(key: ExplorerTabKey, refs: List<Node>, toChecked: Boolean) {
        garden(key) {
            val toRender = mutableListOf<Node>()
            for (item in refs) {
                val (_, state) = states.findState(item.uniqueId)
                if (state?.withOperation != true && checked.tryUpdateCheck(item.uniqueId, toChecked)) {
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
            var state = states.find { it.uniqueId == ref.uniqueId }
            if (state?.operation == installing) return false
            state = states.updateState(ref.uniqueId) {
                nextState(ref.uniqueId, installing = installing)
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
        val fileJob = appScope.launch {
            for (file in files) {
                file.delete(asSu)
                store.emitDeleted(file.copy(children = null))
            }
        }
        val dirJobs = dirs.map { dir ->
            appScope.launch {
                dir.delete(asSu)
                store.emitDeleted(dir.copy(children = null))
            }
        }
        fileJob.join()
        dirJobs.joinAll()
        store.emitDeleted(items)
    }

    suspend fun tryDelete(key: NodeTabKey, its: List<Node>) {
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
                        nextState(item.uniqueId, cachingJob = null, deleting = NodeOperation.Deleting)
                    }
                }
                findItem(item.uniqueId)
                    ?.takeIf { state?.isDeleting == true }
            }.let { items.addAll(it) }
        }
        val files = items.filter { !it.isDirectory }
        val dirs = items.filter { it.isDirectory }
        val deleted = CoroutineSafeList<Node>()
        debugDelay(1)
        val fileJob = appScope.launch {
            for (file in files) {
                if (file.deleteIn(key)) {
                    deleted.add(file)
                }
            }
        }
        val dirJobs = dirs.map { dir ->
            appScope.launch {
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
        delayedRender = delayedRender ?: appScope.launch {
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
        states.replace {
            if (it.empty) null else it
        }
        val roots = renderRoots()
        roots.find { it.isSelected }
            ?.takeIf { !trees.containsKey(it.id) }
            ?.let { putTree(it.id, listOf(it.item.ref)) }

        val rendered = renderItems(roots)
        flow.emit(rendered)

        updateStates(rendered.items)
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
                removeOneIf { it.type is NodeRootType.SystemRoot }
            }
        }
    }

    private fun NodeTab.findDeepest(): Node? {
        return tree.lastOrNull()
            ?.let { findItem(it.uniqueId) }
            ?.let { if (checked.contains(it.uniqueId)) it.copy(isChecked = true) else it }
    }

    private fun NodeTab.updateStates(items: List<Node>) {
        if (states.isNotEmpty()) {
            val iterator = states.listIterator()
            while (iterator.hasNext()) {
                val state = iterator.next()
                if (state.empty) continue
                var item = roots.find { it.id == state.uniqueId }?.item
                item = item ?: items.find { it.uniqueId == state.uniqueId }
                if (item == null) {
                    val next = state.nextState(state.uniqueId, cachingJob = null)
                    iterator.updateState(state, next)
                }
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
            state = states.find { it.uniqueId == item.uniqueId } ?: item.state,
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
        var state = states.find { it.uniqueId == id }
        if (state != null) return state.cachingJob
        val job = appScope.launch(start = CoroutineStart.LAZY, block = caching)
        state = states.updateState(id) {
            nextState(id, cachingJob = job)
        }
        require(state?.cachingJob === job)
        job.start()
        return null
    }

    private suspend fun cacheSync(key: NodeTabKey, item: Node) {
        var updated = item.update(asSu).sortByName()
        garden(key) {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
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
        appScope.launch {
            val size = NativeBridge.usage(item.ref, asSu).unwrapOr("")
            if (size != item.size) garden(key) {
                val current = findItem(item.uniqueId)
                current ?: return@launch
                val updated = current.copy(meta = item.meta.copy(size = size))
                val replaced = replaceItem(updated)
                if (replaced && !updated.areContentsTheSame(item)) {
                    renderUpdate(updated)
                }
            }
        }
    }

    private fun NodeStateImpl?.nextState(
        uniqueId: Int,
        cachingJob: Job? = this?.cachingJob,
        deleting: NodeOperation.Deleting? = this?.operation as? NodeOperation.Deleting,
        copying: NodeOperation.Copying? = this?.operation as? NodeOperation.Copying,
        installing: NodeOperation.Installing? = this?.operation as? NodeOperation.Installing,
    ): NodeStateImpl? {
        val nextOperation = when (this?.operation ?: NodeOperation.None) {
            is NodeOperation.None -> deleting ?: copying ?: installing
            is NodeOperation.Deleting -> deleting ?: copying
            is NodeOperation.Copying -> copying ?: deleting
            is NodeOperation.Installing -> installing ?: deleting
        } ?: NodeOperation.None
        val nextJob = when (cachingJob) {
            null -> null
            else -> this?.cachingJob ?: cachingJob
        }
        return when {
            nextJob == null && nextOperation is NodeOperation.None -> null
            theSame(nextJob, nextOperation) -> return this
            else -> NodeStateImpl(uniqueId, nextJob, nextOperation)
        }
    }

    private fun MutableList<NodeStateImpl>.updateState(
        uniqueId: Int,
        block: NodeStateImpl?.() -> NodeStateImpl?,
    ): NodeStateImpl? {
        val (index, state) = findState(uniqueId)
        val new = state.block()
        when {
            state == null && new != null -> add(new)
            state != null && new == null -> removeAt(index)
            state != null && new != null -> set(index, new)
        }
        return new
    }

    private fun MutableListIterator<NodeStateImpl>.updateState(current: NodeStateImpl?, new: NodeStateImpl?) {
        when {
            current == null && new != null -> add(new)
            current != null && new == null -> remove()
            current != null && new != null -> set(new)
        }
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
        var children = root.item.children
        for (level in tree) {
            if (level.uniqueId != root.item.uniqueId && children != null) {
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

    private fun List<NodeStateImpl>.findState(uniqueId: Int): Pair<Int, NodeStateImpl?> = findWithIndex { it.uniqueId == uniqueId }
}