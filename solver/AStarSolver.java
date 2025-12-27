package solver;

import heuristic.Heuristic;
import model.Node;
import model.SearchResult;
import puzzle.PuzzleState;

import java.util.*;

/**
 * Implements the A* search algorithm for solving the 8-puzzle problem.
 * Uses a provided heuristic function to guide the search.
 */
public class AStarSolver {

    private final Heuristic heuristic;

    /**
     * Constructs a new A* solver with the specified heuristic.
     * 
     * @param heuristic The heuristic function to use for search
     */
    public AStarSolver(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Executes A* search algorithm to find optimal solution path.
     * 
     * @param start The initial puzzle state
     * @return SearchResult containing solution path, search cost, and execution
     *         time, or null if no solution
     */
    public SearchResult solve(String start) {
        long t0 = System.nanoTime();

        // Priority queue ordered by f-value, then g-value, then insertion order
        PriorityQueue<Node> frontier = new PriorityQueue<>(
                Comparator.<Node>comparingInt(Node::getF)
                        .thenComparingInt(Node::getG)
                        .thenComparingLong(Node::getId));

        Map<String, Integer> gScore = new HashMap<>(); // Best known cost to reach each state
        Map<String, String> parent = new HashMap<>(); // Parent pointers for path reconstruction
        Set<String> closed = new HashSet<>(); // Already expanded states

        long pushId = 0; // Monotonically increasing ID for tie-breaking

        // Initialize with start state
        int h0 = heuristic.calculate(start);
        frontier.add(new Node(start, 0, h0, pushId++));
        gScore.put(start, 0);
        parent.put(start, null);

        int nodesGenerated = 0;

        while (!frontier.isEmpty()) {
            Node curNode = frontier.poll();
            String cur = curNode.getState();

            // Skip if this state was already expanded (handles duplicate entries)
            if (closed.contains(cur)) {
                continue;
            }
            closed.add(cur);

            // Check for goal state
            if (cur.equals(PuzzleState.GOAL)) {
                List<String> path = reconstructPath(parent, cur);
                long t1 = System.nanoTime();
                double ms = (t1 - t0) / 1_000_000.0;
                return new SearchResult(path, nodesGenerated, ms);
            }

            int curG = gScore.get(cur);

            // Generate and process all neighboring states
            for (String nxt : PuzzleState.getNeighbors(cur)) {
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
                    int h = heuristic.calculate(nxt);
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
    private List<String> reconstructPath(Map<String, String> parent, String goal) {
        List<String> path = new ArrayList<>();
        String cur = goal;
        while (cur != null) {
            path.add(cur);
            cur = parent.get(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
