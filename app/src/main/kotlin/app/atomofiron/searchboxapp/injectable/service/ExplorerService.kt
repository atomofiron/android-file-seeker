package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import app.atomofiron.common.util.Unreachable
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.common.util.dropLast
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.*
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.close
import app.atomofiron.searchboxapp.utils.ExplorerUtils.completePath
import app.atomofiron.searchboxapp.utils.ExplorerUtils.delete
import app.atomofiron.searchboxapp.utils.ExplorerUtils.open
import app.atomofiron.searchboxapp.utils.ExplorerUtils.rename
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveDirChildren
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveSize
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortByName
import app.atomofiron.searchboxapp.utils.ExplorerUtils.theSame
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateWith
import app.atomofiron.searchboxapp.utils.endingDot
import app.atomofiron.searchboxapp.utils.writeTo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.min

class ExplorerService(
    context: Context,
    private val packageManager: PackageManager,
    private val appStore: AppStore,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
) {
    companion object {
        private const val SUB_PATH_CAMERA = "DCIM/Camera/"
        private const val SUB_PATH_PIC_SCREENSHOTS = "Pictures/Screenshots/"
        private const val SUB_PATH_DCIM_SCREENSHOTS = "DCIM/Screenshots/"
        private const val SUB_PATH_DOWNLOAD = "Download/"
        private const val SUB_PATH_DOWNLOAD_BLUETOOTH = "Download/Bluetooth/"
        private const val SUB_PATH_BLUETOOTH = "Bluetooth/"
    }

    private val scope = appStore.scope
    private val previewSize = context.resources.getDimensionPixelSize(R.dimen.preview_size)
    private var delayedRender: Job? = null

    private var config = CacheConfig(useSu = false)
    private val internalStoragePath = Environment
        .getExternalStorageDirectory()
        .absolutePath
        .completePath(directory = true)

    private val garden = NodeGarden()

    init {
        val useSuDefined = Job()
        val toyboxDefined = Job()
        scope.launch(Dispatchers.IO) {
            withGarden {
                useSuDefined.join()
                toyboxDefined.join()
                context.resolveToybox(preferenceStore.toyboxVariant.value)
            }
        }
        // todo try move out
        val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        preferenceStore.useSu.collect(scope) {
            useSuDefined.complete()
            config = CacheConfig(it, thumbnailSize)
        }
        preferenceStore.toyboxVariant.collect(scope) {
            toyboxDefined.complete()
            context.resolveToybox(it)
        }
        explorerStore.current.collect(scope) {
            preferenceStore.setOpenedDirPath(it?.path)
        }
    }

    private fun Context.resolveToybox(embedded: ToyboxVariant) {
        val variant = verify(embedded)
        Shell.toyboxPath = variant.path
        preferenceStore { setEmbeddedToybox(variant) }
    }

    suspend fun getOrCreateFlow(key: NodeTabKey): MutableSharedFlow<NodeTabItems> {
        return withGarden {
            getOrCreateFlowSync(key)
        }
    }

    fun getOrCreateFlowSync(key: NodeTabKey): MutableSharedFlow<NodeTabItems> {
        return garden.run {
            trees[key]?.run { return flow }
            val tree = NodeTab(key, states)
            tree.initRoots()
            trees[key] = tree
            scope.launch {
                withGarden(key) { tab ->
                    tab.updateRootsAsync()
                }
            }
            tree.flow
        }
    }

    suspend fun dropTab(key: NodeTabKey) {
        withGarden {
            trees.remove(key)
        }
    }

    private fun NodeTab.initRoots() {
        val storagePath = internalStoragePath
        val roots = listOf(
            NodeRoot(NodeRootType.Photos, NodeSort.Date.Reversed, "${storagePath}$SUB_PATH_CAMERA"),
            NodeRoot(NodeRootType.Videos, NodeSort.Date.Reversed, "${storagePath}$SUB_PATH_CAMERA"),
            NodeRoot(NodeRootType.Screenshots, NodeSort.Date.Reversed, "${storagePath}$SUB_PATH_PIC_SCREENSHOTS", "${storagePath}$SUB_PATH_DCIM_SCREENSHOTS"),
            NodeRoot(NodeRootType.Bluetooth, NodeSort.Date.Reversed, "${storagePath}$SUB_PATH_BLUETOOTH", "${storagePath}$SUB_PATH_DOWNLOAD_BLUETOOTH"),
            NodeRoot(NodeRootType.Downloads, NodeSort.Date.Reversed, "${storagePath}$SUB_PATH_DOWNLOAD"),
            NodeRoot(NodeRootType.InternalStorage(), NodeSort.Name, storagePath),
        )
        this.roots.clear()
        this.roots.addAll(roots)
    }

    suspend fun trySelectRoot(key: NodeTabKey, rootItem: NodeRoot) {
        renderTab(key) {
            selectedRootId = when (val typeId = rootItem.type.stableId) {
                selectedRootId -> 0
                else -> typeId
            }
        }
        tryCacheAsync(key, rootItem.item)
    }

    suspend fun tryToggle(key: NodeTabKey, it: Node) {
        renderTab(key) {
            val (levelIndex, parent) = tree.findIndexed(it.parentPath)
            val (index, item) = parent?.children
                ?.findIndexed(it.uniqueId)
                ?: (-1 to tree.find(it.uniqueId)) // click on the root item
            item?.children ?: return
            while (tree.size.dec() > levelIndex) {
                tree.dropLast()
            }
            val other = parent?.getOpenedIndex()
            if (parent?.children != null && other != null && other >= 0) {
                val node = parent.children[other]
                if (node.uniqueId != item.uniqueId) {
                    parent.children.items[other] = node.close()
                }
            }
            when {
                !item.isOpened -> {
                    val opened = item.open()
                    parent?.children?.items?.set(index, opened)
                    tree.add(opened)
                }
                item.hasOpened() -> { // just close a child
                    val sub = item.getOpenedIndex()
                    item.children.items[sub] = item.children[sub].close()
                    tree.add(item)
                }
                // close clicked item
                else -> parent?.children?.items?.set(index, item.close())
            }
        }
        tryCacheAsync(key, it)
    }

    suspend fun updateRootsAsync(key: NodeTabKey) {
        withGarden(key) { tab ->
            updateInternalStorageStats(tab)
            tab.render()
            tab.updateRootsAsync()
        }
    }

    private fun NodeTab.updateRootsAsync() {
        roots.forEach { root ->
            updateRootAsync(key, root)
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        scope.launch {
            withGarden {
                withCachingState(root.stableId) {
                    var updated = root.item.update(config)
                    updated = when (updated.error) {
                        !is NodeError.NoSuchFile -> updated
                        else -> tryAlternative(root, updated)
                    }
                    // todo async updated.resolveDirChildren(config.useSu)
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
        return when {
            alt == null -> missing
            !missing.isOpened -> alt
            alt.children == null -> alt
            else -> alt.copy(children = alt.children.copy(isOpened = true))
        }
    }

    private fun NodeGarden.updateInternalStorageStats(targetTab: NodeTab) {
        var root = targetTab.roots
            .find { it.type is NodeRootType.InternalStorage }
            ?: return Unreachable
        val statFs = StatFs(root.item.path)
        val freeBytes = statFs.freeBytes
        val totalBytes = statFs.totalBytes
        val type = (root.type as NodeRootType.InternalStorage).copy(used = totalBytes - freeBytes, free = freeBytes)
        trees.values.forEach { tab ->
            root = tab.roots.find { it.type is NodeRootType.InternalStorage }!!
            root = root.copy(type = type)
            tab.roots.replace(root) { it.stableId == root.stableId }
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
            ?.sortBy(targetRoot.sort)
            ?.children
            ?.firstOrNull()
        return when {
            newestChild == null -> targetRoot.copy(item = updated, thumbnail = null, thumbnailPath = "")
            targetRoot.thumbnailPath == newestChild.path -> targetRoot
            else -> {
                val config = config.copy(thumbnailSize = previewSize, legacySizeBig = true)
                val updatedChild = newestChild.copy(content = NodeContent.Unknown).update(config)
                val content = updatedChild.content as? NodeContent.File
                targetRoot.copy(item = updated, thumbnail = content?.thumbnail?.bitmap, thumbnailPath = newestChild.path)
            }
        }
    }

    private suspend fun updateRootSync(updated: Node, key: NodeTabKey, targetRoot: NodeRoot) {
        filterMediaRootChildren(updated, targetRoot.type)
        val updatedRoot = updateRootThumbnail(updated, targetRoot)
        withGarden(key) { currentTab ->
            states.updateState(updatedRoot.stableId) {
                nextState(updatedRoot.stableId, cachingJob = null)
            }
            trees.values.forEach { tab ->
                tab.roots.replace { root ->
                    when (root.stableId) {
                        targetRoot.stableId -> {
                            if (root.isSelected && !updatedRoot.item.isCached) {
                                tab.tree.clear()
                            }
                            val updatedItem = root.item.updateWith(updatedRoot.item, targetRoot.sort)
                            if (tab.key == key) updatedRoot.copy(item = updatedItem, type = root.type) else root.copy(
                                type = root.type,
                                thumbnail = updatedRoot.thumbnail,
                                thumbnailPath = updatedRoot.thumbnailPath,
                                item = updatedItem,
                            )
                        }
                        else -> root
                    }.also { updated ->
                        if (!updated.isSelected) return@also
                        val treeRoot = tab.tree.firstOrNull()
                        treeRoot ?: return@also
                        tab.tree[0] = updated.item
                    }
                }
            }
            currentTab.render()
            trees.values.forEach { otherTab ->
                if (otherTab.key != key) otherTab.render()
            }
        }
    }

    private inline fun <T> MutableList<T>.replace(new: T?, action: (T) -> Boolean) {
        replace {
            if (action(it)) new else it
        }
    }

    private inline fun <T> MutableList<T>.replace(action: (T) -> T?) {
        val iterator = listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val new = action(next)
            when {
                new === next -> Unit
                new == null -> iterator.remove()
                else -> iterator.set(new)
            }
        }
    }

    suspend fun tryCacheAsync(key: NodeTabKey, item: Node) {
        withGarden(key) { tab ->
            tab.roots
                .takeIf { item.isRoot }
                ?.find { it.item.uniqueId == item.uniqueId }
                ?.let { return updateRootAsync(key, it) }

            val current = tab.tree
                .findNode(item.uniqueId)
                ?: return

            withCachingState(current.uniqueId) {
                cacheSync(key, current)
                if (item.isDirectory) resolveSizeAsync(key, item)
            }
        }
    }

    private fun NodeGarden.resolveDirChildrenAsync(key: NodeTabKey, it: Node) {
        val children = it.children?.fetch() ?: return
        withCachingState(it.uniqueId) {
            val done = it.copy(children = children)
                .resolveDirChildren(config.useSu)
            renderTab(key, lazy = true) {
                states.updateState(it.uniqueId) {
                    nextState(it.uniqueId, cachingJob = null)
                }
                if (!done) {
                    return@withCachingState
                }
                val item = tree.findNode(it.uniqueId) ?: return@renderTab
                val items = item.children?.items ?: return@withCachingState
                items.forEachIndexed { index, current ->
                    val resolved = children.find { child -> child.uniqueId == current.uniqueId }
                    resolved ?: return@forEachIndexed
                    items[index] = current.updateWith(resolved.content, resolved.properties)
                }
            }
        }
    }

    suspend fun tryRename(key: NodeTabKey, it: Node, name: String) {
        val item = withTab(key) {
            tree.findNode(it.uniqueId)
        }
        item ?: return
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
                nextState(from.uniqueId, copying = Operation.Copying(isSource = true, asMoving = asMoving))
            }.let { if (it?.isCopying != true) return }
            states.updateState(to.uniqueId) {
                nextState(to.uniqueId, copying = Operation.Copying(isSource = false))
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
        tryCacheAsync(key, to)
    }

    suspend fun tryCheckItem(key: NodeTabKey, item: Node, isChecked: Boolean) {
        renderTab(key) {
            val (_, state) = states.findState(item.uniqueId)
            if (state?.withOperation == true) return
            if (!checked.tryUpdateCheck(item.uniqueId, isChecked)) return
        }
    }

    suspend fun tryMarkInstalling(tab: NodeTabKey?, item: Node, installing: Operation.Installing?): Boolean? {
        return withGarden {
            var state = states.find { it.uniqueId == item.uniqueId }
            if (state?.operation == installing) return false
            state = states.updateState(item.uniqueId) {
                nextState(item.uniqueId, installing = installing)
            }
            (state?.operation == installing).also {
                if (it) tab?.let { renderTab(tab) }
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
                it.isDirectory -> scope.launch {
                    it.delete(config.useSu)
                }
                else -> it.delete()
            }
        }
    }

    private suspend fun Node.delete() {
        if (delete(config.useSu) == null) {
            explorerStore.removed.emit(copy(children = null))
        }
    }

    suspend fun tryDelete(key: NodeTabKey, its: List<Node>) {
        var mediaRootAffected: NodeRoot? = null
        val items = mutableListOf<Node>()
        renderTab(key) {
            mediaRootAffected = roots.find { it.isSelected && it.withPreview }
            its.mapNotNull { item ->
                tree.findNode(item.uniqueId)?.takeIf {
                    val state = states.updateState(item.uniqueId) {
                        when (this?.isDeleting) {
                            true -> null
                            else -> {
                                this?.cachingJob?.cancel()
                                checked.tryUpdateCheck(item.uniqueId, makeChecked = false)
                                nextState(item.uniqueId, cachingJob = null, deleting = Operation.Deleting)
                            }
                        }
                    }
                    state?.isDeleting == true
                }
            }.let {
                items.addAll(it)
            }
        }
        val jobs = items.map { item ->
            scope.launch {
                val result = item.delete(config.useSu)
                withGarden(key) { tab ->
                    tab.tree.replaceItem(item.uniqueId, item.parentPath, result)
                    states.updateState(item.uniqueId) { null }
                    explorerStore.removed.emit(item.copy(children = null))
                    tab.render()
                }
            }
        }
        jobs.forEach { it.join() }
        mediaRootAffected?.let { mediaRoot ->
            withGarden {
                updateRootAsync(key, mediaRoot)
            }
        }
    }

    suspend fun tryReceive(where: Node, uri: Uri) {
        val inputStream = appStore.context.contentResolver.openInputStream(uri)
        inputStream ?: return
        val outputStream = FileOutputStream(File(""))
        val success = inputStream.writeTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    private suspend inline fun <R> withGarden(block: NodeGarden.() -> R): R = garden.withGarden(block)

    private suspend inline fun withGarden(key: NodeTabKey, block: NodeGarden.(NodeTab) -> Unit) {
        withGarden {
            get(key)?.let { block(it) }
        }
    }

    private suspend inline fun <R> withTab(key: NodeTabKey, block: NodeTab.() -> R): R? {
        return withGarden {
            get(key)?.block()
        }
    }

    private suspend fun renderTab(key: NodeTabKey) {
        withGarden {
            val tab = get(key) ?: return
            tab.render()
        }
    }

    private suspend inline fun renderTab(key: NodeTabKey, lazy: Boolean = false, block: NodeTab.() -> Unit) {
        withGarden {
            val tab = get(key) ?: return
            tab.block()
            if (lazy) renderTabLazily(key) else tab.render()
        }
    }

    private fun renderTabLazily(key: NodeTabKey) {
        if (delayedRender == null) {
            delayedRender = scope.launch {
                delay(128)
                delayedRender = null
                renderTab(key)
            }
        }
    }

    private suspend inline fun NodeTab.render() {
        delayedRender?.cancel()
        delayedRender = null
        states.replace {
            // todo NullPointerException
            if (it.withoutState) null else it
        }
        syncSelectedRoot()
        updateDirectoryTypes()
        val currentDir = tree.lastOrNull()
            ?.takeIf { it.isOpened }
            ?.run { if (checked.contains(uniqueId)) copy(isChecked = true) else this }
        val items = renderNodes()
        val tabItems = NodeTabItems(roots.toMutableList(), items, currentDir)
        flow.emit(tabItems)

        updateStates(items)
        updateChecked(items)
        val checked = items.filter { it.isChecked }
        explorerStore.searchTargets.set(checked)
        explorerStore.current.value = currentDir
        explorerStore.setCurrentItems(items)
    }

    private fun NodeTab.syncSelectedRoot() {
        roots.forEachIndexed { index, root ->
            if ((root.type.stableId == selectedRootId) != root.isSelected) {
                roots[index] = root.copy(
                    isSelected = root.type.stableId == selectedRootId,
                    item = root.item.copy(children = root.item.children?.copy(isOpened = true)),
                )
            }
        }
        syncSelectedRootWithTree()
    }

    private fun NodeTab.syncSelectedRootWithTree() {
        roots.find { it.isSelected }.let { selected ->
            if (selected?.item?.rootId != tree.firstOrNull()?.rootId) {
                tree.clear()
                selected?.placeOpened(tree)
            }
        }
    }

    private fun NodeRoot.placeOpened(tree: MutableList<Node>) {
        var opened: Node? = item
        while (opened != null) {
            tree.add(opened)
            opened = opened.children?.find { it.isOpened }
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
        var isEmpty = false
        var count = min(1, tree.size)
        count += tree.sumOf {
            if (it.isOpened) it.childCount else 0
        }
        val items = ArrayList<Node>(count)
        tree.firstOrNull()
            ?.let { if (it.isOpened && !it.hasOpened()) it.copy(isCurrent = true) else it }
            ?.also { items.add(updateStateFor(it)) }
            .let { if (it?.isOpened != true) return items }

        for (i in tree.indices) {
            val level = tree[i]
            for (j in 0..level.getOpenedIndex()) {
                var item = updateStateFor(level.children!![j])
                if (item.isOpened) {
                    val isDeepest = i == tree.lastIndex.dec()
                    item = item.copy(isCurrent = isDeepest, children = item.children?.fetch())
                    if (isDeepest) {
                        isEmpty = item.isEmpty
                    }
                }
                items.add(item)
            }
        }
        var skip = true
        for (i in tree.indices.reversed()) {
            val level = tree[i]
            when {
                isEmpty -> isEmpty = false
                skip -> skip = false
                i >= tree.lastIndex.dec() -> Unit
                else -> tree.getOrNull(i)?.getOpened()?.let {
                    val path = it.path.endingDot()
                    val item = Node(path, it.parentPath, rootId = it.rootId, children = it.children, properties = it.properties, content = it.content)
                    items.add(item)
                }
            }
            for (j in level.getOpenedIndex().inc() until level.childCount) {
                items.add(updateStateFor(level.children!![j]))
            }
        }
        return items
    }

    private fun NodeTab.updateStateFor(item: Node): Node {
        val state = states.find { it.uniqueId == item.uniqueId }
        val isChecked = checked.find { it == item.uniqueId } != null
        return when {
            state != null -> item.copy(state = state, isChecked = isChecked)
            isChecked -> item.copy(isChecked = true)
            else -> item
        }
    }

    private fun NodeTab.updateDirectoryTypes() {
        val defaultStoragePath = internalStoragePath
        val (_, level) = tree.findIndexed { it.parentPath == defaultStoragePath }
        level?.children ?: return
        for (i in level.children.indices) {
            val item = level.children[i]
            val content = item.content as? NodeContent.Directory
            content ?: continue
            if (content.type != Type.Ordinary) continue
            val type = ExplorerUtils.getDirectoryType(item.name)
            if (type == Type.Ordinary) continue
            level.children.items[i] = item.copy(content = content.copy(type = type))
        }
    }

    /** @return already existing caching job */
    private fun NodeGarden.withCachingState(id: Int, caching: suspend CoroutineScope.() -> Unit): Job? {
        var state = states.find { it.uniqueId == id }
        if (state != null) return state.cachingJob
        val job = scope.launch(start = CoroutineStart.LAZY, block = caching)
        state = states.updateState(id) {
            nextState(id, cachingJob = job)
        }
        require(state?.cachingJob === job)
        job.start()
        return null
    }

    private suspend fun cacheSync(key: NodeTabKey, item: Node) {
        var updated = item.update(config).sortByName()
        renderTab(key, lazy = true) {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
            }
            val current = tree.findNode(item.uniqueId)
            current ?: return
            if (updated.error is NodeError.NoSuchFile) {
                tree.replaceItem(item.uniqueId, item.parentPath, null)
                return@renderTab
            }
            updated = current.updateWith(updated)
            if (updated.isOpened != current.isOpened) {
                updated = updated.copy(children = updated.children?.copy(isOpened = current.isOpened))
            }
            // todo replace everywhere
            val replaced = tree.replaceItem(updated)
            when {
                !replaced -> return
                updated.isDirectory -> garden.resolveDirChildrenAsync(key, updated)
            }
        }
    }

    private fun resolveSizeAsync(key: NodeTabKey, item: Node) {
        scope.launch {
            val size = item.resolveSize(config.useSu)
            if (size == item.size) {
                return@launch
            }
            renderTab(key, lazy = true) {
                val current = tree.findNode(item.uniqueId)
                current ?: return@launch
                val updated = current.copy(properties = item.properties.copy(size = size))
                val replaced = tree.replaceItem(updated)
                if (!replaced) return@launch
            }
        }
    }

    private fun NodeState?.nextState(
        uniqueId: Int,
        cachingJob: Job? = this?.cachingJob,
        deleting: Operation.Deleting? = this?.operation as? Operation.Deleting,
        copying: Operation.Copying? = this?.operation as? Operation.Copying,
        installing: Operation.Installing? = this?.operation as? Operation.Installing,
    ): NodeState? {
        val nextOperation = when (this?.operation ?: Operation.None) {
            is Operation.None -> deleting ?: copying ?: installing
            is Operation.Deleting -> deleting ?: copying
            is Operation.Copying -> copying ?: deleting
            is Operation.Installing -> installing ?: deleting
        } ?: Operation.None
        val nextJob = when (cachingJob) {
            null -> null
            else -> this?.cachingJob ?: cachingJob
        }
        return when {
            nextJob == null && nextOperation is Operation.None -> null
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
            // todo IndexOutOfBoundsException: Index: 1, Size: 1
            // todo NullPointerException: Attempt to read from field 'java.lang.Object java.util.LinkedList$Node.item' on a null object reference in method 'java.lang.Object java.util.LinkedList.unlink(java.util.LinkedList$Node)'
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
            parentChildren[index].isOpened == item.isOpened -> parentChildren[index] = item
            else -> parentChildren[index] = item.open(!item.isOpened)
        }
        val (currentIndex, current) = findIndexed(uniqueId)
        when {
            current == null -> fails++
            currentIndex < 0 -> fails++ // unreachable, always (-1, null)
            item == null -> removeAt(currentIndex)
            current.isOpened == item.isOpened -> set(currentIndex, item)
            else -> set(currentIndex, item.open(!item.isOpened))
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

    private fun List<Node>.findIndexed(uniqueId: Int): Pair<Int, Node?> = findIndexed { it.uniqueId == uniqueId }

    private fun List<Node>.find(path: String): Node? = find { it.path == path }

    private fun List<Node>.findIndexed(path: String): Pair<Int, Node?> = findIndexed { it.path == path }

    // todo WTF 'NodeState.getUniqueId()' on a null object reference
    private fun List<NodeState>.findState(uniqueId: Int): Pair<Int, NodeState?> = findIndexed { it.uniqueId == uniqueId }

    private fun Node.getOpenedIndex(): Int = children?.indexOfFirst { it.isOpened } ?: -1

    private fun Node.getOpened(): Node? = getOpenedIndex().takeIf { it >= 0 }?.let { children?.get(it) }

    private fun Node.hasOpened(): Boolean = getOpenedIndex() >= 0
}