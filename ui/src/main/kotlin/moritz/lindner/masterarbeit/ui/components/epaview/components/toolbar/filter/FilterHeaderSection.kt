package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun FilterHeaderSection(
    title: String,
    show: Boolean,
    onIconClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Text(
            title,
            style = JewelTheme.Companion.typography.h2TextStyle
        )

        IconButton(
            onClick = { onIconClicked() },
            modifier = Modifier.Companion.padding(start = 8.dp)
        ) {
            if (!show) {
                Icon(AllIconsKeys.General.ChevronDown, "Chevron")
            } else {
                Icon(AllIconsKeys.General.ChevronUp, "Chevron")
            }
        }
    }
}