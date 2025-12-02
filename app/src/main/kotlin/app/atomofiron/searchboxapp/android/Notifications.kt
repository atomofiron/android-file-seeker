package app.atomofiron.searchboxapp.android

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.NoIcon
import app.atomofiron.common.util.withNotNull
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.other.UpdateNotification
import app.atomofiron.searchboxapp.utils.Codes
import app.atomofiron.searchboxapp.utils.canPostNotifications
import app.atomofiron.searchboxapp.utils.immutable

object Notifications {
    const val CHANNEL_ID_UPDATE = "update_channel_id"
    const val CHANNEL_ID_SEARCH = "search_channel_id"
    const val CHANNEL_ID_RESULT = "result_channel_id"
    const val CHANNEL_ID_RECEIVE = "receive_channel_id"
    const val CHANNEL_ID_OPERATIONS = "operations_channel_id"

    const val ID_UPDATE = 100
    const val ID_SCREENSHOTS = 200
}

fun Context.dismiss(notificationId: Int) = NotificationManagerCompat.from(this).cancel(notificationId)

fun Context.dismissUpdateNotification() = dismiss(Notifications.ID_UPDATE)

fun Context.showUpdateNotification(type: UpdateNotification) = notification(
    Notifications.CHANNEL_ID_UPDATE,
    R.string.channel_name_updates,
    NotificationManager.IMPORTANCE_HIGH,
) { context ->
    val title = when (type) {
        is UpdateNotification.Available -> R.string.update_available
        is UpdateNotification.Install -> R.string.update_ready
    }.let { getString(it) }
    setTicker(title)
    setContentTitle(title)
    setSmallIcon(R.drawable.ic_notification_update)
    setContentIntent(PendingIntent.getActivity(context, Codes.UPDATE_APP, Intents.updating(context), FLAG_UPDATE_CURRENT.immutable()))
    build()
}.tryShow(Notifications.ID_UPDATE, this)

fun Context.showAppUpdatedNotification() = notification(
    Notifications.CHANNEL_ID_UPDATE,
    R.string.channel_name_updates,
    NotificationManager.IMPORTANCE_HIGH,
) { context ->
    val title = getString(R.string.update_installed)
    setTicker(title)
    setContentTitle(title)
    setSmallIcon(R.drawable.ic_notification_update)
    setContentIntent(PendingIntent.getActivity(context, Codes.LAUNCH_APP, Intents.mainActivity(context), FLAG_UPDATE_CURRENT.immutable()))
    build()
}.tryShow(Notifications.ID_UPDATE, this)

fun Service.startScreenshotsForeground() = notification(
    Notifications.CHANNEL_ID_OPERATIONS,
    R.string.channel_name_screenshot_operations,
    NotificationManager.IMPORTANCE_MIN,
    vibration = false,
    badge = false,
    lights = false,
    sound = false,
) { context ->
    val title = getString(R.string.screenshot_operations_active)
    setTicker(title)
    setContentTitle(title)
    setVisibility(NotificationCompat.VISIBILITY_SECRET)
    setSmallIcon(R.drawable.ic_screenshot_operations)
    setContentIntent(PendingIntent.getActivity(context, Codes.LAUNCH_APP, Intents.mainActivity(context), FLAG_UPDATE_CURRENT.immutable()))
    setSound(null)
    setVibrate(null)
    build()
}.also {
    startForeground(Notifications.ID_SCREENSHOTS, it)
}

fun Context.showScreenshotOperations(ref: NodeRef) = notification(
    Notifications.CHANNEL_ID_OPERATIONS,
    R.string.channel_name_screenshot_operations,
) { context ->
    val bitmap = BitmapFactory.decodeFile(ref.string)
    val style = NotificationCompat.BigPictureStyle().bigPicture(bitmap)
    val flags = FLAG_UPDATE_CURRENT.immutable()
    val openIntent = PendingIntent.getActivity(context, ref.uniqueId + 1, Intents.openWith(context, ref), flags)
    val sendIntent = PendingIntent.getActivity(context, ref.uniqueId + 2, Intents.shareWith(context, ref), flags)
    val deleteIntent = PendingIntent.getBroadcast(context, ref.uniqueId + 3, Intents.deleteScreenshot(context, ref), flags)
    setSmallIcon(R.drawable.ic_screenshot_operations)
    setContentTitle(getString(R.string.pref_screenshot_operations))
    setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
    setStyle(style)
    addAction(NoIcon, getString(R.string.open), openIntent)
    addAction(NoIcon, getString(R.string.share), sendIntent)
    addAction(NoIcon, getString(R.string.delete), deleteIntent)
    setSound(null)
    setVibrate(null)
    build()
}.tryShow(ref.uniqueId, this)

fun Context.receivingNotificationBuilder(): NotificationCompat.Builder = notification(
    Notifications.CHANNEL_ID_RECEIVE,
    R.string.channel_name_receive,
    sound = false
) {
    setContentTitle(getString(R.string.receiving))
    setSmallIcon(R.drawable.ic_progress_download)
    setOnlyAlertOnce(true)
    setSound(null)
}

/** on Android 8+ creates the new channel if doesn't exist */
inline fun <T> Context.notification(
    channelId: String,
    @StringRes channelName: Int,
    importance: Int = IMPORTANCE_DEFAULT,
    badge: Boolean? = null,
    vibration: Boolean? = null,
    lights: Boolean? = null,
    sound: Boolean? = null,
    action: NotificationCompat.Builder.(Context) -> T,
): T {
    if (Android.O) {
        val manager = NotificationManagerCompat.from(this)
        var channel = manager.getNotificationChannelCompat(channelId)
        if (channel == null) {
            channel = NotificationChannelCompat.Builder(channelId, importance)
                .setName(getString(channelName))
                .withNotNull(vibration) { setVibrationEnabled(it) }
                .withNotNull(badge) { setShowBadge(it) }
                .withNotNull(lights) { setLightsEnabled(it) }
                .withNotNull(sound?.takeIf { !it }) { setSound(null, null) }
                .build()
            manager.createNotificationChannel(channel)
        }
    }
    return NotificationCompat.Builder(this, channelId)
        .run { action(this@notification) }
}

inline fun Notification.flags(action: (Int) -> Int): Notification {
    flags = action(flags)
    return this
}

fun Notification.tryShow(id: Int, context: Context): Boolean {
    return context.canPostNotifications().also {
        @SuppressLint("MissingPermission")
        if (it) NotificationManagerCompat.from(context).notify(id, this)
    }
}
