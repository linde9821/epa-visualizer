package moritz.lindner.masterarbeit.ui.components.treeview.components.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.logger
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutSelection

@Composable
fun LayoutOptionUi(onUpdate: (LayoutConfig) -> Unit) {
    var showLayoutOptions by remember { mutableStateOf(false) }
    var radius by remember { mutableStateOf(120.0f) }
    var margin by remember { mutableStateOf(3.0f) }
    val layouts: List<LayoutSelection> =
        listOf(
            LayoutSelection("Walker Radial Tree"),
            LayoutSelection("Walker"),
            LayoutSelection(
                "Direct Angular Placement",
            ),
        )

    var layoutSelection by remember { mutableStateOf(layouts.first()) }

    Column(horizontalAlignment = Alignment.Companion.End) {
        if (showLayoutOptions) {
            LayoutSettings(
                radius = radius,
                onRadiusChange = {
                    radius = it
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin,
                            layout = layoutSelection,
                        ),
                    )
                },
                margin = margin,
                onMarginChange = {
                    margin = it
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin,
                            layout = layoutSelection,
                        ),
                    )
                },
                layouts = layouts,
                onLayoutSelectionChange = {
                    layoutSelection = it
                    logger.info { "setting layout to $it" }
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin,
                            layout = layoutSelection,
                        ),
                    )
                },
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Toggle layout options",
            modifier =
                Modifier.Companion
                    .clickable { showLayoutOptions = !showLayoutOptions }
                    .padding(top = 8.dp),
        )
    }
}
