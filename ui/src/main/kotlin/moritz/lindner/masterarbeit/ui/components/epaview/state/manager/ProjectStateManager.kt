package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import moritz.lindner.masterarbeit.epa.project.Project

class ProjectStateManager(
    project: Project,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    private val _project = MutableStateFlow<Project>(project)
    val project = _project.asStateFlow()

    fun updateProject(project: Project) {
        project.saveMetadata()
        _project.value = project

        // TODO: invalidate existing epas if necessary
    }
}

