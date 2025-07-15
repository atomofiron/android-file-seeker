package app.atomofiron.common.arch

interface Registerable {
    fun register()

    companion object {
        operator fun invoke(vararg registerable: Registerable) = object : Registerable {
            override fun register() {
                for (it in registerable) {
                    it.register()
                }
            }
        }
    }
}