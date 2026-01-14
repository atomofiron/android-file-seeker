package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color
import androidx.annotation.ColorInt
import app.atomofiron.searchboxapp.custom.drawable.PathBorder

data class DockStyle(
    @ColorInt
    val fill: Int,
    val translucent: Boolean,
    val popupBorder: PathBorder?,
) {
    companion object {
        val Stub = DockStyle(Color.TRANSPARENT, true, null)
    }
}