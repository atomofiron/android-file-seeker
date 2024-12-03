package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession

class TextViewerStore {
    val sessions = mutableMapOf<Int, TextViewerSession>()
}
