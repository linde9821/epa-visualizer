# EPA Visualizer

🎓 _HU Studienarbeit — Moritz Lindner_

[![Gradle Build & Test](https://github.com/linde9821/epa-visualizer/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/linde9821/epa-visualizer/actions/workflows/gradle.yml)

## 🔍 Overview

**EPA Visualizer** constructs and visualizes an **Extended Prefix Automaton (EPA)** from event logs.  
An EPA is a prefix automaton extended with partitions, where each partition represents a distinct *variant* (unique 
execution trace) observed in the event log.

EPAs provide a complete, non-abstracted representation of all observed process behavior — preserving full trace 
information while capturing shared prefixes in a compact graphical form.  
This enables interactive and animated visualization of process behavior and variation, with a primary focus on exploring 
variants within the process.

The tool helps users understand process complexity and behavior through:
- variant-based views of the process
- structural insights into common prefixes and branching points
- flexible filtering options to explore and manage large or deep EPAs 
- statistics and animations of events flowing through an EPA

Key capabilities include advanced filtering, playback of process cases, and statistical summaries. The tool supports 
event logs in `.xes` and `.xes.gz` formats.

---

## ✨ Features

- **Event Log Import**:
  - Supports `.xes` and `.xes.gz` formats
- **EPA Construction**:
  - Automatic generation of an Extended Prefix Automaton from event logs
- **Visualization**:
  - Multiple graph layout algorithms
  - Zoom, scroll, and interactive navigation
  - Animated playback of complete logs or individual cases
  - Flexible filter combinations for tailored analysis
- **Statistics**:
  - Events per node
  - Case counts
  - Activity frequencies
  - Time intervals
  - Partition-based statistics

---

## 📝 Planned Features and Known Issues

### Planned Features / Ideas

#### Layouts

- **New Layout:** *Weighted Direct Angular Placement*  
  → Take the number of nodes in a node's subtree into account when calculating the arc assigned to each subtree

- **New Layout:** *Time-Radius-Semantic*  
  → Make the depth (radius) of a nodes placement dependent on a time component — e.g., maybe also in animation based on
  cycle time in a time window

- **New Layout:** *Probability Semantic*  
  → Add a value representing the probability of each partition and visualize it

#### Filters

- **New Filter:** *Chain Pruning*  
  → Many long "chains" exist in the EPA (subgraphs with only one incoming edge and one outgoing edge).  
  These chains could be collapsed into a single new state for more compact visualization.

- **New Filter:** *Depth Interval*  
  → Filter the visualization to only show nodes within a given radius (depth) interval.

- **New Filter:** *Normalized Entropy Partition Filter*  
  → Based on normalized entropy measures (see Augusto, Mendling, Vidgof, & Wurm (2022) — Extended Prefix Automata).

- **New Filter:** *Normalized Entropy Variant Filter*  
  → Based on normalized entropy measures (see Augusto, Mendling, Vidgof, & Wurm (2022) — Extended Prefix Automata).

#### Visualization

- **State Properties**  
  → States could have visual properties (color, size, etc.) mapped to various attributes — also changing dynamically during animation.
  (Differentiate edge thickness or color based on event count, the ideas are endless)

- **Case Properties**  
  → Each case in the animation could display various properties, with the ability to track a selected case during the animation.

- **Event Properties**  
  → Each event in the animation could display various properties (visualized per event instance).

#### UI

- Improve observability during loading  
  → Currently the UI shows an indeterminate progress bar. This could be changed to display actual progress  
  (both the construction and visitor processes can be extended/utilized to track "x out of total elements processed").

- More "desktop look & feel"  
  → Switching to [JetBrains Jewel](https://github.com/JetBrains/intellij-community/tree/master/platform/jewel) components could provide a more native desktop style.

#### Others

- The tree layout algorithm is fairly general and could be provided as a standalone library for others to use.

- The `epa` module could be packaged and provided as a separate library — reusable in other projects.

### Known Bugs

- Component state (Filter, Animation, etc.) is lost on recomposition  
  → _TODO: fix_

- Slider in Animation UI for full log animation cannot be moved while animation is running  
  → _TODO: fix_

- View Model usage state bugs (e.g., stopping the animation results in no events being drawn for full log animation)  
  → _TODO: fix_

### Potential Improvements

- Pre-render the tree layout to a texture and render the texture instead of the entire tree on every frame  
  _(similar to how labels are currently rendered in the tree); rerender only when necessary (e.g., after filtering)_

- Core data structures in EPA construction are optimized, but some data structures and algorithms in animation/statistics 
  and others are not optimal — can be improved for better performance

---

## 🗂️ Project Structure

The project consists of two modules:

1. **`epa`** — core logic for EPA construction, layout generation, visitors for automation, statistics, etc.
2. **`ui`** — desktop application (Kotlin Compose Desktop), built on top of the `epa` module.

---

## 🚀 Getting Started

### Prerequisites

- JDK **21+**
- Kotlin **2.1.20+**
- Gradle (or use the included Gradle wrapper)

### Run the App

```bash
./gradlew run
```

### Custom `EventLogMapper`

To support additional event log formats, you can implement a custom EventLogMapper and plug it into the application.
Out-of-the-box, the project provides mappers for logs included in the resources folder.

### Writing code

The project is designed for easy extension.
In most cases, using the `AutomataVisitor` interface is sufficient for adding new functionality — it provides complete 
and correct traversal (depth-first or breadth-first) of the EPA.
Core features such as filtering, animation, and statistics are already implemented using this pattern — making it a 
natural extension point for new features or experiments.

## 🖥️ Technologies

- **Kotlin** Gradle project
- **Kotlin Compose Desktop**
- **OpenXES**

---

## 📚 Background
This software is part of a university Studienarbeit (pre-master thesis project) exploring new visualization techniques 
for process mining.

Based on concepts from (but not limited to):
- Augusto, Mendling, Vidgof, & Wurm (2022) – Extended Prefix Automata
- Visualization: Radial tidy tree with optimizations from Buchheim et al. (2002)
- TODO: add all references

## 🙋‍♂️ Author
Moritz Lindner
Senior DevOps Engineer & Informatik M.Sc. Student
Berlin, Germany
