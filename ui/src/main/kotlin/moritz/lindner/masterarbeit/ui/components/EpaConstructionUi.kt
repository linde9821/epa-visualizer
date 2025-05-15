package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import java.io.File

@Composable
fun EpaConstructionUi(
    file: File,
    onAbort: () -> Unit,
    onStartConstructionStart: (ExtendedPrefixAutomataBuilder<Long>) -> Unit,
) {
    Column {
        Text("Selected file: ${file.name}")
        Row {
            Button(
                onClick = {
                    val builder =
                        ExtendedPrefixAutomataBuilder<Long>().apply {
                            setFile(file)
                            setEventLogMapper(BPI2017ChallengeEventMapper()) // default mapper for now
                        }
                    onStartConstructionStart(builder)
                },
            ) {
                Text("Construct EPA")
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
