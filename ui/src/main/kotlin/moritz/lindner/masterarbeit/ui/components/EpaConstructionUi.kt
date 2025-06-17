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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import java.io.File

@Composable
fun EpaConstructionUi(
    file: File,
    onAbort: () -> Unit,
    onStartConstructionStart: (ExtendedPrefixAutomataBuilder<Long>) -> Unit,
) {
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
                            useAutoDetectMapper()
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
    }
}
