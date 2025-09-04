import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import app.atomofiron.searchboxapp.utils.ExplorerUtils.name
import org.junit.Test
import kotlin.test.assertEquals

class Tests {
    @Test
    fun merge() {
        val input = listOf(
            Node("/a/b/c", rootId = 0, content = NodeContent.Unknown),
            Node("/a/b/", rootId = 0, content = NodeContent.Directory()),
            Node("/a/b/c", rootId = 0, content = NodeContent.Unknown),
            Node("/a/b", rootId = 0, content = NodeContent.Unknown),
            Node("/a/c", rootId = 0, content = NodeContent.Unknown),
            Node("/a/c", rootId = 0, content = NodeContent.Unknown),
        )
        val expected = listOf(
            Node("/a/b", rootId = 0, content = NodeContent.Unknown),
            Node("/a/c", rootId = 0, content = NodeContent.Unknown),
        )
        val actual = input.merge()
        assertEquals(expected, actual)
    }

    @Test
    fun name() {
        val input = mapOf(
            "//a/b/c" to "c",
            "/a/b/c" to "c",
            "/a/b/" to "b",
            "/a//" to "a",
            "/a/" to "a",
            "/a" to "a",
            "//" to "/",
            "/./" to ".",
            "./" to ".",
            "/." to ".",
            "/" to "/",
            "" to "",
            "a" to "a",
            "a/" to "a",
            "a/b" to "b",
            "a/b/" to "b",
        )
        val expected = input.values.map { it }
        val actual = input.keys.map { it.name() }
        assertEquals(expected, actual)
    }
}