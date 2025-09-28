package moritz.lindner.masterarbeit.ui.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.project.Project

sealed class ApplicationState {
    data class Start(
        val constructionError: String? = null
    ) : ApplicationState()

    data class ProjectSelected(
        val project: Project,
    ) : ApplicationState()

    data object NewProject : ApplicationState()

    data class EpaConstructed(
        val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    ) : ApplicationState()
}
