package moritz.lindner.masterarbeit.ui.components.epaview.components

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
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateLower
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateUpper
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun TabsUi(
    upperState: EpaViewStateUpper,
    onUpperStateChange: (EpaViewStateUpper) -> Unit,
    lowerState: EpaViewStateLower,
    onLowerStateChange: (EpaViewStateLower) -> Unit,
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

            // Filter
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != EpaViewStateUpper.Filter) EpaViewStateUpper.Filter else EpaViewStateUpper.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == EpaViewStateUpper.Filter) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.General.Filter,
                    contentDescription = "Filter",
                    tint = if (upperState == EpaViewStateUpper.Filter) {
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
                        if (upperState != EpaViewStateUpper.Layout) EpaViewStateUpper.Layout else EpaViewStateUpper.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == EpaViewStateUpper.Layout) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.General.Layout,
                    contentDescription = "Map",
                    tint =
                        if (upperState == EpaViewStateUpper.Layout) {
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
                        if (lowerState != EpaViewStateLower.Animation) EpaViewStateLower.Animation else EpaViewStateLower.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewStateLower.Animation) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.Actions.RunAll,
                    contentDescription = "Animation",
                    tint =
                        if (lowerState == EpaViewStateLower.Animation) {
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
                        if (lowerState != EpaViewStateLower.Statistics) EpaViewStateLower.Statistics else EpaViewStateLower.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewStateLower.Statistics) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    key = AllIconsKeys.Actions.ShowImportStatements,
                    contentDescription = "Statistics",
                    tint =
                        if (lowerState == EpaViewStateLower.Statistics) {
                            JewelTheme.contentColor
                        } else {
                            Color.Unspecified
                        },
                    modifier = Modifier.size(23.dp)
                )
            }
        }
    }
}
