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
        val childrenByParent = epa.transitions.groupBy { it.start }.mapValues { it.value.map { it.end } }
        val parentsByState = invertMap(childrenByParent)

        val childrenByParentWithOneChildren =
            childrenByParent.filterValues { it.size == 1 }.filterKeys { it as? State.PrefixState != null }
                .mapValues { it.value.first() as State.PrefixState }

        val activitiesToRemove = emptyList<Activity>()//childrenByParentWithOneChildren.values.map { it.via }

        val preStates = epa.states.filter { !childrenByParentWithOneChildren.keys.contains(it) }
        val prePartitionByState = preStates.associateWith { epa.partition(it) }
        val preActivities = epa.activities.filter { !activitiesToRemove.contains(it) }
        val preSeqByState: Map<State, Set<Event<T>>> = preStates.associateWith { epa.sequence(it) }

        val umergedLists = childrenByParentWithOneChildren.map { singleTargetState ->
            listOf(singleTargetState.key) + followChain(singleTargetState.key, childrenByParent, emptyList())
        }

        val chains = mergeLists(umergedLists)
        val activityByChain = chains.associateWith { chain ->
            chain.fold(Activity("")) { a, b ->
                Activity(a.name + (b as State.PrefixState).name)
            }
        }
        val newActivities = preActivities + activityByChain.values
        val syntheticStateByChain = chains.associateWith { chain ->
            chain.reduce { a, b ->
                State.PrefixState(
                    from = (a as State.PrefixState).from,
                    via = activityByChain[chain]!!
                )
            }
        }
        val seqBySyntetic: Map<State, Set<Event<T>>> = chains.associateWith { chain ->
            chain.fold(emptyList<Event<T>>()) { acc, state ->
                acc + epa.sequence(state)
            }.toSet()
        }.mapKeys { syntheticStateByChain[it.key]!! }

        val parBySynthetic = chains.associateWith { chain ->
            epa.partition(chain.first())
        }.mapKeys { syntheticStateByChain[it.key]!! }

        val syntheticStateByChainStart = syntheticStateByChain.mapKeys { a -> a.key.first() }

        val newSeqByState = mergeMapsMulti(preSeqByState, seqBySyntetic)
        val newParByState = mergeMaps(prePartitionByState, parBySynthetic)

        val syntheticByChainparts = buildMap {
            syntheticStateByChain.forEach { (states, synth) ->
                states.forEach { state ->
                    put(state, synth)
                }
            }
        }

        val fromCorrectedPreStates = preStates
            .map { state ->
                if (state is State.PrefixState) {
                    val from = state.from
                    if (chains.flatten().contains(from)) {
                        val newFrom = syntheticByChainparts[from]!!
                        State.PrefixState(
                            from = newFrom,
                            via = state.via
                        )
                    } else state
                } else {
                    state
                }
            }
        val newStates = (fromCorrectedPreStates + syntheticStateByChain.values).toSet()

        val syntheticByParent = buildMap<State, List<State>> {
            childrenByParent.forEach { (parent, children) ->
                children.forEach { child ->
                    if (syntheticStateByChainStart[child] != null) {
                        val alreadyPresnt = get(parent) ?: emptyList<State>()
                        put(parent, listOf(syntheticStateByChainStart[child]!!) + alreadyPresnt)
                    }
                }
            }
        }

        val childrenBySynthetic = chains.associateWith { chain ->
            val last = chain.last()
            val targets = (childrenByParent[last] ?: emptyList())

            targets.map { target ->
                // if target is synthetic
                if (chains.flatten().contains(target)) {
                    println("yes")
                    val newFrom = syntheticByChainparts[target]!!
                    println("newFrom for $target is $newFrom")
                    newFrom
                } else State.PrefixState(
                    from = syntheticStateByChain[chain]!!,
                    via = (target as State.PrefixState).via
                )
            }
        }.mapKeys { syntheticStateByChain[it.key]!! }
        val finalMapping = buildMap<State, List<State>> {
            childrenByParent.forEach { parent, children ->
                if (childrenByParentWithOneChildren[parent] != null) return@forEach
                if (chains.flatten().contains(parent)) return@forEach
                val newChildren = children.filter {
                    childrenByParentWithOneChildren[it] == null
                }
                put(parent, newChildren)
            }

            syntheticByParent.forEach { parent, synthetic ->
                val alreadyPreeasent = get(parent) ?: emptyList()

                put(parent, alreadyPreeasent + synthetic)
            }

            putAll(childrenBySynthetic)
        }

        val transitions = finalMapping.flatMap { (parent, children) ->
            children.map { child ->
                Transition(
                    start = parent,
                    activity = (child as State.PrefixState).via,
                    end = child
                )
            }
        }

        return ExtendedPrefixAutomaton(
            eventLogName = epa.eventLogName,
            states = newStates,
            activities = newActivities.toSet(),
            transitions = transitions.toSet(),
            partitionByState = newParByState,
            sequenceByState = newSeqByState
        )
    }

    private fun followChain(a: State, mappings: Map<State, List<State>>, acc: List<State>): List<State> {
        if (mappings[a] != null) {
            val n = mappings[a]!!.first()
            return if (mappings[a]!!.size == 1) followChain(n, mappings, acc + n)
            else acc
        } else return acc
    }

    private fun mergeLists(lists: List<List<State>>): List<List<State>> {
        return lists.filter { current ->
            // Keep only those lists that are not strict subsets of any other
            lists.none { other ->
                current != other && other.containsAll(current)
            }
        }
    }

    private fun <T> invertMap(map: Map<T, List<T>>): Multimap<T, T> {
        val multimap: Multimap<T, T> = ArrayListMultimap.create()
        for ((key, values) in map) {
            for (value in values) {
                multimap.put(value, key)
            }
        }
        return multimap
    }

    private fun <T, R> mergeMapsMulti(
        first: Map<T, Set<R>>,
        second: Map<T, Set<R>>
    ): Map<T, Set<R>> {
        return (first.keys + second.keys).associateWith { key ->
            (first[key].orEmpty() + second[key].orEmpty())
        }
    }

    private fun <T, R> mergeMaps(
        first: Map<T, R>,
        second: Map<T, R>
    ): Map<T, R> {
        return (first.keys + second.keys).associateWith { key ->
            first[key].let { v ->
                if (v != null) v
                else second[key]!!
            }
        }
    }
}