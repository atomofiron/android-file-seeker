package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.dropLast
import app.atomofiron.common.util.extension.clear
import app.atomofiron.common.util.extension.replace
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.debugDelay
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeGarden
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeState
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.NodeTab
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.isMedia
import app.atomofiron.searchboxapp.model.explorer.isMovie
import app.atomofiron.searchboxapp.model.explorer.isPicture
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.delete
import app.atomofiron.searchboxapp.utils.ExplorerUtils.rename
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveDirChildren
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveSize
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortByName
import app.atomofiron.searchboxapp.utils.ExplorerUtils.theSame
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateWith
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.findWithIndex
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.removeOneIf
import app.atomofiron.searchboxapp.utils.replaceEach
import app.atomofiron.searchboxapp.utils.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.math.min

private const val SUB_PATH_CAMERA = "DCIM/Camera/"
private const val SUB_PATH_PIC_SCREENSHOTS = "Pictures/Screenshots/"
private const val SUB_PATH_DCIM_SCREENSHOTS = "DCIM/Screenshots/"
private const val SUB_PATH_DOWNLOAD = "Download/"
private const val SUB_PATH_DOWNLOAD_BLUETOOTH = "Download/Bluetooth/"
private const val SUB_PATH_BLUETOOTH = "Bluetooth/"

class ExplorerService(
    private val context: Context,
    private val appScope: AppScope,
    private val store: ExplorerStore,
    private val preferenceStore: PreferenceStore,
) {
    private val previewSize = context.resources.getDimensionPixelSize(R.dimen.preview_size)
    private var delayedRender: Job? = null

    private var config = CacheConfig(useSu = false)
    private val garden = NodeGarden(store.firstTab, store.middleTab, store.lastTab)
    private val internalStoragePath = store.internalStorage.value.path

    init {
        val useSuDefined = Job()
        val toyboxDefined = Job()
        appScope.launch(Dispatchers.IO) {
            garden {
                useSuDefined.join()
                toyboxDefined.join()
                context.resolveToybox(preferenceStore.toyboxVariant.value)
                initRoots()
                get(store.currentTabKey.value).render()
            }
            store.storage.collect {
                updateRootsAsync(it)
            }
        }
        val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        preferenceStore.useSu.collect(appScope) {
            useSuDefined.complete()
            config = CacheConfig(it, thumbnailSize)
        }
        preferenceStore.toyboxVariant.collect(appScope) {
            toyboxDefined.complete()
            context.resolveToybox(it)
        }
        store.currentNode.collect(appScope) {
            preferenceStore.setOpenedDirPath(it?.path)
        }
    }

    private fun Context.resolveToybox(embedded: ToyboxVariant) {
        val variant = verify(embedded)
        Shell.toyboxPath = variant.path
        preferenceStore { setEmbeddedToybox(variant) }
    }

    fun getFlow(key: NodeTabKey): SharedFlow<NodeTabItems> = garden.getFlow(key)

    private fun NodeGarden.initRoots() {
        val roots = listOf(
            NodeRoot(NodeRootType.Photos, NodeSorting.Date.Reversed, "${internalStoragePath}$SUB_PATH_CAMERA"),
            NodeRoot(NodeRootType.Videos, NodeSorting.Date.Reversed, "${internalStoragePath}$SUB_PATH_CAMERA"),
            NodeRoot(NodeRootType.Screenshots, NodeSorting.Date.Reversed, "${internalStoragePath}$SUB_PATH_PIC_SCREENSHOTS", "${internalStoragePath}$SUB_PATH_DCIM_SCREENSHOTS"),
            NodeRoot(NodeRootType.Bluetooth, NodeSorting.Date.Reversed, "${internalStoragePath}$SUB_PATH_BLUETOOTH", "${internalStoragePath}$SUB_PATH_DOWNLOAD_BLUETOOTH"),
            NodeRoot(NodeRootType.Downloads, NodeSorting.Date.Reversed, "${internalStoragePath}$SUB_PATH_DOWNLOAD"),
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
        var uncached: Node? = null
        renderTab(key) {
            val root = getSelectedRoot() ?: return
            if (tree.isEmpty() && root.item.uniqueId != item.uniqueId) {
                return
            }
            val index = tree.indexOfFirst { it.uniqueId == item.uniqueId }
            if (tree.isEmpty()) {
                tree.add(root.item)
                uncached = root.item
            } else if (index == tree.lastIndex) {
                tree.dropLast()
            } else if (index >= 0) {
                tree.clear(from = index.inc())
            } else {
                val index = tree.indexOfFirst { it.path == item.parentPath }
                tree.clear(from = index.inc())
                uncached = tree[index].children
                    ?.find { it.path == item.path }
                    ?.also { tree.add(it) }
            }
        }
        uncached?.let { tryCache(key, it) }
    }

    suspend fun updateRootsAsync(volumes: List<NodeStorage>) {
        garden {
            volumes.forEach { updateStats(it) }
            removeMissed(volumes)
            val key = store.currentTabKey.value
            val tab = get(key)
            if (roots.none { it.stableId == tab.selectedRootId }) {
                tab.deselectRoot()
            }
            tab.render()
            roots.forEach { root ->
                updateRootAsync(key, root)
            }
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        appScope.launch {
            garden(key) {
                withCachingState(root.stableId) {
                    var updated = root.item.update(config)
                    updated = when (updated.error) {
                        is NodeError.NoSuchFile -> tryAlternative(root, updated)
                        else -> updated
                    }
                    // todo async updated.resolveDirChildren(config.useSu)
                    //updated = updated.copy(children = updated.children?.copy(isOpened = true))
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
        val index = roots.indexOfFirst { it.type is NodeRootType.Storage && it.type.kind == storage.kind && it.item.path == storage.path }
        var root = roots.getOrNull(index)
        var type = root?.type ?: NodeRootType.Storage(storage)
        type = (type as NodeRootType.Storage).copy(storage)
        root = root ?: NodeRoot(type, Node.asRoot(storage.path, type), NodeSorting.Name)
        roots.replace(root) { it.stableId == root.stableId }
    }

    private fun NodeGarden.removeMissed(storage: List<NodeStorage>) {
        roots.removeAll { root ->
            root.type.removable && storage.none { it.path == root.item.path }
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
            targetRoot.thumbnailPath == newestChild.path -> targetRoot
            else -> {
                val config = config.copy(thumbnailSize = previewSize, legacySizeBig = true)
                val updatedChild = newestChild.copy(content = NodeContent.Undefined).update(config)
                val content = updatedChild.content as? NodeContent.File
                targetRoot.copy(item = updated, thumbnail = content?.thumbnail as? Thumbnail.FilePath, thumbnailPath = newestChild.path)
            }
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
                        if (tab.key == key) updatedRoot.copy(item = updatedItem, type = root.type) else root.copy(
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
                .resolveDirChildren(config.useSu)
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

    suspend fun tryRename(key: NodeTabKey, it: Node, name: String) {
        val item = garden(key) {
            tree.findNode(it.uniqueId)
        }
        item ?: return
        // todo change uniqueId in state, create the new one state instance
        val renamed = item.rename(name, config.useSu)
        renderTab(key) {
            val level = tree.find(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children.items[index] = renamed
        }
    }

    suspend fun tryCreate(key: NodeTabKey, dir: Node, name: String, directory: Boolean) {
        val item = ExplorerUtils.create(dir, name, directory, config.useSu)
        renderTab(key) {
            val children = tree.find(dir.uniqueId)
                ?.children
                ?: tree.find(dir.parentPath)
                    ?.children
                    ?.find { it.uniqueId == dir.uniqueId }
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
            val parent = tree.find(to.parentPath)
            parent?.children?.run {
                var index = indexOfFirst { it.isFile }
                if (index < 0) index = size
                items.add(index, to)
                parent.sortByName()
            }
        }
        val new = ExplorerUtils.copy(from, to, config.useSu)
        renderTab(key) {
            states.updateState(from.uniqueId) {
                nextState(from.uniqueId, copying = null)
            }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = null)
            }
            tree.find(new.parentPath)?.children?.run {
                val index = indexOfFirst { it.uniqueId == new.uniqueId }
                if (index < 0) return@run
                items[index] = new
            }
        }
        tryCache(key, to)
    }

    suspend fun tryCheckItem(key: NodeTabKey, item: Node, isChecked: Boolean) {
        garden(key) {
            val (_, state) = states.findState(item.uniqueId)
            if (state?.withOperation == true) return
            if (!checked.tryUpdateCheck(item.uniqueId, isChecked)) return
            renderUpdate(item)
            renderChecked(key, item, isChecked)
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
    private fun MutableList<Int>.tryUpdateCheck(uniqueId: Int, makeChecked: Boolean): Boolean {
        val iter = iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            when {
                item != uniqueId -> Unit
                makeChecked -> return false
                else -> {
                    iter.remove()
                    return true
                }
            }
        }
        if (makeChecked) add(uniqueId)
        return makeChecked
    }

    suspend fun deleteEveryWhere(items: List<Node>) {
        // todo make good
        items.forEach {
            when {
                it.isDirectory -> appScope.launch {
                    it.delete(config.useSu)
                }
                else -> it.delete()
            }
        }
        store.emitDeleted(items)
    }

    private suspend fun Node.delete() {
        if (delete(config.useSu) == null) {
            store.emitRemoved(copy(children = null))
        }
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
                        checked.tryUpdateCheck(item.uniqueId, makeChecked = false)
                        nextState(item.uniqueId, cachingJob = null, deleting = NodeOperation.Deleting)
                    }
                }
                tree.findNode(item.uniqueId)
                    ?.takeIf { state?.isDeleting == true }
            }.let { items.addAll(it) }
        }
        val jobs = items.map { item ->
            appScope.launch {
                debugDelay(1)
                val result = item.delete(config.useSu)
                garden(key) {
                    tree.replaceItem(item.uniqueId, item.parentPath, result)
                    states.updateState(item.uniqueId) { null }
                    store.emitRemoved(item.copy(children = null))
                    lazyRender()
                }
            }
        }
        jobs.forEach { it.join() }
        store.emitDeleted(items)
        mediaRootAffected?.let { mediaRoot ->
            garden {
                updateRootAsync(key, mediaRoot)
            }
        }
    }

    suspend fun resetChecked(key: NodeTabKey) {
        renderTab(key) {
            checked.clear()
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
        states.replace {
            if (it.withoutState) null else it
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
    }

    private fun NodeTab.renderRoots(): List<NodeRoot> {
        return roots.mutate {
            replaceEach {
                when (it.type.stableId) {
                    selectedRootId -> it.copy(isSelected = true)
                    else -> it
                }
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
                if (state.withoutState) continue
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
                val item = items.find { it.uniqueId == uniqueId }
                if (item == null) iterator.remove()
            }
        }
    }

    private fun NodeTab.renderNodes(): List<Node> {
        val root = getSelectedRoot()
            ?: return emptyList()
        val count = min(1, tree.size) + tree.sumOf { it.childCount }
        val items = MutableList<Node>(count)
        tree.firstOrNull()
            .let { it ?: root.item }
            .let { updateStateFor(it).defineDirKind() }
            .run { copy(isDeepest = tree.size == 1, children = children?.fetch(isOpened = tree.isNotEmpty())) }
            .also { items.add(it) }
            .takeIf { !it.isOpened }
            ?.let { return items }
        val openedIndexes = mutableListOf<Int>()
        var parent = items.first()
        for (i in tree.indices) {
            val level = tree[i]
            val nextLevelId = tree.getOrNull(i.inc())?.uniqueId
            for (j in 0..<level.childCount) {
                var item = updateStateFor(level.children!![j])
                    .defineDirKind(i)
                val isOpened = item.uniqueId == nextLevelId
                item = item.copy(
                    isDeepest = isOpened && i == tree.lastIndex.dec(),
                    children = item.children?.fetch(isOpened = isOpened),
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
                updateStateFor(level.children!![j])
                    .defineDirKind(i)
                    .let { items.add(it) }
            }
            if (i < tree.lastIndex) {
                items.find { it.uniqueId == level.uniqueId }
                    ?.asSeparator()
                    ?.let { items.add(it) }
            }
        }
        return items
    }

    private suspend fun NodeTab.renderUpdate(new: Node) {
        val isOpened = tree.any { it.uniqueId == new.uniqueId }
        updateStateFor(new, children = new.children?.fetch(isOpened = isOpened))
            .defineDirKind()
            .let { store.emitUpdate(it) }
    }

    private fun renderChecked(key: NodeTabKey, new: Node, isChecked: Boolean) {
        store.checked.value.mutate {
            when {
                isChecked -> add(new.copy(isChecked = true))
                else -> removeOneIf { it.uniqueId == new.uniqueId }
            }
            store.emitChecked(key, this)
        }
    }

    private fun NodeTab.updateStateFor(item: Node, children: NodeChildren? = item.children): Node {
        val state = states.find { it.uniqueId == item.uniqueId }
        val isChecked = checked.find { it == item.uniqueId } != null
        when {
            state != null -> Unit
            isChecked != item.isChecked -> Unit
            children !== item.children -> Unit
            else -> return item
        }
        return item.copy(isChecked = isChecked, state = state ?: item.state, children = children)
    }

    private fun Node.defineDirKind(levelIndex: Int = -1): Node = when {
        levelIndex > 0 -> this
        !path.startsWith(internalStoragePath) -> this
        internalStoragePath.length != (path.length.dec() - name.length) -> this
        content !is NodeContent.Directory -> this
        else -> ExplorerUtils.getDirectoryType(name)
            .takeIf { it != DirectoryKind.Ordinary }
            ?.let { copy(content = content.copy(kind = it)) }
            ?: this
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
                tree.replaceItem(item.uniqueId, item.parentPath, null)
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
            val size = item.resolveSize(config.useSu)
            if (size == item.size) {
                return@launch
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

    private fun NodeState?.nextState(
        uniqueId: Int,
        cachingJob: Job? = this?.cachingJob,
        deleting: NodeOperation.Deleting? = this?.operation as? NodeOperation.Deleting,
        copying: NodeOperation.Copying? = this?.operation as? NodeOperation.Copying,
        installing: NodeOperation.Installing? = this?.operation as? NodeOperation.Installing,
    ): NodeState? {
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
            else -> NodeState(uniqueId, nextJob, nextOperation)
        }
    }

    private fun MutableList<NodeState>.updateState(
        uniqueId: Int,
        block: NodeState?.() -> NodeState?,
    ): NodeState? {
        val (index, state) = findState(uniqueId)
        val new = state.block()
        when {
            state == null && new != null -> add(new)
            state != null && new == null -> removeAt(index)
            state != null && new != null -> set(index, new)
        }
        return new
    }

    private fun MutableListIterator<NodeState>.updateState(current: NodeState?, new: NodeState?) {
        when {
            current == null && new != null -> add(new)
            current != null && new == null -> remove()
            current != null && new != null -> set(new)
        }
    }

    private fun MutableList<Node>.replaceItem(item: Node) = replaceItem(item.uniqueId, item.parentPath, item)

    private fun MutableList<Node>.replaceItem(uniqueId: Int, parentPath: String, item: Node?): Boolean {
        val parent = find(parentPath)
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

    private fun List<Node>.find(path: String): Node? = find { it.path == path }

    private fun List<Node>.findIndexed(path: String): Pair<Int, Node?> = findWithIndex { it.path == path }

    private fun List<NodeState>.findState(uniqueId: Int): Pair<Int, NodeState?> = findWithIndex { it.uniqueId == uniqueId }
}