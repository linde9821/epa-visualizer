package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
fun FileSelectionUi(onFileSelected: (file: File) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Optional: center vertically too
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            shape = RoundedCornerShape(50),
            onClick = {
                showDialog = true
            },
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(Modifier.width(20.dp))
            Text("Select File")
        }

        if (showDialog) {
            FileDialog { path ->
                val file = File(path)
                onFileSelected(file)
            }
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun isMultipleMode(): Boolean = false

            override fun getFilenameFilter(): FilenameFilter =
                FilenameFilter { _, name ->
                    name.endsWith("xes") || name.endsWith("gz")
                }

            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value && directory != null && file != null) {
                    onCloseRequest(directory + file)
                }
            }
        }
    },
    dispose = FileDialog::dispose,
)
