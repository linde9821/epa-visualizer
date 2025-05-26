package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Selected file: ${file.name}",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    onAbort()
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.ArrowCircleLeft, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Change file", style = MaterialTheme.typography.button)
            }

            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    val builder =
                        ExtendedPrefixAutomataBuilder<Long>().apply {
                            setFile(file)
                            setEventLogMapper(selectedMapper)
                        }
                    onStartConstructionStart(builder)
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.BuildCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Construct EPA", style = MaterialTheme.typography.button)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val mapperNames = listOf("Sample", "Challenge Offer 2017", "Challenge 2017", "Challenge 2018")
            val mappers =
                listOf(
                    SampleEventMapper(),
                    BPI2017OfferChallengeEventMapper(),
                    BPI2017ChallengeEventMapper(),
                    BPI2018ChallangeMapper(),
                )

            val param = mappers.zip(mapperNames)

            Row {
                Icon(Icons.Default.Map, contentDescription = null)
                Spacer(Modifier.width(20.dp))
                Text(
                    "Select Event Log Mapper:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            RadioButtonSingleSelectionColumn(param) { _, index ->
                selectedMapper = mappers[index]
            }
        }
    }
}
