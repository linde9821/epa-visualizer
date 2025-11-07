package moritz.lindner.masterarbeit.epa.features.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import com.github.davidmoten.rtree2.internal.EntryDefault
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle

object RTreeBuilder {
    fun build(
        nodePlacements: List<NodePlacement>,
        epaProgressCallback: EpaProgressCallback? = null
    ): RTree<NodePlacement, PointFloat> {
        val total = nodePlacements.size
        val entries =
            nodePlacements.mapIndexed { index, nodePlacement ->
                epaProgressCallback?.onProgress(index, total, "Create R-Tree")
                EntryDefault(
                    nodePlacement,
                    PointFloat.create(
                        nodePlacement.coordinate.x,
                        nodePlacement.coordinate.y,
                    ),
                )
            }

        return if (entries.size < 10_000) {
            RTree.create(entries)
        } else {
            RTree.star().create(entries)
        }
    }

    fun Rectangle.toRTreeRectangle(): com.github.davidmoten.rtree2.geometry.Rectangle? = Geometries.rectangle(
        this.topLeft.x,
        this.topLeft.y,
        this.bottomRight.x,
        this.bottomRight.y,
    )
}
