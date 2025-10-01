package moritz.lindner.masterarbeit.ui.components.newproject

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.io.File

@Composable
fun ChooseLocationStep(
    selectedFolder: File?,
    onFolderSelect: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Choose project location", style = JewelTheme.Companion.typography.h3TextStyle)

        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .border(1.dp, JewelTheme.Companion.contentColor.copy(alpha = 0.3f))
                .clickable { onFolderSelect() }
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Nodes.Folder,
                    contentDescription = null,
                    modifier = Modifier.Companion.size(48.dp),
                    tint = JewelTheme.Companion.contentColor
                )
                Text(
                    text = selectedFolder?.name ?: "Click to select folder",
                    style = JewelTheme.Companion.typography.regular
                )
                selectedFolder?.let {
                    Text(
                        text = it.absolutePath,
                        style = JewelTheme.Companion.typography.regular.copy(fontSize = 12.sp),
                        color = JewelTheme.Companion.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onNext,
                enabled = selectedFolder != null
            ) {
                Text("Next")
            }
        }
    }
}