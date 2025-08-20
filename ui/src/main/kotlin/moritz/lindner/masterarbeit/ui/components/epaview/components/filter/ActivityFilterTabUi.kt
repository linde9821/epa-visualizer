package moritz.lindner.masterarbeit.ui.components.epaview.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.ActivityFilter
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ActivityFilterTabUi(
    epa: ExtendedPrefixAutomaton<Long>,
    onFilterUpdate: (ActivityFilter<Long>) -> Unit,
) {
    var enabledActivities by remember(epa) {
        mutableStateOf(epa.activities.toSet())
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(epa.activities.toList()) { activity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = activity in enabledActivities,
                    onCheckedChange = { isChecked ->
                        enabledActivities = if (isChecked) {
                            enabledActivities + activity
                        } else {
                            enabledActivities - activity
                        }
                        onFilterUpdate(ActivityFilter(enabledActivities.toHashSet()))
                    }
                )
            }
        }
    }
}
