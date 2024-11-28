package app.atomofiron.searchboxapp.android

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import com.google.android.material.R as MaterialR

object Notifications {
    const val NOTIFICATION_CHANNEL_ID_UPDATE = "channel_update"
    const val NOTIFICATION_ID_UPDATE = 9485

    fun cancel(context: Context, id: Int) = NotificationManagerCompat.from(context).cancel(id)


    fun createUpdateChannel(context: Context) = createChannel(context, NOTIFICATION_CHANNEL_ID_UPDATE, R.string.channel_name_updates)

    fun update(context: Context, action: String, titleId: Int, actionId: Int) {
        createUpdateChannel(context)
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intents.mainActivity(context, action)
        val notificationIntent = PendingIntent.getActivity(context, Intents.REQUEST_UPDATE, intent, FLAG_UPDATE_CURRENT)
        val actionIntent = PendingIntent.getActivity(context, Intents.REQUEST_UPDATE, intent, FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_UPDATE)
            .setTicker(context.getString(titleId))
            .setContentTitle(context.getString(titleId))
            .setSmallIcon(R.drawable.ic_explorer_folder)
            .setContentIntent(notificationIntent)
            .addAction(0, context.getString(actionId), actionIntent)
            .setColor(context.findColorByAttr(MaterialR.attr.colorPrimary))
            .build()
        notificationManager.notify(NOTIFICATION_ID_UPDATE, notification)
    }

    private fun createChannel(context: Context, id: String, nameId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        var channel = notificationManager.getNotificationChannelCompat(id)
        if (channel == null) {
            channel = NotificationChannelCompat.Builder(id, IMPORTANCE_HIGH)
                .setName(context.getString(nameId))
                .build()
            notificationManager.createNotificationChannel(channel)
        }
    }
}