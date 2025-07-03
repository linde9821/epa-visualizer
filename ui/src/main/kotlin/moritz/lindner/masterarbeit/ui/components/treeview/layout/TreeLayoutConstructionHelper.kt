package moritz.lindner.masterarbeit.ui.components.treeview.layout

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.ui.components.treeview.components.degreesToRadians

object TreeLayoutConstructionHelper {
    fun build(
        config: LayoutConfig,
        epa: ExtendedPrefixAutomata<Long>,
    ): TreeLayout =
        when (config.layout.name) {
            "Walker Radial Tree" -> {
                RadialWalkerTreeLayout(
                    expectedCapacity = epa.states.size,
                    layerSpace = config.radius,
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
