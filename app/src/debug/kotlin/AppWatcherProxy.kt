package debug

import leakcanary.LeakCanary

class LeakWatcherImpl : LeakWatcher() {

    override val isAvailable = true

    override var isEnabled: Boolean
        get() = LeakCanary.config.dumpHeap
        set(value) {
            LeakCanary.config = LeakCanary.config.copy(dumpHeap = value)
        }

    init {
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }
}
