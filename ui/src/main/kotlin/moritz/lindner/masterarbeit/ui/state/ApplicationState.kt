package moritz.lindner.masterarbeit.ui.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import java.io.File

sealed class ApplicationState {
    data object NoFileSelected : ApplicationState()

    data class FileSelected(
        val file: File,
        val constructionError: String?
    ) : ApplicationState()

    data class EpaConstructionRunning(
        val selectedFile: File,
        val builder: ExtendedPrefixAutomatonBuilder<Long>,
    ) : ApplicationState()

    data class EpaConstructed(
        val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    ) : ApplicationState()
}
