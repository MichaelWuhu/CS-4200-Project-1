import java.util.*;

/**
 * CS 4200 Project 1 - 8-Puzzle Solver using A* Search
 * 
 * This program solves the 8-puzzle problem using the A* algorithm with two
 * heuristics:
 * - H1: Number of misplaced tiles (excluding the blank)
 * - H2: Sum of Manhattan distances (excluding the blank)
 * 
 * The program:
 * 1. Accepts puzzle input via random generation or manual entry
 * 2. Validates puzzle solvability using inversion count
 * 3. Runs A* search with both heuristics
 * 4. Displays the solution path for the user-selected heuristic
 * 5. Reports search costs and execution times for both heuristics
 */
public class Solution {

    /** Goal state represented as a string in row-major order */
    private static final String GOAL = "012345678";

    /** Pre-computed goal row positions for each tile (0-8) */
    private static final int[] goalRow = new int[9];

    /** Pre-computed goal column positions for each tile (0-8) */
    private static final int[] goalCol = new int[9];

    static {
        // Pre-compute goal positions for efficient Manhattan distance calculation
        for (int i = 0; i < 9; i++) {
            int tile = GOAL.charAt(i) - '0';
            goalRow[tile] = i / 3;
            goalCol[tile] = i % 3;
        }
    }

    /** Random number generator for puzzle scrambling */
    private static final Random RNG = new Random();

    /** Number of random moves to generate a solvable puzzle */
    private static final int SCRAMBLE_MOVES = 30;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("CS 4200 Project 1");
        System.out.println("Select Input Method:");
        System.out.println("[1] Random");
        System.out.println("[2] Manual");
        int inputMethod = safeReadInt(sc);

        String startState;

        if (inputMethod == 1) {
            // Generate a random puzzle that is guaranteed to be solvable
            startState = generateRandomPuzzle();
        } else if (inputMethod == 2) {
            // Accept manual input of 9 integers representing the puzzle state
            System.out.println("Enter the puzzle as 9 integers (0-8) in row-major order:");
            int[][] puzzle = readPuzzleFromStdin(sc);
            startState = puzzleToString(puzzle);
        } else {
            System.out.println("Invalid selection. Please choose 1 or 2.");
            sc.close();
            return;
        }

        System.out.println("Puzzle:");
        System.out.println(toGridString(startState));

        // Check solvability: puzzles with odd inversion counts are unsolvable
        int inversions = countInversions(startState);
        if (inversions % 2 == 1) {
            System.out.println("Puzzle is unsolvable");
            sc.close();
            return;
        }

        System.out.println("Select H Function:");
        System.out.println("[1] H1");
        System.out.println("[2] H2");
        int hChoice = safeReadInt(sc);

        if (hChoice != 1 && hChoice != 2) {
            System.out.println("Invalid H selection. Please choose 1 or 2.");
            sc.close();
            return;
        }

        // Execute A* search with both heuristics to compare performance
        AStarResult resultH1 = aStar(startState, 1);
        AStarResult resultH2 = aStar(startState, 2);

        if (resultH1 == null || resultH2 == null) {
            System.out.println("No solution found.");
            sc.close();
            return;
        }

        // Display solution path for the user-selected heuristic
        AStarResult chosen = (hChoice == 1) ? resultH1 : resultH2;

        System.out.println("Solution Found");

        // Print each step of the solution (step 1 is the first move from initial state)
        for (int step = 1; step < chosen.path.size(); step++) {
            System.out.println("Step: " + step);
            System.out.println(toGridString(chosen.path.get(step)));
        }

        // Display performance metrics for both heuristics
        System.out.println("H1 Search Cost: " + resultH1.nodesGenerated);
        System.out.println("H2 Search Cost: " + resultH2.nodesGenerated);
        System.out.printf("H1 Time: %.3f ms%n", resultH1.timeMs);
        System.out.printf("H2 Time: %.3f ms%n", resultH2.timeMs);

        sc.close();
    }

    // ============================================================
    // A* Search Implementation
    // ============================================================

    /**
     * Represents a node in the A* search tree.
     */
    private static class Node {
        final String state; // Puzzle state as a string
        final int g; // Cost from start to this node (depth)
        final int f; // Estimated total cost: f = g + h
        final long id; // Unique ID for consistent tie-breaking in priority queue

        Node(String state, int g, int f, long id) {
            this.state = state;
            this.g = g;
            this.f = f;
            this.id = id;
        }
    }

    /**
     * Encapsulates the results of an A* search execution.
     */
    private static class AStarResult {
        final List<String> path; // Solution path from start to goal
        final int nodesGenerated; // Total number of nodes generated (search cost)
        final double timeMs; // Execution time in milliseconds

        AStarResult(List<String> path, int nodesGenerated, double timeMs) {
            this.path = path;
            this.nodesGenerated = nodesGenerated;
            this.timeMs = timeMs;
        }
    }

    /**
     * Executes A* search algorithm to find optimal solution path.
     * 
     * @param start           The initial puzzle state
     * @param heuristicChoice 1 for H1 (misplaced tiles), 2 for H2 (Manhattan
     *                        distance)
     * @return AStarResult containing solution path, search cost, and execution
     *         time, or null if no solution
     */
    private static AStarResult aStar(String start, int heuristicChoice) {
        long t0 = System.nanoTime();

        // Priority queue ordered by f-value, then g-value, then insertion order
        PriorityQueue<Node> frontier = new PriorityQueue<>(
                Comparator.<Node>comparingInt(n -> n.f)
                        .thenComparingInt(n -> n.g)
                        .thenComparingLong(n -> n.id));

        Map<String, Integer> gScore = new HashMap<>(); // Best known cost to reach each state
        Map<String, String> parent = new HashMap<>(); // Parent pointers for path reconstruction
        Set<String> closed = new HashSet<>(); // Already expanded states

        long pushId = 0; // Monotonically increasing ID for tie-breaking

        // Initialize with start state
        int h0 = heuristic(start, heuristicChoice);
        frontier.add(new Node(start, 0, h0, pushId++));
        gScore.put(start, 0);
        parent.put(start, null);

        int nodesGenerated = 0;

        while (!frontier.isEmpty()) {
            Node curNode = frontier.poll();
            String cur = curNode.state;

            // Skip if this state was already expanded (handles duplicate entries)
            if (closed.contains(cur)) {
                continue;
            }
            closed.add(cur);

            // Check for goal state
            if (cur.equals(GOAL)) {
                List<String> path = reconstructPath(parent, cur);
                long t1 = System.nanoTime();
                double ms = (t1 - t0) / 1_000_000.0;
                return new AStarResult(path, nodesGenerated, ms);
            }

            int curG = gScore.get(cur);

            // Generate and process all neighboring states
            for (String nxt : neighbors(cur)) {
                nodesGenerated++;

                if (closed.contains(nxt)) {
                    continue;
                }

                int tentativeG = curG + 1;
                Integer bestKnown = gScore.get(nxt);

                // Update if we found a better path to this state
                if (bestKnown == null || tentativeG < bestKnown) {
                    gScore.put(nxt, tentativeG);
                    parent.put(nxt, cur);
                    int h = heuristic(nxt, heuristicChoice);
                    frontier.add(new Node(nxt, tentativeG, tentativeG + h, pushId++));
                }
            }
        }

        return null; // No solution found
    }

    /**
     * Reconstructs the solution path from start to goal using parent pointers.
     * 
     * @param parent Map of state to parent state
     * @param goal   The goal state
     * @return List of states from start to goal
     */
    private static List<String> reconstructPath(Map<String, String> parent, String goal) {
        List<String> path = new ArrayList<>();
        String cur = goal;
        while (cur != null) {
            path.add(cur);
            cur = parent.get(cur);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Evaluates the heuristic function for a given state.
     * 
     * @param state  The puzzle state to evaluate
     * @param choice 1 for H1, 2 for H2
     * @return The heuristic value
     */
    private static int heuristic(String state, int choice) {
        return (choice == 1) ? h1(state) : h2(state);
    }

    // ============================================================
    // Neighbor Generation
    // ============================================================

    /**
     * Generates all valid neighboring states by moving the blank tile.
     * 
     * @param state Current puzzle state
     * @return List of neighboring states (2-4 neighbors depending on blank
     *         position)
     */
    private static List<String> neighbors(String state) {
        int z = state.indexOf('0'); // Find blank tile position
        int zr = z / 3; // Blank row
        int zc = z % 3; // Blank column

        List<String> res = new ArrayList<>(4);

        // Generate neighbors in fixed order for consistency
        if (zr > 0)
            res.add(swap(state, z, z - 3)); // Move up
        if (zr < 2)
            res.add(swap(state, z, z + 3)); // Move down
        if (zc > 0)
            res.add(swap(state, z, z - 1)); // Move left
        if (zc < 2)
            res.add(swap(state, z, z + 1)); // Move right

        return res;
    }

    /**
     * Creates a new state by swapping two tiles.
     * 
     * @param s Current state string
     * @param i First position to swap
     * @param j Second position to swap
     * @return New state string with tiles swapped
     */
    private static String swap(String s, int i, int j) {
        char[] a = s.toCharArray();
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        return new String(a);
    }

    // ============================================================
    // Random Puzzle Generation
    // ============================================================

    /**
     * Generates a random solvable puzzle by applying random moves from the goal
     * state.
     * This ensures the generated puzzle is always solvable.
     * 
     * @return A randomly scrambled puzzle state
     */
    private static String generateRandomPuzzle() {
        String cur = GOAL;
        String prev = null;

        for (int i = 0; i < SCRAMBLE_MOVES; i++) {
            List<String> nexts = neighbors(cur);

            // Avoid immediate backtracking to create more diverse scrambling
            if (prev != null && nexts.size() > 1) {
                nexts.remove(prev);
            }

            String chosen = nexts.get(RNG.nextInt(nexts.size()));
            prev = cur;
            cur = chosen;
        }

        return cur;
    }

    // ============================================================
    // Solvability Check
    // ============================================================

    /**
     * Counts the number of inversions in the puzzle (excluding the blank tile).
     * An inversion occurs when a larger tile appears before a smaller tile.
     * Puzzles with an odd number of inversions are unsolvable.
     * 
     * @param state The puzzle state to check
     * @return The number of inversions
     */
    private static int countInversions(String state) {
        int[] arr = new int[8];
        int k = 0;

        // Extract non-zero tiles into array
        for (int i = 0; i < 9; i++) {
            int v = state.charAt(i) - '0';
            if (v != 0)
                arr[k++] = v;
        }

        // Count inversions using brute force
        int inv = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] > arr[j])
                    inv++;
            }
        }
        return inv;
    }

    // ============================================================
    // Heuristic Functions
    // ============================================================

    /**
     * H1 Heuristic: Counts the number of misplaced tiles (excluding blank).
     * This is an admissible heuristic (never overestimates).
     * 
     * @param state The puzzle state to evaluate
     * @return Number of tiles not in their goal positions
     */
    private static int h1(String state) {
        int misplaced = 0;
        for (int i = 0; i < 9; i++) {
            char ch = state.charAt(i);
            if (ch == '0')
                continue;
            if (ch != GOAL.charAt(i))
                misplaced++;
        }
        return misplaced;
    }

    /**
     * H2 Heuristic: Sum of Manhattan distances for all tiles (excluding blank).
     * Manhattan distance is the number of moves (vertical + horizontal) a tile
     * needs to reach its goal position. This is an admissible and more informed
     * heuristic than H1.
     * 
     * @param state The puzzle state to evaluate
     * @return Sum of Manhattan distances for all tiles
     */
    private static int h2(String state) {
        int dist = 0;
        for (int i = 0; i < 9; i++) {
            int tile = state.charAt(i) - '0';
            if (tile == 0)
                continue;

            int r = i / 3;
            int c = i % 3;
            dist += Math.abs(r - goalRow[tile]) + Math.abs(c - goalCol[tile]);
        }
        return dist;
    }

    // ============================================================
    // Input / Output Utilities
    // ============================================================

    /**
     * Reads a 3x3 puzzle from standard input.
     * 
     * @param sc Scanner for reading input
     * @return 2D array representing the puzzle
     */
    private static int[][] readPuzzleFromStdin(Scanner sc) {
        int[][] p = new int[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                p[r][c] = safeReadInt(sc);
            }
        }
        return p;
    }

    /**
     * Converts a 2D puzzle array to a string representation.
     * 
     * @param p 2D puzzle array
     * @return String representation in row-major order
     */
    private static String puzzleToString(int[][] p) {
        StringBuilder sb = new StringBuilder(9);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                sb.append(p[r][c]);
            }
        }
        return sb.toString();
    }

    /**
     * Formats a puzzle state string as a 3x3 grid for display.
     * 
     * @param state Puzzle state string
     * @return Formatted 3-line string representation
     */
    private static String toGridString(String state) {
        return state.charAt(0) + " " + state.charAt(1) + " " + state.charAt(2) + "\n" +
                state.charAt(3) + " " + state.charAt(4) + " " + state.charAt(5) + "\n" +
                state.charAt(6) + " " + state.charAt(7) + " " + state.charAt(8);
    }

    /**
     * Safely reads an integer from input, skipping invalid tokens.
     * 
     * @param sc Scanner for reading input
     * @return The next valid integer
     */
    private static int safeReadInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next(); // Discard non-integer input
        }
        return sc.nextInt();
    }
}
