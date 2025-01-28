package app.atomofiron.searchboxapp.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.materialColor
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UpdateNotification
import app.atomofiron.searchboxapp.utils.Codes
import app.atomofiron.searchboxapp.utils.immutable

object Notifications {
    const val CHANNEL_ID_UPDATE = "update_channel_id"
    const val CHANNEL_ID_FOREGROUND = "foreground_channel_id"
    const val CHANNEL_ID_RESULT = "result_channel_id"

    const val ID_FOREGROUND = 101
    const val ID_UPDATE = 102
}

fun Context.dismiss(notificationId: Int) = NotificationManagerCompat.from(this).cancel(notificationId)

fun Context.dismissUpdateNotification() = dismiss(Notifications.ID_UPDATE)

fun Context.showUpdateNotification(type: UpdateNotification) = tryShow { context ->
    if (Android.O) {
        updateNotificationChannel(
            Notifications.CHANNEL_ID_UPDATE,
            resources.getString(R.string.channel_name_updates),
            NotificationManager.IMPORTANCE_HIGH,
        )
    }
    val title = when (type) {
        is UpdateNotification.Available -> R.string.update_available
        is UpdateNotification.Install -> R.string.update_ready
    }.let { resources.getString(it) }
    NotificationCompat.Builder(context, Notifications.CHANNEL_ID_UPDATE)
        .setTicker(title)
        .setContentTitle(title)
        .setSmallIcon(R.drawable.ic_notification_update)
        .setContentIntent(PendingIntent.getActivity(context, Codes.UpdateApp, Intents.updating(context), FLAG_UPDATE_CURRENT.immutable()))
        .setColor(materialColor(MaterialAttr.colorPrimary))
        .build() to Notifications.ID_UPDATE
}

fun Context.showAppUpdatedNotification() = tryShow { context ->
    if (Android.O) {
        updateNotificationChannel(
            Notifications.CHANNEL_ID_UPDATE,
            resources.getString(R.string.channel_name_updates),
            NotificationManager.IMPORTANCE_HIGH,
        )
    }
    val title = resources.getString(R.string.update_installed)
    NotificationCompat.Builder(context, Notifications.CHANNEL_ID_UPDATE)
        .setTicker(title)
        .setContentTitle(title)
        .setSmallIcon(R.drawable.ic_notification_update)
        .setContentIntent(PendingIntent.getActivity(context, Codes.LaunchApp, Intents.mainActivity(context), FLAG_UPDATE_CURRENT.immutable()))
        .setColor(materialColor(MaterialAttr.colorPrimary))
        .build() to Notifications.ID_UPDATE
}

fun Context.updateNotificationChannel(
    id: String,
    name: String,
    importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
) {
    if (Android.O) {
        val manager = NotificationManagerCompat.from(this)
        var channel = manager.getNotificationChannelCompat(id)
        if (channel == null || channel.name != name) {
            channel = NotificationChannelCompat.Builder(id, importance).setName(name).build()
            manager.createNotificationChannel(channel)
        }
    }
}

private typealias NotificationId = Pair<Notification, Int>

inline fun Context.tryShow(action: NotificationManagerCompat.(Context) -> NotificationId): Boolean {
    if (Android.Below.T || checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
        val manager = NotificationManagerCompat.from(this)
        val (notification, id) = manager.action(this)
        manager.notify(id, notification)
        return true
    }
    return false
}
