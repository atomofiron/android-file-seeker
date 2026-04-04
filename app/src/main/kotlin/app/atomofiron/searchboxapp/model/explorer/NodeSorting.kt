package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.LazyThreadSafetyMode.NONE

@Serializable(with = NodeSortingSerializer::class)
sealed class NodeSorting(val reversed: Boolean) : DockItem.Id.Auto() {

    abstract val name: String

    sealed class Name(reversed: Boolean) : NodeSorting(reversed) {

        data object Reversed : Name(reversed = true) {

            override val name = "${Name.name}.$ReversedName"

            override fun toString() = name
        }

        companion object : Name(reversed = false) {

            override val name = "Name"

            override fun toString() = name
        }
    }

    sealed class Date(reversed: Boolean) : NodeSorting(reversed) {

        data object Reversed : Date(reversed = true) {

            override val name = "${Date.name}.$ReversedName"

            override fun toString() = name
        }

        companion object : Date(reversed = false) {

            override val name = "Date"

            override fun toString() = name
        }
    }

    sealed class Size(reversed: Boolean) : NodeSorting(reversed) {

        data object Reversed : Size(reversed = true) {

            override val name = "${Size.name}.$ReversedName"

            override fun toString() = name
        }

        companion object : Size(reversed = false) {

            override val name = "Size"

            override fun toString() = name
        }
    }

    companion object {

        const val BaseName = "NodeSorting"
        private const val ReversedName = "Reversed"

        val entries by lazy(NONE) { listOf(Name, Name.Reversed, Date, Date.Reversed, Size, Size.Reversed) }

        operator fun invoke(id: DockItem.Id): NodeSorting? = entries.find { it == id }
    }
}

private object NodeSortingSerializer : KSerializer<NodeSorting> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(NodeSorting.BaseName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NodeSorting) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): NodeSorting {
        val name = decoder.decodeString()
        return NodeSorting.entries.find { it.name == name }
            ?: throw SerializationException("Unknown ${NodeSorting.BaseName} name: $name")
    }
}