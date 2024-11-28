package app.atomofiron.searchboxapp.utils

import leakcanary.LeakCanary

// todo add wrappers for build variants
class AppWatcherProxy {

    var isEnabled: Boolean
        get() = LeakCanary.config.dumpHeap
        set(value) {
            LeakCanary.config = LeakCanary.config.copy(dumpHeap = value)
        }

    init {
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }
}
