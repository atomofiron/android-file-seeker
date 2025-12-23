package app.atomofiron.searchboxapp.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import app.atomofiron.common.util.extension.debugDelay
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.flow.WaitingGate
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.receivingNotificationBuilder
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
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

private const val RECEIVED_UNKNOWN = -1L
private const val MB = 1024 * 1024

private val gates = mutableMapOf<UUID, WaitingGate>()
private val lock = Mutex()

private suspend fun get(id: UUID): WaitingGate = lock.withLock {
    gates.getOrPut(id) { WaitingGate() }
}

suspend fun OneTimeWorkRequest.waitForDataRead() = get(id).await()

private suspend fun ListenableWorker.dataRead() = get(id).finish()
class ReceiveWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val notificationId = hashCode()
    private val progressStyle = NotificationCompat.ProgressStyle()
    private var progressScale = 1.0
    private val withNotification = context.canForegroundService()
    private val threadLimit = max(4, Runtime.getRuntime().availableProcessors() / 2)
    private val notificationBuilder = context.receivingNotificationBuilder().setStyle(progressStyle)

    override suspend fun doWork(): Result {
        if (withNotification) {
            updateProgress(RECEIVED_UNKNOWN)
        }
        return coroutineScope {
            withContext(Dispatchers.IO(threadLimit)) {
                work()
            }
        }
    }

    //context(CoroutineScope) todo try in the future
    private suspend fun CoroutineScope.work(): Result {
        val data = inputData.get<ReceiveData>()
        data ?: return Result.success()
            .also { toastLong(UniText(R.string.unknown_error)) }
        val total = data.uris.size
        toastShort(UniText(R.plurals.receiving_files, total, total))
        val files = data.uris.map { uri ->
            val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
            var name: String? = null
            var size = 0L
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    name = nameIndex.takeIf { it >= 0 }?.let { cursor.getString(it) }
                    size = sizeIndex.takeIf { it >= 0 }?.let { cursor.getLong(it) } ?: 0L
                }
            }
            name = name ?: context.resources.formatDate().let {
                val type = context.contentResolver.getType(uri)
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
                "$it.${ext ?: "bin"}"
            }
            val stream = try {
                context.contentResolver.openInputStream(uri)
            } catch (_: SecurityException) {
                null
            }
            Triple(name, size, stream)
        }
        dataRead()
        val totalSize = files.sumOf { (_, size, _) -> max(0, size) }
            .coerceAtLeast(1)
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
        val deferred = files.map { (name, _, stream) ->
            async {
                stream?.use { input ->
                    var file = File(data.destination.string, name)
                    var i = 1
                    while (file.exists()) {
                        val builder = StringBuilder(name)
                        val index = when {
                            name.contains('.') -> name.lastIndexOf('.')
                            else -> name.length
                        }
                        builder.insert(index, "(${++i})")
                        file = File(data.destination.string, builder.toString())
                    }
                    val output = FileOutputStream(file)
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
                } != null
            }
        }
        val success = awaitAll(*deferred.toTypedArray()).count { it }
        toastLong(UniText(R.plurals.files_received, total, success, total))
        delay(Const.COMMON_DELAY)
        collecting.cancel()
        return Result.success()
    }

    private suspend fun toastShort(message: UniText) = toast(message, Toast.LENGTH_SHORT)

    private suspend fun toastLong(message: UniText) = toast(message, Toast.LENGTH_LONG)

    private suspend fun toast(message: UniText, duration: Int) {
        withContext(Dispatchers.Main.immediate) {
            Toast.makeText(context, context.resources[message], duration).show()
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