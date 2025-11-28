import json
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# Get the rocket colormap
all_cmaps = plt.colormaps()

color_data = {}
n_colors = 64

for palette_name in all_cmaps:
    try:
        cmap = sns.color_palette(palette_name, as_cmap=True)
        colors = []

        for i in range(n_colors):
            color = cmap(i / (n_colors - 1))
            r, g, b = int(color[0] * 255), int(color[1] * 255), int(color[2] * 255)
            colors.append([r, g, b])

        color_data[palette_name] = colors
        print(f"Added {palette_name}")
    except Exception as e:
        print(f"Skipped {palette_name}: {e}")

# Save to JSON
with open('colormaps.json', 'w') as f:
    json.dump(color_data, f, indent=2)

print(f"\nSaved {len(color_data)} palettes to colormaps.json")