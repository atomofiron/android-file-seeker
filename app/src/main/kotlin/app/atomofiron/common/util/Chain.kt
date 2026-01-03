package app.atomofiron.common.util


sealed class Cell<T> {
    open val value: T get() = throw Exception()
    var next: Cell<T>? = null

    class Link<T> : Cell<T>()
    class Data<T>(override val value: T) : Cell<T>()
}

class Chain<T : Any> {
    private var chain: Cell<T> = Cell.Link()

    fun push(value: T) {
        val link = Cell.Link<T>()
        val data = Cell.Data(value)
        link.next = data
        data.next = chain
        chain = link
    }

    fun pull(value: T): T? {
        var x: Cell<T>? = null
        var y: Cell<T>? = null
        var z: Cell<T> = chain
        if (z.next == null) {
            return null
        }
        while (z.next != null) {
            x = z
            y = z.next
            z = y!!.next!!
        }
        x!!.next = null
        return y!!.value
    }
}
