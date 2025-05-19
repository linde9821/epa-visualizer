package moritz.lindner.masterarbeit.ui

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import java.io.File

sealed class ApplicationState {
    data object NoFileSelected : ApplicationState()

    data class FileSelected(
        val file: File,
    ) : ApplicationState()

    data class EpaConstructionRunning(
        val selectedFile: File,
        val builder: ExtendedPrefixAutomataBuilder<Long>,
    ) : ApplicationState()

    data class EpaConstructed(
        val extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
        val tree: EPATreeNode<Long>,
    ) : ApplicationState()
}
