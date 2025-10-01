package moritz.lindner.masterarbeit.ui.state

import moritz.lindner.masterarbeit.epa.project.Project

sealed class ApplicationState {
    data class Start(
        val error: String? = null
    ) : ApplicationState()

    data class ProjectSelected(
        val project: Project,
    ) : ApplicationState()

    data object NewProject : ApplicationState()
}
