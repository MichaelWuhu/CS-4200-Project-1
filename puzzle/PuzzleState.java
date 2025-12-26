package puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents and manages an 8-puzzle state.
 * Provides utilities for state manipulation, neighbor generation, and validation.
 */
public class PuzzleState {
    
    /** Goal state represented as a string in row-major order */
    public static final String GOAL = "012345678";
    
    /**
     * Generates all valid neighboring states by moving the blank tile.
     * 
     * @param state Current puzzle state
     * @return List of neighboring states (2-4 neighbors depending on blank position)
     */
    public static List<String> getNeighbors(String state) {
        int z = state.indexOf('0');  // Find blank tile position
        int zr = z / 3;              // Blank row
        int zc = z % 3;              // Blank column
        
        List<String> neighbors = new ArrayList<>(4);
        
        // Generate neighbors in fixed order for consistency
        if (zr > 0) neighbors.add(swap(state, z, z - 3)); // Move up
        if (zr < 2) neighbors.add(swap(state, z, z + 3)); // Move down
        if (zc > 0) neighbors.add(swap(state, z, z - 1)); // Move left
        if (zc < 2) neighbors.add(swap(state, z, z + 1)); // Move right
        
        return neighbors;
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
    
    /**
     * Counts the number of inversions in the puzzle (excluding the blank tile).
     * An inversion occurs when a larger tile appears before a smaller tile.
     * Puzzles with an odd number of inversions are unsolvable.
     * 
     * @param state The puzzle state to check
     * @return The number of inversions
     */
    public static int countInversions(String state) {
        int[] arr = new int[8];
        int k = 0;
        
        // Extract non-zero tiles into array
        for (int i = 0; i < 9; i++) {
            int v = state.charAt(i) - '0';
            if (v != 0) {
                arr[k++] = v;
            }
        }
        
        // Count inversions using brute force
        int inv = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] > arr[j]) {
                    inv++;
                }
            }
        }
        return inv;
    }
    
    /**
     * Checks if a puzzle state is solvable.
     * A puzzle is solvable if it has an even number of inversions.
     * 
     * @param state The puzzle state to check
     * @return true if solvable, false otherwise
     */
    public static boolean isSolvable(String state) {
        return countInversions(state) % 2 == 0;
    }
    
    /**
     * Converts a 2D puzzle array to a string representation.
     * 
     * @param puzzle 2D puzzle array
     * @return String representation in row-major order
     */
    public static String toString(int[][] puzzle) {
        StringBuilder sb = new StringBuilder(9);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                sb.append(puzzle[r][c]);
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
    public static String toGridString(String state) {
        return state.charAt(0) + " " + state.charAt(1) + " " + state.charAt(2) + "\n" +
               state.charAt(3) + " " + state.charAt(4) + " " + state.charAt(5) + "\n" +
               state.charAt(6) + " " + state.charAt(7) + " " + state.charAt(8);
    }
}
