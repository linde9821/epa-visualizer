import pandas as pd

import seaborn as sns
import numpy as np

# Get the rocket colormap

cmap = sns.color_palette("crest", as_cmap=True)

# Sample at n_colors points (0.0 to 1.0)
n_colors = 64
colors = [cmap(i / (n_colors - 1)) for i in range(n_colors)]

# Convert to 0-255 RGB
for i, color in enumerate(colors):
    r, g, b = int(color[0] * 255), int(color[1] * 255), int(color[2] * 255)
    print(f"Color.makeRGB({r}, {g}, {b}),  // Position {i/(n_colors-1):.2f}")