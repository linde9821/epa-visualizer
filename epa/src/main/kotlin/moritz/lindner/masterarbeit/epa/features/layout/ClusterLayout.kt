package moritz.lindner.masterarbeit.epa.features.layout

import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate

interface ClusterLayout : Layout {
    fun getClusterPolygons(): Map<Int, List<Coordinate>>
}