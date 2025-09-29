package moritz.lindner.masterarbeit.ui.components.project

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun ChooseMapperStep(
    mappers: List<EventLogMapper<Long>>,
    selectedMapper: EventLogMapper<Long>?,
    onMapperSelect: (EventLogMapper<Long>) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var selectedIndex by remember(selectedMapper) {
        mutableIntStateOf(
            if (selectedMapper == null) 0 else mappers.map { it.name }.indexOf(selectedMapper.name)
        )
    }

    // Update selected mapper when index changes
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < mappers.size && selectedIndex >= 0) {
            onMapperSelect(mappers[selectedIndex])
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select Event Log Mapper", style = JewelTheme.Companion.typography.h3TextStyle)

        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .border(1.dp, JewelTheme.Companion.contentColor.copy(alpha = 0.3f))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.MoveToButton,
                        contentDescription = null,
                        tint = JewelTheme.Companion.contentColor
                    )
                    Text(
                        "Select Event Log Mapper:",
                        style = JewelTheme.Companion.typography.regular
                    )
                }

                ListComboBox(
                    items = mappers.map { it.name },
                    selectedIndex = selectedIndex,
                    onSelectedItemChange = { selectedIndex = it },
                    modifier = Modifier.width(250.dp)
                )

                selectedMapper?.let { mapper ->
                    Text(
                        text = "Selected: ${mapper.name}",
                        style = JewelTheme.typography.regular.copy(fontSize = 12.sp),
                        color = JewelTheme.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onNext,
                enabled = selectedMapper != null
            ) {
                Text("Next")
            }
        }
    }
}