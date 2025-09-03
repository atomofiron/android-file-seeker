package app.atomofiron.searchboxapp.utils

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.Date

fun Resources.formatDate(): String {
    val locale = ConfigurationCompat.getLocales(configuration)[0]
    return SimpleDateFormat(Const.DATE_PATTERN, locale).format(Date())
}
