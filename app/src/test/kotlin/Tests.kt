import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import org.junit.Test
import kotlin.test.assertEquals

class Tests {
    @Test
    fun merge() {
        val input = listOf(
            Node(NodeRef("/a/b/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodeRef("/a/b/"), rootId = 0, content = NodeContent.Directory()),
            Node(NodeRef("/a/b/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodeRef("/a/b"), rootId = 0, content = NodeContent.Unknown),
            Node(NodeRef("/a/c"), rootId = 0, content = NodeContent.Unknown),
            Node(NodeRef("/a/c"), rootId = 0, content = NodeContent.Unknown),
        )
        val expected = listOf(
            Node(NodeRef("/a/b"), rootId = 0, content = NodeContent.Unknown),
            Node(NodeRef("/a/c"), rootId = 0, content = NodeContent.Unknown),
        )
        val actual = input.merge()
        assertEquals(expected, actual)
    }
}