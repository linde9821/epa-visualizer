package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewLowerState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Details
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Filter
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Layout
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.None
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Project
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.defaultTabStyle

@Composable
fun ToolbarUi(
    upperState: EpaViewUpperState,
    onUpperStateChange: (EpaViewUpperState) -> Unit,
    lowerState: EpaViewLowerState,
    onLowerStateChange: (EpaViewLowerState) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            IconButton(onClick = onClose) {
                Icon(key = AllIconsKeys.General.Close, contentDescription = "Close", modifier = Modifier.size(23.dp))
            }

            ToolbarButton(
                iconKey = AllIconsKeys.Actions.ProjectDirectory,
                contentDescription = "Project",
                tooltip = "Configure project settings like the mapper.",
                isSelected = upperState == Project,
                onClick = { onUpperStateChange(if (upperState != Project) Project else None) }
            )

            ToolbarButton(
                iconKey = AllIconsKeys.General.Filter,
                contentDescription = "Filter",
                tooltip = "Apply filters to the EPA for the current tab.",
                isSelected = upperState == Filter,
                onClick = { onUpperStateChange(if (upperState != Filter) Filter else None) }
            )

            ToolbarButton(
                iconKey = AllIconsKeys.Debugger.RestoreLayout,
                contentDescription = "Layout",
                tooltip = "Change or configure the layout of the EPA visualization for the current tab",
                isSelected = upperState == Layout,
                onClick = { onUpperStateChange(if (upperState != Layout) Layout else None) }
            )

            ToolbarButton(
                iconKey = AllIconsKeys.General.Note,
                contentDescription = "State-Details",
                isSelected = upperState == Details,
                tooltip = "View details of a selected state in the EPA.",
                onClick = { onUpperStateChange(if (upperState != Details) Details else None) }
            )
        }

        Column {
            ToolbarButton(
                iconKey = AllIconsKeys.Run.Restart,
                contentDescription = "Animation",
                tooltip = "Animate a single case or the whole event log in the EPA visualization.",
                isSelected = lowerState == EpaViewLowerState.Animation,
                onClick = { onLowerStateChange(if (lowerState != EpaViewLowerState.Animation) EpaViewLowerState.Animation else EpaViewLowerState.None) }
            )

            ToolbarButton(
                iconKey = AllIconsKeys.Actions.ProjectWideAnalysisOff,
                contentDescription = "Statistics",
                tooltip = "Show details of the root EPA and the EPA in currently open tab.",
                isSelected = lowerState == EpaViewLowerState.Statistics,
                onClick = { onLowerStateChange(if (lowerState != EpaViewLowerState.Statistics) EpaViewLowerState.Statistics else EpaViewLowerState.None) }
            )
        }
    }

    Divider(
        orientation = Orientation.Vertical,
        modifier = Modifier.fillMaxHeight(),
        thickness = 1.dp,
        color = JewelTheme.defaultTabStyle.colors.underlineSelected.copy(alpha = 0.2f)
    )
}
