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
*variants* within the process.

The tool helps users understand process complexity and behavior through:
- variant-based views of the process
- structural insights into common prefixes and branching points
- flexible filtering options to explore and manage large or deep EPAs 
- statistics and animations of events flowing through a EPA

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
