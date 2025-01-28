package app.atomofiron.searchboxapp.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.Button
import app.atomofiron.fileseeker.R

class ButtonStyle(
    private val filled: Pair<Drawable, ColorStateList>,
    private val outlined: Pair<Drawable, ColorStateList>,
) {
    companion object {
        operator fun invoke(context: Context): ButtonStyle {
            val inflater = LayoutInflater.from(context)
            val filled = inflater.inflate(R.layout.widget_button, null)
                .let { it as Button }
                .let { it.background to it.textColors }
            val outlined = inflater.inflate(R.layout.widget_button_outlined, null)
                .let { it as Button }
                .let { it.background to it.textColors }
            return ButtonStyle(filled, outlined)
        }
    }

    fun filled(button: Button) {
        button.background = filled.first
        button.setTextColor(filled.second)
    }

    fun outlined(button: Button) {
        button.background = outlined.first
        button.setTextColor(outlined.second)
    }
}