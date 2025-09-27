package moritz.lindner.masterarbeit.epa.features.serialization

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class JsonSerializationDeserializationTest {

    @Test
    fun `serializing and deserializing a epa recreate the same epa`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val serializer = JsonSerialization<Long>(epa)

        val jsonRepresentation = serializer.toJson()
        val deserializer = JsonDeserializer<Long>(String::toLong)

        val deserializedEpa = deserializer.fromJson(jsonRepresentation)

        assertThat(deserializedEpa.states).containsExactlyInAnyOrder(*(epa.states.toList().toTypedArray()))
        assertThat(deserializedEpa.transitions).containsExactlyInAnyOrder(*(epa.transitions.toList().toTypedArray()))
        assertThat(deserializedEpa.activities).containsExactlyInAnyOrder(*(epa.activities.toList().toTypedArray()))
    }
}