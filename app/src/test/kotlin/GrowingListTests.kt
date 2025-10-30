import app.atomofiron.common.util.GrowingList
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GrowingListTests {

    @Test
    fun iterator() {
        val growing = GrowingList<Unit>()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        val fetched = growing.fetch()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        var count = 0
        val iterator = fetched.iterator()
        while (iterator.hasNext()) {
            iterator.next()
            count++
        }

        assertEquals(3, count)
    }

    @Test
    fun get() {
        val growing = GrowingList<Unit>()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        val fetched = growing.fetch()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        try {
            fetched[growing.lastIndex]
            fail("fetched[${growing.lastIndex}] of ${fetched.size}")
        } catch (e: IndexOutOfBoundsException) {
            // pass
        }
    }

    @Test
    fun subList() {
        val growing = GrowingList<Unit>()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        val fetched = growing.fetch()
        growing.add(Unit)
        growing.add(Unit)
        growing.add(Unit)

        try {
            fetched.subList(3, growing.size)
            fail("subList(3, ${growing.size}) of ${fetched.size}")
        } catch (e: IndexOutOfBoundsException) {
            // pass
        }
    }
}