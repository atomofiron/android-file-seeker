package app.atomofiron.searchboxapp.android

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore.Downloads
import android.provider.MediaStore.Files.FileColumns
import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Sdk
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.logE
import app.atomofiron.searchboxapp.di.dependencies.BluetoothFilesProvider
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRootSrc
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import java.io.File

interface BluetoothFilesObserver {
    fun register()
    fun unregister()
}

@RequiresApi(Sdk.Q)
class BluetoothFilesObserverImpl(
    private val context: Context,
    private val provider: BluetoothFilesProvider,
) : ContentObserver(Handler(Looper.getMainLooper())), BluetoothFilesObserver {

    private val projection = arrayOf(FileColumns._ID, FileColumns.DATA, FileColumns.DATE_ADDED, FileColumns.SIZE, FileColumns.OWNER_PACKAGE_NAME)
    private val selection = "${Downloads.OWNER_PACKAGE_NAME} = 'com.android.bluetooth'"

    private val idToNode = mutableMapOf<Long, Node>()

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri ?: return debugFail { "onChange uri null" }
        val item = context.contentResolver
            .getBluetoothFiles(uri)
            .firstOrNull()
        when (item) {
            null -> uri.pathSegments
                .lastOrNull()
                ?.toLongOrNull()
                ?.let { idToNode.remove(it) }
                ?.also { provider.remove(it) }
            else -> provider.add(item)
        }
    }

    override fun register() {
        context.contentResolver
            .registerContentObserver(Downloads.EXTERNAL_CONTENT_URI, true, this)
        context.contentResolver
            .getBluetoothFiles(Downloads.EXTERNAL_CONTENT_URI)
            .let { provider.update(it) }
    }

    override fun unregister() {
        context.contentResolver
            .unregisterContentObserver(this)
        idToNode.clear()
    }

    private fun ContentResolver.getBluetoothFiles(uri: Uri): List<Node> = buildList {
        val cursor = query(uri, projection, selection, null, null)
        try {
            cursor?.run {
                val dataColumn = getColumnIndexOrThrow(FileColumns.DATA)
                val dateColumn = getColumnIndexOrThrow(FileColumns.DATE_ADDED)
                val sizeColumn = getColumnIndexOrThrow(FileColumns.SIZE)
                val idColumn = getColumnIndexOrThrow(FileColumns._ID)
                while (moveToNext()) {
                    val path = getString(dataColumn)
                    if (!path.isNullOrEmpty()) {
                        val file = File(path)
                        if (file.exists()) {
                            val id = getLong(idColumn)
                            val timestamp = getLong(dateColumn)
                            val size = getLong(sizeColumn).toULong()
                            val ref = NodeRef(file.absolutePath)
                            val parent = NodeRootSrc.Bluetooth.ref
                            val meta = NodeMeta(length = size, timestamp = timestamp)
                            val node = ref.toNode(parent.uniqueId, parent, meta = meta)
                            idToNode[id] = node
                            add(node)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logE(e.toString())
        } finally {
            cursor?.close()
        }
    }
}