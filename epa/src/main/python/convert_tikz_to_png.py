import os
import subprocess
import glob
import sys
import shutil

def main():
    if len(sys.argv) < 3:
        print("Usage: python3 convert_organized.py [source_dir] [target_dir]")
        return

    # 1. Setup paths
    source_dir = os.path.abspath(sys.argv[1])
    target_dir = os.path.abspath(sys.argv[2])

    if not os.path.isdir(source_dir):
        print(f"Error: Source directory '{source_dir}' not found.")
        return

    # Create target directory if it doesn't exist
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)
        print(f"Created target directory: {target_dir}")

    # 2. Find all .tikz files
    files = glob.glob(os.path.join(source_dir, "*.tikz"))
    if not files:
        print(f"No .tikz files found in {source_dir}")
        return

    for tikz_path in sorted(files):
        filename = os.path.basename(tikz_path)
        base_name = os.path.splitext(filename)[0]

        print(f"--- Processing: {filename} ---")

        # 3. Read and wrap TikZ content
        with open(tikz_path, 'r') as f:
            lines = f.readlines()

        new_content = ["\\documentclass[border=10pt]{standalone}\n"]
        found_start = False
        for line in lines:
            if "\\begin{tikzpicture}" in line and not found_start:
                new_content.append("\\begin{document}\n")
                found_start = True
            new_content.append(line)
        new_content.append("\n\\end{document}")

        # 4. Create a temporary .tex file in the TARGET dir to keep source clean
        temp_tex = os.path.join(target_dir, f"{base_name}_temp.tex")
        with open(temp_tex, "w") as f:
            f.writelines(new_content)

        # 5. Compile with LuaLaTeX
        # We run it inside the target folder so all aux files stay there
        subprocess.run([
            "lualatex",
            "-interaction=nonstopmode",
            "-output-directory=" + target_dir,
            "-jobname=" + base_name,
            temp_tex
        ], stdout=subprocess.DEVNULL)

        # 6. Convert PDF to PNG using 'magick'
        pdf_file = os.path.join(target_dir, f"{base_name}.pdf")
        png_file = os.path.join(target_dir, f"{base_name}.png")

        if os.path.exists(pdf_file):
            subprocess.run([
                "magick",
                "-density", "300",
                pdf_file,
                "-quality", "100",
                png_file
            ])
            print(f"Saved to: {png_file}")
        else:
            print(f"Failed to generate PDF for {filename}")

        # 7. Cleanup intermediate LaTeX files in target directory
        for ext in [".pdf", ".aux", ".log", "_temp.tex"]:
            file_to_del = os.path.join(target_dir, base_name + ext)
            if os.path.exists(file_to_del):
                os.remove(file_to_del)

    print("\nAll done! Your source files remain untouched.")

if __name__ == "__main__":
    main()