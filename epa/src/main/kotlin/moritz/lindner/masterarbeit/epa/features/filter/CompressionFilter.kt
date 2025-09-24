package moritz.lindner.masterarbeit.epa.features.filter

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
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
        mapping.markeParentsIfInvalid(chains)
        mapping.markChildrenIfInvalid(chains)
        mapping.addSyntheticStates(chains)
        mapping.removeAllStatesWhichArePartOfChain(chains)
        mapping.updateParents(chains)
        mapping.updateChildren(chains)

        return mapping.buildNewEpa(epa)
    }

    class MarkedState(
        var state: State,
        var isInvalid: Boolean
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
            return chains.map { chains -> chains.joinToString(",") }.joinToString("\n")
        }
    }

    class Mapping <T : Comparable<T>> {
        var parentByState = mutableMapOf<State, MarkedState>()
        var childrenByState = mutableMapOf<State, List<MarkedState>>()

        fun markeParentsIfInvalid(chains: SyntheticStates) {
            chains.chains.forEach { chain ->
                parentByState.forEach { state, parent ->
                    if (parent.state == chain.last()) {
                        println("marking $parent")
                        parentByState[state]?.isInvalid = true
                    }
                }
            }
        }

        fun markChildrenIfInvalid(chains: SyntheticStates) {
            childrenByState.forEach { _, children ->
                children.forEach { child ->
                    val isPresent = chains.chainByChainStart[child.state] != null
                    if (isPresent) child.isInvalid = true
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

        fun updateParents(syntheticStates: SyntheticStates) {
            parentByState.forEach { state, parent ->
                if (parent.isInvalid) {
                    val chain = syntheticStates.chainByChainEnd[parent.state]!!
                    val newstate = syntheticStates.syntheticStateByChain[chain]!!
                    newstate.isInvalid = false
                    parentByState[state] = newstate
                }
            }
        }

        fun updateChildren(syntheticStates: SyntheticStates) {
            childrenByState.forEach { state, children ->
                val update = children.map { child ->
                    if (child.isInvalid) {
                        val chain = syntheticStates.chainByChainStart[child.state]
                        val newState = syntheticStates.syntheticStateByChain[chain]!!
                        newState.isInvalid = false
                        newState
                    } else child
                }
                childrenByState.put(state, update)
            }
        }

        fun buildNewEpa(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {

            val transitions = childrenByState.flatMap { (state, children) ->
                children.map { child ->
                    Transition(
                        start = state,
                        activity = (child.state as State.PrefixState).via,
                        end = child.state
                    )
                }
            }

            parentByState.map { (state, parent) ->
                state as State.PrefixState
                state.from = parent.state
            }

            return ExtendedPrefixAutomaton<T>(
                eventLogName = epa.eventLogName + "compressed",
                states = (listOf(State.Root) + parentByState.keys.toList()).toSet(),
                activities = transitions.map { it.activity }.toSet(),
                transitions = transitions.toSet(),
                partitionByState = emptyMap(),
                sequenceByState = emptyMap()
            )
        }
    }
}