package moritz.lindner.masterarbeit.ui.components.fileselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FileSelectionUi(onFileSelected: (file: File) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("EPA Visualizer", style = JewelTheme.typography.h1TextStyle)

        OutlinedButton(
            onClick = { showDialog = true },
        ) {
            Row() {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text("Select event log file", style = JewelTheme.typography.regular)
            }
        }

        if (showDialog) {
            FileDialog { path ->
                showDialog = false
                if (path != null) {
                    val file = File(path)
                    onFileSelected(file)
                }
            }
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun isMultipleMode(): Boolean = false

//            override fun getFilenameFilter(): FilenameFilter =
//                FilenameFilter { file, name ->
//                    file.extension == "xes" || file.extension == "gz"
//                }

            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value && directory != null && file != null) {
                    onCloseRequest(directory + file)
                } else {
                    onCloseRequest(null)
                }
            }
        }
    },
    dispose = FileDialog::dispose,
)
