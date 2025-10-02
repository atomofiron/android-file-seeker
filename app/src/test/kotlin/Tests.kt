import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodePath
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import org.junit.Test
import kotlin.test.assertEquals

class Tests {
    @Test
    fun merge() {
        val input = listOf(
            Node(NodePath("/a/b/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodePath("/a/b/"), rootId = 0, content = NodeContent.Directory()),
            Node(NodePath("/a/b/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodePath("/a/b"), rootId = 0, content = NodeContent.Unknown),
            Node(NodePath("/a/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodePath("/a/c"), rootId = 0, content = NodeContent.Unknown),
        )
        val expected = listOf(
            Node(NodePath("/a/b"), rootId = 0, content = NodeContent.Unknown),
            Node(NodePath("/a/c"), rootId = 0, content = NodeContent.Unknown),
        )
        val actual = input.merge()
        assertEquals(expected, actual)
    }
}