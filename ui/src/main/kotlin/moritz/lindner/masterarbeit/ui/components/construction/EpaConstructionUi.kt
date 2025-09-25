package moritz.lindner.masterarbeit.ui.components.construction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.ui.state.ApplicationState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.InlineErrorBanner
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun EpaConstructionUi(
    state: ApplicationState.FileSelected,
    onAbort: () -> Unit,
    onStartConstructionStart: (EpaFromXesBuilder<Long>) -> Unit,
) {
    val file = state.file
    val mappers = listOf(
        SampleEventMapper(),
        BPI2017OfferChallengeEventMapper(),
        BPI2017ChallengeEventMapper(),
        BPI2018ChallangeMapper(),
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedMapper: EventLogMapper<Long> = mappers[selectedIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Configure EPA Construction",
            style = JewelTheme.typography.h1TextStyle
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selected file: ${file.name}",
            style = JewelTheme.typography.regular
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.constructionError != null) {
            InlineErrorBanner(
                text = state.constructionError,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Actions.MoveToButton,
                    contentDescription = null,
                    tint = JewelTheme.contentColor
                )
                Text(
                    "Select Event Log Mapper:",
                    style = JewelTheme.typography.regular
                )
            }

            ListComboBox(
                items = mappers.map { it.name },
                selectedIndex = selectedIndex,
                onSelectedItemChange = { selectedIndex = it },
                modifier = Modifier.width(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = onAbort
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.Back,
                        contentDescription = null,
                        tint = JewelTheme.contentColor
                    )
                    Text("Change File", style = JewelTheme.typography.regular)
                }
            }

            DefaultButton(
                onClick = {
                    val builder = EpaFromXesBuilder<Long>().apply {
                        setFile(file)
                        setEventLogMapper(selectedMapper)
                    }
                    onStartConstructionStart(builder)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        key = AllIconsKeys.Toolwindows.ToolWindowBuild,
                        contentDescription = null,
                        tint = JewelTheme.contentColor
                    )
                    Text("Construct EPA", style = JewelTheme.typography.regular)
                }
            }
        }
    }
}
