package app.atomofiron.searchboxapp.utils

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UriListSerializer : KSerializer<List<Uri>> by ListSerializer(UriSerializer)

object UriSerializer : KSerializer<Uri> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Uri = decoder.decodeString().toUri()
}