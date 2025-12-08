package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ValueRow(name: String, value: Float) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(0.5f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val formatted = "%.2f".format(value * 100)

        Text("$name:")
        Text("$formatted%")
    }
}