package app.atomofiron.searchboxapp.utils

import androidx.annotation.IdRes
import com.google.android.material.button.MaterialButtonToggleGroup

fun MaterialButtonToggleGroup.check(@IdRes id: Int, checked: Boolean) {
    if (checked) check(id) else uncheck(id)
}
