package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.intui.standalone.styling.dark
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.styling.GroupHeaderStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun ClosableGroup(
    title: String,
    modifier: Modifier = Modifier.Companion,
    content: @Composable () -> Unit
) {
    var show by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GroupHeader(
            text = title,
            style = GroupHeaderStyle.Companion.dark(),
            modifier = Modifier.Companion
                .fillMaxWidth(),
            endComponent = {
                IconButton(
                    onClick = { show = !show },
                    modifier = Modifier.Companion
                        .height(16.dp)
                        .width(24.dp) // small fixed width so divider has room
                ) {
                    Icon(
                        key = if (show) AllIconsKeys.General.ChevronUp else AllIconsKeys.General.ChevronDown,
                        contentDescription = "Chevron"
                    )
                }
            }
        )

        if (show) {
            Spacer(Modifier.Companion.height(4.dp))
            content()
        }
    }
}