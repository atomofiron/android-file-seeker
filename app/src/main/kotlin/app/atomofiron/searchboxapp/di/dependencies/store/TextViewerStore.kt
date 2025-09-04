package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession

class TextViewerStore {
    val sessions = mutableMapOf<Int, TextViewerSession>()
}
