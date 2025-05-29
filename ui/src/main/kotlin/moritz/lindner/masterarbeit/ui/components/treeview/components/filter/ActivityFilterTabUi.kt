package moritz.lindner.masterarbeit.ui.components.treeview.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.filter.ActivityFilter

@Composable
fun ActivityFilterTabUi(
    epa: ExtendedPrefixAutomata<Long>,
    onFilterUpdate: (ActivityFilter<Long>) -> Unit,
) {
    val activities =
        remember {
            mutableStateListOf(*epa.activities.map { it to true }.toTypedArray())
        }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(activities) { index, (activity, enabled) ->
            Row(
                modifier =
                    Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = activity.name)
                Checkbox(
                    checked = enabled,
                    onCheckedChange = { enabled ->
                        activities[index] = Pair(activity, enabled)

                        val activityFilter =
                            ActivityFilter<Long>(
                                activities
                                    .toList()
                                    .filter { (_, enabled) -> enabled }
                                    .map { (activity, _) -> activity }
                                    .toHashSet(),
                            )

                        onFilterUpdate(activityFilter)
                    },
                )
            }
        }
    }
}
