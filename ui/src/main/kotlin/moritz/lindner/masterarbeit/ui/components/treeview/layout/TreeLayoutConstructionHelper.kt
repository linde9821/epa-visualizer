package moritz.lindner.masterarbeit.ui.components.treeview.layout

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.ui.components.treeview.components.degreesToRadians

object TreeLayoutConstructionHelper {
    fun build(
        config: LayoutConfig,
        epa: ExtendedPrefixAutomata<Long>,
    ): TreeLayout =
        when (config.layout.name) {
            "Walker Radial Tree" -> {
                RadialWalkerTreeLayout(
                    layerSpace = config.radius,
                    expectedCapacity = epa.states.size,
                    margin = config.margin.degreesToRadians(),
                )
            }

            "Walker" -> {
                WalkerTreeLayout(
                    distance = config.margin,
                    yDistance = config.radius,
                    expectedCapacity = epa.states.size,
                )
            }

            "Direct Angular Placement" -> {
                DirectAngularPlacementTreeLayout(config.radius, epa.states.size)
            }

            else -> {
                TODO()
            }
        }
}
