package moritz.lindner.masterarbeit.ui.components.project

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferAction.Companion.Move
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectXesStep(
    selectedFile: File?,
    onFileSelect: (File?) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select XES event log file", style = JewelTheme.Companion.typography.h3TextStyle)

        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .border(1.dp, JewelTheme.Companion.contentColor.copy(alpha = 0.3f))
                .clickable { onFileSelect(null) }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event.action == Move
                    },
                    target = object : DragAndDropTarget {
                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            val transferable = event.awtTransferable
                            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                                files.firstOrNull()?.let { file ->
                                    if (file.extension == "xes" || file.name.endsWith(".xes.gz")) {
                                        onFileSelect(file)
                                        return true
                                    }
                                }
                            }
                            return false
                        }
                    }
                ).padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Actions.Upload,
                    contentDescription = null,
                    modifier = Modifier.Companion.size(48.dp),
                    tint = JewelTheme.Companion.contentColor
                )
                Text(
                    text = selectedFile?.name ?: "Click to select XES file or drag and drop a XES file",
                    style = JewelTheme.Companion.typography.regular
                )
                selectedFile?.let {
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
                enabled = selectedFile != null
            ) {
                Text("Next")
            }
        }
    }
}