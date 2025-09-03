package app.atomofiron.searchboxapp.model

class Response<D>(
    val recipient: Int,
    val data: D,
) {
    inline fun get(recipient: Int, crossinline action: (D) -> Unit) {
        if (this.recipient == recipient) {
            action(data)
        }
    }
}