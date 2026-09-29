package app.atomofiron.searchboxapp.screens.common

import app.atomofiron.common.util.Alert
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.toAlert

fun interface AlertConsumer {
    fun onAlert(alert: Alert)
}

fun <T> Rslt<T>.errToAlert(consumer: AlertConsumer) {
    err()?.run { consumer.onAlert(UniText(message).toAlert(error = true)) }
}