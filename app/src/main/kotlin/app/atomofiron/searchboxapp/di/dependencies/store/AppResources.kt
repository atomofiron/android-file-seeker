package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.res.Resources
import androidx.annotation.StringRes
import app.atomofiron.common.util.property.MutableStrongProperty

interface Strings {
    operator fun get(@StringRes resId: Int): String
}

class AppResources(resources: Resources) : MutableStrongProperty<Resources>(resources), Strings {

    operator fun invoke() = value

    override fun get(resId: Int) = value.getString(resId)
}
