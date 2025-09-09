package app.atomofiron.searchboxapp.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.atomofiron.common.util.extension.debugDelay
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Notifications
import app.atomofiron.searchboxapp.android.receivingNotificationBuilder
import app.atomofiron.searchboxapp.android.updateChannel
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.canForegroundService
import app.atomofiron.searchboxapp.utils.formatDate
import app.atomofiron.searchboxapp.utils.writeTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

private const val KEY_DATA_BYTES = "KEY_DATA_BYTES"
private const val RECEIVED_UNKNOWN = -1L
private const val MB = 1024 * 1024

fun ReceiveData.toWorkerData() = Data.Builder()
    .putByteArray(KEY_DATA_BYTES, ProtoBuf.encodeToByteArray(this))
    .build()

class ReceiveWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val notifications = NotificationManagerCompat.from(context)
    private val notificationId = hashCode()
    private val progressStyle = NotificationCompat.ProgressStyle()
    private var progressScale = 1.0
    private val withNotification = context.canForegroundService()
    private val threadLimit = max(4, Runtime.getRuntime().availableProcessors() / 2)
    private val notificationBuilder = context.receivingNotificationBuilder().setStyle(progressStyle)

    override suspend fun doWork(): Result {
        if (withNotification) {
            notifications.updateChannel(
                Notifications.CHANNEL_ID_RECEIVE,
                context.getString(R.string.receive_notification_name),
            )
            updateProgress(RECEIVED_UNKNOWN)
        }
        return coroutineScope {
            withContext(Dispatchers.IO.limitedParallelism(threadLimit)) {
                work()
            }
        }
    }

    private suspend fun CoroutineScope.work(): Result {
        val bytes = inputData.getByteArray(KEY_DATA_BYTES)
        bytes ?: return Result.success()
            .also { toast(UniText(R.string.unknown_error)) }
        val data = ProtoBuf.decodeFromByteArray<ReceiveData>(bytes)
        val total = data.uris.size
        toast(UniText(R.plurals.receiving_files, total, total))
        val files = data.uris.map { uri ->
            val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
            var name: String? = null
            var size = 0L
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                    size = cursor.getLong(sizeIndex)
                }
            }
            val type = context.contentResolver.getType(uri)
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
            name = name ?: context.resources.formatDate()
            if (ext != null && !name.endsWith(".$ext", ignoreCase = true)) {
                name = "$name.$ext"
            }
            Triple(name, size, uri)
        }
        val totalSize = files.sumOf { (_, size, _) -> size }
        if (totalSize > Int.MAX_VALUE.toLong()) {
            progressScale = Int.MAX_VALUE / totalSize.toDouble()
        }
        val segments = files.map { (_, size, _) ->
            NotificationCompat.ProgressStyle.Segment((size * progressScale).toInt())
        }
        progressStyle.setProgressSegments(segments)
        debugDelay(3)
        val mutex = Mutex()
        var received = 0L
        val collecting = launch(Dispatchers.Default) {
            while (isActive) {
                delay(Const.MINI_DELAY)
                updateProgress(received)
            }
        }
        val deferred = files.map { (name, _, uri) ->
            async {
                val input = context.contentResolver.openInputStream(uri)
                input?.use { input ->
                    val output = FileOutputStream(File(data.destination, name))
                    val step = max(MB, input.available() / 100)
                    var read = 0L
                    input.writeTo(output) {
                        read += it
                        if (read >= step) {
                            mutex.withLock {
                                received += read
                                read = 0
                            }
                        }
                    }
                    mutex.withLock {
                        received += read
                    }
                }
                input != null
            }
        }
        val success = awaitAll(*deferred.toTypedArray()).count { it }
        toast(UniText(R.plurals.files_received, total, success, total))
        delay(Const.COMMON_DELAY)
        collecting.cancel()
        return Result.success()
    }

    private suspend fun toast(message: UniText) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.resources[message], Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun updateProgress(received: Long) {
        if (!withNotification) {
            return
        } else if (received == RECEIVED_UNKNOWN) {
            progressStyle.setProgressIndeterminate(true)
        } else {
            progressStyle.setProgressIndeterminate(false)
            progressStyle.setProgress((received * progressScale).toInt())
        }
        setForeground(foregroundInfo())
    }

    private fun foregroundInfo(): ForegroundInfo {
        val notification = notificationBuilder.build()
        return ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }
}