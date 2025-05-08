# EPA Visualizer

🎓 _HU Studienarbeit Moritz Linder_

This project constructs and visualizes an **Extended Prefix Automaton (EPA)** from event logs. It supports various log formats (`.xes`, `.xes.gz`) and renders the automaton using a **radial tidy tree layout** based on the optimized **Reingold–Tilford algorithm**.

---

## 🔍 Overview

The EPA serves as a compact representation of all observed process execution traces. This tool transforms the event log into a visual graph structure that resembles a spiderweb, providing interactive and animated insight into process behavior.

### ✨ Planned Features

- 📥 Import of `.xes`, `.xes.gz`, (and `.csv` maybe...) event logs
- 🧠 Automatic construction of an **Extended Prefix Automaton** (EPA)
- 🌐 Visualization as **Radial Tidy Tree** using the optimized Reingold–Tilford layout
- 🧩 Interactive features:
    - Zoom and scroll
    - Highlight individual traces
    - Playback of full event logs or single cases
    - Dynamic trace cursor and trace state visualization

- 📊 Metric overlays (planned):
    - Cycle time (absolute & relative)
    - Path so far / future path
    - Events per node
    - ...
- 🧼 Filter and merge options for high variant complexity

---

## 🖥️ Technologies

- **Kotlin** Gradle project 
- **Kotlin Compose Desktop**
- **OpenXES**
---

## 🚀 Getting Started

### Requirements

- JDK 21+
- Kotlin (2.1.20+)
- Gradle (or use wrapper)

### Run the App

```bash
./gradlew run
```

## 📚 Background
This software is part of a university Studienarbeit (pre-master thesis project) exploring advanced visualization techniques for process mining and behavior modeling.

Based on concepts from:
Augusto, Mendling, Vidgof, & Wurm (2022) – Extended Prefix Automata

Visualization: Radial tidy tree with optimizations from Buchheim et al. (2002)

## 🙋‍♂️ Author
Moritz Lindner
Senior DevOps Engineer & Informatik M.Sc. Student
Berlin, Germany