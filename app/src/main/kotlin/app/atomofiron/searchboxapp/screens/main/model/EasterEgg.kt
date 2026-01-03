package app.atomofiron.searchboxapp.screens.main.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import app.atomofiron.fileseeker.R

enum class EasterEgg(
    @ColorRes val colorId: Int,
    @DrawableRes val drawableId: Int,
) {
    Halloween(R.color.pumpkin, R.drawable.ic_egg_pumpkin),
    NewYear(R.color.ny_ball, R.drawable.ic_egg_ball),
    Clown(R.color.white, R.drawable.ic_egg_clown),
}