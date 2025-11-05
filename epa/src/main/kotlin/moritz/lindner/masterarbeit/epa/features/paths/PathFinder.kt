package moritz.lindner.masterarbeit.epa.features.paths

import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State

@JvmInline
value class Path(
    val states: List<State>
)

data class Route(
    val start: State,
    val end: State
)

class PathFinder<T : Comparable<T>>() {
    private val epaService = EpaService<T>()

    private val allPaths = HashMap<Route, Path>()

    fun getPathBetween(start: State, end: State): Path {
        // Check cache (bidirectional)
        val existing = allPaths[Route(start, end)] ?: allPaths[Route(end, start)]
        if (existing != null) {
            return if (allPaths.contains(Route(start, end))) {
                existing
            } else {
                // Return reversed path if we cached it in opposite direction
                Path(existing.states.reversed())
            }
        }

        // Compute path using LCA algorithm
        val startPath = epaService.getPathToRoot(start)
        val endPath = epaService.getPathFromRoot(end)
        val endPathSet = endPath.toSet()

        val path = mutableListOf<State>()
        var lowestCommonAncestor: State? = null

        for (stateOnPath in startPath) {
            if (stateOnPath in endPathSet) {
                lowestCommonAncestor = stateOnPath
                break
            } else {
                path.add(stateOnPath)
            }
        }

        // Add path down from LCA to end
        val pathDown = endPath.dropWhile { state -> state != lowestCommonAncestor }
        path.addAll(pathDown)

        val finalPath = path.toList()

        // Cache all subpaths
        cacheSubpaths(finalPath)

        return Path(finalPath)
    }

    private fun cacheSubpaths(fullPath: List<State>) {
        // Generate all pairs of states on the path
        for (i in fullPath.indices) {
            for (j in i + 1 until fullPath.size) {
                val start = fullPath[i]
                val end = fullPath[j]

                // Check if we already cached this route (in either direction)
                if (
                    allPaths.contains(Route(start, end))
                    || allPaths.contains(Route(end, start))
                ) {
                    continue
                } else {
                    // Extract subpath from i to j (inclusive on both ends!)
                    val subPath = fullPath.subList(i, j + 1)
                    allPaths[Route(start, end)] = Path(subPath)
                }
            }
        }
    }
}