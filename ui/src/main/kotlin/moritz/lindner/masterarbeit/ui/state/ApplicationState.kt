package moritz.lindner.masterarbeit.ui.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.project.Project
import java.io.File

sealed class ApplicationState {
    data object Start : ApplicationState()

    data class ProjectSelected(
        val project: Project,
        val constructionError: String?
    ) : ApplicationState()

    data object NewProject: ApplicationState()

    data class EpaConstructed(
        val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    ) : ApplicationState()
}
