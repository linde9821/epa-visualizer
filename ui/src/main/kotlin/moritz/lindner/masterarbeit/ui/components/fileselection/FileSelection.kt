package moritz.lindner.masterarbeit.ui.components.fileselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME
import moritz.lindner.masterarbeit.ui.common.Icons
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FileSelectionUi(onFileSelected: (file: File) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.logo,
            contentDescription = "App Logo",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to $APPLICATION_NAME",
            style = JewelTheme.typography.h1TextStyle
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select an event log file to get started",
            style = JewelTheme.typography.regular
        )

        Spacer(modifier = Modifier.height(32.dp))

        DefaultButton(
            onClick = { showDialog = true },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Actions.AddFile,
                    contentDescription = null,
                    tint = JewelTheme.contentColor,
                )
                Text("Select Event Log File", style = JewelTheme.typography.regular)
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
        object : FileDialog(parent, "Select Event Log File", LOAD) {
            override fun isMultipleMode(): Boolean = false

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
