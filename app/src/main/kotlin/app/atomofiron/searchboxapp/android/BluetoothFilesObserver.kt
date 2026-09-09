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
import app.atomofiron.searchboxapp.model.explorer.NodeRef
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

    private val projection = arrayOf(FileColumns.DATA, FileColumns.OWNER_PACKAGE_NAME, FileColumns._ID)
    private val selection = "${Downloads.OWNER_PACKAGE_NAME} = 'com.android.bluetooth'"

    private val idToRef = mutableMapOf<Long, NodeRef>()

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
                ?.let { idToRef.remove(it) }
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
        idToRef.clear()
    }

    private fun ContentResolver.getBluetoothFiles(uri: Uri): List<NodeRef> = buildList {
        val cursor = query(uri, projection, selection, null, null)
        try {
            cursor?.run {
                val dataColumn = getColumnIndexOrThrow(FileColumns.DATA)
                val idColumn = getColumnIndexOrThrow(FileColumns._ID)
                while (moveToNext()) {
                    val path = getString(dataColumn)
                    if (!path.isNullOrEmpty()) {
                        val file = File(path)
                        if (file.exists()) {
                            val id = getLong(idColumn)
                            val ref = NodeRef(file.absolutePath)
                            idToRef[id] = ref
                            add(ref)
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