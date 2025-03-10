package app.atomofiron.searchboxapp.injectable.store

import android.content.res.Resources
import app.atomofiron.common.util.property.MutableStrongProperty

class AppResources(resources: Resources) : MutableStrongProperty<Resources>(resources) {
    operator fun invoke() = value
}
