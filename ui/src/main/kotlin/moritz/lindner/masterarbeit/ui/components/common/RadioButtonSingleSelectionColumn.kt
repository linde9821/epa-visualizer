package moritz.lindner.masterarbeit.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

// https://developer.android.com/develop/ui/compose/components/radio-button?hl=def
@Composable
fun <T> RadioButtonSingleSelectionColumn(
    radioOptions: List<Pair<T, String>>,
    modifier: Modifier = Modifier.Companion,
    onSelection: (T, Int) -> Unit,
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(
        modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        radioOptions.forEach { option ->
            Row(
                Modifier.Companion
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            onOptionSelected(option)
                            onSelection(option.first, radioOptions.indexOf(option))
                        },
                        role = Role.Companion.RadioButton,
                    ).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Companion.CenterVertically,
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null, // null recommended for accessibility with screen readers
                )
                Text(
                    text = option.second,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.body1,
                )
            }
        }
    }
}
