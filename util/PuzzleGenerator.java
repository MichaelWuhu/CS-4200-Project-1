package util;

import puzzle.PuzzleState;

import java.util.List;
import java.util.Random;

/**
 * Utility class for generating random solvable puzzles.
 */
public class PuzzleGenerator {

    private static final Random RNG = new Random();
    private static final int SCRAMBLE_MOVES = 30;

    /**
     * Generates a random solvable puzzle by applying random moves from the goal
     * state.
     * This ensures the generated puzzle is always solvable.
     * 
     * @return A randomly scrambled puzzle state
     */
    public static String generateRandom() {
        String cur = PuzzleState.GOAL;
        String prev = null;

        for (int i = 0; i < SCRAMBLE_MOVES; i++) {
            List<String> nexts = PuzzleState.getNeighbors(cur);

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
}
