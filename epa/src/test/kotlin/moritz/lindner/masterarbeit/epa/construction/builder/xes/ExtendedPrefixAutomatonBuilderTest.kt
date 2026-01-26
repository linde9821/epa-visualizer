package moritz.lindner.masterarbeit.epa.construction.builder.xes

import com.diffplug.selfie.Selfie.expectSelfie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ExtendedPrefixAutomatonBuilderTest {
    @Test
    fun `must create expected epa`() {
        val sut =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        expectSelfie(sut.toString()).toMatchDisk()
    }

    @Test
    fun `must create expected large epa`() {
        val sut =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/large/BPI Challenge 2017 - Offer log.xes.gz"))
                .setEventLogMapper(BPI2017OfferChallengeEventMapper())
                .build()

        expectSelfie(sut.toString()).toMatchDisk()
    }

    @Test
    fun `EPAs constructed from logs with same events but intertwined times create a structurally equivalent epa`() {
        val b1 =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/time_1.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val b2 =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/time_1.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()


        assertThat(b1.toString()).isEqualTo(b2.toString())
    }
}
