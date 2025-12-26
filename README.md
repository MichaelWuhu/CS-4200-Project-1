# CS-4200-Project-1

A* solver for the 8‑puzzle with two heuristics:
- H1: Misplaced tiles
- H2: Manhattan distance

## Prerequisites
- JDK 11+ installed.

## How to Run
Run `Main.java` from IDE to run the program.

- VS Code:
  1) Open the folder in VS Code.
  2) Open `Main.java`.
  3) Click "Run Java" (or the Run ▶ button above `main`).

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
