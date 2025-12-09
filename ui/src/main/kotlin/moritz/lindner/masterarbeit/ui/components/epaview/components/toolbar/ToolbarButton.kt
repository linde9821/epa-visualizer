package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icon.IntelliJIconKey
import org.jetbrains.jewel.ui.theme.defaultTabStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolbarButton(
    iconKey: IntelliJIconKey,
    contentDescription: String,
    isSelected: Boolean,
    tooltip: String? = null,
    onClick: () -> Unit,
) {
    val button: @Composable () -> Unit = {
        IconButton(onClick = onClick) {
            Icon(
                key = iconKey,
                contentDescription = contentDescription,
                tint = if (isSelected) {
                    JewelTheme.Companion.defaultTabStyle.colors.underlineSelected
                } else {
                    Color.Companion.Unspecified
                },
                modifier = Modifier.Companion.size(23.dp)
            )
        }
    }

    if (tooltip != null) {
        Tooltip(
            tooltip = { Text(tooltip) }
        ) {
            button()
        }
    } else {
        button()
    }
}