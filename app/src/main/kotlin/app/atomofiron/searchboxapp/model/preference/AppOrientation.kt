package app.atomofiron.searchboxapp.model.preference

import android.content.pm.ActivityInfo

enum class AppOrientation(val constant: Int) {
    UNDEFINED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    SENSOR(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
}