package heuristic;

/**
 * H1 Heuristic: Counts the number of misplaced tiles (excluding blank).
 * This is an admissible heuristic (never overestimates the true cost).
 */
public class MisplacedTilesHeuristic implements Heuristic {

    private static final String GOAL = "012345678";

    /**
     * Calculates the number of tiles not in their goal positions.
     * 
     * @param state The puzzle state to evaluate
     * @return Number of misplaced tiles
     */
    @Override
    public int calculate(String state) {
        int misplaced = 0;
        for (int i = 0; i < 9; i++) {
            char ch = state.charAt(i);
            if (ch == '0')
                continue;
            if (ch != GOAL.charAt(i)) {
                misplaced++;
            }
        }
        return misplaced;
    }

    @Override
    public String getName() {
        return "H1 (Misplaced Tiles)";
    }
}
