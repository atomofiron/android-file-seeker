package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import android.os.StatFs
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.dropLast
import app.atomofiron.common.util.extension.clear
import app.atomofiron.common.util.extension.debugDelay
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.common.util.extension.replace
import app.atomofiron.common.util.extension.takeIf
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.common.util.flow.TriggerFlow
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.android.verifyNativeBin
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.Node
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
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.delete
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.rename
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveDirChildren
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortByName
import app.atomofiron.searchboxapp.utils.ExplorerUtils.theSame
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateWith
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.findWithIndex
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.removeOneIf
import app.atomofiron.searchboxapp.utils.replaceEach
import app.atomofiron.searchboxapp.utils.showLongToast
import app.atomofiron.searchboxapp.utils.unwrapOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
    private val preferences: PreferenceStore,
) {
    private var delayedRender: Job? = null

    private var config = CacheConfig(asSu = false)
    private val garden = NodeGarden()
    private val internalStorageRef = store.internalStorage.value.ref
    private val updateRootTrigger = TriggerFlow()

    init {
        val suDefined = Job()
        NativeBridge.setBinDir(context.filesDir.absolutePath)
        appScope.launchOnIO {
            garden { // lock due configuration
                suDefined.join()
                if (config.asSu) checkSu()
                initRoots()
            }
            combine(store.storage, preferences.asSu, updateRootTrigger) { volumes, asSu, _ ->
                updateRootsAsync(volumes, asSu)
            }.collect()
        }
        val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        preferences.asSu.collect(appScope) {
            suDefined.complete()
            config = CacheConfig(it, thumbnailSize)
        }
        store.currentNode.collect(appScope) {
            preferences.setOpenedDirPath(it?.ref?.string)
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
        renderTab(key) {
            val root = roots.find { it.stableId == root.stableId }
            when {
                root == null -> return
                selected(root) -> deselectRoot()
                else -> select(root)
            }
        }
        tryCache(key, root.item)
    }

    suspend fun tryToggle(key: NodeTabKey, item: Node) {
        var rootItem: Node? = null
        renderTab(key) {
            val root = getSelectedRoot() ?: return
            if (tree.isEmpty() && root.item.uniqueId != item.uniqueId) {
                return
            }
            val index = tree.indexOfFirst { it.uniqueId == item.uniqueId }
            if (tree.isEmpty()) {
                rootItem = root.item.copy(children = root.item.children?.fetch())
                tree.add(rootItem)
            } else if (index == tree.lastIndex) {
                tree.dropLast()
            } else if (index >= 0) {
                tree.clear(from = index.inc())
            } else {
                val index = tree.indexOfFirst { it.ref == item.parentRef }
                rootItem = tree[index].children
                    ?.find { it.ref == item.ref }
                    ?.takeIf { it.hasChildren }
                    ?: return
                tree.clear(from = index.inc())
                tree.add(rootItem)
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
                roots.none { it.stableId == tab.selectedRootId } -> tab.deselectRoot()
                withSu -> Unit
                tab.selectedRootId == NodeRootType.SystemRoot.stableId -> tab.deselectRoot()
            }
            tab.render()
            roots.forEach { root ->
                if (withSu || root.stableId != NodeRootType.SystemRoot.stableId) {
                    updateRootAsync(key, root)
                }
            }
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        when {
            preferences.asSu.value -> Unit
            root.type !is NodeRootType.SystemRoot -> Unit
            else -> return
        }
        appScope.launch {
            garden(key) {
                withCachingState(root.stableId) {
                    var updated = root.item.update(config)
                    updated = when (updated.error) {
                        is NodeError.NoSuchFile -> tryAlternative(root, updated)
                        else -> updated
                    }
                    updateRootSync(updated, key, root)
                }
            }
        }
    }

    private fun tryAlternative(root: NodeRoot, missing: Node): Node {
        val variants = root.pathVariants?.takeIf { it.isNotEmpty() }
        variants ?: return missing
        val items = variants.map { path ->
            Node.asRoot(path, root.type).update(config)
        }
        val alt = items.find { it.error == null }
            ?: items.find { it.error !is NodeError.NoSuchFile }
        return alt ?: missing
    }

    private fun NodeGarden.updateStats(storage: NodeStorage) {
        val index = roots.indexOfFirst { it.type is NodeRootType.Storage && it.type.kind == storage.kind && it.item.ref.string == storage.path }
        var root = roots.getOrNull(index)
        var type = root?.type ?: NodeRootType.Storage(storage)
        type = (type as NodeRootType.Storage).copy(storage)
        root = root ?: NodeRoot(type, NodeSorting.Name, NodeRef(storage.path))
        roots.replace(root) { it.stableId == root.stableId }
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
            states.updateState(updatedRoot.stableId) {
                nextState(updatedRoot.stableId, cachingJob = null)
            }
            val tab = get(key)
            roots.replace { root ->
                when (root.stableId) {
                    targetRoot.stableId -> {
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
                }.also { updated ->
                    if (!tab.selected(updated)) return@also
                    val treeRoot = tab.tree.firstOrNull()
                    treeRoot ?: return@also
                    tab.tree[0] = updated.item
                }
            }
            tab.render()
            /*tabs.values.forEach { otherTab ->
                if (otherTab.key != key) otherTab.render()
            }*/
        }
    }

    suspend fun tryCache(key: NodeTabKey, item: Node) {
        garden(key) {
            roots.takeIf { item.isRoot }
                ?.find { it.item.uniqueId == item.uniqueId }
                ?.let { return updateRootAsync(key, it) }

            val current = tree
                .findNode(item.uniqueId)
                ?: return

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
                .resolveDirChildren(config.asSu)
            garden {
                states.updateState(it.uniqueId) {
                    nextState(it.uniqueId, cachingJob = null)
                }
                if (!done) {
                    return@withCachingState
                }
                val item = tree.findNode(it.uniqueId) ?: return@garden
                val items = item.children?.items ?: return@withCachingState
                items.forEachIndexed { index, current ->
                    val resolved = children.find { child -> child.uniqueId == current.uniqueId }
                    resolved ?: return@forEachIndexed
                    val updated = current.updateWith(resolved.content, resolved.properties)
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
            tree.findNode(ref.uniqueId)
        }
        item ?: return
        // todo change uniqueId in state, create the new one state instance
        val new = item.rename(name, config.asSu)
            ?: return debugFail { "null after rename $ref to $name" }
        renderTab(key) {
            val level = tree.find(item.parentRef)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children.items[index] = new
            val levelIndex = tree.indexOfFirst { it.ref == item.ref }
            if (levelIndex >= 0) {
                tree[levelIndex] = new
                var prev = new
                for (i in levelIndex.inc()..tree.lastIndex) {
                    val next = tree[i]
                    prev = prev.children?.find { it.name == next.name }
                        ?.also { tree[i] = it }
                        ?: debugFail { "No ${next.name} in ${prev.name} (children=${prev.children?.map { it.name }})" }
                            .let { break }
                }
            }
        }
    }

    suspend fun tryCreate(key: NodeTabKey, parent: Node, name: String, directory: Boolean) {
        val item = ExplorerUtils.create(parent, name, directory, config.asSu)
        item ?: return
        renderTab(key) {
            val children = tree.find(parent.uniqueId)
                ?.children
                ?: tree.find(parent.parentRef)
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
        renderTab(key) {
            states.updateState(from.uniqueId) {
                nextState(from.uniqueId, copying = NodeOperation.Copying(isSource = true, asMoving = asMoving))
            }.let { if (it?.isCopying != true) return }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = NodeOperation.Copying(isSource = false))
            }
            val parent = tree.find(to.parentRef)
            parent?.children?.run {
                var index = indexOfFirst { it.isFile }
                if (index < 0) index = size
                items.add(index, to)
                parent.sortByName()
            }
        }
        val new = ExplorerUtils.copy(from, to, config.asSu)
        renderTab(key) {
            states.updateState(from.uniqueId) {
                nextState(from.uniqueId, copying = null)
            }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = null)
            }
            new ?: return@renderTab debugFail { "null after copy ${from.ref} to ${to.ref}" }
            tree.find(new.parentRef)?.children?.run {
                val index = indexOfFirst { it.uniqueId == new.uniqueId }
                if (index < 0) return@run
                items[index] = new
            }
        }
        tryCache(key, to)
    }

    suspend fun tryCheck(key: NodeTabKey, refs: List<Node>, toChecked: Boolean) {
        garden(key) {
            val toRender = mutableListOf<Node>()
            for (item in refs) {
                val (_, state) = states.findState(item.uniqueId)
                if (state?.withOperation != true && checked.tryUpdateCheck(item.uniqueId, toChecked)) {
                    toRender.add(item)
                }
            }
            if (toRender.size == 1) {
                val item = tree.findNode(toRender.first().uniqueId)
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
                    val item = tab.tree.findNode(ref.uniqueId)
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
                file.delete(config.asSu)
                store.emitRemoved(file.copy(children = null))
            }
        }
        val dirJobs = dirs.map { dir ->
            appScope.launch {
                dir.delete(config.asSu)
                store.emitRemoved(dir.copy(children = null))
            }
        }
        fileJob.join()
        dirJobs.forEach { it.join() }
        store.emitDeleted(items)
    }

    suspend fun tryDelete(key: NodeTabKey, its: List<Node>) {
        var mediaRootAffected: NodeRoot? = null
        val items = mutableListOf<Node>()
        renderTab(key) {
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
                tree.findNode(item.uniqueId)
                    ?.takeIf { state?.isDeleting == true }
            }.let { items.addAll(it) }
        }
        val files = items.filter { !it.isDirectory }
        val dirs = items.filter { it.isDirectory }
        debugDelay(1)
        val fileJob = appScope.launch {
            for (file in files) {
                file.deleteIn(key)
            }
        }
        val dirJobs = dirs.map { dir ->
            appScope.launch {
                dir.deleteIn(key)
            }
        }
        fileJob.join()
        dirJobs.forEach { it.join() }
        store.emitDeleted(items)
        mediaRootAffected?.let { mediaRoot ->
            garden {
                updateRootAsync(key, mediaRoot)
            }
        }
    }

    private suspend fun Node.deleteIn(key: NodeTabKey) {
        val result = delete(config.asSu)
        garden(key) {
            tree.replaceItem(uniqueId, parentRef, result)
            states.updateState(uniqueId) { null }
            store.emitRemoved(copy(children = null))
            lazyRender()
        }
    }

    suspend fun resetChecked(key: NodeTabKey) {
        garden(key) {
            store.emitChecked(key, emptyList())
            checked.mapNotNull { tree.findNode(it) }
                .also { checked.clear() }
                .forEach { renderUpdate(it) }
        }
    }

    private suspend inline fun renderTab(key: NodeTabKey, block: NodeTab.() -> Unit) {
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
            ?.takeIf { !trees.containsKey(it.stableId) }
            ?.let { trees[it.stableId] = mutableListOf(it.item) }

        val deepest = findDeepest()
        val items = renderNodes()
        val tabItems = NodeTabItems(roots, items, deepest)
        flow.emit(tabItems)
        store.setDeepestNode(key, deepest)

        updateStates(items)
        updateChecked(items)
        val checked = items.filter { it.isChecked }
        store.emitChecked(key, checked)
        store.setCurrentItems(key, items)

        require(this.roots.all { !it.isSelected })
        incrementGeneration()
    }

    private fun NodeTab.renderRoots(): List<NodeRoot> {
        return roots.mutate {
            replaceEach {
                when (it.type.stableId) {
                    selectedRootId -> it.copy(isSelected = true)
                    else -> it
                }
            }
            if (!preferences.asSu.value) {
                removeOneIf { it.type is NodeRootType.SystemRoot }
            }
        }
    }

    private fun NodeTab.findDeepest(): Node? {
        return tree.lastOrNull()?.run {
            if (checked.contains(uniqueId)) copy(isChecked = true) else this
        }
    }

    private fun NodeTab.updateStates(items: List<Node>) {
        if (states.isNotEmpty()) {
            val iterator = states.listIterator()
            while (iterator.hasNext()) {
                val state = iterator.next()
                if (state.empty) continue
                var item = roots.find { it.stableId == state.uniqueId }?.item
                item = item ?: items.find { it.uniqueId == state.uniqueId }
                if (item == null) {
                    state.cachingJob?.cancel()
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

    private fun NodeTab.renderNodes(): List<Node> {
        val root = getSelectedRoot()
            ?: return emptyList()
        val count = tree.sumOf { it.childCount }.inc()
        val items = MutableList<Node>(count)
        tree.firstOrNull()
            .let { it ?: root.item }
            .let { renderNode(it, content = it.defineDirKind(), isOpened = tree.isNotEmpty(), isDeepest = tree.size == 1) }
            .also { items.add(it) }
            .takeIf { !it.isOpened }
            ?.let { return items }
        val openedIndexes = mutableListOf<Int>()
        val filteredCounts = when {
            mimeTypes.isEmpty() -> null
            mimeTypes == NodeContent.Directory.mimeTypes -> null
            else -> IntArray(tree.size)
        }
        var parent = items.first()
        for (i in tree.indices) {
            val level = tree[i]
            val nextLevelId = tree.getOrNull(i.inc())?.uniqueId
            for (j in 0..<level.childCount) {
                var item = level.children!![j]
                if (dismatch(item)) {
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
                items.add(item)
                if (isOpened) {
                    parent.children?.items[j] = item
                    parent = item
                    openedIndexes.add(j)
                    break
                }
            }
        }
        for (i in tree.indices.reversed()) {
            if (i == tree.lastIndex) continue
            val level = tree[i]
            val opened = openedIndexes[i]
            for (j in opened.inc() until level.childCount) {
                var item = level.children!![j]
                if (dismatch(item)) {
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
        return items
    }

    private fun IntArray.inc(i: Int) = set(i, get(i).inc())

    private fun NodeTab.dismatch(item: Node): Boolean = mimeTypes.isNotEmpty() && item.isFile && !item.content.matchesAny(mimeTypes)

    private suspend fun NodeTab.renderUpdate(new: Node) {
        store.emitUpdate(renderNode(new))
    }

    private fun renderChecked(key: NodeTabKey, item: Node, toChecked: Boolean) {
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
        var updated = item.update(config).sortByName()
        garden(key) {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
            }
            val current = tree.findNode(item.uniqueId)
            current ?: return
            if (updated.error is NodeError.NoSuchFile) {
                tree.replaceItem(item.uniqueId, item.parentRef, null)
                return render()
            }
            updated = current.updateWith(updated)
            // todo replace everywhere
            val replaced = tree.replaceItem(updated)
            when {
                !replaced -> return
                updated.isDirectory -> resolveDirChildren(updated)
            }
            if (!updated.areContentsTheSame(item)) {
                renderUpdate(updated)
            }
        }
    }

    private fun resolveSizeAsync(key: NodeTabKey, item: Node) {
        appScope.launch {
            val size = NativeBridge.usage(item.ref, config.asSu).unwrapOrNull()
            when (size) {
                null, item.size -> return@launch
            }
            garden(key) {
                val current = tree.findNode(item.uniqueId)
                current ?: return@launch
                val updated = current.copy(properties = item.properties.copy(size = size))
                val replaced = tree.replaceItem(updated)
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

    private fun MutableList<Node>.replaceItem(item: Node) = replaceItem(item.uniqueId, item.parentRef, item)

    private fun MutableList<Node>.replaceItem(uniqueId: Int, parentRef: NodeRef, item: Node?): Boolean {
        val parent = find(parentRef)
        val parentChildren = parent?.children?.items
        val index = parentChildren?.indexOfFirst { it.uniqueId == uniqueId } ?: -1
        var fails = 0
        when {
            parentChildren == null -> fails++
            index < 0 -> fails++
            item == null -> parentChildren.removeAt(index)
            else -> parentChildren[index] = item
        }
        val (currentIndex, current) = findIndexed(uniqueId)
        when {
            current == null -> fails++
            currentIndex < 0 -> fails++ // unreachable, always (-1, null)
            item == null -> removeAt(currentIndex)
            else -> set(currentIndex, item)
        }
        return fails < 2
    }

    private fun List<Node>.findNode(uniqueId: Int): Node? {
        val root = firstOrNull()
        if (root?.uniqueId == uniqueId) {
            return root
        }
        for (i in indices.reversed()) {
            get(i).children
                ?.find { it.uniqueId == uniqueId }
                ?.let { return it }
        }
        return null
    }

    private fun List<Node>.find(uniqueId: Int): Node? = find { it.uniqueId == uniqueId }

    private fun List<Node>.findIndexed(uniqueId: Int): Pair<Int, Node?> = findWithIndex { it.uniqueId == uniqueId }

    private fun List<Node>.find(ref: NodeRef): Node? = find { it.ref == ref }

    private fun List<Node>.findIndexed(ref: NodeRef): Pair<Int, Node?> = findWithIndex { it.ref == ref }

    private fun List<NodeStateImpl>.findState(uniqueId: Int): Pair<Int, NodeStateImpl?> = findWithIndex { it.uniqueId == uniqueId }
}