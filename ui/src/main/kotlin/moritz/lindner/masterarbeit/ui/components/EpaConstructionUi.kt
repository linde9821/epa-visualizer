package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.builder.EventLogMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import java.io.File

@Composable
fun EpaConstructionUi(
    file: File,
    onAbort: () -> Unit,
    onStartConstructionStart: (ExtendedPrefixAutomataBuilder<Long>) -> Unit,
) {
    var selectedMapper: EventLogMapper<Long> = remember { SampleEventMapper() }

    Column {
        Text("Selected file: ${file.name}")
        Row {
            Button(
                onClick = {
                    val builder =
                        ExtendedPrefixAutomataBuilder<Long>().apply {
                            setFile(file)
                            setEventLogMapper(selectedMapper) // default mapper for now
                        }
                    onStartConstructionStart(builder)
                },
            ) {
                Text("Construct EPA")
            }

            val mapperNames = listOf("Sample", "Challenge Offer 2017", "Challenge 2017", "Challenge 2018")
            val mappers =
                listOf(
                    SampleEventMapper(),
                    BPI2017OfferChallengeEventMapper(),
                    BPI2017ChallengeEventMapper(),
                    BPI2018ChallangeMapper(),
                )
            RadioButtonSingleSelection(mapperNames) { _, index ->
                selectedMapper = mappers[index]
            }

            Button(
                onClick = {
                    onAbort()
                },
            ) {
                Icon(Icons.Default.Close, "Abort")
            }
        }
    }
}
