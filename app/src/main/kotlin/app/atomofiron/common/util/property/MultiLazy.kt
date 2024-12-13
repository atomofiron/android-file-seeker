package app.atomofiron.common.util.property


class MultiLazy<T : Any>(private var factory: () -> T?) : Lazy<T?> {

    private var nullable: T? = null

    override var value: T?
        set(value) {
            nullable = value
        }
        get() = nullable ?: run {
            nullable = factory()
            nullable
        }

    override fun isInitialized() = nullable != null
}