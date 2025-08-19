package moritz.lindner.masterarbeit.ui.components.construction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.EventLogMapper
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.io.File

@Composable
fun EpaConstructionUi(
    file: File,
    onAbort: () -> Unit,
    onStartConstructionStart: (ExtendedPrefixAutomatonBuilder<Long>) -> Unit,
) {
    val mappers =
        listOf(
            SampleEventMapper(),
            BPI2017OfferChallengeEventMapper(),
            BPI2017ChallengeEventMapper(),
            BPI2018ChallangeMapper(),
        )

    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedMapper: EventLogMapper<Long> = mappers[selectedIndex]

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Mapper Selection",
            style = JewelTheme.typography.h1TextStyle,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = "Selected file: ${file.name}",
            style = JewelTheme.typography.regular,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DefaultButton(
                onClick = {
                    onAbort()
                },
            ) {
                Row {
                    Icon(
                        key = AllIconsKeys.Actions.Back,
                        contentDescription = null,
                        tint = JewelTheme.contentColor,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Text("Change file", style = JewelTheme.typography.regular)
                }
            }

            DefaultButton(
                onClick = {
                    val builder =
                        ExtendedPrefixAutomatonBuilder<Long>().apply {
                            setFile(file)
                            setEventLogMapper(selectedMapper)
                        }
                    onStartConstructionStart(builder)
                }
            ) {
                Row {
                    Icon(
                        key = AllIconsKeys.Toolwindows.ToolWindowBuild,
                        contentDescription = null,
                        tint = JewelTheme.contentColor,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text("Construct EPA", style = JewelTheme.typography.regular)
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Icon(
                    key = AllIconsKeys.Actions.MoveToButton,
                    contentDescription = null,
                    tint = JewelTheme.contentColor,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    "Select Event Log Mapper:",
                    style = JewelTheme.typography.regular,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            ListComboBox(
                items = mappers.map { it.name },
                selectedIndex = selectedIndex,
                onSelectedItemChange = { selectedIndex = it },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
