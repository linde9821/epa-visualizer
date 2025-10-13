package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun TraceDetail(
    trace: List<Event<Long>>,
    state: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    onStateSelected: (State) -> Unit
) {
    var show by remember { mutableStateOf(false) }

    Row {
        Text("Trace ${trace.first().caseIdentifier}")
        IconButton(
            onClick = { show = !show },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (!show) {
                Icon(AllIconsKeys.General.ChevronDown, "Chevron")
            } else {
                Icon(AllIconsKeys.General.ChevronUp, "Chevron")
            }
        }

        if (show) {
            val epaService = EpaService<Long>()
            val stateByEvent = epaService.getStateByEvent(extendedPrefixAutomaton)
            Column(modifier = Modifier.padding(4.dp)) {
                trace.forEachIndexed { index, event ->
                    val stateOfEvent = stateByEvent[event]
                    val weight = if (stateOfEvent == state) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                    Text(
                        "${index + 1}: ${event.activity} at ${event.timestamp} for ${stateOfEvent?.name}",
                        fontWeight = weight,
                        modifier = Modifier.clickable(
                            onClick = {
                                onStateSelected(stateOfEvent!!)
                            }
                        )
                    )
                    Spacer(Modifier.height(1.dp))
                }
            }
        }

    }
}