import heuristic.ManhattanDistanceHeuristic;
import heuristic.MisplacedTilesHeuristic;
import model.SearchResult;
import puzzle.PuzzleState;
import solver.AStarSolver;
import util.InputReader;
import util.PuzzleGenerator;

import java.util.Scanner;

/**
 * CS 4200 Project 1 - 8-Puzzle Solver using A* Search
 * 
 * Main application class that orchestrates the puzzle solving process.
 * Provides a command-line interface for:
 * - Selecting input method (random or manual)
 * - Choosing heuristic function (H1 or H2)
 * - Displaying solution path and performance metrics
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("CS 4200 Project 1");
        System.out.println("Select Input Method:");
        System.out.println("[1] Random");
        System.out.println("[2] Manual");
        int inputMethod = InputReader.readInt(sc);

        String startState;

        if (inputMethod == 1) {
            // Generate a random puzzle that is guaranteed to be solvable
            startState = PuzzleGenerator.generateRandom();
        } else if (inputMethod == 2) {
            // Accept manual input of 9 integers representing the puzzle state
            System.out.println("Enter the puzzle as 9 integers (0-8) in row-major order:");
            int[][] puzzle = InputReader.readPuzzle(sc);
            startState = PuzzleState.toString(puzzle);
        } else {
            System.out.println("Invalid selection. Please choose 1 or 2.");
            sc.close();
            return;
        }

        // Validate input: must be digits 0-8 exactly once (0 is the blank)
        if (!PuzzleState.isValidState(startState)) {
            System.out.println("Invalid puzzle: must contain digits 0-8 exactly once (0 is the blank).");
            sc.close();
            return;
        }

        System.out.println("Puzzle:");
        System.out.println(PuzzleState.toGridString(startState));

        // Check solvability: puzzles with odd inversion counts are unsolvable
        if (!PuzzleState.isSolvable(startState)) {
            System.out.println("Puzzle is unsolvable");
            sc.close();
            return;
        }

        System.out.println("Select H Function:");
        System.out.println("[1] H1");
        System.out.println("[2] H2");
        int hChoice = InputReader.readInt(sc);

        if (hChoice != 1 && hChoice != 2) {
            System.out.println("Invalid H selection. Please choose 1 or 2.");
            sc.close();
            return;
        }

        // Execute A* search with both heuristics to compare performance
        AStarSolver solverH1 = new AStarSolver(new MisplacedTilesHeuristic());
        AStarSolver solverH2 = new AStarSolver(new ManhattanDistanceHeuristic());
        
        SearchResult resultH1 = solverH1.solve(startState);
        SearchResult resultH2 = solverH2.solve(startState);

        if (resultH1 == null || resultH2 == null) {
            System.out.println("No solution found.");
            sc.close();
            return;
        }

        // Display solution path for the user-selected heuristic
        SearchResult chosen = (hChoice == 1) ? resultH1 : resultH2;

        System.out.println("Solution Found");

        // Print each step of the solution (step 1 is the first move from initial state)
        for (int step = 1; step < chosen.getPath().size(); step++) {
            System.out.println("Step: " + step);
            System.out.println(PuzzleState.toGridString(chosen.getPath().get(step)));
        }

        // Display performance metrics for both heuristics
        System.out.println("H1 Search Cost: " + resultH1.getNodesGenerated());
        System.out.println("H2 Search Cost: " + resultH2.getNodesGenerated());
        System.out.printf("H1 Time: %.3f ms%n", resultH1.getTimeMs());
        System.out.printf("H2 Time: %.3f ms%n", resultH2.getTimeMs());

        sc.close();
    }
}
