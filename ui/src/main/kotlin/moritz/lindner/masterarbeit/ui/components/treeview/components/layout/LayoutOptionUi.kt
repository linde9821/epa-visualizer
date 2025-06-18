package moritz.lindner.masterarbeit.ui.components.treeview.components.layout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutSelection
import moritz.lindner.masterarbeit.ui.logger

@Composable
fun LayoutOptionUi(
    modifier: Modifier = Modifier,
    onUpdate: (LayoutConfig) -> Unit,
) {
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

    Column(
        modifier =
            modifier
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp),
                ).padding(4.dp),
        horizontalAlignment = Alignment.Companion.End,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Layout Settings", style = MaterialTheme.typography.h4)
        }

        Spacer(Modifier.height(10.dp))

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
}
