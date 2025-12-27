package heuristic;

/**
 * Interface for heuristic functions used in A* search.
 * Heuristics estimate the cost to reach the goal from a given state.
 */
public interface Heuristic {

    /**
     * Calculates the heuristic value for the given puzzle state.
     * 
     * @param state The puzzle state to evaluate
     * @return The estimated cost to reach the goal
     */
    int calculate(String state);

    /**
     * @return The name of this heuristic
     */
    String getName();
}
