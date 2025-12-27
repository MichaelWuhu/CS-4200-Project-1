package heuristic;

/**
 * H2 Heuristic: Sum of Manhattan distances for all tiles (excluding blank).
 * Manhattan distance is the number of moves (vertical + horizontal) a tile
 * needs to reach its goal position. This is an admissible and more informed
 * heuristic than H1.
 */
public class ManhattanDistanceHeuristic implements Heuristic {

    private static final String GOAL = "012345678";
    private static final int[] goalRow = new int[9];
    private static final int[] goalCol = new int[9];

    static {
        // Pre-compute goal positions for efficient Manhattan distance calculation
        for (int i = 0; i < 9; i++) {
            int tile = GOAL.charAt(i) - '0';
            goalRow[tile] = i / 3;
            goalCol[tile] = i % 3;
        }
    }

    /**
     * Calculates the sum of Manhattan distances for all tiles.
     * 
     * @param state The puzzle state to evaluate
     * @return Sum of Manhattan distances
     */
    @Override
    public int calculate(String state) {
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

    @Override
    public String getName() {
        return "H2 (Manhattan Distance)";
    }
}
