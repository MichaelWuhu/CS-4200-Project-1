package util;

import java.util.Scanner;

/**
 * Utility class for reading user input safely.
 */
public class InputReader {
    
    /**
     * Safely reads an integer from input, skipping invalid tokens.
     * 
     * @param sc Scanner for reading input
     * @return The next valid integer
     */
    public static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next(); // Discard non-integer input
        }
        return sc.nextInt();
    }
    
    /**
     * Reads a 3x3 puzzle from standard input.
     * 
     * @param sc Scanner for reading input
     * @return 2D array representing the puzzle
     */
    public static int[][] readPuzzle(Scanner sc) {
        int[][] puzzle = new int[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                puzzle[r][c] = readInt(sc);
            }
        }
        return puzzle;
    }
}
