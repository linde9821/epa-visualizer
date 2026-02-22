package moritz.lindner.masterarbeit.epa.features.animation

class TimedStateSegmentTree<T : Comparable<T>>(
    timedStates: List<TimedState<T>>
) {
    private val sortedPoints: List<T> = (timedStates.map { it.startTime } + timedStates.map { it.endTime })
        .distinct()
        .sorted()

    private val tree: Array<MutableList<TimedState<T>>> =
        Array(4 * sortedPoints.size) { mutableListOf<TimedState<T>>() }

    init {
        timedStates.forEach { state ->
            val left = sortedPoints.binarySearch(state.startTime)
            val right = sortedPoints.binarySearch(state.endTime)
            if (left >= 0 && right >= 0) {
                insert(1, 0, sortedPoints.size - 1, left, right, state)
            }
        }
    }

    private fun insert(node: Int, start: Int, end: Int, l: Int, r: Int, state: TimedState<T>) {
        if (l > end || r < start) return
        if (l <= start && end <= r) {
            tree[node].add(state)
            return
        }
        val mid = (start + end) / 2
        insert(2 * node, start, mid, l, r, state)
        insert(2 * node + 1, mid + 1, end, l, r, state)
    }

    private fun query(node: Int, start: Int, end: Int, target: Int, results: MutableList<TimedState<T>>) {
        results.addAll(tree[node])
        if (start == end) return
        val mid = (start + end) / 2
        if (target <= mid) query(2 * node, start, mid, target, results)
        else query(2 * node + 1, mid + 1, end, target, results)
    }

    // O(log(N+K))
    fun getActiveStatesAt(timestamp: T): List<TimedState<T>> {
        val idx = sortedPoints.binarySearch(timestamp).let { if (it < 0) -it - 2 else it }
        if (idx < 0 || idx >= sortedPoints.size) return emptyList()

        val results = mutableListOf<TimedState<T>>()
        query(1, 0, sortedPoints.size - 1, idx, results)
        return results
    }
}