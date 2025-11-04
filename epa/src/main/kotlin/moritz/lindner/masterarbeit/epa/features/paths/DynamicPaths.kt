package moritz.lindner.masterarbeit.epa.features.paths

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State

@JvmInline
value class Path(
    val value: List<State>
)

data class Route(
    val start: State,
    val end: State
)

class DynamicPaths<T : Comparable<T>>(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>
) {
    private val epaService = EpaService<T>()

    private val allPaths = mutableMapOf<Route, Path>()

    fun getPathBetween(start: State, end: State): Path {
        val existing = allPaths[Route(start, end)] ?: allPaths[Route(end, start)]

        if (existing != null) return existing

        val startPath = epaService.getPathToRoot(start)
        val targetPath = epaService.getPathFromRoot(end)
        val targetPathSet = targetPath.toSet()

        val path = mutableListOf<State>()
        var matching: State? = null

        for (stateOnPath in startPath) {
            if (targetPathSet.contains(stateOnPath)) {
                matching = stateOnPath
                break
            } else {
                path.add(stateOnPath)
            }
        }

        val pathDown = targetPath.dropWhile { state -> state != matching }
        path.addAll(pathDown)

        val finalPath = path.toList()

        val allPairs = finalPath.flatMapIndexed { i, a ->
            finalPath.drop(i + 1).map { b -> a to b }
        }

        allPairs.forEach { (start, end) ->
            if (allPaths.contains(Route(start, end)).not() && allPaths.contains(Route(end, start)).not()) {
                val subPath = finalPath.dropWhile { it != start }.takeWhile { it != end }
                allPaths[Route(start, end)] = Path(subPath)
            }
        }

        return Path(finalPath)
    }
}