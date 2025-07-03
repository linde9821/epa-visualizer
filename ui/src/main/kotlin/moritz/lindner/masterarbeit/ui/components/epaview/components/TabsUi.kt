package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TabsUi(
    upperState: EpaViewStateUpper,
    onUpperStateChange: (EpaViewStateUpper) -> Unit,
    lowerState: EpaViewStateLower,
    onLowerStateChange: (EpaViewStateLower) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier =
            Modifier.Companion
                .fillMaxHeight()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Companion.Gray,
                    shape = RoundedCornerShape(4.dp),
                ).padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            // Close
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }

            // Filter
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != EpaViewStateUpper.Filter) EpaViewStateUpper.Filter else EpaViewStateUpper.None,
                    )
                },
                modifier =
                    Modifier.Companion.background(
                        if (upperState == EpaViewStateUpper.Filter) Color.Companion.LightGray else Color.Companion.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint =
                        if (upperState == EpaViewStateUpper.Filter) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Companion.Unspecified
                        },
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
                    Modifier.Companion.background(
                        if (upperState == EpaViewStateUpper.Layout) Color.Companion.LightGray else Color.Companion.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Map",
                    tint =
                        if (upperState == EpaViewStateUpper.Layout) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Companion.Unspecified
                        },
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
                    Modifier.Companion.background(
                        if (lowerState == EpaViewStateLower.Animation) Color.Companion.LightGray else Color.Companion.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Animation,
                    contentDescription = "Animation",
                    tint =
                        if (lowerState == EpaViewStateLower.Animation) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Companion.Unspecified
                        },
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
                    Modifier.Companion.background(
                        if (lowerState == EpaViewStateLower.Statistics) Color.Companion.LightGray else Color.Companion.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Numbers,
                    contentDescription = "Statistics",
                    tint =
                        if (lowerState == EpaViewStateLower.Statistics) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Companion.Unspecified
                        },
                )
            }
        }
    }
}
