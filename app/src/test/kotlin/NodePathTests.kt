import app.atomofiron.searchboxapp.model.explorer.NodePath
import org.junit.Test
import kotlin.test.assertEquals

class NodePathTests {

    @Test
    fun parent() {
        listOf(
            "parent" to NodePath("/parent/child"),
            "parent" to NodePath("/parent/child/"),
            "parent" to NodePath("/parent/child//"),
            "/" to NodePath("/child"),
            "" to NodePath("child"),
            "" to NodePath("child/"),
            "" to NodePath(""),
            "" to NodePath("/"),
            "" to NodePath("//"),
        ).map { (expected, path) ->
            Triple(expected, path, path.parentString)
        }.filter { (expected, _, actual) ->
            actual != expected
        }.let {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun name() {
        listOf(
            "name" to NodePath("/sdcard/name"),
            "name" to NodePath("/sdcard/name/"),
            "name" to NodePath("/sdcard/name//"),
            "name" to NodePath("name"),
            "name" to NodePath("name/"),
            ".name" to NodePath(".name"),
            "." to NodePath("/sdcard/name/."),
            "." to NodePath("/sdcard/name/./"),
            ".." to NodePath("/sdcard/name/.."),
            ".." to NodePath("/sdcard/name/../"),
            "" to NodePath(""),
            "" to NodePath("/"),
            "" to NodePath("//"),
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
            "ext" to NodePath("name.ext"),
            "ext" to NodePath("/sdcard/name.ext"),
            "ext" to NodePath("/sdcard/name.ext/"),
            "ext" to NodePath("/sdcard/.ext"),
            "" to NodePath("/sdcard/name.ext/."),
            "" to NodePath("/sdcard/name.ext/./"),
            "" to NodePath("/sdcard/name.ext/.."),
            "" to NodePath("/sdcard/name.ext/../"),
            "" to NodePath("/sdcard/name/"),
            "" to NodePath("/sdcard/name.."),
            "" to NodePath("name/"),
            "" to NodePath("/"),
            "" to NodePath("///"),
            "" to NodePath(""),
        ).map { (expected, path) ->
            Triple(expected, path, path.ext)
        }.filter { (expected, _, actual) ->
            actual != expected
        }.let {
            assertEquals(emptyList(), it)
        }
    }
}