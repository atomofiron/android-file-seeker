package app.atomofiron.searchboxapp.model.preference

enum class AppLocale(val tag: String?) {
    System(null),
    En("en-US"),
    Ru("ru-RU"),
    Sr("sr-Cyrl-RS"),
    SrLatn("sr-Latn-RS"),
    ;
    companion object {
        operator fun get(index: Int) = entries.find { it.ordinal == index }!!
        operator fun get(localeTag: String) =  entries.find { it.tag == localeTag }
    }
}