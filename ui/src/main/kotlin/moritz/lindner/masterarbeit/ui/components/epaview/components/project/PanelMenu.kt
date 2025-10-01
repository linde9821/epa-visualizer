package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun PanelMenu(
    title: String,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(2.dp),
    modifier: Modifier = Modifier.Companion,
    down: Boolean = false,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text(title, style = JewelTheme.Companion.typography.h1TextStyle)
            IconButton(
                onClick = onClose,
                modifier = Modifier.Companion.padding(start = 8.dp)
            ) {
                if (down) {
                    Icon(AllIconsKeys.General.ChevronDown, "Chevron")
                } else {
                    Icon(AllIconsKeys.General.ChevronLeft, "Chevron")
                }
            }
        }
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.Companion.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.Companion.contentColor.copy(alpha = 0.2f)
        )
        content()
    }
}