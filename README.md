# CS-4200-Project-1

A* solver for the 8‑puzzle with two heuristics:
- H1: Misplaced tiles
- H2: Manhattan distance

## Prerequisites
- JDK 11+ installed and on your PATH (`javac`, `java`).
- Working directory: repository root.

## Quick start (unzip & run)
1) Ensure a JDK with `javac` is installed (Java 11+). Check with:
	- `javac -version` and `java -version`
2) Unzip, then from the project root:
	- Compile: `javac Main.java model/*.java heuristic/*.java puzzle/*.java solver/*.java util/*.java`
	- Run: `java -cp . Main`

### Windows note
The same commands work in PowerShell/CMD. Keep the `-cp .` so the current folder is on the classpath.

## Usage
1) Choose input method:
	- `1` Random solvable puzzle
	- `2` Manual: enter 9 integers (0-8) in row-major order, 0 is the blank
2) Choose heuristic:
	- `1` H1 (misplaced tiles)
	- `2` H2 (Manhattan distance)
3) The program prints the solution path and reports nodes generated and elapsed time for both heuristics.

## Notes
- Unsolvable inputs are detected via inversion count and reported immediately.
- For repeat runs, recompile only if you change source files; otherwise rerun `java Main`.
