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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.domain.Activity

@Composable
fun ActivityFilterTab(activities: SnapshotStateList<Pair<Activity, Boolean>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(activities) { index, (activity, enabled) ->
            Row(
                modifier =
                    Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = activity.name)
                Checkbox(
                    checked = enabled,
                    onCheckedChange = {
                        activities[index] = activity to it
                    },
                )
            }
        }
    }
}
