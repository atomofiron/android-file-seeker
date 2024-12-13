package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.Intent
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Intents.ACTION_UPDATE

object Intents {
    const val ACTION_UPDATE = "ACTION_UPDATE"
}

fun Context.updateIntent() = Intent(this, MainActivity::class.java).apply {
    action = ACTION_UPDATE
}