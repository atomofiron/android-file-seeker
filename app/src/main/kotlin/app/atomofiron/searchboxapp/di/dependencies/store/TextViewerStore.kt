package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextViewerStore @Inject constructor() {
    val sessions = mutableMapOf<Int, TextViewerSession>()
}
