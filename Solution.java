import java.util.*;

/**
 * CS 4200 Project 1 - 8 Puzzle A* with H1 and H2
 *
 * UI per instructor notes:
 * - Only Input Method: Random or File
 * - No solution depth prompt
 *
 * Behavior:
 * - Print Puzzle
 * - If unsolvable: print "Puzzle is unsolvable"
 * - Ask for H function selection (H1 or H2)
 * - Run A* with BOTH H1 and H2 (so you can print both costs/times)
 * - Print the solution steps for the user-selected heuristic
 */
public class Solution {

    // Goal state in row-major order
    private static final String GOAL = "012345678";

    // goal positions for Manhattan distance
    private static final int[] goalRow = new int[9];
    private static final int[] goalCol = new int[9];

    static {
        for (int i = 0; i < 9; i++) {
            int tile = GOAL.charAt(i) - '0';
            goalRow[tile] = i / 3;
            goalCol[tile] = i % 3;
        }
    }

    // Random generation: fixed scramble length (no depth prompt)
    private static final Random RNG = new Random();
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
            // Random puzzle (solvable by construction)
            startState = generateRandomPuzzle();
        } else if (inputMethod == 2) {
            // "File" mode per your current flow: read a single puzzle from stdin (9 ints)
            // Later you can replace this with actual file reading if needed.
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

        // Solvability check (ignore 0)
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

        // Run BOTH heuristics so you can print both costs/times (matches the sample
        // behavior)
        AStarResult resultH1 = aStar(startState, 1);
        AStarResult resultH2 = aStar(startState, 2);

        if (resultH1 == null || resultH2 == null) {
            System.out.println("No solution found.");
            sc.close();
            return;
        }

        // Print the chosen solution steps (H1 or H2)
        AStarResult chosen = (hChoice == 1) ? resultH1 : resultH2;

        System.out.println("Solution Found");

        // Step numbering: Step 1 is the first move after the initial puzzle
        for (int step = 1; step < chosen.path.size(); step++) {
            System.out.println("Step: " + step);
            System.out.println(toGridString(chosen.path.get(step)));
        }

        // Print both costs/times
        System.out.println("H1 Search Cost: " + resultH1.nodesGenerated);
        System.out.println("H2 Search Cost: " + resultH2.nodesGenerated);
        System.out.printf("H1 Time: %.3f ms%n", resultH1.timeMs);
        System.out.printf("H2 Time: %.3f ms%n", resultH2.timeMs);

        sc.close();
    }

    // ============================================================
    // A* Search
    // ============================================================

    private static class Node {
        final String state;
        final int g; // depth so far
        final int f; // g + h
        final long id; // tie-breaker for deterministic PQ ordering

        Node(String state, int g, int f, long id) {
            this.state = state;
            this.g = g;
            this.f = f;
            this.id = id;
        }
    }

    private static class AStarResult {
        final List<String> path; // start..goal
        final int nodesGenerated; // "search cost" (we count generated neighbors)
        final double timeMs;

        AStarResult(List<String> path, int nodesGenerated, double timeMs) {
            this.path = path;
            this.nodesGenerated = nodesGenerated;
            this.timeMs = timeMs;
        }
    }

    private static AStarResult aStar(String start, int heuristicChoice) {
        long t0 = System.nanoTime();

        // Deterministic PQ: order by f, then g, then insertion id
        PriorityQueue<Node> frontier = new PriorityQueue<>(
                Comparator.<Node>comparingInt(n -> n.f)
                        .thenComparingInt(n -> n.g)
                        .thenComparingLong(n -> n.id));

        Map<String, Integer> gScore = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> closed = new HashSet<>();

        long pushId = 0;

        int h0 = heuristic(start, heuristicChoice);
        frontier.add(new Node(start, 0, h0, pushId++));
        gScore.put(start, 0);
        parent.put(start, null);

        int nodesGenerated = 0;

        while (!frontier.isEmpty()) {
            Node curNode = frontier.poll();
            String cur = curNode.state;

            // If we popped an outdated duplicate, skip it
            if (closed.contains(cur)) {
                continue;
            }
            closed.add(cur);

            if (cur.equals(GOAL)) {
                List<String> path = reconstructPath(parent, cur);
                long t1 = System.nanoTime();
                double ms = (t1 - t0) / 1_000_000.0;
                return new AStarResult(path, nodesGenerated, ms);
            }

            int curG = gScore.get(cur);

            for (String nxt : neighbors(cur)) {
                nodesGenerated++; // count generated neighbors as "search cost"

                if (closed.contains(nxt)) {
                    continue;
                }

                int tentativeG = curG + 1;
                Integer bestKnown = gScore.get(nxt);

                if (bestKnown == null || tentativeG < bestKnown) {
                    gScore.put(nxt, tentativeG);
                    parent.put(nxt, cur);
                    int h = heuristic(nxt, heuristicChoice);
                    frontier.add(new Node(nxt, tentativeG, tentativeG + h, pushId++));
                }
            }
        }

        return null;
    }

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

    private static int heuristic(String state, int choice) {
        return (choice == 1) ? h1(state) : h2(state);
    }

    // ============================================================
    // Neighbor generation (String state)
    // ============================================================

    private static List<String> neighbors(String state) {
        int z = state.indexOf('0');
        int zr = z / 3;
        int zc = z % 3;

        List<String> res = new ArrayList<>(4);

        // Fixed order for consistency
        // (If you want to try matching instructor sample more closely, we can tweak
        // this order.)
        if (zr > 0)
            res.add(swap(state, z, z - 3)); // up
        if (zr < 2)
            res.add(swap(state, z, z + 3)); // down
        if (zc > 0)
            res.add(swap(state, z, z - 1)); // left
        if (zc < 2)
            res.add(swap(state, z, z + 1)); // right

        return res;
    }

    private static String swap(String s, int i, int j) {
        char[] a = s.toCharArray();
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        return new String(a);
    }

    // ============================================================
    // Random puzzle generation (no depth prompt)
    // ============================================================

    private static String generateRandomPuzzle() {
        String cur = GOAL;
        String prev = null;

        for (int i = 0; i < SCRAMBLE_MOVES; i++) {
            List<String> nexts = neighbors(cur);

            // avoid immediate backtracking if possible
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
    // Solvability (inversions)
    // ============================================================

    private static int countInversions(String state) {
        int[] arr = new int[8];
        int k = 0;

        // flatten ignoring 0
        for (int i = 0; i < 9; i++) {
            int v = state.charAt(i) - '0';
            if (v != 0)
                arr[k++] = v;
        }

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
    // Heuristics
    // ============================================================

    // H1: number of misplaced tiles excluding 0
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

    // H2: sum of Manhattan distances excluding 0
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
    // Input / Output Helpers
    // ============================================================

    private static int[][] readPuzzleFromStdin(Scanner sc) {
        int[][] p = new int[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                p[r][c] = safeReadInt(sc);
            }
        }
        return p;
    }

    private static String puzzleToString(int[][] p) {
        StringBuilder sb = new StringBuilder(9);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                sb.append(p[r][c]);
            }
        }
        return sb.toString();
    }

    private static String toGridString(String state) {
        // prints 3 lines "a b c"
        return state.charAt(0) + " " + state.charAt(1) + " " + state.charAt(2) + "\n" +
                state.charAt(3) + " " + state.charAt(4) + " " + state.charAt(5) + "\n" +
                state.charAt(6) + " " + state.charAt(7) + " " + state.charAt(8);
    }

    private static int safeReadInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next(); // discard non-int token
        }
        return sc.nextInt();
    }
}
