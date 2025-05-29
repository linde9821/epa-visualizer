package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
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
        Text(
            text = "EPA Visualizer",
            style = MaterialTheme.typography.h4,
        )

        Image(
            painter = painterResource("logo.png"),
            contentDescription = "App Logo",
            modifier =
                Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f),
        )

        Button(
            shape = RoundedCornerShape(24.dp),
            onClick = { showDialog = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier =
                Modifier
                    .height(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Select event log", style = MaterialTheme.typography.button)
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
