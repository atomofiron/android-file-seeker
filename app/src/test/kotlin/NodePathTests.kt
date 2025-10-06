import app.atomofiron.searchboxapp.model.explorer.NodeRef
import org.junit.Test
import kotlin.test.assertEquals

class NodePathTests {

    @Test
    fun parent() {
        listOf(
            "parent" to NodeRef("/parent/child"),
            "parent" to NodeRef("/parent/child/"),
            "parent" to NodeRef("/parent/child//"),
            "/" to NodeRef("/child"),
            "" to NodeRef("child"),
            "" to NodeRef("child/"),
            "" to NodeRef(""),
            "" to NodeRef("/"),
            "" to NodeRef("//"),
        ).map { (expected, path) ->
            Triple(expected, path, path.parent.string)
        }.filter { (expected, _, actual) ->
            actual != expected
        }.let {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun name() {
        listOf(
            "name" to NodeRef("/sdcard/name"),
            "name" to NodeRef("/sdcard/name/"),
            "name" to NodeRef("/sdcard/name//"),
            "name" to NodeRef("name"),
            "name" to NodeRef("name/"),
            ".name" to NodeRef(".name"),
            "." to NodeRef("/sdcard/name/."),
            "." to NodeRef("/sdcard/name/./"),
            ".." to NodeRef("/sdcard/name/.."),
            ".." to NodeRef("/sdcard/name/../"),
            "" to NodeRef(""),
            "" to NodeRef("/"),
            "" to NodeRef("//"),
        ).map { (expected, path) ->
            Triple(expected, path, path.name)
        }.filter { (expected, _, actual) ->
            actual != expected
        }.let {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun ext() {
        listOf(
            "ext" to NodeRef("name.ext"),
            "ext" to NodeRef("/sdcard/name.ext"),
            "ext" to NodeRef("/sdcard/name.ext/"),
            "ext" to NodeRef("/sdcard/.ext"),
            "" to NodeRef("/sdcard/name.ext/."),
            "" to NodeRef("/sdcard/name.ext/./"),
            "" to NodeRef("/sdcard/name.ext/.."),
            "" to NodeRef("/sdcard/name.ext/../"),
            "" to NodeRef("/sdcard/name/"),
            "" to NodeRef("/sdcard/name.."),
            "" to NodeRef("name/"),
            "" to NodeRef("/"),
            "" to NodeRef("///"),
            "" to NodeRef(""),
        ).map { (expected, path) ->
            Triple(expected, path, path.ext)
        }.filter { (expected, _, actual) ->
            actual != expected
        }.let {
            assertEquals(emptyList(), it)
        }
    }
}