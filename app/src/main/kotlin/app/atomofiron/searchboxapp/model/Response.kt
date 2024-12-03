package app.atomofiron.searchboxapp.model

class Response<D>(
    val recipient: String,
    val data: D,
) {
    inline fun get(recipient: String, crossinline action: (D) -> Unit) {
        if (this.recipient == recipient) {
            action(data)
        }
    }
}