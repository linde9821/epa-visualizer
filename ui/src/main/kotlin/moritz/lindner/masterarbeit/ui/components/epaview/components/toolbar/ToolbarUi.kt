package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewLowerState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Analysis
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Filter
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Layout
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.NaturalLanguage
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.None
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Project
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys

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
            // Close
            IconButton(onClick = onClose) {
                Icon(key = AllIconsKeys.General.Close, contentDescription = "Close", modifier = Modifier.size(23.dp))
            }

            // Project
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != Project) Project else None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == None) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    )
            ) {
                Icon(
                    key = AllIconsKeys.General.ProjectStructure,
                    contentDescription = "Project",
                    tint = if (upperState == Project) {
                        JewelTheme.contentColor
                    } else {
                        Color.Unspecified
                    },
                    modifier = Modifier.size(23.dp)
                )
            }

            // Filter
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != Filter) Filter else None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == Filter) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.General.Filter,
                    contentDescription = "Filter",
                    tint = if (upperState == Filter) {
                        JewelTheme.contentColor
                    } else {
                        Color.Unspecified
                    },
                    modifier = Modifier.size(23.dp)
                )
            }

            // Layout
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != Layout) Layout else None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == Layout) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.General.Layout,
                    contentDescription = "Map",
                    tint =
                        if (upperState == Layout) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }

            // Analysis
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != Analysis) Analysis else None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == Analysis) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.General.InspectionsEye,
                    contentDescription = "Analyse",
                    tint =
                        if (upperState == Layout) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }

            // NLI
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != NaturalLanguage) NaturalLanguage else None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == NaturalLanguage) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.FileTypes.Text,
                    contentDescription = "Natural Language Interface",
                    tint =
                        if (upperState == Layout) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }

        }

        Column {
            // Animation
            IconButton(
                onClick = {
                    onLowerStateChange(
                        if (lowerState != EpaViewLowerState.Animation) EpaViewLowerState.Animation else EpaViewLowerState.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewLowerState.Animation) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.Run.Restart,
                    contentDescription = "Animation",
                    tint =
                        if (lowerState == EpaViewLowerState.Animation) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }

            // Statistics
            IconButton(
                onClick = {
                    onLowerStateChange(
                        if (lowerState != EpaViewLowerState.Statistics) EpaViewLowerState.Statistics else EpaViewLowerState.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewLowerState.Statistics) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.Actions.ShowImportStatements,
                    contentDescription = "Statistics",
                    tint =
                        if (lowerState == EpaViewLowerState.Statistics) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }
        }
    }

    Divider(
        orientation = Orientation.Vertical,
        modifier = Modifier.fillMaxHeight(),
        thickness = 1.dp,
        color = JewelTheme.contentColor.copy(alpha = 0.2f)
    )
}
