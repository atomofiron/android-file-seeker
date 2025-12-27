package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import app.atomofiron.common.util.property.MutableStrongProperty
import javax.inject.Inject
import javax.inject.Singleton

interface Strings {
    operator fun get(@StringRes resId: Int): String
}

@Singleton
class AppResources(resources: Resources) : MutableStrongProperty<Resources>(resources), Strings {

    @Inject constructor(context: Context) : this(context.resources)

    operator fun invoke() = value

    override fun get(resId: Int) = value.getString(resId)
}
