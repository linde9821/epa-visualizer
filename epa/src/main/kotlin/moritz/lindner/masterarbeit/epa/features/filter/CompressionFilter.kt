package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class CompressionFilter<T : Comparable<T>> : EpaFilter<T> {
    override val name: String
        get() = "Compression Filter"

    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
        val mapping = Mapping<T>()

        val childrenByParent = epa.transitions.groupBy { it.start }.mapValues { it.value.map { it.end } }
        val parentByChild = epa.transitions.groupBy { it.end }.mapValues { it.value.map { it.start }.first() }

        childrenByParent.forEach { state, children ->
            mapping.addChildrenForState(state, children.map { MarkedState(it as State.PrefixState, false) })
        }

        parentByChild.forEach { state, parent ->
            mapping.addParentForState(state, MarkedState(parent, false))
        }

        epa.states.forEach { state ->
            mapping.addIfNotPresent(state)
        }

        val chains = mapping.detectChains()

        // Update to use returned immutable maps
        mapping.parentByState = mapping.markParentsIfInvalid(chains).toMutableMap()
        mapping.childrenByState = mapping.markChildrenIfInvalid(chains).toMutableMap()

        mapping.addSyntheticStates(chains)
        mapping.removeAllStatesWhichArePartOfChain(chains)

        mapping.parentByState = mapping.updateParents(chains).toMutableMap()
        mapping.childrenByState = mapping.updateChildren(chains).toMutableMap()

        return mapping.buildNewEpa(epa.copy())
    }

    data class MarkedState(
        val state: State,
        val isInvalid: Boolean
    ) {
        override fun toString(): String {
            return "$state: $isInvalid"
        }
    }

    class SyntheticStates(
        val chains: List<List<State.PrefixState>>,
    ) {
        private val allChainParts = chains.flatten().toSet()

        val chainByChainStart = chains.map { it.first() to it }.toMap()
        val chainByChainEnd = chains.map { it.last() to it }.toMap()

        val syntheticStateByChain = chains.associateWith { chain ->
            MarkedState(
                state = State.PrefixState(
                    from = chain.first().from,
                    via = chain.fold(Activity("")) { acc, s -> Activity(acc.name + s.name) }
                ),
                isInvalid = true
            )
        }

        fun isPartOfChain(state: State): Boolean {
            return allChainParts.contains(state)
        }

        override fun toString(): String {
            return chains.joinToString("\n") { chains -> chains.joinToString(",") }
        }
    }

    class Mapping<T : Comparable<T>> {
        var parentByState = mutableMapOf<State, MarkedState>()
        var childrenByState = mutableMapOf<State, List<MarkedState>>()

        fun markParentsIfInvalid(chains: SyntheticStates): Map<State, MarkedState> {
            return parentByState.mapValues { (state, parent) ->
                val shouldMarkInvalid = chains.chains.any { chain ->
                    parent.state == chain.last()
                }

                if (shouldMarkInvalid) {
                    println("marking $parent")
                    parent.copy(isInvalid = true)
                } else {
                    parent
                }
            }
        }

        fun markChildrenIfInvalid(chains: SyntheticStates): Map<State, List<MarkedState>> {
            return childrenByState.mapValues { (_, children) ->
                children.map { child ->
                    val isPresent = chains.chainByChainStart[child.state] != null
                    if (isPresent) {
                        child.copy(isInvalid = true)
                    } else {
                        child
                    }
                }
            }
        }

        fun addParentForState(key: State, value: MarkedState) {
            parentByState.put(key, value)
        }

        fun addChildrenForState(key: State, values: List<MarkedState>) {
            val children = childrenByState.get(key) ?: emptyList()

            childrenByState.put(key, children + values)
        }

        fun addIfNotPresent(state: State) {
            if (childrenByState.contains(state).not()) {
                childrenByState.put(state, emptyList())
            }
        }

        fun <T> mergeSublistsKeepLongest(lists: List<List<T>>): List<List<T>> {
            return lists.filter { currentList ->
                // Keep this list only if no other list contains all of its elements
                lists.none { otherList ->
                    otherList != currentList && otherList.containsAll(currentList)
                }
            }
        }

        fun detectChains(): SyntheticStates {
            val chains = childrenByState
                .filter { it.key is State.PrefixState }
                .filter { it.value.size == 1 }
                .map { (state, _) ->
                    listOf(state) + followChain(state, emptyList())
                }.map { chain ->
                    chain.map { it as State.PrefixState }
                }

            return SyntheticStates(mergeSublistsKeepLongest(chains))
        }

        fun followChain(a: State, acc: List<State>): List<State> {
            if (childrenByState[a] != null && childrenByState[a]!!.isNotEmpty()) {
                val n = childrenByState[a]!!.first()
                return if (childrenByState[a]!!.size == 1) followChain(n.state, acc + n.state)
                else acc
            } else return acc
        }

        override fun toString(): String {
            return "parents:\n${parentByState.map { "${it.key} -> ${it.value}\n" }}\n" +
                    "children:\n${childrenByState.map { "${it.key} -> [${it.value.joinToString()}]" }}"
        }

        fun addSyntheticStates(syntheticStates: SyntheticStates) {
            syntheticStates.chains.forEach { chain ->
                val parent = parentByState[chain.first()]!!
                parentByState.put(syntheticStates.syntheticStateByChain[chain]!!.state, parent)

                val children = childrenByState[chain.last()]!!
                childrenByState.put(syntheticStates.syntheticStateByChain[chain]!!.state, children)
            }
        }

        fun removeAllStatesWhichArePartOfChain(syntheticStates: SyntheticStates) {
            val newparentByState = mutableMapOf<State, MarkedState>()
            val newchildrenByState = mutableMapOf<State, List<MarkedState>>()

            parentByState.forEach { state, p ->
                if (syntheticStates.isPartOfChain(state).not()) newparentByState.put(state, p)
            }

            childrenByState.forEach { state, c ->
                if (syntheticStates.isPartOfChain(state).not()) newchildrenByState.put(state, c)
            }

            parentByState = newparentByState
            childrenByState = newchildrenByState
        }

        fun updateParents(syntheticStates: SyntheticStates): Map<State, MarkedState> {
            return parentByState.mapValues { (state, parent) ->
                if (parent.isInvalid) {
                    val chain = syntheticStates.chainByChainEnd[parent.state]!!
                    syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                } else parent
            }
        }

        fun updateChildren(syntheticStates: SyntheticStates): Map<State, List<MarkedState>> {
            return childrenByState.mapValues { (state, children) ->
                children.map { child ->
                    if (child.isInvalid) {
                        val chain = syntheticStates.chainByChainStart[child.state]
                        syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                    } else child
                }
            }
        }

        fun buildNewEpa(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
            // Create new states with updated parents
            val stateMapping = parentByState.mapKeys { (state, parent) ->
                state to when (state) {
                    is State.PrefixState -> State.PrefixState(
                        from = parent.state,
                        via = state.via
                    )
                    else -> state
                }
            }.map { it.key.first to it.key.second }.toMap()

            val newStates = stateMapping.values.toSet() + State.Root

            // Create transitions using new state references
            val transitions = childrenByState.flatMap { (oldState, children) ->
                children.map { child ->
                    val newStart = stateMapping[oldState] ?: oldState
                    val newEnd = stateMapping[child.state] ?: child.state
                    Transition(
                        start = newStart,
                        activity = (child.state as State.PrefixState).via,
                        end = newEnd
                    )
                }
            }

            return ExtendedPrefixAutomaton<T>(
                eventLogName = epa.eventLogName + "compressed",
                states = newStates,
                activities = transitions.map { it.activity }.toSet(),
                transitions = transitions.toSet(),
                partitionByState = emptyMap(),
                sequenceByState = emptyMap()
            )
        }    }
}