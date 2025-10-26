package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
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

        val stateClusteringLayout = StateClusteringLayout(
            epa,
            config = LayoutConfig.StateClusteringLayoutConfig(
                useGraphEmbedding = true,
                umapK = 5,
                umapIterations = 200,
                useForceDirected = false,
                useResolveOverlap = false,
            )
        )

        assertThat(stateClusteringLayout.isBuilt()).isFalse
        stateClusteringLayout.build()
        assertThat(stateClusteringLayout.isBuilt()).isTrue

    }

}