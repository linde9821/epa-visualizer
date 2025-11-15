package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun FilterOverview(
    filters: List<EpaFilter<Long>>,
    allowRemoval: Boolean = false,
    onRemove: (EpaFilter<Long>) -> Unit = {}
) {
    val size = filters.size
    filters.forEachIndexed { index, filter ->
        Row(
            modifier = Modifier.Companion.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Column(
                modifier = Modifier.Companion
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = filter.name,
                    style = JewelTheme.Companion.typography.medium
                )
                Text(
                    text = "Filter ${index + 1}",
                    style = JewelTheme.Companion.typography.small,
                    color = JewelTheme.Companion.contentColor.copy(alpha = 0.7f)
                )
            }

            if (allowRemoval) {
                IconButton(
                    onClick = {
                        onRemove(filter)
                    }
                ) {
                    Icon(
                        key = AllIconsKeys.General.Delete,
                        contentDescription = "Delete filter",
                        tint = JewelTheme.Companion.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Arrow pointing to next filter (except for the last one)
        if (index < size - 1) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "THEN",
                        style = JewelTheme.Companion.typography.small,
                        color = JewelTheme.Companion.contentColor.copy(alpha = 0.6f)
                    )
                    Icon(
                        key = AllIconsKeys.General.ArrowDown,
                        contentDescription = "Next filter",
                        tint = JewelTheme.Companion.contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.Companion.size(16.dp)
                    )
                }
            }
        }
    }
}