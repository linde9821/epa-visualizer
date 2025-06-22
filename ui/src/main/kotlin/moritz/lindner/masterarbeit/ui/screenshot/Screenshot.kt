package moritz.lindner.masterarbeit.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Rectangle
import java.awt.Robot
import javax.imageio.ImageIO
import javax.swing.JFileChooser

@Composable
private fun SaveFileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Save Screenshot", SAVE) {
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

object Screenshot {
    fun takeWindowScreenshotAndSaveIt(window: java.awt.Window) {
        val bounds = window.bounds

        val robot = Robot()
        val screenshot = robot.createScreenCapture(Rectangle(bounds))

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save Screenshot"
        val userSelection = fileChooser.showSaveDialog(window)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            ImageIO.write(screenshot, "png", file)
        }
    }
}
