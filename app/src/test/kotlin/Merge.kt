import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.utils.ExplorerUtils.merge
import junit.framework.Assert.assertEquals
import org.junit.Test

class Merge {
    @Test
    fun foo() {
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
}