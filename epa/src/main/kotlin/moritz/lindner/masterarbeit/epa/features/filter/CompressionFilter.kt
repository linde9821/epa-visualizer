package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
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
            val combinedActivityName = chain.joinToString("-", transform = State.PrefixState::name)
            MarkedState(
                state = State.PrefixState(
                    from = chain.first().from,
                    via = Activity(combinedActivityName)
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

        fun markParentsIfInvalid(
            chains: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ): Map<State, MarkedState> {
            progressCallback?.onProgress(0, parentByState.size, "Marking invalid parents")
            return parentByState.entries.mapIndexed { index, (state, parent) ->
                val shouldMarkInvalid = parent.state in chains.chainEnds

                val result = state to if (shouldMarkInvalid) {
                    parent.copy(isInvalid = true)
                } else {
                    parent
                }

                progressCallback?.onProgress(index + 1, parentByState.size, "Marking invalid parents")
                result
            }.toMap()
        }

        fun markChildrenIfInvalid(
            chains: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ): Map<State, List<MarkedState>> {
            progressCallback?.onProgress(0, childrenByState.size, "Marking invalid children")
            return childrenByState.entries.mapIndexed { index, (state, children) ->
                val result = state to children.map { child ->
                    val isPresent = child.state in chains.chainStarts
                    if (isPresent) {
                        child.copy(isInvalid = true)
                    } else {
                        child
                    }
                }

                progressCallback?.onProgress(index + 1, childrenByState.size, "Marking invalid children")
                result
            }.toMap()
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

        fun detectChains(
            epa: ExtendedPrefixAutomaton<T>,
            progressCallback: EpaProgressCallback?
        ): SyntheticStates<T> {
            progressCallback?.onProgress(0, 100, "Detecting chains: building single-child map")
            val processed = mutableSetOf<State>()
            val chains = mutableListOf<List<State.PrefixState>>()

            // Find all states that are part of single-child relationships
            val singleChildMap = childrenByState
                .filter { it.key is State.PrefixState && it.value.size == 1 }
                .mapValues { it.value.first().state }

            progressCallback?.onProgress(30, 100, "Detecting chains: finding chain starts")
            // Find chain starts: states with single child that aren't themselves a single child
            val chainStarts = singleChildMap.keys.filter { state ->
                // Is this state the single child of another state?
                val isChildOfSingleParent = parentByState[state]?.let { parent ->
                    singleChildMap[parent.state] == state
                } ?: false

                !isChildOfSingleParent // Only start chains from states that aren't single children
            }

            progressCallback?.onProgress(50, 100, "Detecting chains: building chains")
            chainStarts.forEachIndexed { index, start ->
                if (start !in processed) {
                    val chain = buildChainFromStart(start, singleChildMap, processed)
                    if (chain.size > 1) {
                        chains.add(chain.map { it as State.PrefixState })
                    }
                }
                progressCallback?.onProgress(
                    50 + ((index + 1) * 50) / chainStarts.size,
                    100,
                    "Detecting chains: processing chain ${index + 1}/${chainStarts.size}"
                )
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

        fun addSyntheticStates(
            syntheticStates: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ) {
            progressCallback?.onProgress(0, syntheticStates.chains.size, "Adding synthetic states")
            syntheticStates.chains.forEachIndexed { index, chain ->
                val parent = parentByState.getOrElse(chain.first(), {
                    throw Exception("This shouldnt happen")
                })
                parentByState[syntheticStates.syntheticStateByChain[chain]!!.state] = parent

                val children = childrenByState[chain.last()]!!
                childrenByState[syntheticStates.syntheticStateByChain[chain]!!.state] = children

                progressCallback?.onProgress(index + 1, syntheticStates.chains.size, "Adding synthetic states")
            }
        }

        fun removeAllStatesWhichArePartOfChain(
            syntheticStates: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ) {
            val newparentByState = mutableMapOf<State, MarkedState>()
            val newchildrenByState = mutableMapOf<State, List<MarkedState>>()

            val totalStates = parentByState.size + childrenByState.size
            var processed = 0

            progressCallback?.onProgress(0, totalStates, "Removing chain states: filtering parents")
            parentByState.forEach { state, p ->
                if (syntheticStates.isPartOfChain(state).not()) newparentByState.put(state, p)
                processed++
                progressCallback?.onProgress(processed, totalStates, "Removing chain states")
            }

            childrenByState.forEach { state, c ->
                if (syntheticStates.isPartOfChain(state).not()) newchildrenByState.put(state, c)
                processed++
                progressCallback?.onProgress(processed, totalStates, "Removing chain states")
            }

            parentByState = newparentByState
            childrenByState = newchildrenByState
        }

        fun updateParents(
            syntheticStates: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ): Map<State, MarkedState> {
            progressCallback?.onProgress(0, parentByState.size, "Updating parent mappings")
            return parentByState.entries.mapIndexed { index, (state, parent) ->
                val result = state to if (parent.isInvalid) {
                    val chain = syntheticStates.chainByChainEnd[parent.state]!!
                    syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                } else parent

                progressCallback?.onProgress(index + 1, parentByState.size, "Updating parent mappings")
                result
            }.toMap()
        }

        fun updateChildren(
            syntheticStates: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ): Map<State, List<MarkedState>> {
            progressCallback?.onProgress(0, childrenByState.size, "Updating children mappings")
            return childrenByState.entries.mapIndexed { index, (state, children) ->
                val result = state to children.map { child ->
                    if (child.isInvalid) {
                        val chain = syntheticStates.chainByChainStart[child.state]
                        syntheticStates.syntheticStateByChain[chain]!!.copy(isInvalid = false)
                    } else child
                }

                progressCallback?.onProgress(index + 1, childrenByState.size, "Updating children mappings")
                result
            }.toMap()
        }

        fun buildNewEpa(
            epa: ExtendedPrefixAutomaton<T>,
            syntheticStates: SyntheticStates<T>,
            progressCallback: EpaProgressCallback?
        ): ExtendedPrefixAutomaton<T> {
            progressCallback?.onProgress(0, 100, "Building new EPA: creating state mappings")
            val oldToNewStateMapping = mutableMapOf<State, State>()

            oldToNewStateMapping[State.Root] = State.Root

            // Step 1: Build states in dependency order (parents before children)
            val processedStates = mutableSetOf<State>()
            val stateQueue = ArrayDeque<State>()

            // Start with states that have Root as parent
            parentByState.entries
                .filter { (_, parent) -> parent.state == State.Root }
                .forEach { (state, _) -> stateQueue.add(state) }

            val totalStatesToProcess = parentByState.size
            var statesProcessed = 0

            progressCallback?.onProgress(10, 100, "Building new EPA: processing states in dependency order")
            // Process states in dependency order
            while (stateQueue.isNotEmpty()) {
                val currentState = stateQueue.removeFirst()

                if (currentState in processedStates) continue

                val parentInfo = parentByState[currentState]
                if (parentInfo == null) {
                    processedStates.add(currentState)
                    statesProcessed++
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
                statesProcessed++

                // Add children to queue
                val children = childrenByState[currentState] ?: emptyList()
                children.forEach { child ->
                    if (child.state !in processedStates) {
                        stateQueue.add(child.state)
                    }
                }

                if (statesProcessed % 100 == 0 || statesProcessed == totalStatesToProcess) {
                    progressCallback?.onProgress(
                        10 + (statesProcessed * 30) / totalStatesToProcess,
                        100,
                        "Building new EPA: processed $statesProcessed/$totalStatesToProcess states"
                    )
                }
            }

            // Step 2: Create transitions using new state instances
            progressCallback?.onProgress(40, 100, "Building new EPA: creating transitions")
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
            progressCallback?.onProgress(60, 100, "Building new EPA: building partition mappings")
            val newPartitionByState =
                buildPartitionMapping(epa, syntheticStates, oldToNewStateMapping, progressCallback)

            // Step 4: Build sequence mappings
            progressCallback?.onProgress(80, 100, "Building new EPA: building sequence mappings")
            val newSequenceByState = buildSequenceMapping(epa, syntheticStates, oldToNewStateMapping, progressCallback)

            progressCallback?.onProgress(95, 100, "Building new EPA: finalizing")
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
            oldToNewStateMapping: Map<State, State>,
            progressCallback: EpaProgressCallback?
        ): Map<State, Int> {
            val newPartitionByState = mutableMapOf<State, Int>()

            val nonChainStates = epa.states.filter { !syntheticStates.isPartOfChain(it) }
            val totalItems = nonChainStates.size + syntheticStates.partitionByChain.size
            var processed = 0

            progressCallback?.onProgress(0, totalItems, "Building partition mappings: processing non-chain states")
            // Add partitions for non-chain states (mapped to new state instances)
            nonChainStates.forEachIndexed { index, oldState ->
                val partition = epa.partition(oldState)
                val newState = oldToNewStateMapping[oldState] ?: oldState
                newPartitionByState[newState] = partition
                processed++

                if (processed % 100 == 0 || processed == nonChainStates.size) {
                    progressCallback?.onProgress(processed, totalItems, "Building partition mappings")
                }
            }

            progressCallback?.onProgress(
                nonChainStates.size,
                totalItems,
                "Building partition mappings: processing synthetic states"
            )
            // Add partitions for synthetic states
            syntheticStates.partitionByChain.entries.forEachIndexed { index, (chain, partition) ->
                val syntheticState = oldToNewStateMapping[syntheticStates.syntheticStateByChain[chain]!!.state]
                if (syntheticState != null) {
                    newPartitionByState[syntheticState] = partition
                }
                processed++
                progressCallback?.onProgress(processed, totalItems, "Building partition mappings")
            }

            return newPartitionByState
        }

        private fun buildSequenceMapping(
            epa: ExtendedPrefixAutomaton<T>,
            syntheticStates: SyntheticStates<T>,
            oldToNewStateMapping: Map<State, State>,
            progressCallback: EpaProgressCallback?
        ): Map<State, Set<Event<T>>> {
            val newSequenceByState = mutableMapOf<State, Set<Event<T>>>()

            val nonChainStates = epa.states.filter { !syntheticStates.isPartOfChain(it) }
            val totalItems = nonChainStates.size + syntheticStates.seqByChain.size
            var processed = 0

            progressCallback?.onProgress(0, totalItems, "Building sequence mappings: processing non-chain states")
            // Add sequences for non-chain states (mapped to new state instances)
            nonChainStates.forEachIndexed { index, oldState ->
                val sequence = epa.sequence(oldState)
                val newState = oldToNewStateMapping[oldState] ?: oldState
                newSequenceByState[newState] = sequence
                processed++

                if (processed % 100 == 0 || processed == nonChainStates.size) {
                    progressCallback?.onProgress(processed, totalItems, "Building sequence mappings")
                }
            }

            progressCallback?.onProgress(
                nonChainStates.size,
                totalItems,
                "Building sequence mappings: processing synthetic states"
            )
            // Add sequences for synthetic states
            syntheticStates.seqByChain.entries.forEachIndexed { index, (chain, sequence) ->
                val syntheticState = oldToNewStateMapping[syntheticStates.syntheticStateByChain[chain]!!.state]
                if (syntheticState != null) {
                    newSequenceByState[syntheticState] = sequence
                }
                processed++
                progressCallback?.onProgress(processed, totalItems, "Building sequence mappings")
            }

            return newSequenceByState
        }
    }

    override fun apply(
        epa: ExtendedPrefixAutomaton<T>,
        progressCallback: EpaProgressCallback?
    ): ExtendedPrefixAutomaton<T> {
        val mapping = Mapping<T>()

        // Step 1: Initialize mappings
        progressCallback?.onProgress(0, 100, "$name: Building parent-child mappings")
        val childrenByParent = epa.transitions.groupBy { it.start }.mapValues { it.value.map { it.end } }
        val parentByChild =
            epa.transitions.groupBy { it.end }.mapValues { it.value.map { transition -> transition.start }.first() }

        progressCallback?.onProgress(0, childrenByParent.size, "$name: Processing children mappings")
        childrenByParent.entries.forEachIndexed { index, (state, children) ->
            mapping.addChildrenForState(state, children.map { MarkedState(it as State.PrefixState, false) })
            progressCallback?.onProgress(index + 1, childrenByParent.size, "$name: Processing children mappings")
        }

        progressCallback?.onProgress(0, parentByChild.size, "$name: Processing parent mappings")
        parentByChild.entries.forEachIndexed { index, (state, parent) ->
            mapping.addParentForState(state, MarkedState(parent, false))
            progressCallback?.onProgress(index + 1, parentByChild.size, "$name: Processing parent mappings")
        }

        progressCallback?.onProgress(0, epa.states.size, "$name: Initializing state registry")
        epa.states.forEachIndexed { index, state ->
            mapping.addIfNotPresent(state)
            progressCallback?.onProgress(index + 1, epa.states.size, "$name: Initializing state registry")
        }

        // Step 2: Detect chains
        val syntheticStates = mapping.detectChains(epa, progressCallback)

        // Step 3: Build new mappings
        mapping.parentByState = mapping.markParentsIfInvalid(syntheticStates, progressCallback).toMutableMap()
        mapping.childrenByState = mapping.markChildrenIfInvalid(syntheticStates, progressCallback).toMutableMap()
        mapping.addSyntheticStates(syntheticStates, progressCallback)
        mapping.removeAllStatesWhichArePartOfChain(syntheticStates, progressCallback)
        mapping.parentByState = mapping.updateParents(syntheticStates, progressCallback).toMutableMap()
        mapping.childrenByState = mapping.updateChildren(syntheticStates, progressCallback).toMutableMap()

        // Step 4: Construct filtered EPA
        return mapping.buildNewEpa(epa.copy(), syntheticStates, progressCallback)
    }
}