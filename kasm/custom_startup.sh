#!/bin/bash

# Give Kasm 5 seconds to spin up the virtual display
sleep 5

# Force Compose Desktop / Skiko to use software rendering instead of OpenGL
export _JAVA_OPTIONS="-Dskiko.renderApi=SOFTWARE"

# Run the app in the foreground
"/opt/EPA Visualizer/bin/EPA Visualizer"