package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ClusteringLayoutTest {
    @Test
    fun `must create a valid layout`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val clusteringLayout = ClusteringLayout(epa)

        assertThat(clusteringLayout.isBuilt()).isFalse
        clusteringLayout.build()
        assertThat(clusteringLayout.isBuilt()).isTrue

        expectSelfie(clusteringLayout.joinToString { "," }).toMatchDisk()
    }

}