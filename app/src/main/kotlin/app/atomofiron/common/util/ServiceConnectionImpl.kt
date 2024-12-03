package app.atomofiron.common.util

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class ServiceConnectionImpl : ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) = Unit
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit
}