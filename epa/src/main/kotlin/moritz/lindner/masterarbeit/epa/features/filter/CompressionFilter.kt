package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class CompressionFilter<T : Comparable<T>> : EpaFilter<T> {
    override val name: String
        get() = "Compression Filter"

    data class MarkedState(
        val state: State,
        val isInvalid: Boolean
    ) {
        override fun toString(): String {
            return "$state: $isInvalid"
        }
    }

    class SyntheticStates<T : Comparable<T>>(
        epa: ExtendedPrefixAutomaton<T>,
        val chains: List<List<State.PrefixState>>,
    ) {
        private val allChainParts = chains.flatten().toSet()

        val chainByChainStart = chains.associateBy { it.first() }
        val chainByChainEnd = chains.associateBy { it.last() }
        val chainEnds = chains.map { it.last() }.toSet()
        val chainStarts = chains.map { it.first() }.toSet()

        val partitionByChain = chains.associateWith { chain ->
            // does it really make sens to just take partition of the first chain state
            epa.partition(chain.first())
        }

        val seqByChain = chains.associateWith { chain ->
            chain.fold(emptySet<Event<T>>()) { acc, state ->
                acc + epa.sequence(state)
            }
        }

        val syntheticStateByChain = chains.associateWith { chain ->
            MarkedState(
                state = State.PrefixState(
                    from = chain.first().from,
                    via = chain.fold(Activity("")) { acc, s -> Activity(acc.name + s.via.name) }
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

        override fun toString(): String {
            return "parents:\n${parentByState.map { "${it.key} -> ${it.value}\n" }}\n" +
                    "children:\n${childrenByState.map { "${it.key} -> [${it.value.joinToString()}]" }}"
        }

        fun markParentsIfInvalid(chains: SyntheticStates<T>): Map<State, MarkedState> {
            return parentByState.mapValues { (_, parent) ->
                val shouldMarkInvalid = parent.state in chains.chainEnds

                if (shouldMarkInvalid) {
                    parent.copy(isInvalid = true)
                } else {
                    parent
                }
            }
        }

        fun markChildrenIfInvalid(chains: SyntheticStates<T>): Map<State, List<MarkedState>> {
            return childrenByState.mapValues { (_, children) ->
                children.map { child ->
                    val isPresent = child.state in chains.chainStarts
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
            val children = childrenByState[key] ?: emptyList()

            childrenByState[key] = children + values
        }

        fun addIfNotPresent(state: State) {
            if (childrenByState.contains(state).not()) {
                childrenByState[state] = emptyList()
            }
        }

        fun detectChains(epa: ExtendedPrefixAutomaton<T>): SyntheticStates<T> {
            val processed = mutableSetOf<State>()
            val chains = mutableListOf<List<State.PrefixState>>()

            // Find all states that are part of single-child relationships
            val singleChildMap = childrenByState
                .filter { it.key is State.PrefixState && it.value.size == 1 }
                .mapValues { it.value.first().state }

            // Find chain starts: states with single child that aren't themselves a single child
            val chainStarts = singleChildMap.keys.filter { state ->
                // Is this state the single child of another state?
                val isChildOfSingleParent = parentByState[state]?.let { parent ->
                    singleChildMap[parent.state] == state
                } ?: false

                !isChildOfSingleParent // Only start chains from states that aren't single children
            }

            chainStarts.forEach { start ->
                if (start !in processed) {
                    val chain = buildChainFromStart(start, singleChildMap, processed)
                    if (chain.size > 1) {
                        chains.add(chain.map { it as State.PrefixState })
                    }
                }
            }

            return SyntheticStates(epa = epa, chains = chains)
        }

        private fun buildChainFromStart(
            start: State,
            singleChildMap: Map<State, State>,
            processed: MutableSet<State>
        ): List<State> {
            val chain = mutableListOf<State>()
            var current: State? = start

            while (current != null && current !in processed) {
                chain.add(current)
                processed.add(current)
                current = singleChildMap[current]
            }

            return chain
        }

        fun addSyntheticStates(syntheticStates: SyntheticStates<T>) {
            syntheticStates.chains.forEach { chain ->
                val parent = parentByState.getOrElse(chain.first(), {
                    throw Exception("This shouldnt happen")
                })
                parentByState[syntheticStates.syntheticStateByChain[chain]!!.state] = parent

                val children = childrenByState[chain.last()]!!
                childrenByState[syntheticStates.syntheticStateByChain[chain]!!.state] = children
            }
        }

        fun removeAllStatesWhichArePartOfChain(syntheticStates: SyntheticStates<T>) {
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

        fun updateParents(syntheticStates: SyntheticStates<T>): Map<State, MarkedState> {
            return parentByState.mapValues { (_, parent) ->
                if (parent.isInvalid) {
                    val chain = syntheticStates.chainByChainEnd[parent.state]!!
                    syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                } else parent
            }
        }

        fun updateChildren(syntheticStates: SyntheticStates<T>): Map<State, List<MarkedState>> {
            return childrenByState.mapValues { (_, children) ->
                children.map { child ->
                    if (child.isInvalid) {
                        val chain = syntheticStates.chainByChainStart[child.state]
                        syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                    } else child
                }
            }
        }

        fun buildNewEpa(
            epa: ExtendedPrefixAutomaton<T>,
            syntheticStates: SyntheticStates<T>
        ): ExtendedPrefixAutomaton<T> {
            val oldToNewStateMapping = mutableMapOf<State, State>()

            oldToNewStateMapping[State.Root] = State.Root

            // Step 1: Build states in dependency order (parents before children)
            val processedStates = mutableSetOf<State>()
            val stateQueue = ArrayDeque<State>()

            // Start with states that have Root as parent
            parentByState.entries
                .filter { (_, parent) -> parent.state == State.Root }
                .forEach { (state, _) -> stateQueue.add(state) }

            // Process states in dependency order
            while (stateQueue.isNotEmpty()) {
                val currentState = stateQueue.removeFirst()

                if (currentState in processedStates) continue

                val parentInfo = parentByState[currentState]
                if (parentInfo == null) {
                    processedStates.add(currentState)
                    continue
                }

                // Check if parent has been processed
                val parentState = parentInfo.state
                if (parentState !in oldToNewStateMapping && parentState != State.Root) {
                    // Parent not ready, put current state back and try parent first
                    stateQueue.addLast(currentState)
                    if (parentState !in processedStates) {
                        stateQueue.addFirst(parentState)
                    }
                    continue
                }

                // Create new state with proper parent reference
                val newState = when (currentState) {
                    is State.PrefixState -> {
                        val newParent = oldToNewStateMapping[parentState] ?: parentState
                        State.PrefixState(
                            from = newParent,
                            via = currentState.via
                        )
                    }

                    else -> currentState
                }

                oldToNewStateMapping[currentState] = newState
                processedStates.add(currentState)

                // Add children to queue
                val children = childrenByState[currentState] ?: emptyList()
                children.forEach { child ->
                    if (child.state !in processedStates) {
                        stateQueue.add(child.state)
                    }
                }
            }

            // Step 2: Create transitions using new state instances
            val newTransitions = childrenByState.flatMap { (oldParentState, children) ->
                children.map { child ->
                    val newStart = oldToNewStateMapping[oldParentState] ?: oldParentState
                    val newEnd = oldToNewStateMapping[child.state] ?: child.state

                    Transition(
                        start = newStart,
                        activity = (child.state as State.PrefixState).via,
                        end = newEnd
                    )
                }
            }

            val allNewStates = oldToNewStateMapping.values.toSet()

            // Step 3: Build partition mappings
            val newPartitionByState = buildPartitionMapping(epa, syntheticStates, oldToNewStateMapping)

            // Step 4: Build sequence mappings
            val newSequenceByState = buildSequenceMapping(epa, syntheticStates, oldToNewStateMapping)

            return ExtendedPrefixAutomaton<T>(
                eventLogName = epa.eventLogName + "compressed",
                states = allNewStates,
                activities = newTransitions.map { it.activity }.toSet(),
                transitions = newTransitions.toSet(),
                partitionByState = newPartitionByState,
                sequenceByState = newSequenceByState
            )
        }

        private fun buildPartitionMapping(
            epa: ExtendedPrefixAutomaton<T>,
            syntheticStates: SyntheticStates<T>,
            oldToNewStateMapping: Map<State, State>
        ): Map<State, Int> {
            val newPartitionByState = mutableMapOf<State, Int>()

            // Add partitions for non-chain states (mapped to new state instances)
            epa.states.map { state ->
                state to epa.partition(state)
            }.forEach { (oldState, partition) ->
                if (!syntheticStates.isPartOfChain(oldState)) {
                    val newState = oldToNewStateMapping[oldState] ?: oldState
                    newPartitionByState[newState] = partition
                }
            }

            // Add partitions for synthetic states
            syntheticStates.partitionByChain.forEach { (chain, partition) ->
                val syntheticState = oldToNewStateMapping[syntheticStates.syntheticStateByChain[chain]!!.state]
                if (syntheticState != null) {
                    newPartitionByState[syntheticState] = partition
                }
            }

            return newPartitionByState
        }

        private fun buildSequenceMapping(
            epa: ExtendedPrefixAutomaton<T>,
            syntheticStates: SyntheticStates<T>,
            oldToNewStateMapping: Map<State, State>
        ): Map<State, Set<Event<T>>> {
            val newSequenceByState = mutableMapOf<State, Set<Event<T>>>()

            // Add sequences for non-chain states (mapped to new state instances)
            epa.states.map { state ->
                state to epa.sequence(state)
            }.forEach { (oldState, sequence) ->
                if (!syntheticStates.isPartOfChain(oldState)) {
                    val newState = oldToNewStateMapping[oldState] ?: oldState
                    newSequenceByState[newState] = sequence
                }
            }

            // Add sequences for synthetic states
            syntheticStates.seqByChain.forEach { (chain, sequence) ->
                val syntheticState = oldToNewStateMapping[syntheticStates.syntheticStateByChain[chain]!!.state]
                if (syntheticState != null) {
                    newSequenceByState[syntheticState] = sequence
                }
            }

            return newSequenceByState
        }
    }

    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
        val mapping = Mapping<T>()

        val childrenByParent = epa.transitions.groupBy { it.start }.mapValues { it.value.map { it.end } }
        val parentByChild =
            epa.transitions.groupBy { it.end }.mapValues { it.value.map { transition -> transition.start }.first() }

        childrenByParent.forEach { (state, children) ->
            mapping.addChildrenForState(state, children.map { MarkedState(it as State.PrefixState, false) })
        }

        parentByChild.forEach { (state, parent) ->
            mapping.addParentForState(state, MarkedState(parent, false))
        }

        epa.states.forEach { state ->
            mapping.addIfNotPresent(state)
        }

        val syntheticStates = mapping.detectChains(epa)

        mapping.parentByState = mapping.markParentsIfInvalid(syntheticStates).toMutableMap()
        mapping.childrenByState = mapping.markChildrenIfInvalid(syntheticStates).toMutableMap()
        mapping.addSyntheticStates(syntheticStates)
        mapping.removeAllStatesWhichArePartOfChain(syntheticStates)
        mapping.parentByState = mapping.updateParents(syntheticStates).toMutableMap()
        mapping.childrenByState = mapping.updateChildren(syntheticStates).toMutableMap()

        return mapping.buildNewEpa(epa.copy(), syntheticStates)
    }
}
