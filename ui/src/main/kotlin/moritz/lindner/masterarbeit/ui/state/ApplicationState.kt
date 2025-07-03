package moritz.lindner.masterarbeit.ui.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
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
    ) : ApplicationState()
}
